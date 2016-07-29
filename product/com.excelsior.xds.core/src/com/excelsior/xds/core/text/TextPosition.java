package com.excelsior.xds.core.text;

/**
 * A text position is considered a value object. Its offset and length do not  
 * change over time.
 */
public class TextPosition 
{
    /** source char line number */
    private final int line; 
    
    /** source char column */
    private final int column;

    /** source char offset in the source text */
    private final int offset;
    
    public TextPosition (int line, int column, int offset) {
        this.line   = line;
        this.column = column;
        this.offset = offset;
    }
    
    /** @return source char line number  */
    public int getLine() {
    	return line;
    }
    
    /** @return source char column */
    public int getColumn() {
    	return column;
    }

    /** @return source char offset in the source text */
    public int getOffset() {
    	return offset;
    }
    
	@Override
	public String toString() {
        return "TokenPosition [line=" + line + ", column=" + column
             + ", offset=" + offset + "]";
	}
	
}
