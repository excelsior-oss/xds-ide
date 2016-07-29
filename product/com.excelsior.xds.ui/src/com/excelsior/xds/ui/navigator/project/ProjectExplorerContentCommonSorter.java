package com.excelsior.xds.ui.navigator.project;

import java.text.Collator;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import com.excelsior.xds.core.model.IXdsExternalDependenciesContainer;
import com.excelsior.xds.core.model.IXdsSdkLibraryContainer;

public class ProjectExplorerContentCommonSorter extends ViewerSorter {
    private static Map<ComparableClass, Integer> class2Weight = new TreeMap<ComparableClass, Integer>(); // tree map since order is important
    
    static {
        class2Weight.put(new ComparableClass(IXdsExternalDependenciesContainer.class), 3);
        class2Weight.put(new ComparableClass(IXdsSdkLibraryContainer.class), 2);
        class2Weight.put(new ComparableClass(IContainer.class), 1);
    }
    
    /** We need this entity since we are going to store java.lang.Class as key in the TreeMap, which requires keys to be sortable
     * @author lsa80
     */
    @SuppressWarnings("rawtypes")
    private static class ComparableClass implements Comparable<ComparableClass> {
        private Class clazz;
        
        ComparableClass(Class clazz) {
            this.clazz = clazz;
        }

        @Override
        public int compareTo(ComparableClass o) {
            return clazz.getCanonicalName().compareTo(o.clazz.getCanonicalName());
        }
    }
    
    @SuppressWarnings("unchecked")
    private static int getWeight(Object e) {
        for (Map.Entry<ComparableClass, Integer> class2WeightEntry : class2Weight.entrySet()) {
            boolean isFound = class2WeightEntry.getKey().clazz.isAssignableFrom(e.getClass());
            if (!isFound) {
                if (e instanceof IAdaptable) {
                    IAdaptable adaptable = (IAdaptable)e;
                    IResource res = (IResource) adaptable.getAdapter(IResource.class);
                    if (res != null) {
                    	isFound = class2WeightEntry.getKey().clazz.isAssignableFrom(res.getClass());
                    }
                }
            }
            if (isFound) {
                return class2WeightEntry.getValue();
            }
        }
        return -1;
    }
    
	public ProjectExplorerContentCommonSorter() {
	}

	public ProjectExplorerContentCommonSorter(Collator collator) {
		super(collator);
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
	    int weight1 = getWeight(e1);
	    int weight2 = getWeight(e2);
	    
	    if (weight2 != weight1) {  // if elements are from different weight 'categories' - compare who is fatter
	        return weight2 - weight1; // ascending order
	    }
	    
	    // otherwise compare as usual
		return super.compare(viewer, e1, e2);
	}
}
