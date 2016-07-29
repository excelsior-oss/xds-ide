package com.excelsior.xds.parser.internal.modula.symbol.reference;

import com.excelsior.xds.parser.commons.symbol.ParsedModuleKey;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.binding.ModulaSymbolCache;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;

public class ModuleReference implements IModulaSymbolReference<IModuleSymbol> {
    
	private ParsedModuleKey parsedModuleKey;
    private final Class<?> symbolClass;
    
    ModuleReference(IModuleSymbol moduleSymbol){
    	parsedModuleKey = moduleSymbol.getKey();
        symbolClass = moduleSymbol.getClass();
    }

    @Override
    public IModuleSymbol resolve() {
        IModuleSymbol moduleSymbol = ModulaSymbolCache.instance().getModuleSymbol(parsedModuleKey);
        if (moduleSymbol == null || !symbolClass.equals(moduleSymbol.getClass())) {
            return null;
        }
        return moduleSymbol;
    }

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((parsedModuleKey == null) ? 0 : parsedModuleKey.hashCode());
		result = prime * result
				+ ((symbolClass == null) ? 0 : symbolClass.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ModuleReference other = (ModuleReference) obj;
		if (parsedModuleKey == null) {
			if (other.parsedModuleKey != null)
				return false;
		} else if (!parsedModuleKey.equals(other.parsedModuleKey))
			return false;
		if (symbolClass == null) {
			if (other.symbolClass != null)
				return false;
		} else if (!symbolClass.equals(other.symbolClass))
			return false;
		return true;
	}

	@Override
    public String toString() {
        return "ModuleReference [moduleFile=" + parsedModuleKey.moduleFile + "]";
    }
}
