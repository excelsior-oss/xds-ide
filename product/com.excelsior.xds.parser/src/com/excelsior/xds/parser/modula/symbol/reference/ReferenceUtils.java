package com.excelsior.xds.parser.modula.symbol.reference;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.excelsior.xds.core.utils.Lambdas;
import com.excelsior.xds.parser.internal.modula.symbol.reference.IStaticModulaSymbolReference;
import com.excelsior.xds.parser.internal.modula.symbol.reference.ReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;

/**
 * Helpers to work with the symbol references
 * @author lsa80
 */
public class ReferenceUtils 
{
    public static <T extends IModulaSymbol> IModulaSymbolReference<T> createRef(T symbol) {
        return ReferenceFactory.createRef(symbol);
    }
    
    public static <T extends IModulaSymbol> IChainedReference<T> createChainedRef(List<IModulaSymbolReference<T>> refs) {
        return ReferenceFactory.createChainedRef(refs);
    }
    
    public static <T extends IModulaSymbol> IStaticModulaSymbolReference<T> createStaticRef(final T symbol) {
        return ReferenceFactory.createStaticRef(symbol);
    }
    
    public static <T extends IModulaSymbol> T resolve(IModulaSymbolReference<T> ref) {
        return ref != null ? ref.resolve() : null;
    }
    
    public static <T extends IModulaSymbol> Collection<T> resolve(Collection<IModulaSymbolReference<T>> refs) {
    	return refs.stream().map(ReferenceUtils::resolve).collect(Collectors.toList());
    }

    public static Collection<IModulaSymbol> transformToSymbols(Collection<IModulaSymbolReference<IModulaSymbol>> refs) 
    {
    	return refs.stream().map(ReferenceUtils::resolve).filter(Lambdas.nonnull()).collect(Collectors.toList());
    }
}
