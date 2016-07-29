package com.excelsior.xds.ui.commands.refactoring;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.internal.ui.refactoring.actions.AbstractResourcesHandler;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.ui.handlers.HandlerUtil;

import com.excelsior.xds.core.compiler.libset.LibraryFileSetManager;
import com.excelsior.xds.core.ide.symbol.ParseTaskFactory;
import com.excelsior.xds.core.ide.symbol.SymbolModelManager;
import com.excelsior.xds.core.refactoring.rename.RenameRefactoring;
import com.excelsior.xds.core.refactoring.rename.RenameRefactoringInfo;
import com.excelsior.xds.core.refactoring.rename.RenameRefactoringProcessor;
import com.excelsior.xds.core.utils.launch.LaunchConfigurationUtils;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.ui.internal.nls.Messages;
import com.excelsior.xds.ui.refactoring.rename.RenameRefactoringWizard;

@SuppressWarnings("restriction")
public class RenameCompilationUnitHandler extends AbstractResourcesHandler implements
		IHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection sel= HandlerUtil.getCurrentSelection(event);
		IFile iFile = getIFileFrom(sel);
		RenameRefactoringInfo refactoringInfo = getRenameRefactoringInfo(iFile);
		if (refactoringInfo == null) {
			MessageDialog.openError(HandlerUtil.getActiveShell(event), Messages.RenameCompilationUnitHandler_InvalidSelection, Messages.RenameCompilationUnitHandler_CannotPerformRefactoringWithCurrentSelection);
			return null;
		}
		
		IProject iProject = iFile.getProject();
		ILaunch launch = LaunchConfigurationUtils.getLaunch(iProject);
		if (launch != null && launch.getDebugTarget() != null && !launch.getDebugTarget().isTerminated()) {
			MessageDialog.openError(HandlerUtil.getActiveShell(event), Messages.RenameCompilationUnitHandler_ProjectDebugged, Messages.RenameCompilationUnitHandler_CannotChangeFilesOfDebuggedProject);
			return null;
		}
		
		RenameRefactoringProcessor refactoringProcessor = new RenameRefactoringProcessor(refactoringInfo);
		RenameRefactoring renameRefactoring = new RenameRefactoring(refactoringProcessor);
		RenameRefactoringWizard wizard = new RenameRefactoringWizard(renameRefactoring, refactoringInfo);
		
		RefactoringWizardOpenOperation op 
	      = new RefactoringWizardOpenOperation( wizard );
	    try {
	      String titleForFailedChecks = ""; //$NON-NLS-1$
	      op.run( HandlerUtil.getActiveShell(event), titleForFailedChecks );
	    } catch( final InterruptedException irex ) {
	    }
	    
		return null;
	}
	
	/**
	 * Converts {@link ISelection} to {@link IFile} 
	 * @param s
	 * @return
	 */
	private IFile getIFileFrom(ISelection s) {
		if (s instanceof IStructuredSelection) {
			IResource resource= getCurrentResource((IStructuredSelection) s);
			if (resource != null) {
				IFile iFile = (IFile)resource;
				return iFile;
			}
		}
		return null;
	}

	private RenameRefactoringInfo getRenameRefactoringInfo(IFile iFile) {
		if (!LibraryFileSetManager.getInstance().isInLibraryFileSet(iFile) ){
			IModuleSymbol moduleSymbol = SymbolModelManager.instance().syncParseFirstSymbol(ParseTaskFactory.create(iFile));
			if (moduleSymbol != null) {
				return new RenameRefactoringInfo(iFile, moduleSymbol.getName(), moduleSymbol);
			}
		}
		return null;
	}
	
	private IResource getCurrentResource(IStructuredSelection sel) {
		IResource[] resources= getSelectedResources(sel);
		if (resources.length == 1) {
			return resources[0];
		}
		return null;
	}
}
