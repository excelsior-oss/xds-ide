package com.excelsior.xds.parser.internal.modula.symbol;

import com.excelsior.xds.parser.commons.symbol.QualifiedNameFactory;
import com.excelsior.xds.parser.internal.modula.symbol.reference.IStaticModulaSymbolReference;
import com.excelsior.xds.parser.internal.modula.symbol.type.ProcedureTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IOberonMethodDeclarationSymbol;
import com.excelsior.xds.parser.modula.symbol.IOberonMethodDefinitionSymbol;
import com.excelsior.xds.parser.modula.symbol.IOberonMethodReceiverSymbol;
import com.excelsior.xds.parser.modula.symbol.IProcedureDeclarationSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.SymbolAttribute;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.reference.ReferenceUtils;
import com.excelsior.xds.parser.modula.symbol.type.IOberonMethodTypeSymbol;

public class OberonMethodDefinitionSymbol extends    ProcedureDefinitionSymbol
                                          implements IOberonMethodDefinitionSymbol
{
    public OberonMethodDefinitionSymbol( String name, ISymbolWithScope parent
                                       , IStaticModulaSymbolReference<ProcedureTypeSymbol> typeSymbolRef ) 
    {
        super(name, parent, typeSymbolRef);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getQualifiedName() {
        String qualifiedName = QualifiedNameFactory.getOberonQualifiedName(
            getName() + getNameCollosionId(), getReceiverSymbol(), getParentScope()
        );
        if (isAttributeSet(SymbolAttribute.FORWARD_DECLARATION)) {
            qualifiedName = QualifiedNameFactory.getQualifiedName(qualifiedName, SymbolAttribute.FORWARD_DECLARATION);
        }
        return qualifiedName;  
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
    public IOberonMethodReceiverSymbol getReceiverSymbol() {
        return getTypeSymbol().getReceiverSymbol();
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public IOberonMethodDeclarationSymbol getDeclarationSymbol() {
        return (IOberonMethodDeclarationSymbol)super.getDeclarationSymbol();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setDeclarationSymbol(IModulaSymbolReference<IProcedureDeclarationSymbol> declarationSymbolRef) throws IllegalArgumentException 
    {
    	IProcedureDeclarationSymbol declarationSymbol = ReferenceUtils.resolve(declarationSymbolRef);
        if (declarationSymbol instanceof IOberonMethodDeclarationSymbol) {
            super.setDeclarationSymbol(declarationSymbolRef);
        }
        else {
            throw new IllegalArgumentException("Argument must be an instance of IOberonMethodDeclarationSymbol");   //$NON-NLS-1$
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IModulaSymbol findSymbolInScope(final String name) {
        IModulaSymbol symbol = getReceiverSymbol();
        if ((symbol == null) || !symbol.equals(name)) {
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
