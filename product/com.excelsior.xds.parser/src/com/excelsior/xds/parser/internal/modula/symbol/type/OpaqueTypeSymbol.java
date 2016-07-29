package com.excelsior.xds.parser.internal.modula.symbol.type;

import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.internal.modula.symbol.reference.ReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.reference.ReferenceUtils;
import com.excelsior.xds.parser.modula.symbol.type.IOpaqueTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;
import com.excelsior.xds.parser.modula.type.OpaqueType;

public class OpaqueTypeSymbol extends    TypeSymbol<OpaqueType>
                              implements IOpaqueTypeSymbol
{
    private IModulaSymbolReference<ITypeSymbol> actualTypeSymbolRef;
    
    public OpaqueTypeSymbol(String name, ISymbolWithScope parentScope) {
        super(name, parentScope, new OpaqueType(name));
        getType().setSymbol(this);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ITypeSymbol getActualTypeSymbol() {
        return ReferenceUtils.resolve(actualTypeSymbolRef);
    }

    public void setActualTypeSymbol (ITypeSymbol actualTypeSymbol) {
        this.actualTypeSymbolRef = ReferenceFactory.createRef(actualTypeSymbol);
    }
    
    @Override
    public IOpaqueTypeSymbol createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory) {
        return new OpaqueTypeSynonymSymbol(name, parentScope, refFactory.createRef(this), refFactory.createRef((IModulaSymbol)this)); 
    }

	@Override
	protected void doAccept(ModulaSymbolVisitor visitor) {
		visitor.visit(this);
	}
}
