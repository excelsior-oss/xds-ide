package com.excelsior.xds.parser.internal.modula.symbol.reference;

import java.util.List;

import com.excelsior.xds.parser.internal.modula.symbol.UnresovedModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IStandardModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.reference.IChainedReference;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.reference.IProxyReference;
import com.excelsior.xds.parser.modula.utils.ModulaSymbolUtils;

public abstract class ReferenceFactory
{
    private static final NullModulaSymbolReference NULL_SYMBOL_REFERENCE = new NullModulaSymbolReference();
    
    public static <T extends IModulaSymbol> IModulaSymbolReference<T> createRef(final T symbol) {
        return internalCreateRefence(symbol);
    }

    @SuppressWarnings("unchecked")
    public static <T extends IModulaSymbol, U extends IModulaSymbol> IModulaSymbolReference<U> 
           createRef(final T symbol, Class<U> resultType)
    {
        return (IModulaSymbolReference<U>)internalCreateRefence(symbol);
    }
    
    /**
     * Creates reference with trivial implementation, which stores the symbol and returns it on demand.
     * This will work on with immutable symbols, like one from system module.
     * @param symbol
     * @return
     */
    public static <T extends IModulaSymbol> IStaticModulaSymbolReference<T> createStaticRef(final T symbol) {
        return new StaticModulaSymbolReference<T>(symbol);
    }
    
    public static <T extends IModulaSymbol> IProxyReference<T> createProxyRef(IModulaSymbolReference<T> innerReference) {
        return new ProxyReference<T>(innerReference);
    }
    
    public static <T extends IModulaSymbol> IChainedReference<T> createChainedRef(List<IModulaSymbolReference<T>> refs) {
        return new ChainedReference<T>(refs);
    }
    
    @SuppressWarnings("unchecked")
    public static <T extends IModulaSymbol> IModulaSymbolReference<T>  nullReference() {
        return (IModulaSymbolReference<T>)NULL_SYMBOL_REFERENCE;
    }
    
    private static <T extends IModulaSymbol> IModulaSymbolReference<T> 
            internalCreateRefence(final T symbol) 
    {
        if (symbol == null) {
            return nullReference();
        }
        else{
            IModuleSymbol definingModule = ModulaSymbolUtils.getHostModule(symbol);
            if (definingModule == null) { // this is possible for symbols having
                                          // parentScope = null. TODO : how this is possible
                return nullReference();
            }
            if (definingModule == symbol) {
                return createModuleReference((IModuleSymbol)symbol);
            }
            return new ModulaSymbolReference<T>(definingModule, symbol);
        }
    }
    
    @SuppressWarnings("unchecked")
    static <T extends IModulaSymbol> IModulaSymbolReference<T> 
           createModuleReference(IModuleSymbol definingModule) 
    {
        IModulaSymbolReference<T> moduleReference;
        boolean isStaticModeule = (definingModule instanceof IStandardModuleSymbol)
                               || (definingModule instanceof UnresovedModuleSymbol);
        if (isStaticModeule) {
            moduleReference = ReferenceFactory.createStaticRef((T)definingModule);
        }
        else{
            moduleReference = (IModulaSymbolReference<T>)new ModuleReference(definingModule);
        }
        return moduleReference;
    }
    
}   
