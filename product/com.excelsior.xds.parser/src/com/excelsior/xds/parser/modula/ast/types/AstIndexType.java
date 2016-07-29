package com.excelsior.xds.parser.modula.ast.types;

import com.excelsior.xds.parser.modula.ast.ModulaAstNode;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;

public class AstIndexType extends ModulaAstNode
{
    public AstIndexType(ModulaCompositeType<AstIndexType> elementType) {
        super(null, elementType);
    }

}
