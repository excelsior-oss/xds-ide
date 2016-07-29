package com.excelsior.xds.core.model;

import com.excelsior.xds.parser.modula.symbol.IRecordVariantSelectorSymbol;

public interface IXdsRecordVariantSelector extends IXdsRecordField, IXdsContainer
{
    @Override
    public IRecordVariantSelectorSymbol getSymbol();

}
