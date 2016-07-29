package com.excelsior.xds.parser.modula.symbol;

import java.util.Collection;

import org.eclipse.core.filesystem.IFileStore;

import com.excelsior.xds.core.builders.BuildSettings;
import com.excelsior.xds.parser.commons.symbol.IBlockSymbolTextBinding;
import com.excelsior.xds.parser.commons.symbol.ParsedModuleKey;

/**
 * Base interface for definition and program Modula-2/Oberon-2 modules.
 */
public interface IModuleSymbol extends ISymbolWithScope
                                     , ISymbolWithDefinitions
                                     , ISymbolWithImports
                                     , IModulaSymbolUsages
                                     , IBlockSymbolTextBinding
{
    public boolean hasConstructor();
    
    public IFileStore getSourceFile();
    
    public ParsedModuleKey getKey();
    
    /**
     * @return build settings used to construct this module
     */
    public BuildSettings getBuildSettings();
 
    /**
     * Returns an object to iterate a set of symbols which are exported from 
     * the symbol's scope.
     * 
     * @return an object to be the target of the "foreach" statement.
     */
    public Collection<IModulaSymbol> getExportedSymbols();
    
}
