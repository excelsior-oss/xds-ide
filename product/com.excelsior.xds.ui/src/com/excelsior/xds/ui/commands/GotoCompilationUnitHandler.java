package com.excelsior.xds.ui.commands;

import java.util.HashSet;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;

import com.excelsior.xds.core.ide.utils.CoreEditorUtils;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.utils.XdsFileUtils;
import com.excelsior.xds.ui.commons.utils.WorkbenchUtils;
import com.excelsior.xds.ui.dialogs.SelectModulaSourceFileDialog;
import com.excelsior.xds.ui.internal.nls.Messages;

public class GotoCompilationUnitHandler extends AbstractHandler implements
        IHandler {
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = WorkbenchUtils.getActivePartShell();
        
        HashSet<String> exts = new HashSet<String>(); 
        exts.addAll(XdsFileUtils.COMPILATION_UNIT_FILE_EXTENSIONS);
        
        SelectModulaSourceFileDialog dlg = new SelectModulaSourceFileDialog(Messages.GotoCompilationUnitHandler_OpenModule, shell, ResourceUtils.getWorkspaceRoot(), exts);
        if (SelectModulaSourceFileDialog.OK == dlg.open()){
            IFile file = (IFile)dlg.getResultAsResource();
            if (file != null) {
                IEditorInput editorInput = new FileEditorInput(file);
                try {
                	CoreEditorUtils.openInEditor(editorInput, true);
                } catch (CoreException e) {
                    LogHelper.logError(e);
                }
            }
        }
        return null;
    }
}
