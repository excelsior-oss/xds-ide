package com.excelsior.xds.parser.modula.ast.statements;

import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;

public class AstIfStatement extends AstStatement
{
    public AstIfStatement(ModulaCompositeType<AstIfStatement> elementType) {
        super(null, elementType);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getFrameName() {
        return "IF";
    }
}
