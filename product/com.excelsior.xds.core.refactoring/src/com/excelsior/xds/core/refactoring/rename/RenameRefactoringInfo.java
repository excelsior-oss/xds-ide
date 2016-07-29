package com.excelsior.xds.core.refactoring.rename;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;

public final class RenameRefactoringInfo {
	private IProject project; // project being refactored
	private String selectedIdentifier;
	private IModulaSymbol symbolFromSelection;
	private String newName;
	private IFile activeIFile;
	
	public RenameRefactoringInfo(IFile activeIFile, String selectedIdentifier,
			IModulaSymbol symbolFromSelection) {
		this.activeIFile = activeIFile;
		this.project = activeIFile.getProject();
		this.selectedIdentifier = selectedIdentifier;
		this.symbolFromSelection = symbolFromSelection;
	}
	
	public IProject getProject() {
		return project;
	}
	
	public IFile getActiveIFile() {
		return activeIFile;
	}

	public String getSelectedIdentifier() {
		return selectedIdentifier;
	}

	public IModulaSymbol getSymbolFromSelection() {
		return symbolFromSelection;
	}

	public void setSelectedIdentifier(String selectedIdentifier) {
		this.selectedIdentifier = selectedIdentifier;
	}

	public void setSymbolFromSelection(IModulaSymbol symbolFromSelection) {
		this.symbolFromSelection = symbolFromSelection;
	}

	public String getNewName() {
		return newName;
	}

	public void setNewName(String newName) {
		this.newName = newName;
	}
}
