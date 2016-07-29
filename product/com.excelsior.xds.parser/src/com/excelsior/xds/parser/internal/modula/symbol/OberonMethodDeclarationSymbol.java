package com.excelsior.xds.parser.internal.modula.symbol;

import com.excelsior.xds.parser.commons.symbol.QualifiedNameFactory;
import com.excelsior.xds.parser.internal.modula.symbol.reference.IStaticModulaSymbolReference;
import com.excelsior.xds.parser.internal.modula.symbol.type.OberonMethodTypeSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.type.ProcedureTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IOberonMethodDeclarationSymbol;
import com.excelsior.xds.parser.modula.symbol.IOberonMethodDefinitionSymbol;
import com.excelsior.xds.parser.modula.symbol.IOberonMethodReceiverSymbol;
import com.excelsior.xds.parser.modula.symbol.IProcedureDefinitionSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.reference.ReferenceUtils;
import com.excelsior.xds.parser.modula.symbol.type.IOberonMethodTypeSymbol;

public class OberonMethodDeclarationSymbol extends    ProcedureDeclarationSymbol
                                           implements IOberonMethodDeclarationSymbol
{
    public OberonMethodDeclarationSymbol( String name
                                        , IModulaSymbolReference<ProcedureTypeSymbol> typeSymbolRef
                                        , ISymbolWithScope parent) 
    {
        super(name, typeSymbolRef, parent);
    }
    
    public OberonMethodDeclarationSymbol( OberonMethodDefinitionSymbol forwarDeclaration
                                        , ISymbolWithScope parent ) 
    {
        super(forwarDeclaration, parent);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getQualifiedName() {
        IOberonMethodReceiverSymbol receiverSymbol = getReceiverSymbol();
        if (receiverSymbol == null) {
        	return null;
        }
		return QualifiedNameFactory.getOberonQualifiedName(
            getName() + getNameCollosionId(), receiverSymbol, getParentScope()
        );
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IOberonMethodTypeSymbol getTypeSymbol() {
        return (IOberonMethodTypeSymbol)super.getTypeSymbol();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLocal() {
        return false;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public void finalizeDeclaration (IModulaSymbolReference<ProcedureTypeSymbol> typeSymbolRef) throws IllegalArgumentException 
    {
        ProcedureTypeSymbol s = ReferenceUtils.resolve(typeSymbolRef);
        if (s instanceof OberonMethodTypeSymbol) {
            super.finalizeDeclaration(typeSymbolRef);
        }
        else {
            throw new IllegalArgumentException("Argument must be an instance of OberonMethodTypeSymbol");   //$NON-NLS-1$
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addForwardDeclaration(IStaticModulaSymbolReference<ProcedureDefinitionSymbol> forwardDeclSymbolRef) throws IllegalArgumentException {
        ProcedureDefinitionSymbol forwardDeclSymbol = ReferenceUtils.resolve(forwardDeclSymbolRef);
        if (forwardDeclSymbol instanceof IOberonMethodDefinitionSymbol) {
            super.addForwardDeclaration(forwardDeclSymbolRef);
        }
        else {
            throw new IllegalArgumentException("Argument must be an instance of IOberonMethodDefinitionSymbol");   //$NON-NLS-1$
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IOberonMethodReceiverSymbol getReceiverSymbol() {
        IOberonMethodTypeSymbol typeSymbol = getTypeSymbol();
        if (typeSymbol == null) {
        	return null;
        }
		return typeSymbol.getReceiverSymbol();
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public IOberonMethodDefinitionSymbol getDefinitionSymbol() {
        return (IOberonMethodDefinitionSymbol)super.getDefinitionSymbol();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDefinitionSymbol(IModulaSymbolReference<IProcedureDefinitionSymbol> definitionSymbolRef) throws IllegalArgumentException
    {
    	IProcedureDefinitionSymbol definitionSymbol = ReferenceUtils.resolve(definitionSymbolRef);
        if (definitionSymbol instanceof IOberonMethodDefinitionSymbol) {
            super.setDefinitionSymbol(definitionSymbolRef);
        }
        else {
            throw new IllegalArgumentException("Argument must be an instance of IOberonMethodDefinitionSymbol");   //$NON-NLS-1$
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IModulaSymbol findSymbolInScope(final String name) {
        IModulaSymbol symbol = getReceiverSymbol();
        boolean notFound = (symbol == null) || !symbol.getName().equals(name); 
        if (notFound) {
            symbol = super.findSymbolInScope(name);
        }
        return symbol;
    }
    
    @Override
	protected void doAccept(ModulaSymbolVisitor visitor) {
		if (visitor.visit(this)) {
			acceptChildren(visitor);
		}
	}

	protected void acceptChildren(ModulaSymbolVisitor visitor) {
		super.acceptChild(visitor, getReceiverSymbol());
		super.acceptChildren(visitor);
	}
}
