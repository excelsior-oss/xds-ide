package com.excelsior.xds.parser.internal.modula.symbol.reference;

import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;

final class NullModulaSymbolReference implements IModulaSymbolReference<IModulaSymbol>{
    @Override
    public IModulaSymbol resolve() {
        return null;
    }
}