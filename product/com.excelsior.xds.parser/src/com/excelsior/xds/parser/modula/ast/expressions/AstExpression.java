package com.excelsior.xds.parser.modula.ast.expressions;

import com.excelsior.xds.parser.modula.ast.ModulaAstNode;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;

public class AstExpression extends ModulaAstNode
{
    public AstExpression(ModulaCompositeType<? extends AstExpression> elementType) {
        super(null, elementType);
    }

}
