package com.excelsior.xds.core.model.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.CompositeResourceMapping;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.ui.ide.ResourceUtil;

import com.excelsior.xds.core.model.ISourceBound;
import com.excelsior.xds.core.model.IXdsCompilationUnit;
import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.IXdsExternalDependenciesContainer;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.IXdsResource;
import com.excelsior.xds.core.model.SourceBinding;
import com.excelsior.xds.core.model.internal.XdsElementCommons;
import com.excelsior.xds.core.model.internal.resources.mapping.SimpleResourceMapping;
import com.excelsior.xds.core.model.plugin.ModelPlugin;
import com.excelsior.xds.core.resource.IResourceAccess;
import com.excelsior.xds.core.text.ITextRegion;
import com.excelsior.xds.parser.modula.ast.ModulaAst;

public final class XdsElementUtils 
{
	public static ResourceMapping createResourceMappingFrom(Collection<IXdsElement> elements, IXdsElement modelObject) {
    	List<ResourceMapping> resourceMappings = new ArrayList<ResourceMapping>();
		for (final Object element : elements) {
			final IResource r = (IResource) ResourceUtil.getAdapter(element, IResource.class, false);
			ResourceMapping resourceMapping;
			if (r != null) {
				resourceMapping = new SimpleResourceMapping(element, r);
			}
			else{
				resourceMapping = (ResourceMapping) ResourceUtil.getAdapter(element, ResourceMapping.class, false);
			}
			
			if (resourceMapping != null) {
				resourceMappings.add(resourceMapping);
			}
		}
		return new CompositeResourceMapping(ModelPlugin.PLUGIN_ID, modelObject, resourceMappings.toArray(new ResourceMapping[]{}));
    }
	
    public static IXdsElement findBottomostChildCoveringPosition( IXdsElement xdsElement
                                                                , int pos ) 
    {
        if (!(xdsElement instanceof ISourceBound)) {
            return null;
        }
        ISourceBound sourceBound = (ISourceBound) xdsElement;
        if (!isContainsPosition(sourceBound, pos))
            return null;

        IXdsElement thinnestChild = null;
        int thinnestRangeSize = Integer.MAX_VALUE;

        Collection<IXdsElement> children = new ArrayList<IXdsElement>();
        if (xdsElement instanceof IXdsContainer) {
            IXdsContainer xdsContainer = (IXdsContainer) xdsElement;
            children = xdsContainer.getChildren();
        }

        for (IXdsElement child : children) {
            if (child instanceof ISourceBound) {
                ISourceBound childSourceBound = (ISourceBound) child;
                ITextRegion elementRegion = childSourceBound.getSourceBinding().getElementRegion();
                if (elementRegion != null) {
                    int length = elementRegion.getLength();
                    if (isContainsPosition(childSourceBound, pos) && length < thinnestRangeSize) 
                    {
                        thinnestChild = child;
                        thinnestRangeSize = length;
                    }
                }
            }
        }

        IXdsElement result = null;
        if (thinnestChild != null) {
            result = findBottomostChildCoveringPosition(thinnestChild, pos);
            if (result == null)
                result = thinnestChild;
        }

        return result;
    }

    private static boolean isContainsPosition(ISourceBound sourceBound, int pos) 
    {
    	SourceBinding sourceBinding = sourceBound.getSourceBinding();
        Collection<ITextRegion> elementRegions = sourceBinding.getElementRegions();
        for (ITextRegion textRegion : elementRegions) {
			if ((textRegion != null) && textRegion.contains(pos)) {
				return true;
			}
		}
        return false; 
    }
    
    public static ModulaAst getModulaAst(IXdsElement xdsElement) {
        if (xdsElement instanceof IXdsCompilationUnit) {
            IXdsCompilationUnit compilationUnit = (IXdsCompilationUnit) xdsElement;
            return compilationUnit.getModulaAst();
        }
        return null;
    }
    
    /**
     * Checks whether there is a corresponding IXdsElement for the targetResource
     * @param targetResource
     * @param elements
     * @return
     */
    public static boolean isTargetResourceAffected(IResource targetResource, Collection<IXdsElement> elements) {
    	if (targetResource == null) {
    		return false;
    	}
    	
    	if (getElementWithResource(targetResource, elements) != null){
    		return true;
    	}
    	
    	return false;
    }
    
    /**
     * Gets the corresponding IXdsElement for the targetResource
     * @return
     */
    public static IXdsElement getElementWithResource(IResource targetResource, Collection<IXdsElement> elements) {
        if (targetResource != null) {
        	for (IXdsElement e : elements) {
            	if (e instanceof IXdsResource) {
            		IXdsResource er = (IXdsResource)e;
            		if (targetResource.equals(er.getResource())) {
            			return er;
                    }
            	}
            }
        }
    	return null;
    }
    
    /**
     * Returns all IXdsContainer`s which can contain user-editable source
     * 
     */
    public static Collection<IXdsContainer> getSourceRoots(IXdsProject xdsProject) {
    	Collection<IXdsContainer> roots = new ArrayList<IXdsContainer>();
    	roots.add(xdsProject);
    	IXdsExternalDependenciesContainer externalDependenciesContainer = xdsProject.getXdsExternalDependenciesContainer();
    	if (externalDependenciesContainer != null) {
    		Collection<IXdsElement> children = externalDependenciesContainer.getChildren();
    		for (IXdsElement child : children) {
				if (child instanceof IXdsContainer) {
					IXdsContainer c = (IXdsContainer) child;
					roots.add(c);
				}
			}
    	}
    	
    	return roots;
    }
    
    public static IResource adaptToResource(IXdsElement e, Class<?> adapter) {
    	if (e instanceof IResourceAccess) {
			return XdsElementCommons.adaptToResource((IResourceAccess) e, adapter);
		}
    	return null;
    }
    
    /**
     * Static methods only
     */
    private XdsElementUtils(){
    }
}
