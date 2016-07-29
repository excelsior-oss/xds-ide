package com.excelsior.xds.core.model;

import com.excelsior.xds.parser.modula.symbol.IOberonMethodReceiverSymbol;

public interface IXdsOberonMethodReceiver extends IXdsFormalParameter
{
    @Override
    public IOberonMethodReceiverSymbol getSymbol();

}
