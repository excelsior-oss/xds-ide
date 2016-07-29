package com.excelsior.xds.ui.editor.modula.scanner.rules;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * A rule for detecting Modula-2 real numbers.
 *
 * ScaleFactor = "E" ["+""-"]? {Digit}+
 * Real        = {Digit}+ "." {Digit}* {ScaleFactor}?
 */
public class RealNumberRule implements IRule {

    /** The token to be returned on success */
    protected final IToken successToken;
    
    /** Number of characters we had read. */
    private int readLength; 

    /**
     * Default constructor with the token this detector should return in
     * the case of success parsing.
     * @param successToken the token for detector.
     */
    public RealNumberRule (IToken successToken) {
        this.successToken = successToken;
    }
    
    /*
     * @see IRule#evaluate(ICharacterScanner)
     */
    public IToken evaluate(ICharacterScanner scanner) {
        int c = scanner.read();
        if ((c != ICharacterScanner.EOF) && Character.isDigit((char)c)) {
            readLength = 1;
            
            do {
                c = scanner.read();
                readLength++;
            } while ((c != ICharacterScanner.EOF) && Character.isDigit((char)c));
            if (c == '.') {
                do {
                    c = scanner.read();
                } while ((c != ICharacterScanner.EOF) && Character.isDigit((char)c));
                if (c == 'E') {
                    c = scanner.read();
                    if ((c == '+') || (c == '-')) {
                        c = scanner.read();
                    }
                    while ((c != ICharacterScanner.EOF) && Character.isDigit((char)c)) {
                        c = scanner.read();
                    }
                }
                scanner.unread();
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
