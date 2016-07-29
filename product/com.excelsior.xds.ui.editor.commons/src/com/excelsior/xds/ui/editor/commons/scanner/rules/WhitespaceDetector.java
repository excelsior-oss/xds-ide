package com.excelsior.xds.ui.editor.commons.scanner.rules;

import org.eclipse.jface.text.rules.IWhitespaceDetector;

public class WhitespaceDetector implements IWhitespaceDetector {

    /**
     * Returns whether the specified character is whitespace.
     *
     * @param c the character to be checked
     * @return <code>true</code> if the specified character is a whitespace char
     */
    public boolean isWhitespace(char c) {
        return Character.isWhitespace(c);
    }
    
}
