package com.excelsior.xds.parser.internal.modula.symbol.type;

import java.util.Collection;

import com.excelsior.xds.parser.internal.modula.symbol.SynonymSymbolWithScope;
import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IProcedureSymbol;
import com.excelsior.xds.parser.modula.symbol.IRecordFieldSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.type.IRecordTypeSymbol;
import com.excelsior.xds.parser.modula.type.RecordType;

public class RecordTypeSynonymSymbol extends    SynonymSymbolWithScope<IRecordTypeSymbol>
                                     implements IRecordTypeSymbol 
{
    public RecordTypeSynonymSymbol( String name, ISymbolWithScope parentScope
                                  , IModulaSymbolReference<IRecordTypeSymbol> originalSymbolRef 
                                  , IModulaSymbolReference<IModulaSymbol> referencedSymbolRef ) 
    {
        super(name, parentScope, originalSymbolRef, referencedSymbolRef);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RecordType getType() {
        return getOriginalSymbol().getType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IRecordTypeSymbol createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory) {
        return new RecordTypeSynonymSymbol(name, parentScope, refFactory.createRef(getOriginalSymbol()), refFactory.createRef((IModulaSymbol)this)); 
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IRecordFieldSymbol> getFields() {
        return getOriginalSymbol().getFields();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addField(IRecordFieldSymbol s) {
        getOriginalSymbol().addField(s);
    }

    
    //--------------------------------------------------------------------------
    // Oberon-2 specific part
    //--------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public IRecordTypeSymbol getBaseTypeSymbol() {
        IRecordTypeSymbol originalSymbol = getOriginalSymbol();
		if (originalSymbol != null) {
			return originalSymbol.getBaseTypeSymbol();
		}
		else{
			return null;
		}
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void addProcedure(IProcedureSymbol s) {
        getOriginalSymbol().addProcedure(s);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IProcedureSymbol> getProcedures() {
        return getOriginalSymbol().getProcedures();
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
