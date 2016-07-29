package com.excelsior.xds.parser.internal.modula.symbol;

import com.excelsior.xds.parser.modula.symbol.IStandardProcedureSymbol;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;

public class StandardProcedureSymbol extends    ModulaSymbol 
                                     implements IStandardProcedureSymbol
{
    public StandardProcedureSymbol(String name, StandardModuleSymbol parentScope) {
        super(name, parentScope);
    }

	@Override
	protected void doAccept(ModulaSymbolVisitor visitor) {
		visitor.visit(this);
	}
}
