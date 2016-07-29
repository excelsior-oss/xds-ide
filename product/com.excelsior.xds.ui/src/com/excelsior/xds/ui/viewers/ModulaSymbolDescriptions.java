package com.excelsior.xds.ui.viewers;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import com.excelsior.xds.parser.modula.symbol.IEnumElementSymbol;
import com.excelsior.xds.parser.modula.symbol.IFinallyBodySymbol;
import com.excelsior.xds.parser.modula.symbol.IFormalParameterSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModuleAliasSymbol;
import com.excelsior.xds.parser.modula.symbol.IModuleBodySymbol;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IProcedureSymbol;
import com.excelsior.xds.parser.modula.symbol.IRecordFieldSymbol;
import com.excelsior.xds.parser.modula.symbol.IStandardModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IVariableSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IArrayTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IEnumTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IOpaqueTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IOrdinalTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IPointerTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IProcedureTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IRangeTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IRecordTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ISetTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSynonymSymbol;
import com.excelsior.xds.parser.modula.utils.ModulaSymbolUtils;

public abstract class ModulaSymbolDescriptions
{
    public static String getSymbolDescription(IModulaSymbol symbol) {
        String description;
        if (symbol instanceof IProcedureSymbol) {
            description = getProcedureDescription((IProcedureSymbol)symbol);
        }
        else if (symbol instanceof IVariableSymbol) {
            description = getVariableDescription((IVariableSymbol)symbol);
        }
        else if (symbol instanceof ITypeSymbol) {
            description = getTypeDescription((ITypeSymbol)symbol);
        }
        else if (symbol instanceof IRecordFieldSymbol) {
            description = getRecordFieldSignature((IRecordFieldSymbol)symbol);
        }
        else if (symbol instanceof IEnumElementSymbol) {
            description = getEnumElementDescription((IEnumElementSymbol)symbol);
        }
        else if (symbol instanceof IFormalParameterSymbol) {
            description = getFormalParameterDescription((IFormalParameterSymbol)symbol);
        }
        else if (symbol instanceof IModuleAliasSymbol) {
            description = getModuleAliasDescription((IModuleAliasSymbol)symbol);
        }
        else {
            description = "";    //$NON-NLS-1$
        }
        return description;
    }

    public static String getVariableDescription(IVariableSymbol symbol) {
        String description = "";    //$NON-NLS-1$
        if (symbol != null) {
            ITypeSymbol typeSymbol = symbol.getTypeSymbol();
            if (typeSymbol != null) {
                if (!typeSymbol.isAnonymous()) {
                    description = typeSymbol.getName();
                } else {
                    description = getTypeAbbreviation(typeSymbol);
                }
                if (StringUtils.isNotBlank(description)) {
                    description = " : " + description;    //$NON-NLS-1$
                }
            }
        }
        return description;
    }

    public static String getRecordFieldSignature(IRecordFieldSymbol symbol) {
        String description = "";    //$NON-NLS-1$
        if (symbol != null) {
            ITypeSymbol typeSymbol = symbol.getTypeSymbol();
            if (typeSymbol != null) {
                if (!typeSymbol.isAnonymous()) {
                    description = typeSymbol.getName();
                } else {
                    description = getTypeAbbreviation(typeSymbol);
                }
                if (StringUtils.isNotBlank(description)) {
                    description = " : " + description;    //$NON-NLS-1$
                }
            }
        }
        return description;
    }

    public static String getTypeDescription(ITypeSymbol symbol) {
        String description = "";    //$NON-NLS-1$
        if (symbol != null) {
            if (!(symbol.getParentScope() instanceof IStandardModuleSymbol)) {
                // signature isn't required for types from standard modules. 
                description = getTypeAbbreviation(symbol);
                if (StringUtils.isNotBlank(description)) {
                    description = " = " + description;    //$NON-NLS-1$
                }
            }
        }
        return description;
    }

