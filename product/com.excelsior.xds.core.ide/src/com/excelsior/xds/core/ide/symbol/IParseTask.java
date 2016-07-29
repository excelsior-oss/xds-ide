package com.excelsior.xds.core.ide.symbol;

import java.util.Collection;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;

import com.excelsior.xds.core.builders.BuildSettings;

/**
 * Each parse task 
 * @author lsa80
 */
public interface IParseTask {
	/**
	 * Project under which these modules were discovered and will be parsed
	 */
	IProject project();
	
	/**
	 * Absolute pathes of modules to parse
	 */
	Collection<IFileStore> files();
	
	/**
	 * Build settings that will be used to parse
	 */
	BuildSettings buildSettings();
	
	/**
	 * Parse even if it was parsed before and exists in model. Typically set to true.
	 * Set to false when it is OK to use cached symbol or AST if any
	 */
	boolean isForce(); 
	
	/**
	 * if true - parse errors will be reported as markers on workspace resources
	 */
	boolean isReportParseErrors();
	
	/**
	 * if true - AST will be reported back to the ISymbolModelListener
	 */
	boolean isNeedModulaAst();
	
	/**
	 * if true - def corresponding to mod or mod corresponding to def will be scheduled for parse (please note it will not be necessarily parsed after completion of this parse task!)
	 */
	boolean isParseDualModule();
	
	/**
	 * if true - mod files corresponding to modules from import section will be scheduled to parse
	 */
	boolean isParseImportSection();
}
