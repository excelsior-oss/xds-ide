package com.excelsior.xds.parser.modula.ast.types;

import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.symbol.type.IProcedureTypeSymbol;

public class AstProcedureTypeOberon extends AstTypeDef<IProcedureTypeSymbol> 
{
    public AstProcedureTypeOberon(ModulaCompositeType<AstProcedureTypeOberon> elementType) {
        super(null, elementType);
    }

}
