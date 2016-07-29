package com.excelsior.xds.parser.modula.ast.statements;

import java.util.List;

import com.excelsior.xds.parser.modula.ast.ModulaAstNode;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;
import com.excelsior.xds.parser.modula.ast.expressions.AstCaseLabel;

/**
 * CaseLabelList = CaseLabel {"," CaseLabel} <br>
 */
public class AstCaseLabelList extends ModulaAstNode
{
    public AstCaseLabelList(ModulaCompositeType<AstCaseLabelList> elementType) {
        super(null, elementType);
    }

    public List<AstCaseLabel> getCaseLabels() {
        return findChildren( ModulaElementTypes.CASE_LABEL
                           , ModulaElementTypes.CASE_LABEL.getNodeClass() );
    }
    
}
