package com.excelsior.xds.core.text;

/**
 * The default implementation of the {@link com.excelsior.xds.core.text.ITextRegion} 
 * interface.
 */
public class TextRegion implements ITextRegion
{
    /** The text region offset */
	private int offset;
    /** The text region length */
	private int length;
     
    /**
     * Create a new text region.
     *
     * @param offset the offset of the text region
     * @param length the length of the text region
     */
    public TextRegion(int offset, int length) {
        this.offset = offset;
        this.length = length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getOffset() {
        return offset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLength() {
        return length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(int position) {
        return (offset <= position) && (position < offset + getLength());
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof ITextRegion) {
            ITextRegion r= (ITextRegion) o;
            return r.getOffset() == offset && r.getLength() == length;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return (offset << 24) | (length << 16);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "offset: " + offset + ", length: " + length; //$NON-NLS-1$ //$NON-NLS-2$;
    }
    
}
