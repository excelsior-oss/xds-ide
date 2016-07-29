package com.excelsior.xds.ui.editor.commons.scanner.rules;

import org.eclipse.jface.text.rules.IWordDetector;

/**
 * "To do" task tag detector.
 */
public class TodoTaskTagDetector implements IWordDetector
{
    public boolean isWordStart(char c) {
        return Character.isLetter(c) || c == '_' || c == '@' || c == '#';
    }

    public boolean isWordPart(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '.';
    }

}
