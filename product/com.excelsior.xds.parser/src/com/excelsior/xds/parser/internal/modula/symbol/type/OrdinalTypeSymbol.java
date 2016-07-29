package com.excelsior.xds.parser.internal.modula.symbol.type;

import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.type.IOrdinalTypeSymbol;
import com.excelsior.xds.parser.modula.type.OrdinalType;

public class OrdinalTypeSymbol<T extends OrdinalType> extends    TypeSymbol<T> 
                                                      implements IOrdinalTypeSymbol 
{

    public OrdinalTypeSymbol(String name, ISymbolWithScope parentScope) {
        this(name, parentScope, null);
    }

    public OrdinalTypeSymbol(String name, ISymbolWithScope parentScope, T type) {
        super(name, parentScope, type);
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public IOrdinalTypeSymbol createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory) {
        return new OrdinalTypeSynonymSymbol<T>(name, parentScope, refFactory.createRef(this), refFactory.createRef((IModulaSymbol)this)); 
    }

	@Override
	protected void doAccept(ModulaSymbolVisitor visitor) {
		visitor.visit(this);
	}
}
