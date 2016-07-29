package com.excelsior.xds.ui.editor.commons.scanner.jflex;

import com.excelsior.xds.ui.commons.syntaxcolor.TokenDescriptor;

/**
 * A JFlex token scanner.
 *   
 * @author lion
 */
public interface IFlexScanner {

    static final int INITIAL_STATE = 0;
    
    /**
     * Enters a new lexical state
     *
     * @param newState the new lexical state
     */
    void yybegin(int state);

    /**
     * Returns the current lexical state.
     */
    int yystate();

    /**
     * Resumes scanning until the next regular expression is matched,
     * the end of input is encountered or an I/O-Error occurs.
     *
     * @return the next token
     * @exception   java.io.IOException  if any I/O-Error occurs
     */
    TokenDescriptor nextToken() throws java.io.IOException;
    
    /**
     * Returns the offset of the matched text region.
     *
     * @return the offset of the matched text region
     */
    int getTokenOffset();

    
    /**
     * Returns the length of the matched text region.
     *
     * @return the length of the matched text region
     */
    int yylength();

    /**
     * Configures the scanner by providing access to the document range that should
     * be scanned.
     *
     * @param buffer  the readable sequence of <code>char</code> values to scan
     * @param start  the offset of the document range to scan
     * @param end  the offset the last character in the buffer, that has been read
     * @param initialState  initial lexical state
     */
    void reset(CharSequence buffer, int start, int end, int initialState);
    
}
