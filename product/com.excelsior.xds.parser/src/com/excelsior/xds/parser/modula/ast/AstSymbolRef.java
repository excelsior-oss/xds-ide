package com.excelsior.xds.parser.modula.ast;

import com.excelsior.xds.parser.commons.ast.ElementType;
import com.excelsior.xds.parser.commons.pst.PstCompositeNode;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.reference.ReferenceUtils;

/**
 * AST node with reference to a Modula-2/Oberon-2 symbol.
 * 
 * @param <T> the type of referenced symbol 
 */
public abstract class AstSymbolRef<T extends IModulaSymbol> extends    ModulaAstNode 
                                                            implements IAstSymbolRef
{
    private IModulaSymbolReference<T> symbolRef;

    public AstSymbolRef(PstCompositeNode parent, ElementType elementType) {
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
        return ReferenceUtils.resolve(symbolRef);
    }
    public void setSymbol(IModulaSymbolReference<T> symbolRef) {
        this.symbolRef = symbolRef;
    }
    
}
