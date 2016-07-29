package com.excelsior.xds.parser.modula.symbol;

import com.excelsior.xds.parser.modula.symbol.type.IRecordTypeSymbol;

public interface IOberonMethodReceiverSymbol extends IFormalParameterSymbol 
{
    public IRecordTypeSymbol getBoundTypeSymbol();

}
