package com.excelsior.xds.parser.modula.ast.types;

import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;

public class AstOpenArrayType extends AstArrayType {

    public AstOpenArrayType(ModulaCompositeType<AstOpenArrayType> elementType) {
        super(elementType);
    }

}
