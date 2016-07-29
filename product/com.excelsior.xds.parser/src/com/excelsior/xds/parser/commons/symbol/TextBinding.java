package com.excelsior.xds.parser.commons.symbol;

import com.excelsior.xds.core.text.ITextRegion;

public class TextBinding implements ITextBinding
                                  , IMutableTextBinding
{
    /** Text region in which this symbol's primary identifier is located.  */
    private ITextRegion nameRegion;

    /** Text region in which this symbol is declared.  */
    private ITextRegion declarationRegion;

    /**
     * {@inheritDoc}
     */
    @Override
    public ITextRegion getNameTextRegion() {
        return nameRegion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNameTextRegion(ITextRegion region) {
        this.nameRegion = region;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public ITextRegion getDeclarationTextRegion() {
        return declarationRegion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDeclarationTextRegion(ITextRegion region) {
        this.declarationRegion = region;
    }
    
}
