package com.excelsior.xds.parser.internal.modula.symbol.type;

import java.util.Collection;

import com.excelsior.xds.parser.internal.modula.symbol.SynonymSymbolWithScope;
import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.IFormalParameterSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.type.IProcedureTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;
import com.excelsior.xds.parser.modula.type.ProcedureType;

public class ProcedureTypeSynonymSymbol extends    SynonymSymbolWithScope<IProcedureTypeSymbol>
                                        implements IProcedureTypeSymbol 
{

    public ProcedureTypeSynonymSymbol( String name, ISymbolWithScope parentScope
                                     , IModulaSymbolReference<IProcedureTypeSymbol> originalSymbolRef, 
                                     IModulaSymbolReference<IModulaSymbol> referencedSymbolRef) 
    {
        super(name, parentScope, originalSymbolRef, referencedSymbolRef);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ProcedureType getType() {
        return getOriginalSymbol().getType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IProcedureTypeSymbol createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory) {
        return new ProcedureTypeSynonymSymbol(name, parentScope, refFactory.createRef(getOriginalSymbol()), refFactory.createRef((IModulaSymbol)this)); 
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ITypeSymbol getReturnTypeSymbol() {
        return getOriginalSymbol().getReturnTypeSymbol();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IFormalParameterSymbol> getParameters() {
        return getOriginalSymbol().getParameters();
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
		// TODO : unclear whether should we visit structure coming from getOriginalSymbol()
		visitor.visit(this);  
	}    
}
