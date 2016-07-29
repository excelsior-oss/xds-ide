package com.excelsior.xds.parser.internal.modula.symbol;

import com.excelsior.xds.parser.modula.symbol.IRecordVariantSelectorSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;

public class RecordVariantSelectorSymbol extends    RecordFieldSymbol 
                                         implements IRecordVariantSelectorSymbol 
{
    public RecordVariantSelectorSymbol(String name, ISymbolWithScope parentScope) {
        super(name, parentScope);
    }

}
