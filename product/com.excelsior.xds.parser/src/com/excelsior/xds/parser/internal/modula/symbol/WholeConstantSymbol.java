package com.excelsior.xds.parser.internal.modula.symbol;

import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.IWholeConstantSymbol;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;

public class WholeConstantSymbol<T extends ITypeSymbol> extends    ConstantSymbol<T> 
                                                        implements IWholeConstantSymbol 
{
    private final long value; 
    
    public WholeConstantSymbol( String name, ISymbolWithScope parentScope
                              , IModulaSymbolReference<T> typeSymbolRef, long value) 
    {
        super(name, parentScope, typeSymbolRef);
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getValue() {
        return value;
    }
    
}
