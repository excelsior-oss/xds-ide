package com.excelsior.xds.parser.internal.modula.symbol.type;

import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.reference.ReferenceUtils;
import com.excelsior.xds.parser.modula.symbol.type.IArrayTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IOrdinalTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;
import com.excelsior.xds.parser.modula.type.ArrayType;
import com.excelsior.xds.parser.modula.type.OrdinalType;
import com.excelsior.xds.parser.modula.type.Type;

public class ArrayTypeSymbol extends    TypeSymbol<ArrayType> 
                             implements IArrayTypeSymbol
{
	private final IModulaSymbolReference<IOrdinalTypeSymbol> indexTypeSymbolRef;
	private final IModulaSymbolReference<ITypeSymbol> elementTypeSymbolRef;

    public ArrayTypeSymbol( String name, ISymbolWithScope parentScope
                          , IModulaSymbolReference<IOrdinalTypeSymbol> indexTypeSymbolRef
                          , IModulaSymbolReference<ITypeSymbol> elementTypeSymbolRef ) 
    {
        this( name, parentScope
            , indexTypeSymbolRef, elementTypeSymbolRef    
            , new ArrayType( name
                           , getOrdinalType(indexTypeSymbolRef)
                           , getType(elementTypeSymbolRef)
                           ) 
            );
    }

    
    protected ArrayTypeSymbol( String name, ISymbolWithScope parentScope
                             , IModulaSymbolReference<IOrdinalTypeSymbol> indexTypeSymbolRef
                             , IModulaSymbolReference<ITypeSymbol> elementTypeSymbolRef
                             , ArrayType type )
    {
        super(name, parentScope, type);
        this.indexTypeSymbolRef   = indexTypeSymbolRef;
        this.elementTypeSymbolRef = elementTypeSymbolRef;
    }
    
    @Override
    public IArrayTypeSymbol createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory) {
        return new ArrayTypeSynonymSymbol(name, parentScope, refFactory.createRef(this), refFactory.createRef((IModulaSymbol)this)); 
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IOrdinalTypeSymbol getIndexTypeSymbol() {
        return ReferenceUtils.resolve(indexTypeSymbolRef);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ITypeSymbol getElementTypeSymbol() {
        return ReferenceUtils.resolve(elementTypeSymbolRef);
    }

    
    protected static Type getType(IModulaSymbolReference<ITypeSymbol> elementTypeSymbolRef) {
    	ITypeSymbol elementTypeSymbol = ReferenceUtils.resolve(elementTypeSymbolRef);
        return elementTypeSymbol == null 
             ? null : elementTypeSymbol.getType();
    }

    protected static OrdinalType getOrdinalType(IModulaSymbolReference<IOrdinalTypeSymbol> indexTypeSymbolRef) {
    	IOrdinalTypeSymbol indexTypeSymbol = ReferenceUtils.resolve(indexTypeSymbolRef);
        return indexTypeSymbol == null 
             ? null : indexTypeSymbol.getType();
    }


	@Override
	protected void doAccept(ModulaSymbolVisitor visitor) {
		boolean isVisitChildren = visitor.visit(this);
		if (isVisitChildren){
			ITypeSymbol elementType = getElementTypeSymbol();
			if (elementType != null) {
				elementType.accept(visitor);
			}
			IOrdinalTypeSymbol indexType = getIndexTypeSymbol();
			if (indexType != null) {
				indexType.accept(visitor);
			}
		}
	}
}
