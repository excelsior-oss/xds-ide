package com.excelsior.xds.ui.editor.modula.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;

import com.excelsior.xds.ui.commons.utils.WorkbenchUtils;
import com.excelsior.xds.ui.editor.modula.outline.ModulaQuickOutlineDialog;

/**
 * A command handler to open outline pop-up dialog in the active editor.
 */
public class QuickOutlineHandler extends AbstractHandler implements IHandler 
{
    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ModulaQuickOutlineDialog dlg = new ModulaQuickOutlineDialog(WorkbenchUtils.getActivePartShell());
        dlg.open();
        return null;
    }

}