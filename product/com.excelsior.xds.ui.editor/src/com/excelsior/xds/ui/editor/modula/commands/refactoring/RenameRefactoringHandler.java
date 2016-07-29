package com.excelsior.xds.ui.editor.modula.commands.refactoring;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.excelsior.xds.core.refactoring.rename.RenameRefactoring;
import com.excelsior.xds.core.refactoring.rename.RenameRefactoringInfo;
import com.excelsior.xds.core.refactoring.rename.RenameRefactoringProcessor;
import com.excelsior.xds.parser.commons.pst.PstLeafNode;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.ui.commons.utils.SelectionUtils;
import com.excelsior.xds.ui.commons.utils.WordAndRegion;
import com.excelsior.xds.ui.commons.utils.WorkbenchUtils;
import com.excelsior.xds.ui.editor.commons.SourceCodeTextEditor;
import com.excelsior.xds.ui.editor.commons.debug.DebugCommons;
import com.excelsior.xds.ui.editor.modula.utils.ModulaEditorSymbolUtils;
import com.excelsior.xds.ui.internal.nls.Messages;
import com.excelsior.xds.ui.refactoring.rename.RenameRefactoringWizard;

public class RenameRefactoringHandler extends AbstractHandler implements
		IHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		RenameRefactoringInfo refactoringInfo = getRenameRefactoringInfo();
		if (refactoringInfo == null) {
			MessageDialog.openError(HandlerUtil.getActiveShell(event), Messages.RenameCompilationUnitHandler_InvalidSelection, Messages.RenameCompilationUnitHandler_CannotPerformRefactoringWithCurrentSelection);
			return null;
		}
		
		if (DebugCommons.isProjectInDebug(refactoringInfo.getProject())) {
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
	
	private RenameRefactoringInfo getRenameRefactoringInfo() {
		ITextSelection textSelection = WorkbenchUtils.getActiveTextSelection();
		if (textSelection == null) {
			return null;
		}
		
		IEditorPart editor = WorkbenchUtils.getActiveEditor(false);
		if (editor == null) {
			return null;
		}
		ISourceViewer textViewer = (ISourceViewer)editor.getAdapter(ISourceViewer.class);
		SourceCodeTextEditor textEditor = null;
		if (editor instanceof SourceCodeTextEditor) {
			textEditor = (SourceCodeTextEditor) editor;
		}
		else{
			return null;
		}
		
		PstLeafNode pstLeafNode = ModulaEditorSymbolUtils.getIdentifierPstLeafNode(textEditor, textSelection.getOffset());
		IModulaSymbol symbol = ModulaEditorSymbolUtils.getModulaSymbol(
				textViewer.getDocument(), pstLeafNode);
		WordAndRegion wordUnderCursor = SelectionUtils.getWordUnderCursor(false);
		
		if (symbol == null || wordUnderCursor == null) {
			return null;
		}
		
		IFile ifileEdited = WorkbenchUtils.getIFileFrom(editor.getEditorInput());
		if (ifileEdited == null) {
			return null;
		}
		
	    return new RenameRefactoringInfo(ifileEdited, wordUnderCursor.word, symbol);
	}
}
