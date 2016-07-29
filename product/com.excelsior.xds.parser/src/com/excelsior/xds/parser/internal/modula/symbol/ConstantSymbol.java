package com.excelsior.xds.parser.internal.modula.symbol;

import com.excelsior.xds.parser.modula.symbol.IConstantSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.reference.ReferenceUtils;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;

public class ConstantSymbol<T extends ITypeSymbol> extends    ModulaSymbol
                                                   implements IConstantSymbol
{
    private IModulaSymbolReference<T> typeSymbolRef;

    public ConstantSymbol( String name, ISymbolWithScope parentScope
                         , IModulaSymbolReference<T> typeSymbolRef ) 
    {
        super(name, parentScope);
        this.typeSymbolRef = typeSymbolRef;
    }

    public ConstantSymbol(String name, ISymbolWithScope parentScope) {
        this(name, parentScope, null);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public T getTypeSymbol() {
        return ReferenceUtils.resolve(typeSymbolRef);
    }
    
    public void setTypeSymbol(IModulaSymbolReference<T> typeSymbolRef) {
        this.typeSymbolRef = typeSymbolRef;
    }

	@Override
	protected void doAccept(ModulaSymbolVisitor visitor) {
		visitor.visit(this);
	}
}
