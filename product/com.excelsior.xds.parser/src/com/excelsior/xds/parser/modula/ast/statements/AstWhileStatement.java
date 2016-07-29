package com.excelsior.xds.parser.modula.ast.statements;

import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;

public class AstWhileStatement extends AstStatement
{
    public AstWhileStatement(ModulaCompositeType<AstWhileStatement> elementType) {
        super(null, elementType);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getFrameName() {
        return "WHILE";
    }
}