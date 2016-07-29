package com.excelsior.xds.parser.internal.modula.symbol.reference;

import java.util.HashMap;
import java.util.Map;

import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;

public class DefaultReferenceResolver implements IReferenceResolver
{
    private Map<ReferenceLocation, IModulaSymbol> locationToSymbol = new HashMap<ReferenceLocation, IModulaSymbol>();
    
    @Override
    public IModulaSymbol resolve(ReferenceLocation location) {
        return locationToSymbol.get(location);
    }

    @Override
    public void addSymbol(IModulaSymbol symbol) {
        if (symbol != null) {
            ReferenceLocation location = createReferenceLocation(symbol);
            IModulaSymbol previousSymbol = locationToSymbol.put(location, symbol);
            if (previousSymbol != null && previousSymbol != symbol) {
                previousSymbol = (IModulaSymbol)previousSymbol;
            }
        }
    }

    public ReferenceLocation createReferenceLocation(IModulaSymbol symbol) {
        if (symbol == null)
            return null;

        String refrenceKey = symbol.getQualifiedName();
        if (refrenceKey == null) {
        	return null;
        }
        return new ReferenceLocation(refrenceKey);
    }
    
}
