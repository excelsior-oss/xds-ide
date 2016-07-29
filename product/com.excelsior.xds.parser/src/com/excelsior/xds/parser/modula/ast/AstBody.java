package com.excelsior.xds.parser.modula.ast;

import com.excelsior.xds.parser.commons.ast.ElementType;
import com.excelsior.xds.parser.commons.pst.PstCompositeNode;
import com.excelsior.xds.parser.modula.symbol.IBlockBodySymbol;

/**
 * Base class for module initialization, module finalization and procedure body. 
 * 
 * @param <T> the type of referenced symbol 
 */
public abstract class AstBody<T extends IBlockBodySymbol> extends    AstStatementBlock
                                                          implements IAstSymbolRef
{
    private T symbol;

    protected AstBody(PstCompositeNode parent, ElementType elementType) {
        super(parent, elementType);
    }

    //--------------------------------------------------------------------------
    // Implementation of IAstSymbolRef interface
    //--------------------------------------------------------------------------
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        T s = getSymbol();
        return (s != null) ? s.getName() : "";   //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getSymbol() {
        return symbol;
    }

    public void setSymbol(T symbol) {
        this.symbol = symbol;
    }
    
}
