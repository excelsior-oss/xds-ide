package com.excelsior.xds.parser.internal.modula.symbol.reference;

import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;

public interface IReferenceResolver {
    IModulaSymbol resolve(ReferenceLocation location);
    
    ReferenceLocation createReferenceLocation(IModulaSymbol symbol);
    void addSymbol(IModulaSymbol symbol);
}
