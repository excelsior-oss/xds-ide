package com.excelsior.xds.parser.modula.scanner.jflex;

import com.excelsior.xds.core.text.TextPosition;

public class TokenRestorePosition extends TextPosition  
{
    /** the current lexical state */
    private final int state;

    public TokenRestorePosition (int line, int column, int offset, int state) {
        super(line, column, offset);
        this.state  = state;
    }
    
    /** @return lexical state of the current position */
    public int getState() {
        return state;
    }
        
}
