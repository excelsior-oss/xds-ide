package com.excelsior.xds.parser.internal.modula.symbol.reference;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.reference.IChainedReference;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.reference.ReferenceUtils;

public class ChainedReference<T extends IModulaSymbol> implements IModulaSymbolReference<T>, IChainedReference<T> {
    private final Collection<IModulaSymbolReference<T>> referenceChain;
    
    public ChainedReference(Collection<IModulaSymbolReference<T>> referenceChain) {
        this.referenceChain = new LinkedList<IModulaSymbolReference<T>>(referenceChain);
    }

    @Override
    public T resolve() {
        T symbol = null;
        for (IModulaSymbolReference<T> ref : referenceChain) {
            symbol = ReferenceUtils.resolve(ref);
            if (symbol != null) {
                break;
            }
        }
        return symbol;
    }

    @Override
    public Iterator<IModulaSymbolReference<T>> iterator() {
        return referenceChain.iterator();
    }
}
