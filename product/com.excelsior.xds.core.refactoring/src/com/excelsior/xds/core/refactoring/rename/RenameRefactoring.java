package com.excelsior.xds.core.refactoring.rename;

import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;

public class RenameRefactoring extends ProcessorBasedRefactoring {

	private RenameRefactoringProcessor processor;

	public RenameRefactoring(RenameRefactoringProcessor processor) {
		super(processor);
		this.processor = processor;
	}

	public RenameRefactoringProcessor getProcessor() {
		return processor;
	}
}
