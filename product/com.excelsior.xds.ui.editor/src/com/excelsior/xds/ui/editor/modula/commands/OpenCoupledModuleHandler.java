package com.excelsior.xds.ui.editor.modula.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.ITextEditor;

import com.excelsior.xds.core.ide.symbol.utils.EntityUtils;
import com.excelsior.xds.core.ide.utils.CoreEditorUtils;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.model.IXdsCompilationUnit;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.XdsModelManager;
import com.excelsior.xds.parser.modula.symbol.IDefinitionModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IImplemantationModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.parser.modula.utils.ModulaSymbolUtils;
import com.excelsior.xds.ui.commons.utils.WorkbenchUtils;
import com.excelsior.xds.ui.editor.modula.ModulaEditor;

/**
 * A command handler to open coupled definition module or implementation module  
 * in the active editor.
 */
public class OpenCoupledModuleHandler extends AbstractHandler implements IHandler
{
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        final ITextEditor editor = (ITextEditor) WorkbenchUtils.getActiveEditor(false);
        if (editor instanceof ModulaEditor) {
        	Job getTargetModuleSymbolJob = new Job("Target module symbol job") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					final IModuleSymbol tagretModuleSymbol = getTargetModuleSymbol(editor);
		            if (tagretModuleSymbol != null) {
		            	Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								IProject project = CoreEditorUtils.getIProjectFrom(editor.getEditorInput());
								openModuleInEditor(project, tagretModuleSymbol);
							}
						});
		            }
		            return Status.OK_STATUS;
				}
			};
			getTargetModuleSymbolJob.schedule();
        }
        return null;
    }

    private static IModuleSymbol getTargetModuleSymbol(ITextEditor editor) {
        IModuleSymbol tagretModuleSymbol = null;
        IXdsElement xdsElement = XdsModelManager.getModel().getXdsElement(editor.getEditorInput());
        if (xdsElement instanceof IXdsCompilationUnit) {
            IModuleSymbol activeModuleSymbol = ((IXdsCompilationUnit)xdsElement).getSymbol();
            if (activeModuleSymbol instanceof IDefinitionModuleSymbol) {
                tagretModuleSymbol = EntityUtils.syncGetImplementationModuleSymbol(null, false, (IDefinitionModuleSymbol)activeModuleSymbol);
            }
            else if (activeModuleSymbol instanceof IImplemantationModuleSymbol) {
                tagretModuleSymbol = EntityUtils.syncGetDefinitionModuleSymbol(null, false, (IImplemantationModuleSymbol)activeModuleSymbol);
            }
        }
        return tagretModuleSymbol;
    }
    
    private static void openModuleInEditor (IProject project, IModuleSymbol moduleSymbol) {
        IFile moduleFile = ModulaSymbolUtils.findFirstFileForSymbol(project, moduleSymbol);
        if (moduleFile != null) {
            try {
            	CoreEditorUtils.openInEditor(moduleFile, true);
            } catch (CoreException e) {
                LogHelper.logError(e);
            }
        }
    }
    
}
