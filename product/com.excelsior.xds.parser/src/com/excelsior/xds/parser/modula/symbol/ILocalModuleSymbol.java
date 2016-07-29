package com.excelsior.xds.parser.modula.symbol;


public interface ILocalModuleSymbol extends IProgramModuleSymbol 
{
    public boolean isExportQualified();
    
    /**
     * Searches for symbol which is exported from the symbol's scope.
     * 
     * @param symbolName the name of symbol which is to be searched
     * @return the first exported symbol with specified name, or
     *         {@code null} if there is no such symbol.
     */
    public IModulaSymbol findSymbolInExport(String name);

}
