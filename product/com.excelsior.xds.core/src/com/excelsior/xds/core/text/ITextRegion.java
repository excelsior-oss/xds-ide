package com.excelsior.xds.core.text;

/**
 * A region in an indexed text store. A region is defined by its offset into 
 * the text store and its length.
 * <p>
 * A region is considered a value object. Its offset and length do not change
 * over time.
 * <p>
 * Clients may implement this interface or use the standard implementation
 * {@link com.excelsior.xds.core.text.TextRegion}.
 * </p>
 */
public interface ITextRegion
{
    /**
     * Returns the length of the region.
     *
     * @return the length of the region
     */
    int getLength();

    /**
     * Returns the offset of the region.
     *
     * @return the offset of the region
     */
    int getOffset();

    /**
     * Returns <tt>true</tt> if this text region contains the specified position.
     * 
     * @param position a position to be verified.
     */
    public boolean contains(int position);
    
}
