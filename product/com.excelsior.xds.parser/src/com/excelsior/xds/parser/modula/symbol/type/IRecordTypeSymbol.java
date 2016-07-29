package com.excelsior.xds.parser.modula.symbol.type;

import java.util.Collection;

import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.IRecordFieldSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithProcedures;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.type.RecordType;

public interface IRecordTypeSymbol extends ITypeSymbol
                                         , ISymbolWithProcedures
{
    /**
     * {@inheritDoc}
     */
    @Override
    public RecordType getType();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IRecordTypeSymbol createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory);
    
    public void addField(IRecordFieldSymbol s);
    
    public Collection<IRecordFieldSymbol> getFields();

    
    //--------------------------------------------------------------------------
    // Oberon-2 specific part
    //--------------------------------------------------------------------------

    public IRecordTypeSymbol getBaseTypeSymbol();
    
}
