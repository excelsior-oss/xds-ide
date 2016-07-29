package com.excelsior.xds.parser.modula.symbol.binding;

import org.eclipse.core.filesystem.IFileStore;

import com.excelsior.xds.parser.commons.symbol.ParsedModuleKey;

/**
 * Can create {@link ParsedModuleKey}
 * 
 * @author lsa80
 */
public interface IModuleKeyFactory {
	ParsedModuleKey createModuleKey(IFileStore moduleFile);
}
