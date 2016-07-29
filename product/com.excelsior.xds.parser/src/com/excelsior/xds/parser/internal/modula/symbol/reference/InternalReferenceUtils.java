package com.excelsior.xds.parser.internal.modula.symbol.reference;

import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;

public abstract class InternalReferenceUtils 
{
    public static void addSymbolForResolving( IModuleSymbol moduleSymbol
                                            , IModulaSymbol symbol ) 
    {
        IReferenceResolver referenceResolver = getReferenceResolver(moduleSymbol);
        if (referenceResolver != null) {
            referenceResolver.addSymbol(symbol);
        }
    }
    
    public static IReferenceResolver getReferenceResolver(IModuleSymbol moduleSymbol) {
        if (moduleSymbol instanceof IReferenceResolverProvider) {
            IReferenceResolverProvider referenceResolverProvider = (IReferenceResolverProvider) moduleSymbol;
            IReferenceResolver referenceResolver = referenceResolverProvider.getReferenceResolver();
            return referenceResolver;
        }
        return null;
    }
    
}
