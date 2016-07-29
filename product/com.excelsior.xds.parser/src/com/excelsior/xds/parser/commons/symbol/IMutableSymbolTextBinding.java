package com.excelsior.xds.parser.commons.symbol;

import com.excelsior.xds.core.text.TextPosition;

public interface IMutableSymbolTextBinding extends ISymbolTextBinding
                                                 , IMutableTextBinding
{
    /**
     * Sets position in which symbol is defined. 
     * 
     * @param position - position in which symbol is defined.
     */
    public void setPosition(TextPosition position);

}
