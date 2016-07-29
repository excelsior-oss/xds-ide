package com.excelsior.xds.parser.modula.ast.statements;

import com.excelsior.xds.parser.modula.ast.ModulaAstNode;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;

public class AstCaseElsePart extends ModulaAstNode
{
    public AstCaseElsePart(ModulaCompositeType<AstCaseElsePart> elementType) {
        super(null, elementType);
    }

    public AstStatementList getAstStatementList() {
        return findFirstChild( ModulaElementTypes.STATEMENT_LIST
                             , ModulaElementTypes.STATEMENT_LIST.getNodeClass() );
    }
    
}
