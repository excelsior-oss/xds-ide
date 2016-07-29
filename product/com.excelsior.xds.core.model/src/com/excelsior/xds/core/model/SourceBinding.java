package com.excelsior.xds.core.model;

import java.util.Arrays;
import java.util.Collection;

import com.excelsior.xds.core.text.ITextRegion;
import com.excelsior.xds.core.text.TextRegion;
import com.excelsior.xds.parser.commons.symbol.ITextBinding;

public class SourceBinding implements ITextBinding 
{
	/**
	 * The collection of source text intervals covered by element
	 */
	private final Collection<ITextRegion> elementRegions;
	
	/**
	 * The location of element's main identifier in the source text
	 */
	private final ITextRegion nameTextRegion;
	
	/**
	 * The source text interval, where element is declared
	 */
	private final ITextRegion declarationTextRegion;
	
	public SourceBinding(ITextRegion elementRegion, ITextRegion identifierRegion) 	{
		this(Arrays.asList(elementRegion), identifierRegion, elementRegion);
	}
	
	public SourceBinding(Collection<ITextRegion> elementRegions, ITextRegion nameTextRegion, ITextRegion declarationTextRegion) 
	{
		this.elementRegions    = elementRegions;
		this.nameTextRegion = nameTextRegion;
		this.declarationTextRegion = declarationTextRegion;
	}

	public Collection<ITextRegion> getElementRegions() {
		return elementRegions;
	}
	
	/**
	 * @return Cumulative continuous region, containing all of the elementRegions
	 */
	public ITextRegion getElementRegion() {
		if (elementRegions.isEmpty()){
			return new TextRegion(0, 0);
		}
		else if (elementRegions.size() == 1) {
			return elementRegions.iterator().next();
		}
			
		int minOffset = Integer.MAX_VALUE;
		int maxOffset = 0;
		for (ITextRegion textRegion : elementRegions) {
			if (textRegion.getOffset() < minOffset) {
				minOffset = textRegion.getOffset();
			}
			
			int offset = textRegion.getOffset() + textRegion.getLength();
			if (offset > maxOffset) {
				maxOffset = offset;
			}
		}
		return new TextRegion(minOffset, maxOffset - minOffset);
	}
	
	
	/* (non-Javadoc)
	 * @see com.excelsior.xds.core.model.ISourceBinding#getDeclarationTextRegion()
	 */
	@Override
	public ITextRegion getDeclarationTextRegion() {
		return declarationTextRegion;
	}

	/* (non-Javadoc)
	 * @see com.excelsior.xds.core.model.ISourceBinding#getNameTextRegion()
	 */
	@Override
	public ITextRegion getNameTextRegion() {
		return nameTextRegion;
	}
}
