package com.excelsior.xds.ui.editor.commons.scanner.rules;

import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;

public class CommentScanner extends BufferedRuleBasedScanner
{
    public CommentScanner(IToken defaultToken, IToken taskToken) {
        setDefaultReturnToken(defaultToken);
        setRules(new IRule[]{new TodoTaskTagRule(new TodoTaskTagDetector(), defaultToken, taskToken)});
    }

}
