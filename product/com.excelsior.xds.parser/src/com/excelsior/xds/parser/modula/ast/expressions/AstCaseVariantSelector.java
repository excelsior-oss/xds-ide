package com.excelsior.xds.parser.modula.ast.expressions;

import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;

/**
 * CaseSelector = OrdinalExpression <br>
 */
public class AstCaseVariantSelector extends AstExpression
{
    public AstCaseVariantSelector(ModulaCompositeType<AstCaseVariantSelector> elementType) {
        super(elementType);
    }

}
