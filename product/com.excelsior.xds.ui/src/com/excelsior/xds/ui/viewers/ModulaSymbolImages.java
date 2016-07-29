package com.excelsior.xds.ui.viewers;

import org.eclipse.swt.graphics.Image;

import com.excelsior.xds.parser.modula.symbol.IConstantSymbol;
import com.excelsior.xds.parser.modula.symbol.IDefinitionModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IFormalParameterSymbol;
import com.excelsior.xds.parser.modula.symbol.ILocalModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModuleAliasSymbol;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IOberonMethodReceiverSymbol;
import com.excelsior.xds.parser.modula.symbol.IProcedureSymbol;
import com.excelsior.xds.parser.modula.symbol.IRecordFieldSymbol;
import com.excelsior.xds.parser.modula.symbol.IStandardProcedureSymbol;
import com.excelsior.xds.parser.modula.symbol.IVariableSymbol;
import com.excelsior.xds.parser.modula.symbol.SymbolAttribute;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;
import com.excelsior.xds.ui.images.ImageUtils;

public abstract class ModulaSymbolImages
{
    public static Image getImage(IModulaSymbol symbol) {
        Image image = null;
        if (symbol instanceof IProcedureSymbol) {
            image = getProcedureImage((IProcedureSymbol)symbol);
        }
        else if (symbol instanceof IVariableSymbol) {
            image = getVariableImage((IVariableSymbol)symbol);
        }
        else if (symbol instanceof IConstantSymbol) {
            image = getConstantIgame((IConstantSymbol)symbol);
        }
        else if (symbol instanceof IModuleSymbol) {
            image = getModuleImage((IModuleSymbol)symbol);
        } else if (symbol instanceof IModuleAliasSymbol) {
            image = ImageUtils.getImage(ImageUtils.OBJ_DEF_MODULE_16x16);
        }
        else if (symbol instanceof ITypeSymbol) {
            image = getTypeImage((ITypeSymbol)symbol);
        }
        else if (symbol instanceof IRecordFieldSymbol) {
            image = getRecordFieldImage((IRecordFieldSymbol)symbol);
        }
        else if (symbol instanceof IFormalParameterSymbol) {
            image = getFormalParameterIgame((IFormalParameterSymbol)symbol);
        }
        else if (symbol instanceof IStandardProcedureSymbol) {
            image = ImageUtils.getImage(ImageUtils.OBJ_PROCEDURE_PUB_16x16);
        }
        return image;
    }
    
    
    public static Image getModuleImage(IModuleSymbol symbol) {
        Image image;
        if (symbol instanceof IDefinitionModuleSymbol) {
            image = ImageUtils.getImage(ImageUtils.OBJ_DEF_MODULE_16x16);
        }
        else if (symbol instanceof ILocalModuleSymbol) {
            image = ImageUtils.getImage(ImageUtils.OBJ_LOCAL_MODULE_16x16);
        }
        else {
            image = ImageUtils.getImage(ImageUtils.OBJ_PROGRAM_MODULE_16x16);
        }
        return image;
    }
    
    
    public static Image getUnqualifiedImportImage(IModulaSymbol symbol) {
        Image image = null;
        if (symbol instanceof IProcedureSymbol) {
            image = ImageUtils.getImage(ImageUtils.OBJ_PROCEDURE_PUB_16x16);
        }
        else if (symbol instanceof IStandardProcedureSymbol) {
            image = ImageUtils.getImage(ImageUtils.OBJ_PROCEDURE_PUB_16x16);
        }
        else if (symbol instanceof ITypeSymbol) {
            if (symbol.isAttributeSet(SymbolAttribute.READ_ONLY)) {
                image = ImageUtils.getImage(ImageUtils.OBJ_TYPE_RO_16x16);
            } else {
                image = ImageUtils.getImage(ImageUtils.OBJ_TYPE_PUB_16x16);
            }
        }
        else if (symbol instanceof IVariableSymbol) {
            if (symbol.isAttributeSet(SymbolAttribute.READ_ONLY)) {
                image = ImageUtils.getImage(ImageUtils.OBJ_GLOB_VAR_RO_16x16);
            }
            else {
                image = ImageUtils.getImage(ImageUtils.OBJ_GLOB_VAR_PUB_16x16);
            } 
        }
        else if (symbol instanceof IConstantSymbol) {
            image = ImageUtils.getImage(ImageUtils.OBJ_CONSTANT_PUB_16x16);
        }
        if (image == null) {
            image =  ImageUtils.getImage(ImageUtils.OBJ_IMPORT_16x16);
        }
        return image;
    }
    
    
    public static Image getConstantIgame(IConstantSymbol symbol) {
        Image image;
        if (symbol == null) {
            image = ImageUtils.getImage(ImageUtils.OBJ_CONSTANT_16x16);
        }
        else if (symbol.isAttributeSet(SymbolAttribute.PUBLIC)) {
            image = ImageUtils.getImage(ImageUtils.OBJ_CONSTANT_PUB_16x16);
        } else {
            image = ImageUtils.getImage(ImageUtils.OBJ_CONSTANT_16x16);
        }
        return image;
    }

    
    public static Image getTypeImage(ITypeSymbol symbol) {
        Image image;
        if (symbol == null) {
            image = ImageUtils.getImage(ImageUtils.OBJ_TYPE_16x16);
        }
        else if (symbol.isAttributeSet(SymbolAttribute.READ_ONLY)) {
            image = ImageUtils.getImage(ImageUtils.OBJ_TYPE_RO_16x16);
        }
        else if (symbol.isAttributeSet(SymbolAttribute.PUBLIC)) {
            image = ImageUtils.getImage(ImageUtils.OBJ_TYPE_PUB_16x16);
        } 
        else {
            image = ImageUtils.getImage(ImageUtils.OBJ_TYPE_16x16);
        }
        return image;
    }

    
    public static Image getRecordFieldImage(IRecordFieldSymbol symbol) {
        Image image;
        if (symbol == null) {
            image = ImageUtils.getImage(ImageUtils.OBJ_RECORD_FIELD_16x16);
        }
        else if (symbol.isAttributeSet(SymbolAttribute.READ_ONLY)) {
            image = ImageUtils.getImage(ImageUtils.OBJ_RECORD_FIELD_RO_16x16);
        }
        else if (symbol.isAttributeSet(SymbolAttribute.PUBLIC)) {
            image = ImageUtils.getImage(ImageUtils.OBJ_RECORD_FIELD_PUB_16x16);
        } 
        else {
            image = ImageUtils.getImage(ImageUtils.OBJ_RECORD_FIELD_16x16);
        }
        return image;
    }
    
    
    public static Image getVariableImage(IVariableSymbol symbol) {
        Image image;
        if (symbol.isLocal()) {
            image = ImageUtils.getImage(ImageUtils.OBJ_LOC_VAR_16x16);
        } 
        else if (symbol.isAttributeSet(SymbolAttribute.READ_ONLY)) {
            image = ImageUtils.getImage(ImageUtils.OBJ_GLOB_VAR_RO_16x16);
        }
        else if (symbol.isAttributeSet(SymbolAttribute.EXTERNAL)) {
            image = ImageUtils.getImage(ImageUtils.OBJ_GLOB_VAR_EXT_16x16);
        } 
        else if (symbol.isAttributeSet(SymbolAttribute.PUBLIC)) {
            image = ImageUtils.getImage(ImageUtils.OBJ_GLOB_VAR_PUB_16x16);
        } 
        else {
            image = ImageUtils.getImage(ImageUtils.OBJ_GLOB_VAR_16x16);
        }
        return image;
    }

    
    public static Image getProcedureImage(IProcedureSymbol symbol) {
        Image image;
        if (symbol.isAttributeSet(SymbolAttribute.EXTERNAL)) {
            image = ImageUtils.getImage(ImageUtils.OBJ_PROCEDURE_EXT_16x16);
        }
        else if (symbol.isAttributeSet(SymbolAttribute.PUBLIC)) {
            image = ImageUtils.getImage(ImageUtils.OBJ_PROCEDURE_PUB_16x16);
        }
        else {
            image = ImageUtils.getImage(ImageUtils.OBJ_PROCEDURE_16x16);
        }
        return image;
    }
    

    public static Image getFormalParameterIgame(IFormalParameterSymbol symbol) {
        Image image;
        if (symbol == null) {
            image = ImageUtils.getImage(ImageUtils.OBJ_FORAML_PARAMETER_16x16);
        }
        else if (symbol instanceof IOberonMethodReceiverSymbol) {
            image = ImageUtils.getImage(ImageUtils.OBJ_OBERON_METHOD_RECEIVER_16x16);
        }
        else if (symbol.isAttributeSet(SymbolAttribute.VAR_PARAMETER)) {
            image = ImageUtils.getImage(ImageUtils.OBJ_FORAML_PARAMETER_PUB_16x16);
        }
        else if (symbol.isAttributeSet(SymbolAttribute.READ_ONLY)) {
                image = ImageUtils.getImage(ImageUtils.OBJ_FORAML_PARAMETER_RO_16x16);
        } else {
            image = ImageUtils.getImage(ImageUtils.OBJ_FORAML_PARAMETER_16x16);
        }
        return image;
    }

}
