package com.excelsior.xds.parser.internal.modula.symbol.type;

import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.reference.ReferenceUtils;
import com.excelsior.xds.parser.modula.symbol.type.IPointerTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;
import com.excelsior.xds.parser.modula.type.PointerType;

public class PointerTypeSymbol extends    TypeSymbol<PointerType>
                               implements IPointerTypeSymbol 
{
    private IModulaSymbolReference<ITypeSymbol> boundTypeSymbolRef;
    
    public PointerTypeSymbol(String name, ISymbolWithScope parentScope) {
        this(name, parentScope, new PointerType(name));
        getType().setSymbol(this);
    }

    public PointerTypeSymbol( String name, ISymbolWithScope parentScope
                            , PointerType type ) 
    {
        super(name, parentScope, type);
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IPointerTypeSymbol createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory) {
        return new PointerTypeSynonymSymbol(name, parentScope, refFactory.createRef(this), refFactory.createRef((IModulaSymbol)this)); 
    }
        
    /**
     * {@inheritDoc}
     */
    @Override
    public ITypeSymbol getBoundTypeSymbol() {
        return ReferenceUtils.resolve(boundTypeSymbolRef);
    }
    
    
    public void setBoundType(IModulaSymbolReference<ITypeSymbol> boundTypeSymbolRef) {
        this.boundTypeSymbolRef = boundTypeSymbolRef;
    }

	@Override
	protected void doAccept(ModulaSymbolVisitor visitor) {
		visitor.visit(this);
//		if (isVisitChildren) {
//			ITypeSymbol boundType = getBoundTypeSymbol();
//			if (boundType != null) {
//				boundType.accept(visitor);
//			}
//		}
	}
}
