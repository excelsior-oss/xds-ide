package com.excelsior.xds.parser.internal.modula.symbol.reference;

import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.reference.IProxyReference;
import com.excelsior.xds.parser.modula.symbol.reference.ReferenceUtils;

public class ProxyReference<T extends IModulaSymbol> implements IProxyReference<T>
{
    private IModulaSymbolReference<T> reference;
    
    public ProxyReference(IModulaSymbolReference<T> reference) {
        this.reference = reference;
    }

    @Override
    public T resolve() {
        return ReferenceUtils.resolve(reference);
    }

    @Override
    public void setReference(IModulaSymbolReference<T> reference) {
        this.reference = reference;
    }

    @Override
    public IModulaSymbolReference<T> getReference() {
        return reference;
    }

	@Override
	public String toString() {
		return String.format("ProxyReference [reference=%s]", reference);
	}
}
