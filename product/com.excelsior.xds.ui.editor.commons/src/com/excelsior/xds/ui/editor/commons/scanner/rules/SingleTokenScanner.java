package com.excelsior.xds.ui.editor.commons.scanner.rules;

import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.IToken;

public class SingleTokenScanner extends BufferedRuleBasedScanner {

    public SingleTokenScanner(IToken token) {
        setDefaultReturnToken(token);
    }
}
