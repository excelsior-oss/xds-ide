package com.excelsior.xds.parser.modula.ast.expressions;

import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;

/**
 * CaseLabel = ConstantExpression [".." ConstantExpression] <br>
 */
public class AstCaseLabel extends AstConstantExpression
{
    public AstCaseLabel(ModulaCompositeType<AstCaseLabel> elementType) {
        super(elementType);
    }

}
