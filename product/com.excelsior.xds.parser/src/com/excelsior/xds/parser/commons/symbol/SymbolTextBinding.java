package com.excelsior.xds.parser.commons.symbol;

import com.excelsior.xds.core.text.TextPosition;

public class SymbolTextBinding extends    TextBinding
                               implements IMutableSymbolTextBinding
{
    /** Position in which symbol is defined.  */
    private TextPosition position;

    /**
     * {@inheritDoc}
     */
    @Override
    public TextPosition getPosition() {
        return position;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPosition(TextPosition position) {
        this.position = position;
    }
        
}
