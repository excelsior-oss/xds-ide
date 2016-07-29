package com.excelsior.xds.parser.commons.symbol;

import com.excelsior.xds.core.text.ITextRegion;

public interface IMutableTextBinding extends ITextBinding
{
    /**
     * Sets text region in which symbol's primary identifier is located. 
     * 
     * @param region the text region in which symbol's primary identifier is located.
     */
    public void setNameTextRegion(ITextRegion region);

    /**
     * Sets text region in which symbol is defined. 
     * 
     * @param region the text region in which symbol is defined.
     */
    public void setDeclarationTextRegion(ITextRegion region);
    
}
