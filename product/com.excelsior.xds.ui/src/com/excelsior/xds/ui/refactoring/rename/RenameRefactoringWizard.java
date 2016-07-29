package com.excelsior.xds.ui.refactoring.rename;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import com.excelsior.xds.core.refactoring.rename.RenameRefactoring;
import com.excelsior.xds.core.refactoring.rename.RenameRefactoringInfo;

public class RenameRefactoringWizard extends RefactoringWizard {
	private RenameRefactoringInfo refactoringInfo;

	public RenameRefactoringWizard(RenameRefactoring refactoring, RenameRefactoringInfo refactoringInfo) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE);
		this.refactoringInfo = refactoringInfo;
	}

	@Override
	protected void addUserInputPages() {
		 setDefaultPageTitle( getRefactoring().getName() );
		 addPage( new RenameRefactoringPage(refactoringInfo) );
	}
}
