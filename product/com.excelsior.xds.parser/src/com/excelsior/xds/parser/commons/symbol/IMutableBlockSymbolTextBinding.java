package com.excelsior.xds.parser.commons.symbol;

import com.excelsior.xds.core.text.ITextRegion;

public interface IMutableBlockSymbolTextBinding extends IBlockSymbolTextBinding
                                                      , IMutableSymbolTextBinding
{
    /**
     * Appends the specified region to the collection of symbol name regions.
     */
    public void addNameTextRegion(ITextRegion region);

}
