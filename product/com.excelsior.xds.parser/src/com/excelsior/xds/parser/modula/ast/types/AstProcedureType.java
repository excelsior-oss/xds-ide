package com.excelsior.xds.parser.modula.ast.types;

import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.symbol.type.IProcedureTypeSymbol;

/**
 * procedure type definition in Modula-2 style. 
 */
public class AstProcedureType extends AstTypeDef<IProcedureTypeSymbol>  
{
    public AstProcedureType(ModulaCompositeType<AstProcedureType> elementType) {
        super(null, elementType);
    }

}
