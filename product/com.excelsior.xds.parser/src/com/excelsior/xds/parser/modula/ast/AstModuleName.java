package com.excelsior.xds.parser.modula.ast;

import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenTypes;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;

public class AstModuleName extends AstName<IModuleSymbol> 
{
    public AstModuleName(ModulaCompositeType<AstModuleName> elementType) {
        super(null, elementType);
    }
    
    public PstNode getIdentifier() {
        return findFirstChild(ModulaTokenTypes.IDENTIFIER, PstNode.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
    	visitor.visit(this);
    }

}
