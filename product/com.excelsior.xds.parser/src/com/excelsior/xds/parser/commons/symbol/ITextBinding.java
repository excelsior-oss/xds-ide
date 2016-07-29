package com.excelsior.xds.parser.commons.symbol;

import com.excelsior.xds.core.text.ITextRegion;

/**
 * Binds element with its location in the source text.
 */
public interface ITextBinding 
{
	/**
	 * Returns the location of element's primary identifier. 
	 * 
     * @return text region with name of this symbol or <tt>null</tt> for anonymous element. 
	 */
	public abstract ITextRegion getNameTextRegion();

    /**
     * The source text interval, where element is declared.
     */
    public abstract ITextRegion getDeclarationTextRegion();

}