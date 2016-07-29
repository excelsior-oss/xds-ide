package com.excelsior.xds.parser.modula.ast.statements;

import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;

public class AstForStatement extends AstStatement
{
    public AstForStatement(ModulaCompositeType<AstForStatement> elementType) {
        super(null, elementType);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getFrameName() {
        return "FOR";
    }
}
