package com.excelsior.xds.ui.viewers;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.excelsior.xds.core.model.CompilationUnitType;
import com.excelsior.xds.core.model.IXdsAliasQualifiedImportElement;
import com.excelsior.xds.core.model.IXdsCompilationUnit;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.parser.modula.symbol.IModuleAliasSymbol;
import com.excelsior.xds.ui.images.ImageUtils;

public class XdsElementLabelProvider implements ILabelProvider 
{
	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
    //NOTE: changes in node type recognition must me mirrored in XdsOutlineFilter.isFiltered()
	public Image getImage(Object element) {
	    Image image = null;
	    if (element instanceof IXdsCompilationUnit) {
    		IXdsCompilationUnit xdsCompilationUnit = (IXdsCompilationUnit)element;
    		CompilationUnitType compilationUnitType = xdsCompilationUnit.getCompilationUnitType();
    		if (CompilationUnitType.DEFINITION_MODULE == compilationUnitType) {
    			image = ImageUtils.getImage(ImageUtils.DEFINITION_MODULE_IMAGE_NAME);
    		}
    		else if (CompilationUnitType.PROGRAM_MODULE == compilationUnitType) {
    			image = ImageUtils.getImage(ImageUtils.IMPLEMENTATION_MODULE_IMAGE_NAME);
    		}
            else if (CompilationUnitType.SYMBOL_FILE == compilationUnitType) {
                return ImageUtils.getImage(ImageUtils.SYMBOL_FILE_IMAGE_NAME);
            }
    	}
	    else if (element instanceof IXdsElement) {
	        image = XdsElementImages.getModulaElementImage((IXdsElement)element);
	    }
        
        if (image == null) {
            image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
        }
		return image;
	}

    
    @Override
    public String getText(Object element)
    {
        if (element instanceof IXdsAliasQualifiedImportElement) {
            IModuleAliasSymbol aliasSymbol = ((IXdsAliasQualifiedImportElement)element).getAliasSymbol();
            if (aliasSymbol == null) {
            	return null;
            }
			return aliasSymbol.getName();
        } else if (element instanceof IXdsElement) {
        	IXdsElement e = (IXdsElement)element;
            return e.getElementName();
        }
        return null;
    }
    
}
