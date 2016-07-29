package com.excelsior.xds.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.wizards.newresource.BasicNewFolderResourceWizard;

import com.excelsior.xds.ui.commons.utils.WizardUtils;

public class NewFolderCommandHandler extends AbstractHandler implements
        IHandler {
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        WizardUtils.openWizard(BasicNewFolderResourceWizard.WIZARD_ID, HandlerUtil.getActiveShell(event), HandlerUtil.getCurrentSelection(event));
        return null;
    }
}
