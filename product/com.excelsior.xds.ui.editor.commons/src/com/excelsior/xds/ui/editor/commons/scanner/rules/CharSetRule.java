package com.excelsior.xds.ui.editor.commons.scanner.rules;

import java.util.TreeSet;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * A rule for detecting words which contain only from restricted number of characters.
 */
public class CharSetRule implements IRule {

    /** The token to be returned on success */
    protected final IToken successToken;
    
    private final TreeSet<Character> charSet;
    

    /**
     * Creates a new char set rule.
     * 
     * @param successToken Token to use for this rule
     * @param chars characters to detect
     */
    public CharSetRule (IToken successToken, char... chars) {
        this.successToken = successToken;
        charSet = new TreeSet<Character>();
        for (char ch : chars) {
            charSet.add(ch);
        }
    }
    
    /*
     * @see org.eclipse.jface.text.rules.IRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner)
     */
    public IToken evaluate(ICharacterScanner scanner) {
        int c = scanner.read();
        if (charSet.contains((char)c)) {
            do {
                c = scanner.read();
            } while ((c != ICharacterScanner.EOF) && charSet.contains((char)c));
            scanner.unread();
            return successToken;
        }

        scanner.unread();
        return Token.UNDEFINED;
    }
    
}
