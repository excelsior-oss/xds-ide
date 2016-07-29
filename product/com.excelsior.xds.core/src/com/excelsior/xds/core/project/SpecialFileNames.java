package com.excelsior.xds.core.project;

import java.util.HashSet;
import java.util.Set;

public final class SpecialFileNames {
	public static final String PROJECT_PROPERTIES_FILE = ".project"; //$NON-NLS-1$
	
	private static Set<String> specialFileNames;
	
	public synchronized static Set<String> getSpecialFileNames() {
		if (specialFileNames == null) {
			specialFileNames = new HashSet<String>();
			specialFileNames.add(PROJECT_PROPERTIES_FILE);
		}
		return specialFileNames;
	}
}
