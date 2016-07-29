package com.excelsior.xds.parser.internal.modula.symbol.type;

import com.excelsior.xds.parser.internal.modula.symbol.ModulaSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;
import com.excelsior.xds.parser.modula.type.Type;

/**
 * Symbol which defines type.
 */
public class TypeSymbol<T extends Type> extends    ModulaSymbol
                                        implements ITypeSymbol 
{
    private T type;
    
    public TypeSymbol(String name, ISymbolWithScope parentScope, T type) {
        super(name, parentScope);
        this.type = type;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public T getType() {
        return type;
    }

    public void setType(T type) {
        this.type = type;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public ITypeSymbol createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory) {
        return new TypeSynonymSymbol<T, TypeSymbol<T>>(name, parentScope, refFactory.createRef(this), refFactory.createRef((IModulaSymbol)this)); 
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "TypeSymbol " + getName() + " [type=" + type + "]";     //$NON-NLS-1$  //$NON-NLS-2$
    }


	@Override
	protected void doAccept(ModulaSymbolVisitor visitor) {
		visitor.visit(this);		
	}
}
