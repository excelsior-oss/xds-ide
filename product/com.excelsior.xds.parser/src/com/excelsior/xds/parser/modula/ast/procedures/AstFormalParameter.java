package com.excelsior.xds.parser.modula.ast.procedures;

import com.excelsior.xds.parser.commons.ast.IAstNodeWithIdentifier;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.AstSymbolDef;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenTypes;
import com.excelsior.xds.parser.modula.symbol.IFormalParameterSymbol;

public class AstFormalParameter extends    AstSymbolDef<IFormalParameterSymbol>  
                                implements IAstNodeWithIdentifier
{

    public AstFormalParameter(ModulaCompositeType<AstFormalParameter> elementType) {
        super(null, elementType);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
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
