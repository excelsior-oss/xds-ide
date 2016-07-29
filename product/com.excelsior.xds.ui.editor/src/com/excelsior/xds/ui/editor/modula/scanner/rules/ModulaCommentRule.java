package com.excelsior.xds.ui.editor.modula.scanner.rules;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
* A rule for detecting nested and multiline comment of Modula-2 language.
*/
public class ModulaCommentRule implements IPredicateRule {

    /** The token to be returned on success */
    protected final IToken successToken;
    
    public ModulaCommentRule (IToken token) {
        successToken = token;
    }
    
    /*
     * @see IPredicateRule#getSuccessToken()
     */
    public IToken getSuccessToken() {
        return successToken;
    }
    
    /*
     * @see IRule#evaluate(ICharacterScanner)
     */
    @Override
    public IToken evaluate(ICharacterScanner scanner) {
        return evaluate(scanner, false);
    }
    
    
    /*
     * @see IPredicateRule#evaluate(ICharacterScanner, boolean)
     */
    @Override
    public IToken evaluate(ICharacterScanner scanner, boolean resume) {
        return doEvaluate(scanner, resume);
    }
    
    
    /**
     * Evaluates Modula-2 comment rules. Resumes detection, i.e. look sonly for
     * the end comment required by this rule if the <code>resume</code> flag is set.
     *
     * @param scanner the character scanner to be used
     * @param resume <code>true</code> if detection should be resumed, <code>false</code> otherwise
     * @return the token resulting from this evaluation
     */
    protected IToken doEvaluate(ICharacterScanner scanner, boolean resume) {
        if (! resume) {
            int c = scanner.read();
            if (c != '(') {
                scanner.unread();
                return Token.UNDEFINED;
            }
            c = scanner.read();
            if (c != '*') {
                scanner.unread();
                scanner.unread();
                return Token.UNDEFINED;
            }
        }
        endCommentDetected(scanner);
        return successToken;
    }
    

    /**
     * Returns when the end comment was detected or if the EOF character is read.
     *
     * @param scanner the character scanner to be used
     * @return <code>true</code> if the end sequence has been detected
     */
    protected void endCommentDetected(ICharacterScanner scanner) {
        int nestLevel = 1;
        int c = scanner.read();
        while (c != ICharacterScanner.EOF) {
            if (c == '*') {
                // Check if the comment end sequence has been found.
                c = scanner.read();
                if (c == ')') {
                    nestLevel--;
                    if (nestLevel == 0) {
                        return;
                    }
                    c = scanner.read();
                }
            } else if (c == '(') {
                // Check if the comment start sequence has been found.
                c = scanner.read();
                if (c == '*') {
                    nestLevel++;
                    c = scanner.read();
                }
            } else {
                c = scanner.read();
            }
        }
    }
    
}
