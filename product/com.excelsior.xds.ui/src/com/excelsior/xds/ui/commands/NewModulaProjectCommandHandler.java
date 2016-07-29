package com.excelsior.xds.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.excelsior.xds.ui.commons.utils.WizardUtils;
import com.excelsior.xds.ui.project.wizard.NewProjectFromScratchWizard;

public class NewModulaProjectCommandHandler extends AbstractHandler {
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        WizardUtils.openWizard(NewProjectFromScratchWizard.ID, HandlerUtil.getActiveShell(event));
        return null;
    }
}
