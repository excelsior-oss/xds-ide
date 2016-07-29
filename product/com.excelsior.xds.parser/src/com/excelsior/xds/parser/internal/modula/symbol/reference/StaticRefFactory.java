package com.excelsior.xds.parser.internal.modula.symbol.reference;

import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;

public final class StaticRefFactory implements IReferenceFactory {
	private StaticRefFactory(){
	}
	
	private static class StaticRefFactoryHolder{
        static StaticRefFactory INSTANCE = new StaticRefFactory();
    }

	public static StaticRefFactory instance(){
        return StaticRefFactoryHolder.INSTANCE;
    }
	
	@Override
	public <T extends IModulaSymbol> IModulaSymbolReference<T> createRef(
			T symbol) {
		return ReferenceFactory.createStaticRef(symbol);
	}
}
