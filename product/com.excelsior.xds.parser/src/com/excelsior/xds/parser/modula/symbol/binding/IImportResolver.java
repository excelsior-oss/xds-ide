package com.excelsior.xds.parser.modula.symbol.binding;

import org.eclipse.core.filesystem.IFileStore;

import com.excelsior.xds.parser.modula.XdsLanguage;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;

/**
 * @author lsa80
 */
public interface IImportResolver extends IModuleKeyFactory{
	/**
	 * Obtains symbol (either by parsing or from {@link ModulaSymbolCache})
	 * 
	 * @param language - how to parse (assuming what language)
	 * @param moduleName - name of the module
	 * @param hostFile - actual source file containing the code
	 * @param chars 
	 * @param i 
	 * @return
	 */
	IModuleSymbol resolveModuleSymbol( XdsLanguage language, String moduleName, IFileStore sourceFile);
}
