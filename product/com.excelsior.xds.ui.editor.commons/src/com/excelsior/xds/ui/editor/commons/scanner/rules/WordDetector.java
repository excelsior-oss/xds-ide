package com.excelsior.xds.ui.editor.commons.scanner.rules;

import org.eclipse.jface.text.rules.IWordDetector;

/**
 * Word detector.
 * 
 * An identifier is a list of alphanumeric and low line ("_") characters starting
 * with a letter. The ISO Standard permits an implementation-defined set of national 
 * alphanumeric characters to be used in identifiers. XDS defines this set as empty.  
 */
public class WordDetector implements IWordDetector 
{
    public boolean isWordStart(char c) {
        return Character.isLetter(c) || c == '_';
    }

    public boolean isWordPart(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

}
