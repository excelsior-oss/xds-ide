package com.excelsior.xds.parser.modula.ast.statements;

import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;

public class AstRepeatStatement extends AstStatement
{
    public AstRepeatStatement(ModulaCompositeType<AstRepeatStatement> elementType) {
        super(null, elementType);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getFrameName() {
        return "REPEAT";
    }
}