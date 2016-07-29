package com.excelsior.xds.parser.internal.modula.symbol.type;

import org.apache.commons.lang.StringUtils;

import com.excelsior.xds.parser.modula.XdsLanguage;
import com.excelsior.xds.parser.modula.XdsStandardNames;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.binding.ModulaSymbolCache;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.reference.ReferenceUtils;
import com.excelsior.xds.parser.modula.symbol.type.IArrayTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IOrdinalTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;
import com.excelsior.xds.parser.modula.type.OpenArrayType;

public class OpenArrayTypeSymbol extends    ArrayTypeSymbol
                                 implements IArrayTypeSymbol
{
    public OpenArrayTypeSymbol( String name
                              , ISymbolWithScope parentScope
                              , XdsLanguage moduleLanguage 
                              , IModulaSymbolReference<ITypeSymbol> elementTypeSymbolRef ) 
    {
        this( name, parentScope
            , ReferenceUtils.createStaticRef((IOrdinalTypeSymbol)ModulaSymbolCache.getSystemModule(moduleLanguage).resolveName(XdsStandardNames.INDEX))    
            , elementTypeSymbolRef );
    }
    
    private OpenArrayTypeSymbol( String name, ISymbolWithScope parentScope
                               , IModulaSymbolReference<IOrdinalTypeSymbol> indexTypeSymbolRef
                               , IModulaSymbolReference<ITypeSymbol> elementTypeSymbolRef ) 
    {
        super( name, parentScope
             , indexTypeSymbolRef, elementTypeSymbolRef   
             , new OpenArrayType( name
                                , getOrdinalType(indexTypeSymbolRef)
                                , getType(elementTypeSymbolRef) ) 
             );
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public OpenArrayType getType() {
        return (OpenArrayType)super.getType();
    }
    
    
    @Override
	protected void doAccept(ModulaSymbolVisitor visitor) {
		boolean isVisitChildren = visitor.visit(this);
		if (isVisitChildren) {
            ITypeSymbol elementType = getElementTypeSymbol();
            if (elementType != null) {
                elementType.accept(visitor);
            }
		}
	}

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OpenArrayTypeSymbol other = (OpenArrayTypeSymbol) obj;
        
        return StringUtils.equals(getQualifiedName(), other.getQualifiedName());
    }
    
    
}
