package com.excelsior.xds.ui.editor.commons.scanner.rules;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;

/**
 * A specific single line rule which stipulates that the end character occur 
 * within a single word, as defined by a word detector.
 *
 * @see IWordDetector
 */
public class SuffixedWordRule implements IRule {

    /** The token to be returned on success */
    protected final IToken successToken;
    
    /** The word detector used by this rule. */
    protected final IWordDetector wordDetector;
    
    /** The rule's end character */
    protected final char endCharacter;
    
    /** Number of characters we had read. */
    private int readLength; 
    
    
    /**
     * Creates a rule for the given ending character which, if detected, will 
     * return the specified token. A word detector is used to identify words.
     *
     * @param detector the word detector to be used
     * @param endCharacter the end character of the word pattern
     * @param token the token to be returned on success
     */
    public SuffixedWordRule(IWordDetector detector, char endCharacter, IToken successToken) {
        wordDetector      = detector;
        this.successToken = successToken;
        this.endCharacter = endCharacter;
    }
    
    /*
     * @see IRule#evaluate(ICharacterScanner)
     */
    public IToken evaluate(ICharacterScanner scanner) {
        int c = scanner.read();
        if ((c != ICharacterScanner.EOF) && wordDetector.isWordStart((char)c)) {
            readLength = 1;
            
            do {
                c = scanner.read();
                readLength++;
            } while ((c != ICharacterScanner.EOF) && wordDetector.isWordPart((char)c));
            if (c == endCharacter) {
                return successToken;
            }
            
            while (readLength > 1) {
                readLength--;
                scanner.unread();
            }
        }
        scanner.unread();
        return Token.UNDEFINED;
    }
    
}
