package com.excelsior.xds.parser.internal.modula.symbol;

import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.IVariableSymbol;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;

public class VariableSymbol extends ModulaSymbol implements IVariableSymbol 
{
    private IModulaSymbolReference<ITypeSymbol> typeSymbolRef;
    
    public VariableSymbol(String name, ISymbolWithScope parentScope) {
        super(name, parentScope);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLocal() {
        return !(getParentScope() instanceof IModuleSymbol);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ITypeSymbol getTypeSymbol() {
        return typeSymbolRef.resolve();
    }

    public void setTypeSymbol(IModulaSymbolReference<ITypeSymbol> typeSymbolRef) {
        this.typeSymbolRef = typeSymbolRef;
    }

	@Override
	protected void doAccept(ModulaSymbolVisitor visitor) {
		visitor.visit(this);
	}
}