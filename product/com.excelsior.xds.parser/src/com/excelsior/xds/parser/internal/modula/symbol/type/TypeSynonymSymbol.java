package com.excelsior.xds.parser.internal.modula.symbol.type;

import com.excelsior.xds.parser.internal.modula.symbol.SynonymSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSynonymSymbol;
import com.excelsior.xds.parser.modula.type.Type;

public class TypeSynonymSymbol<T extends Type, S extends TypeSymbol<T>> extends    SynonymSymbol<S> 
                                                                        implements ITypeSynonymSymbol
{
    public TypeSynonymSymbol( String name, ISymbolWithScope parentScope
                            , IModulaSymbolReference<S> originalSymbolRef, 
                            IModulaSymbolReference<IModulaSymbol> referencedSymbolRef) 
    {
        super(name, parentScope, originalSymbolRef, referencedSymbolRef);
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public T getType() {
        return getOriginalSymbol().getType();
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public ITypeSymbol createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory) {
        return new TypeSynonymSymbol<T,S>(name, parentScope, refFactory.createRef(getOriginalSymbol()), refFactory.createRef((IModulaSymbol)this)); 
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public ITypeSymbol getReferencedSymbol() {
        return (ITypeSymbol)super.getReferencedSymbol(); 
    }

    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "TypeSynonymSymbol: " + getName() + " [type=" + getType() + "]";   //$NON-NLS-1$  //$NON-NLS-2$
    }

	@Override
	protected void doAccept(ModulaSymbolVisitor visitor) {
		visitor.visit(this);
	}    
}
