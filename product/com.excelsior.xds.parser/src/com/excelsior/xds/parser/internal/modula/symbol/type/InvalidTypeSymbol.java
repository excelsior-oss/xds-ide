package com.excelsior.xds.parser.internal.modula.symbol.type;

import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.type.IInvalidTypeSymbol;
import com.excelsior.xds.parser.modula.type.VoidType;
import com.excelsior.xds.parser.modula.type.XdsStandardTypes;

/**
 * Type symbol for invalid or unknown type.
 */
public class InvalidTypeSymbol extends    TypeSymbol<VoidType>
                               implements IInvalidTypeSymbol
{
    public InvalidTypeSymbol(String name, ISymbolWithScope parentScope) {
        super(name, parentScope, XdsStandardTypes.VOID);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IInvalidTypeSymbol createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory) {
    	return new InvalidTypeSymbol(name, parentScope);
    }

	@Override
	protected void doAccept(ModulaSymbolVisitor visitor) {
		visitor.visit(this);
	}
}