    public static String getEnumElementDescription(IEnumElementSymbol symbol) {
        String description = "";    //$NON-NLS-1$
        if (symbol != null) {
            description = "" + symbol.getValue();    //$NON-NLS-1$
            if (StringUtils.isNotBlank(description)) {
                String typeName = ModulaSymbolUtils.getSymbolName(symbol.getTypeSymbol());
                if (StringUtils.isNotBlank(typeName)) {
                    description = typeName + "{" + description + "}";    //$NON-NLS-1$   //$NON-NLS-2$
                }
                description = " = " + description;    //$NON-NLS-1$
            }
        }
        return description;
    }

    public static String getProcedureDescription(IProcedureSymbol symbol) {
        String description = "";
        boolean hasDescription = (symbol != null)
                              && !(symbol instanceof IModuleBodySymbol)
                              && !(symbol instanceof IFinallyBodySymbol);
        if (hasDescription) {
            StringBuilder sb = new StringBuilder();
            sb.append(" (");    //$NON-NLS-1$
            Collection<IFormalParameterSymbol> parameters = symbol.getParameters();
            boolean isFirstParam = true;
            for (IFormalParameterSymbol paramSymbol : parameters) {
                if (!isFirstParam) {
                    sb.append(", ");    //$NON-NLS-1$
                }
                else {
                    isFirstParam = false;
                }
                if (paramSymbol.isVarParameter()) {
                    sb.append("VAR ");    //$NON-NLS-1$
                }
                sb.append(paramSymbol.getName());
            }
            sb.append(')');   //$NON-NLS-1$
            
            ITypeSymbol its = symbol.getReturnTypeSymbol();
            if (its != null) {
                sb.append(" : " + its.getName());    //$NON-NLS-1$    
            }
            description = sb.toString();
        }
        return description;
    }
    
    public static String getProcedureSignature(IProcedureTypeSymbol symbol) {
         String description = "";    //$NON-NLS-1$
         if (symbol != null) {
             StringBuilder sb = new StringBuilder();
             sb.append(" (");    //$NON-NLS-1$
             Collection<IFormalParameterSymbol> parameters = symbol.getParameters();
             boolean isFirstParam = true;
             for (IFormalParameterSymbol paramSymbol : parameters) {
                 if (!isFirstParam) {
                     sb.append(", ");    //$NON-NLS-1$
                 }
                 else {
                     isFirstParam = false;
                 }
                 if (paramSymbol.isVarParameter()) {
                     sb.append("VAR ");    //$NON-NLS-1$
                 }
                 sb.append(paramSymbol.getName());
             }
             sb.append(')');   //$NON-NLS-1$
             
             ITypeSymbol its = symbol.getReturnTypeSymbol();
             if (its != null) {
                 sb.append(" : " + its.getName());    //$NON-NLS-1$    
             }
             description = sb.toString();
         }
         return description;
    }

    public static String getFormalParameterDescription(IFormalParameterSymbol symbol) {
        String description = "";    //$NON-NLS-1$
        if (symbol != null) {
            ITypeSymbol typeSymbol = symbol.getTypeSymbol();
            if (typeSymbol != null) {
                if (!typeSymbol.isAnonymous()) {
                    description = typeSymbol.getName();
                } else {
                    description = getTypeAbbreviation(typeSymbol);
                }
                if (description != null && !description.isEmpty()) {
                    description = " : " + description;    //$NON-NLS-1$
                }
            }
        }
        return description;
    }
    
