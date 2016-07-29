package com.excelsior.xds.parser.modula.ast.types;

import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.symbol.type.IArrayTypeSymbol;

public class AstArrayType extends AstTypeDef<IArrayTypeSymbol>  {

    public AstArrayType(ModulaCompositeType<? extends AstArrayType> elementType) {
        super(null, elementType);
    }

}
