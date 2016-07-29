package com.excelsior.xds.parser.internal.modula.symbol.type;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.excelsior.xds.parser.commons.symbol.QualifiedNameFactory;
import com.excelsior.xds.parser.internal.modula.symbol.SymbolWithScope;
import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.IFormalParameterSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.SymbolAttribute;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.reference.ReferenceUtils;
import com.excelsior.xds.parser.modula.symbol.type.IProcedureTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;
import com.excelsior.xds.parser.modula.type.ProcedureType;

public class ProcedureTypeSymbol extends    SymbolWithScope
                                 implements IProcedureTypeSymbol 
{
    protected final ProcedureType type;
    private final Map<String, IFormalParameterSymbol> parameters;

    private IModulaSymbolReference<ITypeSymbol> returnTypeSymbolRef;
    
    public ProcedureTypeSymbol(String name, ISymbolWithScope parentScope) {
        super(name, parentScope);
        parameters = new LinkedHashMap<String, IFormalParameterSymbol>();
        attach(parameters);
        type = createType(name);        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getQualifiedName() {
        String qualifiedName = super.getQualifiedName();
        if (isAttributeSet(SymbolAttribute.FORWARD_DECLARATION)) {
            qualifiedName = QualifiedNameFactory.getQualifiedName(qualifiedName, SymbolAttribute.FORWARD_DECLARATION);
        }
        return qualifiedName;  
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ProcedureType getType() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IProcedureTypeSymbol createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory) {
        return new ProcedureTypeSynonymSymbol(name, parentScope, refFactory.createRef((IProcedureTypeSymbol)this), refFactory.createRef((IModulaSymbol)this)); 
    }

    
    public void attach (SymbolWithScope parentScope) {
        parentScope.attach(parameters);
    }

    
    public ITypeSymbol getReturnTypeSymbol() {
    	 return ReferenceUtils.resolve(returnTypeSymbolRef);
    }

    public void setReturnTypeSymbol(IModulaSymbolReference<ITypeSymbol> ref) {
    	returnTypeSymbolRef = ref;
    }

    public Collection<IFormalParameterSymbol> getParameters() {
        return parameters.values();
    }
    
    public void addParameter(IFormalParameterSymbol s) {
        parameters.put(s.getName(), s);
    }
    
    
    protected ProcedureType createType(String name) {
        return new ProcedureType(name, this);
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "TypeSymbol: " + getName() + " [type=" + type + "]";     //$NON-NLS-1$  //$NON-NLS-2$
    }

	@Override
	protected void doAccept(ModulaSymbolVisitor visitor) {
		boolean isVisitChildren = visitor.visit(this);
		if (isVisitChildren){
			acceptChildren(visitor, getParameters());
		}
	}
}
