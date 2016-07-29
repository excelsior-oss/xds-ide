package com.excelsior.xds.parser.internal.modula.symbol;

import com.excelsior.xds.parser.modula.symbol.IEnumElementSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.type.IEnumTypeSymbol;

public class EnumElementSymbol extends    WholeConstantSymbol<IEnumTypeSymbol>
                               implements IEnumElementSymbol
{

    public EnumElementSymbol( String name, ISymbolWithScope parentScope
                            , IModulaSymbolReference<IEnumTypeSymbol> typeSymbolRef
                            , long value ) 
    {
        super(name, parentScope, typeSymbolRef, value);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getQualifiedName() {
        String qualifiedName = null;
        
        IEnumTypeSymbol typeSymbol = getTypeSymbol();
        if (typeSymbol != null) {
            qualifiedName = typeSymbol.getQualifiedName() + ".";   //$NON-NLS-1$;
        }
        
        if (qualifiedName == null) {
            ISymbolWithScope parentScope = getParentScope();
            if (parentScope != null) {
                qualifiedName = parentScope.getQualifiedName() + ".";   //$NON-NLS-1$
            }
            else {
                qualifiedName = "";   //$NON-NLS-1$
            }
        }

        qualifiedName += getName() + getNameCollosionId();
        return qualifiedName;
    }
    
}
