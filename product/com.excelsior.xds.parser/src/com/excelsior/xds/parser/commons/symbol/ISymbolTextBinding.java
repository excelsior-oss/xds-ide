package com.excelsior.xds.parser.commons.symbol;

import com.excelsior.xds.core.text.TextPosition;

/**
 * Binds symbol with its location in the source text.
 */
public interface ISymbolTextBinding extends ITextBinding 
{
	/**
     * @return position in which this symbol is defined. 
     */
    public TextPosition getPosition();
    
}
