package com.excelsior.xds.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.handlers.HandlerUtil;

import com.excelsior.xds.ui.commons.utils.WizardUtils;
import com.excelsior.xds.ui.project.wizard.NewProjectFromSourcesWizard;

public class NewModulaProjectFromSourcesCommandHandler extends AbstractHandler
        implements IHandler {
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        WizardUtils.openWizard(NewProjectFromSourcesWizard.ID, HandlerUtil.getActiveShell(event));
        return null;
    }
}
