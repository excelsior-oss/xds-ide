package com.excelsior.xds.core.ide.symbol;

import com.excelsior.xds.parser.commons.symbol.ParsedModuleKey;
import com.excelsior.xds.parser.modula.ast.ModulaAst;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;

/**
 * Listener of the Symbol Model Modifications
 * @author lsa80
 */
public interface ISymbolModelListener {
	/**
	 * @return Which source file modifications should be reported to listener
	 */
	Iterable<ParsedModuleKey> getModulesOfInterest();
	
	/**
	 * if true - {@link #modelUpToDate()} will be called 
	 */
	boolean isInterestedInModelUpToDateEvent();
	
	/**
	 * Callback when file was parsed, results are ready
	 */
	void parsed(ParsedModuleKey key, IModuleSymbol moduleSymbol, ModulaAst ast);
	
	/**
	 * Callback when symbol for the given source file was removed
	 * @param sourceFile
	 */
	void removed(ParsedModuleKey key);
	
	/**
	 * Callback for unchecked exceptions occured during the processing of the listener
	 */
	void error(Throwable error);
	
	/**
	 * Called when model modification queue is empty - i.e. model as complete as possible
	 */
	void modelUpToDate();
}
