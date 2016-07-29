package com.excelsior.xds.parser.modula.ast;

import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;

public class AstSimpleName extends AstName<IModulaSymbol> 
{
    public AstSimpleName(ModulaCompositeType<? extends AstSimpleName> elementType) {
        super(null, elementType);
    }

}
