package com.excelsior.xds.parser.internal.modula.symbol.type;

import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.internal.modula.symbol.reference.ReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.reference.ReferenceUtils;
import com.excelsior.xds.parser.modula.symbol.type.IOrdinalTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ISetTypeSymbol;
import com.excelsior.xds.parser.modula.type.SetType;

public class SetTypeSymbol extends    OrdinalTypeSymbol<SetType> 
                           implements ISetTypeSymbol 
{
    private IModulaSymbolReference<IOrdinalTypeSymbol> baseTypeSymbolRef;
    private final boolean isPackedSet;

    public SetTypeSymbol(String name, ISymbolWithScope parentScope, boolean isPacked) {
        super(name, parentScope);
        isPackedSet = isPacked;
    }
   
    public void setBaseTypeSymbol(IOrdinalTypeSymbol s) {
        baseTypeSymbolRef = ReferenceFactory.createRef(s);
        if (s != null) {
            setType(new SetType(getName(), s.getType()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ISetTypeSymbol createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory) {
        return new SetTypeSynonymSymbol(name, parentScope, refFactory.createRef(this), refFactory.createRef((IModulaSymbol)this)); 
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IOrdinalTypeSymbol getBaseTypeSymbol() {
        return ReferenceUtils.resolve(baseTypeSymbolRef);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPacked() {
        return isPackedSet;
    }

}
