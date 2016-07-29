package com.excelsior.xds.parser.modula.ast.statements;

import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;

public class AstAsmStatement extends AstStatement
{
    public AstAsmStatement(ModulaCompositeType<AstAsmStatement> elementType) {
        super(null, elementType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFrameName() {
        return "ASM";
    }

}
