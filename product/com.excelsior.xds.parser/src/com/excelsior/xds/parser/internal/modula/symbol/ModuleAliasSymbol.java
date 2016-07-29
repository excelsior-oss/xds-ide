package com.excelsior.xds.parser.internal.modula.symbol;

import java.util.Iterator;

import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModuleAliasSymbol;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.reference.ReferenceUtils;

public class ModuleAliasSymbol extends    ModulaSymbol 
                               implements IModuleAliasSymbol 
{
    private IModulaSymbolReference<IModuleSymbol> referenceSymbolRef;
    

    public ModuleAliasSymbol(String name, IModuleSymbol parentScope) {
        super(name, parentScope);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IModuleSymbol getReference() {
        return ReferenceUtils.resolve(referenceSymbolRef);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setReference(IModulaSymbolReference<IModuleSymbol> referenceSymbolRef) {
        this.referenceSymbolRef = referenceSymbolRef;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public IModulaSymbol resolveName(String name) {
        IModulaSymbol symbol;
        IModuleSymbol reference = ReferenceUtils.resolve(referenceSymbolRef);
        if (reference != null) {
            symbol = reference.resolveName(name);
        }
        else {
            symbol = null;
        }
        return symbol;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IModulaSymbol findSymbolInScope(String name) {
        IModulaSymbol symbol;
        IModuleSymbol reference = ReferenceUtils.resolve(referenceSymbolRef);
        if (reference != null) {
            symbol = reference.findSymbolInScope(name);
        }
        else {
            symbol = null;
        }
        return symbol;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IModulaSymbol findSymbolInScope(String symbolName, boolean isPublic) {
        IModulaSymbol symbol;
        IModuleSymbol reference = ReferenceUtils.resolve(referenceSymbolRef);
        if (reference != null) {
            symbol = reference.findSymbolInScope(symbolName, isPublic);
        }
        else {
            symbol = null;
        }
        return symbol;
    }

	@Override
	public Iterator<IModulaSymbol> iterator() {
	    IModuleSymbol reference = ReferenceUtils.resolve(referenceSymbolRef);
		Iterator<IModulaSymbol> iterator;
		if (reference != null) {
			iterator = reference.iterator();
        }
        else {
        	iterator = null;
        }
		return iterator;
	}

	@Override
	protected void doAccept(ModulaSymbolVisitor visitor) {
		visitor.visit(this);
	}
}
