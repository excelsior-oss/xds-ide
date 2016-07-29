package com.excelsior.xds.ui.editor.modula.scanner.rules;

import java.util.TreeSet;

import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;

import com.excelsior.xds.ui.editor.commons.scanner.rules.SuffixedWordRule;

/**
* A rule for detecting Modula-2 octal numbers.
*/
public class OctNumberRule extends SuffixedWordRule {

    /**
     * Word detector for octal number words.
     */
    private static class OctNumberDetector implements IWordDetector {
        
        private final TreeSet<Character> hexChars;

        public OctNumberDetector() {
            hexChars = new TreeSet<Character>();
            
            hexChars.add('0');
            hexChars.add('1');
            hexChars.add('2');
            hexChars.add('3');
            hexChars.add('4');
            hexChars.add('5');
            hexChars.add('6');
            hexChars.add('7');
        }
        
        public boolean isWordPart(char c) {
            return hexChars.contains(c);
        }

        public boolean isWordStart(char c) {
            return hexChars.contains(c);
        }
    }

    /**
     * Default constructor with the token this detector should return in
     * the case of success parsing.
     * @param token the token for detector.
     */
    public OctNumberRule(IToken token) {
        super(new OctNumberDetector(), 'C', token);
    }
    
}
