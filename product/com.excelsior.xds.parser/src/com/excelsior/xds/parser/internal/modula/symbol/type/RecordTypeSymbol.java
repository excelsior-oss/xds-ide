package com.excelsior.xds.parser.internal.modula.symbol.type;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.excelsior.xds.parser.internal.modula.symbol.SymbolWithProcedures;
import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IOberonMethodSymbol;
import com.excelsior.xds.parser.modula.symbol.IRecordFieldSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.reference.ReferenceUtils;
import com.excelsior.xds.parser.modula.symbol.type.IRecordTypeSymbol;
import com.excelsior.xds.parser.modula.type.RecordType;

public class RecordTypeSymbol extends    SymbolWithProcedures 
                              implements IRecordTypeSymbol 
{
    private final RecordType type;
    private final Map<String, IRecordFieldSymbol> fields;
    
    private final IModulaSymbolReference<IRecordTypeSymbol> baseTypeSymbolRef;

    public RecordTypeSymbol( String name, IModulaSymbolReference<IRecordTypeSymbol> baseTypeSymbolRef
                           , ISymbolWithScope parentScope ) 
    {
        super(name, parentScope);
        this.baseTypeSymbolRef = baseTypeSymbolRef;
        type    = new RecordType(name, this);
        fields  = new LinkedHashMap<String, IRecordFieldSymbol>();
        attach(fields);
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public RecordType getType() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IRecordTypeSymbol createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory) {
        return new RecordTypeSynonymSymbol(name, parentScope, refFactory.createRef((IRecordTypeSymbol)this), refFactory.createRef((IModulaSymbol)this)); 
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IRecordFieldSymbol> getFields() {
        return fields.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addField(IRecordFieldSymbol s) {
        fields.put(s.getName(), s);
    }

    
    //--------------------------------------------------------------------------
    // Oberon-2 specific part
    //--------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public IRecordTypeSymbol getBaseTypeSymbol() {
        return ReferenceUtils.resolve(baseTypeSymbolRef);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IModulaSymbol findSymbolInScope(String name) {
        IModulaSymbol s = super.findSymbolInScope(name);
        IRecordTypeSymbol baseTypeSymbol = ReferenceUtils.resolve(baseTypeSymbolRef);
        if ((s == null) && (baseTypeSymbol != null)) {
            s = baseTypeSymbol.findSymbolInScope(name);
            if (s instanceof IOberonMethodSymbol) {
                // Oberon-method can be overridden  
                s = null;
            }
        }
        return s;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "TypeSymbol: " + getName() + " [type=" + type + "]";   //$NON-NLS-1$  //$NON-NLS-2$
    }

	@Override
	protected void doAccept(ModulaSymbolVisitor visitor) {
		boolean isVisitChildren = visitor.visit(this);
		if (isVisitChildren) {
			acceptChildren(visitor, getFields());
			acceptChildren(visitor, getProcedures());
		}
	}
}
