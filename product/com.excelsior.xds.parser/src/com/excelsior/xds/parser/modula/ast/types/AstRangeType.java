package com.excelsior.xds.parser.modula.ast.types;

import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.symbol.type.IRangeTypeSymbol;

public class AstRangeType extends AstTypeDef<IRangeTypeSymbol> 
{
    public AstRangeType(ModulaCompositeType<AstRangeType> elementType) {
        super(null, elementType);
    }

}
