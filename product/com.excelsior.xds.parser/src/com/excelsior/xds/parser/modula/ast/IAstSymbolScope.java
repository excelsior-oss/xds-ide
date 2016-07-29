package com.excelsior.xds.parser.modula.ast;

import com.excelsior.xds.parser.modula.symbol.IModulaSymbolScope;

/**
 * AST node corresponding to a Modula-2/Oberon-2 scope.
 */
public interface IAstSymbolScope
{
    public IModulaSymbolScope getScope();
}
