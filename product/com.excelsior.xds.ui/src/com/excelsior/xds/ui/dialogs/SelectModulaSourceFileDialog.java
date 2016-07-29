package com.excelsior.xds.ui.dialogs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;
import org.eclipse.ui.dialogs.SearchPattern;
import org.eclipse.ui.keys.KeySequence;
import org.eclipse.ui.keys.ParseException;
import org.eclipse.ui.keys.SWTKeySupport;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.excelsior.xds.core.compiler.compset.CompilationSetManager;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.natures.NatureIdRegistry;
import com.excelsior.xds.core.project.SpecialFolderNames;
import com.excelsior.xds.core.project.XdsProjectSettings;
import com.excelsior.xds.core.project.XdsProjectSettingsManager;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.core.utils.IClosure;
import com.excelsior.xds.ui.commons.utils.WorkbenchUtils;
import com.excelsior.xds.ui.images.ImageUtils;
import com.excelsior.xds.ui.internal.nls.Messages;

@SuppressWarnings("deprecation")
public class SelectModulaSourceFileDialog extends FilteredItemsSelectionDialog 
{
    private static final String CONCAT_STRING = " - "; //$NON-NLS-1$
    private static final String SHOW_COMPILATOPN_SET_ONLY = "ShowCompilationSetOnly"; //$NON-NLS-1$
    private static WorkbenchLabelProvider workbenchLabelProvider = new WorkbenchLabelProvider();
    private SourceFileItemsFilter sourceFileItemsFilter;
    private ToggleCompilationSetOnlyAction toggleCompilationSetOnlyAction;
    private boolean forceToShowAllItems = false;
    private List<ListItem> listItems = new ArrayList<ListItem>();
    private IProject curProject;
	private KeyAdapter fKeyAdapter;
    private static final IDialogSettings dialogSettings = new DialogSettings("XDS_Modules_Dailog"); //$NON-NLS-1$
    
