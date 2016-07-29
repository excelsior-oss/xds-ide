package com.excelsior.xds.xbookmarks;

import java.net.URL;
import java.util.HashMap;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * The activator class controls the plug-in life cycle
 */
public class XBookmarksPlugin extends AbstractUIPlugin implements IStartup, IResourceChangeListener
{
	// The plug-in ID
	public static final String PLUGIN_ID = "com.excelsior.texteditor.xbookmarks"; //$NON-NLS-1$
	
	public static final String BOOKMARK_MARKER_ID = PLUGIN_ID + ".bookmark"; //$NON-NLS-1$

	/**
	 * Attribute name for markers to store bookmark number. 
	 */
    public static final String BOOKMARK_NUMBER_ATTR = "number"; //$NON-NLS-1$
	
	private static final String PREF_SCOPE = "xBookmarks.pref.scope"; //$NON-NLS-1$
	
	public static final int SCOPE_WORKSPACE = 0;

	
	// The shared instance
	private static XBookmarksPlugin plugin;
	
	
	// Images cache:
	//
	public static int IMGID_ERROR   = 0;
	public static int IMGID_SET     = 1;
	public static int IMGID_DELETED = 2;
	public static int IMGID_GOTO    = 3;
	public static int IMGID_INFO    = 4;

	private static int IMGID_BOOKMARKS = -100;
    private static String IMG_SOURCES_BOOKMARKS = "icons/xBmark_%d.png"; //$NON-NLS-1$
	//
	private static final String IMG_SOURCES[] = new String[] {
		"icons/xBmarkStatus_error.gif", //$NON-NLS-1$
		"icons/xBmarkStatus_set.gif", //$NON-NLS-1$
		"icons/xBmarkStatus_deleted.gif", //$NON-NLS-1$
		"icons/xBmarkStatus_goto.gif", //$NON-NLS-1$
		"icons/xBmarkStatus_info.gif" //$NON-NLS-1$
	};
	//
	private HashMap<Integer, Image> hmCachedImages;
	
	
	

	/**
	 * The constructor
	 */
	public XBookmarksPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		hmCachedImages = new HashMap<Integer, Image>();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);

		// dispose cached images:
		if (hmCachedImages != null) {
			Set<Integer> ks = hmCachedImages.keySet();
			for (int k:ks) {
				Image im = hmCachedImages.get(k);
				im.dispose();
			}
			hmCachedImages = null;
		}
	}

	/**
	 * Initially enables 'Goto xBookmark N' menu items according to 
	 * existing bookmark numbers.
	 * 
	 * Will be called in a separated thread after the workbench initializes,
	 * thus this method delegates it's work to an UI thread by means of
	 * <code>Display.getDefault().asyncExec(new Runnable() { ... }</code>
	 */
	@Override
	public void earlyStartup() {
	   ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
	}
	
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static XBookmarksPlugin getDefault() {
		return plugin;
	}

	/**
	 * Sets default preference values. These values will be used until some
	 * preferences are actually set using Preference dialog.
	 * 
	 * @param store
	 *            the store to use
	 */
	@Override
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		store.setDefault(PREF_SCOPE, SCOPE_WORKSPACE);
	}
	
	/**
	 * 
	 * @param imgid IMG_* constant
	 * @return Image 
	 */
	public Image getCachedImage(int imgid) {
		if (hmCachedImages == null) { 
			return null; // hbz
		}
		Integer key = new Integer(imgid);
		Image img = hmCachedImages.get(key);
		if (img == null) {
			Assert.isTrue(imgid>=0 && imgid<IMG_SOURCES.length, "getCachedImage - wrong Image id=" + imgid); //$NON-NLS-1$
			IPath path = new Path(IMG_SOURCES[imgid]); 
			URL   url  = FileLocator.find(XBookmarksPlugin.getDefault().getBundle(), path, null);
			if (url != null) {
				img = ImageDescriptor.createFromURL(url).createImage();
			}
			if (img != null) {
				hmCachedImages.put(key, img);
			}
		}
		return img;
	}
	
    /**
     * 
     * @param imgid IMG_* constant
     * @return Image 
     */
    public Image getCachedBookmarkImage(int bookmarkNum) { // 0..9
        if (hmCachedImages == null || bookmarkNum < 0 || bookmarkNum > 9) { 
            return null; // hbz
        }
        Integer key = new Integer(IMGID_BOOKMARKS + bookmarkNum);
        Image img = hmCachedImages.get(key);
        if (img == null) {
            IPath path = new Path(String.format(IMG_SOURCES_BOOKMARKS, bookmarkNum)); 
            URL   url  = FileLocator.find(XBookmarksPlugin.getDefault().getBundle(), path, null);
            if (url != null) {
                img = ImageDescriptor.createFromURL(url).createImage();
            }
            if (img != null) {
                hmCachedImages.put(key, img);
            }
        }
        return img;
    }
	
    //--- Remove bookmarks from the resource when it is deleted:

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
            try {
                event.getDelta().accept(new IResourceDeltaVisitor() {
                    @Override
                    public boolean visit(IResourceDelta delta) throws CoreException {
                        IResource res = delta.getResource();
                        switch (delta.getKind()) {
                        case IResourceDelta.REMOVED:
                            XBookmarksUtils.removeBookmarksFrom(res);
                            break;
                        }
                        return true; // visit the children
                    }
                });
            } catch (CoreException e) {}
        }
    }
    
}
