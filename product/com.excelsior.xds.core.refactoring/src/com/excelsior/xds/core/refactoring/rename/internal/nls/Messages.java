package com.excelsior.xds.core.refactoring.rename.internal.nls;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages";     //$NON-NLS-1$
	
	public static String RenameRefactoringProcessor_Creatingchanges;
	public static String RenameRefactoringProcessor_ErrorWhileComputingLocationsToRename;
	public static String RenameRefactoringProcessor_Name;
	public static String RenameRefactoringProcessor_RenameModule;
	public static String RenameRefactoringProcessor_SymbolCannotBeRenamed;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
