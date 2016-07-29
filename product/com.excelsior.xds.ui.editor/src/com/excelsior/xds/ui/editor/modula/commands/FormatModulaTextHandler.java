package com.excelsior.xds.ui.editor.modula.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;

import com.excelsior.xds.ui.editor.modula.format.ModulaTextFormatter;

/**
 * A command handler to format a Modula-2 source code in the active editor.
 */
public class FormatModulaTextHandler extends AbstractHandler implements IHandler 
{
    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        (new ModulaTextFormatter()).doFormat();
        return null;
    }

}