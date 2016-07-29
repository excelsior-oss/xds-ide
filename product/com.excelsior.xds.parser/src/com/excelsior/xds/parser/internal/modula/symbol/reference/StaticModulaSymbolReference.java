package com.excelsior.xds.parser.internal.modula.symbol.reference;

import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;

final class StaticModulaSymbolReference<T extends IModulaSymbol> implements IStaticModulaSymbolReference<T>
{
    private final T symbol;

    StaticModulaSymbolReference(T symbol) {
        this.symbol = symbol;
    }

    @Override
    public T resolve() {
        return symbol;
    }

    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        @SuppressWarnings("unchecked")
        StaticModulaSymbolReference<T> other = (StaticModulaSymbolReference<T>) obj;
        if (symbol == null) {
            if (other.symbol != null)
                return false;
        } else if (!symbol.equals(other.symbol))
            return false;
        return true;
    }

	@Override
	public String toString() {
		return "StaticModulaSymbolReference [symbol=" + symbol + "]";
	}
}