    public static String getModuleAliasDescription(IModuleAliasSymbol symbol) {
        String description = "";    //$NON-NLS-1$
        IModuleSymbol reference = symbol.getReference();
        if (reference != null) {
            description = " := " + reference.getName();
        }
        return description;
    }

    
    public static String getTypeAbbreviation(ITypeSymbol symbol) {
        String abbreviation;
        if (symbol instanceof ITypeSynonymSymbol) {
            ITypeSymbol   synonymSymbol = ((ITypeSynonymSymbol)symbol).getReferencedSymbol();
            IModuleSymbol synonymModule = ModulaSymbolUtils.getParentModule(synonymSymbol);
            String symbolDeclModuleName  = ModulaSymbolUtils.getSymbolName(ModulaSymbolUtils.getParentModule(symbol));
            String synonymDeclModuleName = ModulaSymbolUtils.getSymbolName(synonymModule);
            boolean isSynonymToImportedType = StringUtils.isNotBlank(synonymDeclModuleName) 
                                           && !StringUtils.equals(symbolDeclModuleName, synonymDeclModuleName);
            if (isSynonymToImportedType && !(synonymModule instanceof IStandardModuleSymbol)) {
                abbreviation = synonymDeclModuleName + "." + synonymSymbol.getName();   //$NON-NLS-1$
            }
            else {
                abbreviation = synonymSymbol.getName();
            }
        }
        else if (symbol instanceof IArrayTypeSymbol) {
            abbreviation = "ARRAY";       //$NON-NLS-1$
            ITypeSymbol elementTypeSymbol = ((IArrayTypeSymbol)symbol).getElementTypeSymbol();
            if ((elementTypeSymbol != null) && !elementTypeSymbol.isAnonymous()) {
                abbreviation += " of " + elementTypeSymbol.getName();    //$NON-NLS-1$ 
            }
        } else if (symbol instanceof IEnumTypeSymbol) {
            abbreviation = "( ,,, )";        //$NON-NLS-1$
        } else if (symbol instanceof IPointerTypeSymbol) {
            abbreviation = "POINTER";        //$NON-NLS-1$
            ITypeSymbol boundTypeSymbol = ((IPointerTypeSymbol)symbol).getBoundTypeSymbol();
            if ((boundTypeSymbol != null) && !boundTypeSymbol.isAnonymous()) {
                abbreviation += " to " + boundTypeSymbol.getName();     //$NON-NLS-1$
            }
        } else if (symbol instanceof IProcedureTypeSymbol) {
            abbreviation = "PROCEDURE";   //$NON-NLS-1$
        } else if (symbol instanceof IRangeTypeSymbol) {
            abbreviation = "";            //$NON-NLS-1$ 
            IOrdinalTypeSymbol baseTypeSymbol = ((IRangeTypeSymbol)symbol).getBaseTypeSymbol();
            if ((baseTypeSymbol != null) && !baseTypeSymbol.isAnonymous()) {
                abbreviation += baseTypeSymbol.getName();     //$NON-NLS-1$
            }
            abbreviation += " [ .. ]";    //$NON-NLS-1$ 
        } else if (symbol instanceof IRecordTypeSymbol) {
            abbreviation = "RECORD";      //$NON-NLS-1$
            IRecordTypeSymbol baseTypeSymbol = ((IRecordTypeSymbol)symbol).getBaseTypeSymbol(); 
            if (baseTypeSymbol != null) {
                abbreviation += " (" + baseTypeSymbol.getName() + ")";    //$NON-NLS-1$ //$NON-NLS-2$
            }
        } else if (symbol instanceof ISetTypeSymbol) {
            if (((ISetTypeSymbol)symbol).isPacked()) {
                abbreviation = "PACKEDSET";      //$NON-NLS-1$ 
            }
            else {
                abbreviation = "SET";            //$NON-NLS-1$ 
            }
            IOrdinalTypeSymbol baseTypeSymbol = ((ISetTypeSymbol)symbol).getBaseTypeSymbol();
            if (baseTypeSymbol != null) {
                if (!baseTypeSymbol.isAnonymous()) {
                    abbreviation += " of " + baseTypeSymbol.getName();     //$NON-NLS-1$
                } else {
                    String s = getTypeAbbreviation(baseTypeSymbol);
                    if (!s.isEmpty()) {
                        abbreviation += " of " + s;     //$NON-NLS-1$
                    }
                    
                }
            }
        } else if (symbol instanceof IOpaqueTypeSymbol) {
            abbreviation = "OPAQUE";      //$NON-NLS-1$ 
        } else if (symbol != null) {
            abbreviation = symbol.getName();
        }
        else {
            abbreviation = "";
        }
        return abbreviation;
    }
    
}
