package com.excelsior.xds.parser.commons.symbol;

import java.util.Collection;

import com.excelsior.xds.core.text.ITextRegion;

/**
 * Binds together symbol with repeated name and corresponding code where it is defined.
 */
public interface IBlockSymbolTextBinding extends ISymbolTextBinding
{
	/**
	 * Returns regions of the symbol name occurrences among text region defined
	 * by {@link #getDeclarationTextRegion() } 
	 */
	public abstract Collection<ITextRegion> getNameTextRegions();
	
}
