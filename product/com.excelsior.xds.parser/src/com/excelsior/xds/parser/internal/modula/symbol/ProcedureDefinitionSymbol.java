package com.excelsior.xds.parser.internal.modula.symbol;

import java.util.Collection;

import com.excelsior.xds.parser.commons.symbol.QualifiedNameFactory;
import com.excelsior.xds.parser.internal.modula.symbol.reference.IStaticModulaSymbolReference;
import com.excelsior.xds.parser.internal.modula.symbol.type.ProcedureTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.IFormalParameterSymbol;
import com.excelsior.xds.parser.modula.symbol.IProcedureDeclarationSymbol;
import com.excelsior.xds.parser.modula.symbol.IProcedureDefinitionSymbol;
import com.excelsior.xds.parser.modula.symbol.IProcedureSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.SymbolAttribute;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.reference.ReferenceUtils;
import com.excelsior.xds.parser.modula.symbol.type.IProcedureTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;

public class ProcedureDefinitionSymbol extends    SymbolWithScope 
                                       implements IProcedureDefinitionSymbol 
{
    /** Procedure type symbol is implementation level entity and has the same life time as the procedure symbol */
    protected final IStaticModulaSymbolReference<ProcedureTypeSymbol>  typeSymbolRef;
    protected IModulaSymbolReference<IProcedureDeclarationSymbol> declarationSymbolRef;
    
    public ProcedureDefinitionSymbol( String name, ISymbolWithScope parent
                                    , IStaticModulaSymbolReference<ProcedureTypeSymbol> typeSymbolRef) 
    {
        super(name, parent);
        this.typeSymbolRef = typeSymbolRef;
        ProcedureTypeSymbol typeSymbol = ReferenceUtils.resolve(typeSymbolRef);
        typeSymbol.attach(this);
        setAttributes(typeSymbol.getAttributes());
        removeAttribute(SymbolAttribute.ANONYMOUS_NAME);
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
    public boolean isLocal() {
        return getParentScope() instanceof IProcedureSymbol;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPublic() {
        return isAttributeSet(SymbolAttribute.PUBLIC);
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public IProcedureTypeSymbol getTypeSymbol() {
        return ReferenceUtils.resolve(typeSymbolRef);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IFormalParameterSymbol> getParameters() {
        return getTypeSymbol().getParameters();
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public ITypeSymbol getReturnTypeSymbol() {
        return getTypeSymbol().getReturnTypeSymbol();
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public IProcedureDeclarationSymbol getDeclarationSymbol() {
        return ReferenceUtils.resolve(declarationSymbolRef);
    }
    
    public void setDeclarationSymbol(IModulaSymbolReference<IProcedureDeclarationSymbol> declarationSymbolRef) {
        this.declarationSymbolRef = declarationSymbolRef;
    }
    
    
    /**
     * {@inheritDoc}
     */
	@Override
	protected void doAccept(ModulaSymbolVisitor visitor) {
		boolean isVisitChildren = visitor.visit(this);
		if (isVisitChildren){
			acceptChildren(visitor);
		}
	}

	protected void acceptChildren(ModulaSymbolVisitor visitor) {
		super.acceptChildren(visitor, getParameters());
		super.acceptChild(visitor, getReturnTypeSymbol());
	}
}
