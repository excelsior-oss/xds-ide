package com.excelsior.xds.parser.modula.ast.statements;

import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;

/**
 * CaseStatement = "CASE" CaseSelector "OF" CaseList "END" <br>
 */
public class AstCaseStatement extends AstStatement
{
    public AstCaseStatement(ModulaCompositeType<AstCaseStatement> elementType) {
        super(null, elementType);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getFrameName() {
        return "CASE";
    }

}
