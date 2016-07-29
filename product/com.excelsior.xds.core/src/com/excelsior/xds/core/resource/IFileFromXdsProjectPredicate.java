package com.excelsior.xds.core.resource;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import com.excelsior.xds.core.project.SpecialFolderNames;
import com.excelsior.xds.core.utils.JavaUtils;
import com.google.common.base.Predicate;

/**
 * Base class for predicates to filter all {@link IResource} except {@link IFile}<br>
 * Skips settings directory.
 * see {@link ResourceUtils#getProjectResources}<br>
 * @author lsa80
 */
public class IFileFromXdsProjectPredicate implements Predicate<IResource> {
	@Override
	public boolean apply(IResource ifile) {
		if (!JavaUtils.isOneOf(ifile, IFile.class)
				|| ResourceUtils.isInsideFolder(SpecialFolderNames.SETTINGS_DIR_NAME, ifile)
				|| ResourceUtils.isInsideFolder(SpecialFolderNames.EXTERNAL_DEPENDENCIES_DIR_NAME, ifile)
				) {
			return false;
		}
		return true;
	}
}