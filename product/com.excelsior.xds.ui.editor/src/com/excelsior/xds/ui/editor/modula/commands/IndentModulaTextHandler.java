package com.excelsior.xds.ui.editor.modula.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.excelsior.xds.ui.editor.modula.format.ModulaTextFormatter;

public class IndentModulaTextHandler extends AbstractHandler 
{
    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        (new ModulaTextFormatter()).doIndent();
        return null;
    }

}