    // Select items with the given (lowercase) extensions
	public SelectModulaSourceFileDialog(String title, Shell shell, IResource rootResource, final Set<String> extensions) {
		super(shell);
		setTitle(title);
		final IResourceFilter flt = new IResourceFilter() {
			@Override
			public boolean showItem(IResource resource) {
				if (ResourceUtils.isInsideFolder(SpecialFolderNames.EXTERNAL_DEPENDENCIES_DIR_NAME, resource)) {
					return false;
				}
				String ext = resource.getLocation() != null? resource.getLocation().getFileExtension() : null;
				if (ext != null) ext = ext.toLowerCase();
				return extensions.contains(ext);
			}
		};

		curProject = WorkbenchUtils.getCurrentXdsProject(NatureIdRegistry.MODULA2_SOURCE_PROJECT_NATURE_ID);
        final boolean diffPrj[] = {false};
        try {
            rootResource.accept(new IResourceVisitor() {
                @Override
                public boolean visit(IResource resource) throws CoreException {
                    if (flt.showItem(resource)){
                        listItems.add(new ListItem(resource));
                        if (!diffPrj[0] && curProject != null && !resource.getProject().equals(curProject)) {
                            diffPrj[0] = true;
                        }
                    }
                    return true;
                }
            });
        } catch (CoreException e) {
            LogHelper.logError(e);
        }

        if (diffPrj[0]) {
            listItems.add(new ListItem(null)); // separator line
        }
        
        setListLabelProvider(new StyledDecoratingLabelProvider(new ListLabelProvider(), new MyLabelDecorator()));
        setDetailsLabelProvider(new DetailsLabelProvider());
        setInitialPattern("*"); //$NON-NLS-1$
    }
	
	
   private void forEachChild(Composite c, IClosure<Control> operation) {
        Queue<Composite> composites2Walk = new LinkedList<Composite>();
        composites2Walk.add(c);
        
        while(!composites2Walk.isEmpty()) {
            Composite composite = composites2Walk.poll();
            Control[] children = composite.getChildren();
            for (Control child : children) {
                operation.execute(child);
                if (child instanceof Composite) {
                    composites2Walk.add((Composite)child);
                }
            }
        }
    }

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite)super.createDialogArea(parent);
		forEachChild(dialogArea, new IClosure<Control>() {
			@Override
			public void execute(Control c) {
				c.addKeyListener(getKeyAdapter());
			}
		});
		return dialogArea;
	}

	// TODO : replace deprecated classes
	private KeyAdapter getKeyAdapter() {
		if (fKeyAdapter == null) {
			try {
				// TODO : take CTRL+M sequence from the command 'com.excelsior.xds.commands.goto.compilation.unit'
				final KeySequence instance = KeySequence.getInstance("CTRL+M"); //$NON-NLS-1$
				fKeyAdapter = new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						int accelerator = SWTKeySupport
								.convertEventToUnmodifiedAccelerator(e);
						KeySequence keySequence = KeySequence
								.getInstance(SWTKeySupport
										.convertAcceleratorToKeyStroke(accelerator));
						if (instance.equals(keySequence)) {
							e.doit = false;
							toggleCompilationSetOnlyAction.setChecked(!toggleCompilationSetOnlyAction.isChecked());
							applyFilter();
							return;
						}
					}
				};
			} catch (ParseException e2) {
				LogHelper.logError(e2);
			}
		}
		return fKeyAdapter;
	}
	
	public void forceToShowAllItems() {
	    forceToShowAllItems = true;
	}

    public String getResultAsRelativePath() {
        IResource result = getResultAsResource();
        return getRelativePath(result);
    }
    
    public IResource getResultAsResource() {
        return ((ListItem)getResult()[0]).getResource();
    }

    private String getRelativePath(IResource result) {
        return ResourceUtils.getProjectRelativePath(result);
    }

    @Override
    protected Control createExtendedContentArea(Composite parent) {
        return null;
    }
    
    @Override
    protected void fillViewMenu(IMenuManager menuManager) {
        toggleCompilationSetOnlyAction = new ToggleCompilationSetOnlyAction();
        menuManager.add(toggleCompilationSetOnlyAction);
        super.fillViewMenu(menuManager);
    }
    
    @Override   
    protected void restoreDialog(IDialogSettings settings) {
        super.restoreDialog(settings);

        boolean b = !forceToShowAllItems;

        if (!forceToShowAllItems && settings.get(SHOW_COMPILATOPN_SET_ONLY) != null) {
            b = settings.getBoolean(SHOW_COMPILATOPN_SET_ONLY);
        }

        toggleCompilationSetOnlyAction.setChecked(b);
    }

    @Override
    protected void storeDialog(IDialogSettings settings) {
        super.storeDialog(settings);
        if (!forceToShowAllItems) {
            settings.put(SHOW_COMPILATOPN_SET_ONLY, toggleCompilationSetOnlyAction.isChecked());
        }
    }


    @Override
    protected IDialogSettings getDialogSettings() {
        return dialogSettings;
    }

    @Override
    protected IStatus validateItem(Object item) {
        return Status.OK_STATUS;
    }

    @Override
    protected ItemsFilter createFilter() {
        return sourceFileItemsFilter = new SourceFileItemsFilter();
    }
    
    @Override
    protected Comparator<ListItem> getItemsComparator() {
        return new ListItemsComparator();
    }

    @Override
    protected void fillContentProvider(AbstractContentProvider contentProvider,
            ItemsFilter itemsFilter, IProgressMonitor progressMonitor)
            throws CoreException {
        for (ListItem li : listItems) {
            contentProvider.add(li, itemsFilter);
        }
    }

    @Override
    public String getElementName(Object item) {
        return (item != null) ? item.toString() : "NULL??"; //$NON-NLS-1$
    }



    @Override
    public void create() {
        super.create();
        
        this.getShell().addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
            }
        });
    }
    
    
    @Override
    protected void updateButtonsEnableState(IStatus status) {
        Button okButton = getOkButton();
        StructuredSelection ss = getSelectedItems();
        if (ss.size() == 0 || (ss.getFirstElement() instanceof ListItem && ((ListItem)ss.getFirstElement()).isDelimiter())) {
            if (okButton != null && !okButton.isDisposed()) {
                okButton.setEnabled(false);
            }
        } else {
            super.updateButtonsEnableState(status);
        }
    }

    
    private class ListLabelProvider implements ILabelProvider {
        @Override
        public void removeListener(ILabelProviderListener listener) {
        }
        
        @Override
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }
        
        @Override
        public void dispose() {
        }
        
        @Override
        public void addListener(ILabelProviderListener listener) {
            
        }
        
        @Override
        public String getText(Object element) {
            if (element instanceof ListItem) {
                ListItem li = (ListItem)element;
                if (li.isDelimiter()) {
                    return (Messages.SelectModulaSourceFileDialog_WorkspaceMatches);
                } else {
                    return li.getName() + CONCAT_STRING + li.getPath();
                }
            }
            return getElementName(element);
        }
        
        @Override
        public Image getImage(Object element) {
            if (element instanceof ListItem) {
                element = ((ListItem)element).getResource();
            }
            return workbenchLabelProvider.getImage(element);
        }

    } // ListLabelProvider
    
    private class DetailsLabelProvider extends ListLabelProvider {
        @Override
        public String getText(Object element) {
            if (element instanceof ListItem) {
                ListItem li = (ListItem)element;
                if (li.isDelimiter()) {
                    return (""); //$NON-NLS-1$
                } else {
                    IProject ip = li.getProject();
                    IResource res = li.getResource();
    
                    String s = ip == null ? "" : (ip.getName() + CONCAT_STRING);
                    if (res != null && res instanceof IFile) {
                        IPath loc = ((IFile)res).getLocation();
                        if (loc != null){
                        	s += loc.toOSString();
                        }
                    }
                    return s;
                }
            }
            return super.getText(element);
        }
        
        @Override
        public Image getImage(Object element) {
            if (element instanceof ListItem) {
                IProject ip = ((ListItem)element).getProject();
                if (ip != null) {
                    element = ip;
                    try {
                        if (ip.getNature(NatureIdRegistry.MODULA2_SOURCE_PROJECT_NATURE_ID) != null) {
                            // icon with transparent backgroung (prj. explorer needs white background in its icon)
                            return ImageUtils.getImage(ImageUtils.M2_PRJ_FOLDER_TRANSP);
                        }
                    } catch (Exception e){}
                }
                
            }
            return workbenchLabelProvider.getImage(element);
        }

    }

    
    private class MyLabelDecorator implements ILabelDecorator {
        ILabelDecorator decorator;            
        
        public MyLabelDecorator() {
            decorator = PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator();            
        }

        @Override
        public void addListener(ILabelProviderListener listener) {
            decorator.addListener(listener);
        }

        @Override
        public void dispose() {
            decorator.dispose();
        }

        @Override
        public boolean isLabelProperty(Object element, String property) {
            return decorator.isLabelProperty(element, property);
        }

        @Override
        public void removeListener(ILabelProviderListener listener) {
            decorator.removeListener(listener);
        }

        @Override
        public Image decorateImage(Image image, Object element) {
//XXX   decorator works only from time to time and images are randomly changes on the fly. Bug in eclipse? :(
//            if ((element instanceof ListItem)) {
//                element =  ((ListItem)element).getResource();
//            }
//            return decorator.decorateImage(image, element);
            return null; 
        }

        @Override
        public String decorateText(String text, Object element) {
            if ((element instanceof ListItem)) {
                element =  ((ListItem)element).getResource();
            }
            //XXX return decorator.decorateText(text, element);
            return text;
        }
        
    }
    
    private class StyledDecoratingLabelProvider extends DecoratingLabelProvider implements IStyledLabelProvider {
        
        private Styler boldStyler;
        private Font boldFont;

        public StyledDecoratingLabelProvider(ILabelProvider provider, ILabelDecorator decorator) {
            super(provider, decorator);
            boldStyler = new Styler() {
                @Override
                public void applyStyles(TextStyle textStyle) {
                    textStyle.font= getBoldFont();
                }
            };
        }
        
        private Font getBoldFont() {
            if (boldFont == null) {
                Font font= getDialogArea().getFont();
                FontData[] data= font.getFontData();
                for (int i= 0; i < data.length; i++) {
                    data[i].setStyle(SWT.BOLD);
                }
                boldFont= new Font(font.getDevice(), data);
            }
            return boldFont;
        }
        
        @Override
        public StyledString getStyledText(Object element) {
            String text= getText(element);
            StyledString string= new StyledString(text);
            
            if (element instanceof ListItem) {
                ListItem li = (ListItem)element;
                if (li.isDelimiter()) {
                    string.setStyle(0, text.length(), StyledString.QUALIFIER_STYLER);
                } else { 
                    int concatPos = text.indexOf(CONCAT_STRING);
                    String modName = concatPos == -1 ? text : text.substring(0, concatPos);

                    if (sourceFileItemsFilter != null) {
                        ArrayList<Integer> ints = sourceFileItemsFilter.getMatchedIntervals(modName); 
                        markMatchingRegions(string, ints, boldStyler);
                    }

                    if (concatPos != -1) {
                        string.setStyle(concatPos, text.length() - concatPos, StyledString.QUALIFIER_STYLER);
                    }
                }
            }

            return string;
        }
        
        private void markMatchingRegions(StyledString string, ArrayList<Integer> intervals, Styler styler) {
            if (intervals != null) {
                int offset= -1;
                int length= 0;
                for (int i= 0; i + 1 < intervals.size(); i= i + 2) {
                    int beg = intervals.get(i); 
                    int len = intervals.get(i+1) - beg; 

                    if (offset == -1) {
                        offset = beg;
                    }
                    // Concatenate adjacent regions
                    if (i + 2 < intervals.size() && beg+len == intervals.get(i + 2)) {
                        length= length + len;
                    } else {
                        string.setStyle(offset, length + len, styler);
                        offset= -1;
                        length= 0;
                    }
                }
            }
        }
    }
    

    
    private class ListItem {
        private IResource res;         // the resource  (null for delimeter string):
        private String    path;        // file name ~= "Module.mod"
        private String    name;        // path      ~= M2Project/src/dir1 (may be "")
        private IProject  project;     // project (or null)
        private boolean   isInCompSet; // in compilation set?
        private boolean   isExtDep;    // in external dependencies? (only for current project)
        
        public ListItem(IResource res) {
            this.res = res;
            if (res == null) {
                return; // delimiter string;
            }
            project  = res.getProject();
            if (res instanceof IFile) {
                IFile f = (IFile)res;
                name = f.getName();
                if (ResourceUtils.isInsideFolder(SpecialFolderNames.VIRTUAL_MOUNT_ROOT_DIR_NAME, res)) {
                    path = getExternalDependencyDecortator(project, f);
                } else {
                    path = f.getFullPath().removeLastSegments(1).toString();
                }
            } else { // hbz
                name = res.toString();
                path = ""; //$NON-NLS-1$
            }
            
            String absolutePath = ResourceUtils.getAbsolutePath(res);

            if (project != null) {
                // Is resource in compilation set?
                String projectName = project.getName();
                boolean isInCompilationSet = CompilationSetManager.getInstance().isInCompilationSet(projectName, absolutePath);
                boolean isParentContainsCompilationSetChildren = CompilationSetManager.getInstance().isHasCompilationSetChildren(project, absolutePath);
                isInCompSet = isInCompilationSet || isParentContainsCompilationSetChildren;
                
                if (isInCompSet && project != null) {
                    XdsProjectSettings projectSettings = XdsProjectSettingsManager.getXdsProjectSettings(project);
                    path += "  [" + projectSettings.getCompilationRootName() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
                }
                
                IPath extFolder = project.getFolder(SpecialFolderNames.VIRTUAL_MOUNT_ROOT_DIR_NAME).getFullPath();
                isExtDep = extFolder.isPrefixOf(res.getFullPath());
            }
        }

        private String getExternalDependencyDecortator(IProject  project, IFile iFile) {
            String filePath = iFile.getLocation().toOSString(); 
            String decorator;
            
            if (project == null) {
                decorator = filePath;
            }
            else {
                decorator = null;
                XdsProjectSettings settings = XdsProjectSettingsManager.getXdsProjectSettings(project);
                Sdk sdk = settings.getProjectSdk();
                if ((sdk != null) && StringUtils.isNotEmpty(sdk.getName())) {
                    String sdkLibraryPath = sdk.getLibraryDefinitionsPath();
                    if (sdkLibraryPath != null) {
                        if (filePath.startsWith(sdkLibraryPath)) {
                            decorator = Messages.format( Messages.SelectModulaSourceFileDialog_SdkLibraryDecorator
                                                        , new Object[]{ project.getFullPath().toString()
                                                        , sdk.getName() });
                        }
                        else {
                            decorator = Messages.format( Messages.SelectModulaSourceFileDialog_ExternalFilesDecorator
                                                       , project.getFullPath().toString());
                        }
                    }
                }
                if (decorator == null) {
                    decorator = project.getFullPath().toString() + "  " + filePath;   //$NON-NLS-1$
                }
            }
            return decorator;
        }
        
        
        public String getName() {
            return name;
        }

        public String getPath() {
            return path;
        }
        
        public IProject getProject() {
            return project;
        }

        public IResource getResource() {
            return res;
        }
        
        public boolean isDelimiter() {
            return res == null;
        }
        
        public boolean isInCompilationSet() {
            return isInCompSet;
        }
        
        public boolean isExtDep() {
            return this.isExtDep;
        }

    } // class ListItem

    private class ListItemsComparator implements Comparator<ListItem> {
        @Override
        public int compare(ListItem o1, ListItem o2) {
            IProject ip1= o1.getProject();
            IProject ip2= o2.getProject();
            if (curProject != null) {
                if (curProject.equals(ip1)) {
                    if (!curProject.equals(ip2)) {
                        return -1;
                    } else {
                        return compare2(o1, o2, true);
                    }
                }
                if (curProject.equals(ip2)) {
                    if (!curProject.equals(ip1)) {
                        return 1;
                    } else {
                        return compare2(o1, o2, true);
                    }
                }
            }
            
            if (o1.isDelimiter()) {
                return -1;
            }
            if (o2.isDelimiter()) {
                return 1;
            }
            return compare2(o1, o2, false);
        }
        
        private int compare2(ListItem o1, ListItem o2, boolean compExtDep) {
            if (compExtDep && (o1.isExtDep() != o2.isExtDep())) {
                return o1.isExtDep() ? 1 : -1;
            }
            return o1.getName().compareToIgnoreCase(o2.getName());
        }

    }
    
    
    private class ToggleCompilationSetOnlyAction extends Action {

        /**
         * Creates a new instance of the class.
         */
        public ToggleCompilationSetOnlyAction() {
            super(
                    Messages.SelectModulaSourceFileDialog_CompilationSetOnly,
                    IAction.AS_CHECK_BOX);
        }

        public void run() {
            applyFilter();
        }
    }

    
    private final class SourceFileItemsFilter extends ItemsFilter {
        
        private MyMatcher myMatcher;
        private boolean isCompilationSetOnly;

        public SourceFileItemsFilter() {
            super(new SearchPattern() {
                @Override
                public void setPattern(String stringPattern) {
                    if (!StringUtils.containsAny(stringPattern, new char[]{'*', '?'})) {
                        stringPattern = "*"+stringPattern+"*"; //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    // System.out.println("-- setPattern: \'" + stringPattern + '\'');
                    super.setPattern(stringPattern);
                }
            });
            
            String patt = this.getPattern();
            myMatcher = new MyMatcher(patt+'*', false);
            isCompilationSetOnly = toggleCompilationSetOnlyAction.isChecked();
        }
        
        @Override
        public boolean equalsFilter(ItemsFilter filter) {
            if (filter instanceof SourceFileItemsFilter) {
                if (((SourceFileItemsFilter)filter).isCompilationSetOnly != isCompilationSetOnly) {
                    return false;
                }
            }
            return super.equalsFilter(filter);
        }

        @Override
        public boolean matchItem(Object item) {
            ListItem li = (ListItem)item;
            if (li.isDelimiter()) {
                return true;
            }
            if (isCompilationSetOnly && !li.isInCompilationSet()) {
                return false;
            }
            if (StringUtils.isEmpty(patternMatcher.getPattern())) return true;
            //return patternMatcher.matches(li.getName());
            return myMatcher.match(li.getName());
            
        }

        @Override
        public boolean isConsistentItem(Object item) {
            ListItem li = (ListItem)item;
            IResource resource = li.getResource();
            return resource==null || !resource.isPhantom();
        }
        
        @Override
        public boolean isSubFilter(ItemsFilter filter) {
            return false;
        }
        
        public ArrayList<Integer> getMatchedIntervals(String s) {
            try {
                if (myMatcher.match(s)) {
                    return myMatcher.getMatchedIntervals(); 
                }
            } catch (Exception e) { //hbz
            }
            return null;
        }
        
    }

    public static interface IResourceFilter {
        public boolean showItem(IResource resource);
    }
    
    
    
    
    private class MyMatcher {
        // Matcher, understands '*' and '?'
        
        private boolean caseSensitive;
        private Pattern jPattern;
        private Matcher jMatcher;
        
        public MyMatcher(String pattern, boolean caseSensitive) {
            this.caseSensitive = caseSensitive;

            if (!caseSensitive) {
                pattern = pattern.toLowerCase();
            }

            StringBuilder sb = new StringBuilder(); 
            boolean group = false;
            
            for (int i=0; i<pattern.length(); ++i) {
                char ch = pattern.charAt(i);
                if (ch == '?' || ch == '*') {
                    if (group) {
                        sb.append(')');
                        group = false;
                    }
                    sb.append('.');
                    if (ch == '*') {
                        sb.append('*');
                    }
                } else {
                    if (!group) {
                        sb.append('(');
                        group = true;
                    }
                    if ("[]\\/^$.|?*+(){}".indexOf(ch) >= 0){ //$NON-NLS-1$
                        sb.append('\\');
                    }
                    sb.append(ch);
                }
            }
            if (group) {
                sb.append(')');
            }
            try {
                // System.out.println("MyMatcher: \'" + pattern + "' --> '"+ sb.toString() + '\'');
                jPattern = Pattern.compile(sb.toString());
            } catch (PatternSyntaxException e) {
                jPattern = null;
            }
        }
        
        public boolean match(String s) {
            // System.out.print('*');
            try {
                jMatcher = jPattern.matcher(caseSensitive ? s : s.toLowerCase());
                return jMatcher.matches();
            } catch (Exception e) {
                return false;
            }
        }
        
        public ArrayList<Integer> getMatchedIntervals() {
            ArrayList<Integer> al = new ArrayList<Integer>();
            if (jMatcher != null) {
                for (int i=1; i < jMatcher.groupCount()+1; ++i) {
                    al.add(jMatcher.start(i));
                    al.add(jMatcher.end(i));
                }
            }
            return al;
        }
    }
    
}
