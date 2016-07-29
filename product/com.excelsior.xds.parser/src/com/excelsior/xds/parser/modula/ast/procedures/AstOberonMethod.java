package com.excelsior.xds.parser.modula.ast.procedures;

import com.excelsior.xds.parser.commons.ast.ElementType;
import com.excelsior.xds.parser.commons.pst.PstCompositeNode;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;
import com.excelsior.xds.parser.modula.symbol.IOberonMethodSymbol;

public abstract class AstOberonMethod<T extends IOberonMethodSymbol>
                extends AstProcedure<T> 
{
    protected AstOberonMethod(PstCompositeNode parent, ElementType elementType) {
        super(parent, elementType);
    }

    public AstOberonMethodReceiver getOberonMethodReceiver() {
        return findFirstChild( ModulaElementTypes.OBERON_METHOD_RECEIVER
                             , ModulaElementTypes.OBERON_METHOD_RECEIVER.getNodeClass() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void acceptChildren(ModulaAstVisitor visitor) {
        acceptChild(visitor, getOberonMethodReceiver());
        super.acceptChildren(visitor);
    }
    
}
