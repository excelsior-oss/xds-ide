package com.excelsior.xds.parser.internal.modula.symbol.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.excelsior.xds.parser.commons.symbol.QualifiedNameFactory;
import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.SymbolAttribute;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.reference.ReferenceUtils;
import com.excelsior.xds.parser.modula.symbol.type.IForwardTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;
import com.excelsior.xds.parser.modula.type.Type;
import com.excelsior.xds.parser.modula.type.XdsStandardTypes;

/**
 * Forward type declaration symbol.
 * Forward type symbol will replace itself by the actual type symbol, 
 * when last one will be defined.  
 */
public class ForwardTypeSymbol extends TypeSymbol<Type>
                               implements IForwardTypeSymbol
{
    private IModulaSymbolReference<ITypeSymbol> actualTypeSymbolRef;
    private List<IModulaSymbol> usages; 
    
    public ForwardTypeSymbol (String name, ISymbolWithScope parentScope) {
        super(name, parentScope, XdsStandardTypes.VOID);
        usages = new ArrayList<IModulaSymbol>(1);
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getQualifiedName() {
        return QualifiedNameFactory.getQualifiedName(super.getQualifiedName(), SymbolAttribute.FORWARD_DECLARATION);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ITypeSymbol getActualTypeSymbol() {
        return ReferenceUtils.resolve(actualTypeSymbolRef);
    }

    public void setActualTypeSymbol (IModulaSymbolReference<ITypeSymbol> actualTypeSymbolRef) {
        this.actualTypeSymbolRef = actualTypeSymbolRef;
        ITypeSymbol s = ReferenceUtils.resolve(actualTypeSymbolRef);
        if (s != null) {
            setType(s.getType());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ITypeSymbol createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory) {
        ITypeSymbol typeSymbol;
        ITypeSymbol actualTypeSymbol = getActualTypeSymbol();
        if (actualTypeSymbol != null) {
            typeSymbol =  actualTypeSymbol.createSynonym(name, parentScope, refFactory);
        }
        else {
        	typeSymbol = new ForwardTypeSynonymSymbol(name, parentScope, refFactory.createRef(this), refFactory.createRef((IModulaSymbol)this)); 
        }
        return typeSymbol; 
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void addUsage(IModulaSymbol s) {
        usages.add(s);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IModulaSymbol> getUsages() {
        return usages;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void releaseUsages() {
        usages = null;
    }


	@Override
	protected void doAccept(ModulaSymbolVisitor visitor) {
		boolean isVisitChildren = visitor.visit(this);
		if (isVisitChildren){
			ITypeSymbol actualType = getActualTypeSymbol();
			if (actualType != null) {
				actualType.accept(visitor);
			}
		}
	}

	@Override
	public String toString() {
		return "Forward@ " + super.toString();
	}
}
