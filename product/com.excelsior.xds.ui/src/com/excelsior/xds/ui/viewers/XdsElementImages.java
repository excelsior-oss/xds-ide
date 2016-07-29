package com.excelsior.xds.ui.viewers;

import org.eclipse.swt.graphics.Image;

import com.excelsior.xds.core.model.CompilationUnitType;
import com.excelsior.xds.core.model.IXdsCompilationUnit;
import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.IXdsElementWithSymbol;
import com.excelsior.xds.core.model.IXdsExternalDependenciesContainer;
import com.excelsior.xds.core.model.IXdsImportElement;
import com.excelsior.xds.core.model.IXdsImportSection;
import com.excelsior.xds.core.model.IXdsProjectFile;
import com.excelsior.xds.core.model.IXdsQualifiedImportElement;
import com.excelsior.xds.core.model.IXdsRecordVariant;
import com.excelsior.xds.core.model.IXdsRecordVariantSelector;
import com.excelsior.xds.core.model.IXdsSdkLibraryContainer;
import com.excelsior.xds.core.model.IXdsUnqualifiedImportContainer;
import com.excelsior.xds.core.model.IXdsUnqualifiedImportElement;
import com.excelsior.xds.core.model.LoadingXdsElement;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IRecordVariantSelectorSymbol;
import com.excelsior.xds.ui.images.ImageUtils;

public abstract class XdsElementImages
{
    public static Image getModulaElementImage(IXdsElement element) {
        Image image = null;
        if (element instanceof IXdsImportElement) {
            if (element instanceof IXdsQualifiedImportElement) {
                IModuleSymbol symbol = ((IXdsQualifiedImportElement)element).getSymbol();
                image = ModulaSymbolImages.getModuleImage(symbol);
            }
            else if (element instanceof IXdsUnqualifiedImportElement) {
                IModulaSymbol symbol = ((IXdsUnqualifiedImportElement)element).getSymbol();
                image = ModulaSymbolImages.getUnqualifiedImportImage(symbol);
            }
            else if (element instanceof IXdsUnqualifiedImportContainer) {
                image = ImageUtils.getImage(ImageUtils.OBJ_IMPORT_LIST_16x16);
            }
            else if (element instanceof IXdsImportSection) {
                image = ImageUtils.getImage(ImageUtils.OBJ_IMPORT_LIST_16x16);
            }
        }
        else if (element instanceof IXdsElementWithSymbol) {
            IModulaSymbol symbol = ((IXdsElementWithSymbol)element).getSymbol();
            image = ModulaSymbolImages.getImage(symbol);
        }
        else if (element instanceof IXdsRecordVariant) {
            image = ImageUtils.getImage(ImageUtils.OBJ_IMPORT_LIST_16x16);
        }
        else if (element instanceof IXdsRecordVariantSelector) {
            IRecordVariantSelectorSymbol symbol = ((IXdsRecordVariantSelector)element).getSymbol();
            if (symbol != null && symbol.getName() != null) {
                image = ModulaSymbolImages.getRecordFieldImage(symbol);
            }
            else {
                image = ImageUtils.getImage(ImageUtils.OBJ_VARIANT_RECORD_16x16);
            }
        }
        else if (element instanceof LoadingXdsElement) {
            image = ImageUtils.getImage(ImageUtils.OBJ_LOADING_16x16);
        }
        return image;
    }

    
    public static Image getProjectElementImage(IXdsElement element) {
        Image image = null;
        if (element instanceof IXdsCompilationUnit ) {
            IXdsCompilationUnit xdsCompilationUnit = (IXdsCompilationUnit)element;
            CompilationUnitType compilationUnitType = xdsCompilationUnit.getCompilationUnitType();
            if (CompilationUnitType.DEFINITION_MODULE == compilationUnitType) {
                return ImageUtils.getImage(ImageUtils.DEFINITION_MODULE_IMAGE_NAME);
            }
            else if (CompilationUnitType.PROGRAM_MODULE == compilationUnitType) {
                return ImageUtils.getImage(ImageUtils.IMPLEMENTATION_MODULE_IMAGE_NAME);
            }
            else if (CompilationUnitType.SYMBOL_FILE == compilationUnitType) {
                return ImageUtils.getImage(ImageUtils.SYMBOL_FILE_IMAGE_NAME);
            }
        }
        else if (element instanceof IXdsProjectFile) {
            return ImageUtils.getImage(ImageUtils.XDS_PROJECT_FILE_IMAGE_NAME);
        }
        else if (element instanceof IXdsExternalDependenciesContainer) {
            return ImageUtils.getImage(ImageUtils.EXTERNAL_DEPENDENCIES_FOLDER_IMAGE_NAME);
        }
        else if (element instanceof IXdsSdkLibraryContainer) {
            return ImageUtils.getImage(ImageUtils.SDK_LIBRARY_FOLDER_IMAGE_NAME);
        }
        else if (element instanceof IXdsContainer) {
            return ImageUtils.getImage(ImageUtils.PACKAGE_FRAGMENT_IMAGE_NAME);
        }
        return image;
    }
    
}
