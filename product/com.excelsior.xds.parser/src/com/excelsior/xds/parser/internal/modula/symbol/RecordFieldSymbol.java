package com.excelsior.xds.parser.internal.modula.symbol;

import com.excelsior.xds.parser.modula.symbol.IRecordFieldSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.reference.ReferenceUtils;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;

public class RecordFieldSymbol extends    ModulaSymbol
                               implements IRecordFieldSymbol 
{
    private IModulaSymbolReference<ITypeSymbol> typeSymbolRef;
    
    public RecordFieldSymbol(String name, ISymbolWithScope parentScope) {
        super(name, parentScope);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ITypeSymbol getTypeSymbol() {
        return ReferenceUtils.resolve(typeSymbolRef);
    }

    public void setTypeSymbol(IModulaSymbolReference<ITypeSymbol> typeSymbolRef) {
        this.typeSymbolRef = typeSymbolRef;
    }

	@Override
	protected void doAccept(ModulaSymbolVisitor visitor) {
		visitor.visit(this);
	}
}
