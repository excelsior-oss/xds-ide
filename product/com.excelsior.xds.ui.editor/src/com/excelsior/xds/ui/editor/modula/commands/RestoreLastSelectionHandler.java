package com.excelsior.xds.ui.editor.modula.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class RestoreLastSelectionHandler extends AbstractHandler
{
    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        SelectEnclosingElementHandler.restoreSelection();
        return null;
    }
   
}
