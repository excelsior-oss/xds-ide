package com.excelsior.xds.core.project;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import com.excelsior.xds.core.natures.NatureIdRegistry;
import com.excelsior.xds.core.resource.ResourceUtils;

/**
 * Special folders are used for the internal purposes.
 * @author lsa80
 */
public final class SpecialFolderNames 
{
    public static final String EXTERNAL_DEPENDENCIES_DIR_NAME = ".externals"; //$NON-NLS-1$
    public static final String VIRTUAL_MOUNT_ROOT_DIR_NAME = ".mnt"; //$NON-NLS-1$
    public static final String SETTINGS_DIR_NAME = ".settings"; //$NON-NLS-1$

    private static final Set<String> SPECIAL_FOLDER_NAMES = new HashSet<>(Arrays.asList(EXTERNAL_DEPENDENCIES_DIR_NAME, SETTINGS_DIR_NAME, VIRTUAL_MOUNT_ROOT_DIR_NAME));
    private static final Set<String> IGNORED_SPECIAL_FOLDER_NAMES = new HashSet<>();
    static{
    	IGNORED_SPECIAL_FOLDER_NAMES.addAll(SPECIAL_FOLDER_NAMES);
    	IGNORED_SPECIAL_FOLDER_NAMES.remove(VIRTUAL_MOUNT_ROOT_DIR_NAME);
    }

    /**
     * Only static methods are allowed.
     */
    private SpecialFolderNames() {
	}
    
    public static Set<String> getIgnoredSpecialFolderNames() {
    	return Collections.unmodifiableSet(IGNORED_SPECIAL_FOLDER_NAMES);
    }

	public static Set<String> getSpecialFolderRelativePathes(IProject p) {
        if (!NatureUtils.hasNature(p, NatureIdRegistry.MODULA2_SOURCE_PROJECT_NATURE_ID)) {
            return Collections.emptySet();
        }
        
        Set<String> pathes = new HashSet<>();
        for (String name : SPECIAL_FOLDER_NAMES) {
            IFolder f = p.getFolder(name);
            if (f != null) {
                pathes.add(f.getProjectRelativePath().toPortableString());
            }
        }
        
        return pathes;
    }
	
	/**
	 * Checks whether resource is inside special folder (like {@link #VIRTUAL_MOUNT_ROOT_DIR_NAME})
	 * @param r resource to check
	 * @return true if inside
	 */
	public static boolean isInsideIgnoredSpecialFolder(IResource r) {
		for (String folderName : getIgnoredSpecialFolderNames()) {
			if (ResourceUtils.isInsideFolder(folderName, r)) {
				return true;
			}
		}
		return false;
	}
}