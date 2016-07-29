package com.excelsior.xds.parser.internal.modula.symbol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.excelsior.xds.core.text.TextPosition;
import com.excelsior.xds.parser.internal.modula.symbol.reference.ReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbolUsages;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.reference.ReferenceUtils;

public class ModulaSymbolUsages implements IModulaSymbolUsages
{
    private final Map<IModulaSymbolReference<IModulaSymbol>, List<TextPosition>> symbolUsageTable;
    
    public ModulaSymbolUsages() {
        symbolUsageTable = new HashMap<IModulaSymbolReference<IModulaSymbol>, List<TextPosition>>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IModulaSymbol> getUsedSymbols() {
        return ReferenceUtils.transformToSymbols(symbolUsageTable.keySet());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<TextPosition> getSymbolUsages(IModulaSymbol symbol) {
        return internalGetSymbolUsages(symbol);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addSymbolUsage(IModulaSymbol symbol, TextPosition usagePosition) {
        List<TextPosition> sumbolUsages = internalGetSymbolUsages(symbol);
        if (sumbolUsages == null) {
            sumbolUsages = new ArrayList<TextPosition>();
            symbolUsageTable.put(ReferenceFactory.createRef(symbol), sumbolUsages);
        }
        
        int idx = Collections.binarySearch(sumbolUsages, usagePosition, new Comparator<TextPosition>() {
			@Override
			public int compare(TextPosition o1, TextPosition o2) {
				return o1.getOffset() - o2.getOffset();
			}
		});
        int insertionPoint = -(1 + idx); // maintain sorted order
        sumbolUsages.add(insertionPoint, usagePosition);
    }

    private List<TextPosition> internalGetSymbolUsages(IModulaSymbol symbol) {
        return symbolUsageTable.get(ReferenceFactory.createRef(symbol));
    }

    /**
     * The empty symbol usages (immutable).
     */
    public static final IModulaSymbolUsages EMPTY_USAGES = new EmptySymbolUsages();
    
    private static class EmptySymbolUsages implements IModulaSymbolUsages
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public Collection<IModulaSymbol> getUsedSymbols() {
            return Collections.emptyList();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public Collection<TextPosition> getSymbolUsages(IModulaSymbol symbol) {
            return Collections.emptyList();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void addSymbolUsage(IModulaSymbol symbol, TextPosition usagePosition) {
            throw new UnsupportedOperationException("Immutable empty symbol usages information");      //$NON-NLS-1$
        }
    }
    
}
