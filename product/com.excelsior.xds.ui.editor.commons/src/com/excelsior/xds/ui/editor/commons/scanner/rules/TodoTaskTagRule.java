package com.excelsior.xds.ui.editor.commons.scanner.rules;

import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.WordRule;

import com.excelsior.xds.core.todotask.TodoTask;
import com.excelsior.xds.core.todotask.TodoTaskManager;

/**
 * A rule for detecting "to-do" task tags.
 */
public class TodoTaskTagRule extends WordRule
{
    /**
     * Creates a rule which, with the help of a task tag detector, will return the token
     * associated with the detected task tag. If no token has been associated, the
     * specified default token will be returned.
     *
     * @param detector the task tag detector to be used by this rule, 
     *        may not be <code>null</code>
     * @param defaultToken the default token to be returned on success
     *        if nothing else is specified, may not be <code>null</code>
     * @param taskToken the token to be returned if the tag task has been found, 
     *        may not be <code>null</code>
     */
    public TodoTaskTagRule(IWordDetector detector, IToken defaultToken, IToken taskToken) 
    {
        super(detector, defaultToken, !TodoTaskManager.getInstance().isCaseSensitive());
        for (TodoTask task : TodoTaskManager.getInstance().getAllTasks()) {
            addWord(task.tag, taskToken);
        }
    }

}
