package com.excelsior.xds.parser.modula.ast.statements;

import java.util.List;

import com.excelsior.xds.parser.modula.ast.ModulaAstNode;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;

/**
 * CaseVariantList = CaseVariant {"|" CaseVariant} [CaseElsePart] <br>
 */
public class AstCaseVariantList extends ModulaAstNode
{
    public AstCaseVariantList(ModulaCompositeType<AstCaseVariantList> elementType) {
        super(null, elementType);
    }

    public List<AstCaseVariant> getAstCaseVariants() {
        return findChildren(ModulaElementTypes.CASE_VARIANT);
    }
    
    public AstCaseElsePart getAstCaseElsePart() {
        return findFirstChild( ModulaElementTypes.CASE_ELSE_PART
                             , ModulaElementTypes.CASE_ELSE_PART.getNodeClass() );
    }

}
