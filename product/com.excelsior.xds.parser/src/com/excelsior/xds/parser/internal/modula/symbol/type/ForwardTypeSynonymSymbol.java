package com.excelsior.xds.parser.internal.modula.symbol.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.reference.ReferenceUtils;
import com.excelsior.xds.parser.modula.symbol.type.IForwardTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;
import com.excelsior.xds.parser.modula.type.Type;

/**
 * Synonym to forward type declaration symbol. 
 * Forward type synonym symbol will replace itself by the synonym of actual type symbol, 
 * when last one will be defined.  
 */
public class ForwardTypeSynonymSymbol extends    TypeSynonymSymbol<Type, ForwardTypeSymbol> 
                                      implements IForwardTypeSymbol
{
    private List<IModulaSymbol> usages; 

    public ForwardTypeSynonymSymbol( String name, ISymbolWithScope parentScope
                                   ,  IModulaSymbolReference<ForwardTypeSymbol> originalSymbolRef, 
                                   IModulaSymbolReference<IModulaSymbol> referencedSymbolRef) 
    {
        super(name, parentScope, originalSymbolRef, referencedSymbolRef);
        usages = new ArrayList<IModulaSymbol>(1);
        ForwardTypeSymbol originalSymbol = ReferenceUtils.resolve(originalSymbolRef);
        originalSymbol.addUsage(this);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IForwardTypeSymbol createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory) {
        return new ForwardTypeSynonymSymbol(name, parentScope, refFactory.createRef(getOriginalSymbol()), refFactory.createRef((IModulaSymbol)this)); 
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Type getType() {
        return getOriginalSymbol().getType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ITypeSymbol getActualTypeSymbol() {
        return getOriginalSymbol().getActualTypeSymbol();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void addUsage(IModulaSymbol s) {
        usages.add(s);
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IModulaSymbol> getUsages() {
        return usages;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void releaseUsages() {
        usages = null;
    }

	@Override
	protected void doAccept(ModulaSymbolVisitor visitor) {
		visitor.visit((IForwardTypeSymbol)this);
	}
}
