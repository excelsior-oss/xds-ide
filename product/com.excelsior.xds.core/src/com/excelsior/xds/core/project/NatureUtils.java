package com.excelsior.xds.core.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.natures.NatureIdRegistry;

public final class NatureUtils {
	private NatureUtils() {
		super();
	}

	public static void addNature(IProject project, String nature) {
		try {
			IProjectDescription description = project.getDescription();
			String[] currentNatures = description.getNatureIds();
			String[] newNatures = new String[currentNatures.length + 1];
			System.arraycopy(currentNatures, 0, newNatures, 0, currentNatures.length);
			newNatures[currentNatures.length] = nature;
			description.setNatureIds(newNatures);
			project.setDescription(description, null);
		} catch (CoreException e) {
			LogHelper.logError(e);
		}
	}

	public static void removeNature(IProject project, String nature) {
		try {
			IProjectDescription description = project.getDescription();
			String[] currentNatures = description.getNatureIds();

			int index = 0;
			for (int i = 0; i < currentNatures.length; i++) {
				if (nature.equals(currentNatures[i])) {
					index = i;
					break;
				}
			}

			if (index != -1) {
				String[] newNatures = new String[currentNatures.length - 1];
				System.arraycopy(currentNatures, 0, newNatures, 0, index);
				System.arraycopy(currentNatures, index + 1, newNatures, index,
						newNatures.length - index);
				description.setNatureIds(newNatures);
				project.setDescription(description, null);
			}
		} catch (CoreException e) {
			LogHelper.logError(e);
		}
	}
	
	public static boolean hasModula2Nature(IProject project) {
		return hasNature(project, NatureIdRegistry.MODULA2_SOURCE_PROJECT_NATURE_ID);
	}
	
	public static boolean hasNature(IProject project, String nature) {
	    if (project == null || !project.isOpen()) return false;
	    try {
            return project.getNature(nature) != null;
        } catch (CoreException e) {
            LogHelper.logError(e);
        }
	    return false;
	}
}