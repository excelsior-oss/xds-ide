package com.excelsior.xds.parser.modula.ast.statements;

import com.excelsior.xds.parser.modula.ast.ModulaAstNode;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;

/**
 * CaseVariant = [CaseLabelList ":" StatementSequence] <br>
 */
public class AstCaseVariant extends ModulaAstNode
{
    public AstCaseVariant(ModulaCompositeType<AstCaseVariant> elementType) {
        super(null, elementType);
    }

    public AstCaseLabelList getAstCaseVariantLabelList() {
        return findFirstChild( ModulaElementTypes.CASE_LABEL_LIST
                             , ModulaElementTypes.CASE_LABEL_LIST.getNodeClass() );
    }
    
    public AstStatementList getAstStatementList() {
        return findFirstChild( ModulaElementTypes.STATEMENT_LIST
                             , ModulaElementTypes.STATEMENT_LIST.getNodeClass() );
    }
    
}
