package com.excelsior.xds.parser.modula.ast.statements;

import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;

/**
 * LoopStatement = "LOOP" StatementSequence "END" <br>
 */
public class AstLoopStatement extends AstStatement
{
    public AstLoopStatement(ModulaCompositeType<AstLoopStatement> elementType) {
        super(null, elementType);
    }

    public AstStatementList getAstStatementList() {
        return findFirstChild( ModulaElementTypes.STATEMENT_LIST
                             , ModulaElementTypes.STATEMENT_LIST.getNodeClass() );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getFrameName() {
        return "LOOP";
    }

}
