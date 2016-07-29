package com.excelsior.xds.parser.internal.modula.symbol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.excelsior.xds.core.text.ITextRegion;
import com.excelsior.xds.parser.commons.symbol.BlockSymbolTextBinding;
import com.excelsior.xds.parser.commons.symbol.IMutableBlockSymbolTextBinding;
import com.excelsior.xds.parser.internal.modula.symbol.reference.IStaticModulaSymbolReference;
import com.excelsior.xds.parser.internal.modula.symbol.reference.ReferenceFactory;
import com.excelsior.xds.parser.internal.modula.symbol.type.ProcedureTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.IFormalParameterSymbol;
import com.excelsior.xds.parser.modula.symbol.IProcedureBodySymbol;
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

public class ProcedureDeclarationSymbol extends    SymbolWithDeclarations
                                        implements IProcedureDeclarationSymbol 
                                                 , IMutableBlockSymbolTextBinding
{
    protected IModulaSymbolReference<ProcedureTypeSymbol> typeSymbolRef;
    private IModulaSymbolReference<IProcedureBodySymbol>  bodySymbolRef;

    /** Procedure forward declarations have the same life time as the procedure symbol */
    private final List<IStaticModulaSymbolReference<ProcedureDefinitionSymbol>> forwardDeclarations;
    
    protected IModulaSymbolReference<IProcedureDefinitionSymbol> definitionSymbolRef;
    
    private ProcedureDeclarationSymbol(String name, ISymbolWithScope parent) {
        super(name, parent);
        forwardDeclarations = new ArrayList<IStaticModulaSymbolReference<ProcedureDefinitionSymbol>>(1);
    }
    
    public ProcedureDeclarationSymbol( String name
                                     , IModulaSymbolReference<ProcedureTypeSymbol> typeSymbolRef
                                     , ISymbolWithScope parent ) 
    {
        this(name, parent);
        finalizeDeclaration(typeSymbolRef);
    }

    public ProcedureDeclarationSymbol( ProcedureDefinitionSymbol forwardDeclSymbol
                                     , ISymbolWithScope parent ) 
    {
        this(forwardDeclSymbol.getName(), parent);
        forwardDeclarations.add(ReferenceFactory.createStaticRef(forwardDeclSymbol));
        forwardDeclSymbol.setDeclarationSymbol(ReferenceFactory.createStaticRef((IProcedureDeclarationSymbol)this));
        setDefinitionSymbol(ReferenceFactory.createStaticRef((IProcedureDefinitionSymbol)forwardDeclSymbol));
        setLanguage(forwardDeclSymbol.getLanguage());
        setAttributes(forwardDeclSymbol.getAttributes());
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public IProcedureBodySymbol getProcedureBodySymbol() {
        return ReferenceUtils.resolve(bodySymbolRef);
    }

    public void setProcedureBodySymbol(IModulaSymbolReference<IProcedureBodySymbol> bodySymbolRef) {
        this.bodySymbolRef = bodySymbolRef;
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
    public Collection<IFormalParameterSymbol> getParameters() {
        ProcedureTypeSymbol typeSymbol = ReferenceUtils.resolve(typeSymbolRef);
        if (typeSymbol != null) {
            return typeSymbol.getParameters();
        }
        return forwardDeclarations.get(0).resolve().getParameters();
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IProcedureTypeSymbol getTypeSymbol() {
        ProcedureTypeSymbol typeSymbol = ReferenceUtils.resolve(typeSymbolRef);
        if (typeSymbol != null) {
            return typeSymbol;
        }
        if (forwardDeclarations.size() == 0) {
        	return null;
        }
        return forwardDeclarations.get(0).resolve().getTypeSymbol();
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
    public IProcedureDefinitionSymbol getDefinitionSymbol() {
        return ReferenceUtils.resolve(definitionSymbolRef);
    }

    public void setDefinitionSymbol(IModulaSymbolReference<IProcedureDefinitionSymbol>  definitionSymbolRef) {
        this.definitionSymbolRef = definitionSymbolRef;
    }
    
    
    /* -------------------------------------------------------------------------
     *         Interface used to construct symbol 
     * -------------------------------------------------------------------------
     */
    
    public void finalizeDeclaration (IModulaSymbolReference<ProcedureTypeSymbol> typeSymbolRef) throws IllegalArgumentException 
    {
        this.typeSymbolRef = typeSymbolRef;
        ProcedureTypeSymbol typeSymbol = ReferenceUtils.resolve(typeSymbolRef);
        typeSymbol.attach(this);
        
        setLanguage(typeSymbol.getLanguage());
        addAttributes(typeSymbol.getAttributes());
        removeAttribute(SymbolAttribute.ANONYMOUS_NAME);
        removeAttribute(SymbolAttribute.FORWARD_DECLARATION);
    }
    
    public boolean isForwardDeclaration() {
        return (typeSymbolRef == null);
    }

    public void addForwardDeclaration(IStaticModulaSymbolReference<ProcedureDefinitionSymbol> forwardDeclSymbolRef) {
        forwardDeclarations.add(forwardDeclSymbolRef);
        ProcedureDefinitionSymbol forwardDeclSymbol = ReferenceUtils.resolve(forwardDeclSymbolRef);
        
        forwardDeclSymbol.setDeclarationSymbol(ReferenceFactory.createStaticRef((IProcedureDeclarationSymbol)this));
        if (definitionSymbolRef == null) {
            setDefinitionSymbol(ReferenceFactory.createStaticRef((IProcedureDefinitionSymbol)forwardDeclSymbol));
        }
    }
    
    public Collection<IProcedureDefinitionSymbol> getForwardDeclarations() {
    	return forwardDeclarations.stream().map(ReferenceUtils::resolve).collect(Collectors.toList());
	}
    

	/**
     * {@inheritDoc}
     */
	@Override
	protected void doAccept(ModulaSymbolVisitor visitor) {
		boolean isVisitChildren = visitor.visit(this);
		if (isVisitChildren) {
			acceptChildren(visitor);
		}
	}

	protected void acceptChildren(ModulaSymbolVisitor visitor) {
		super.acceptChildren(visitor, getParameters());
		super.acceptChild(visitor, getReturnTypeSymbol());
		super.acceptChildren(visitor);
	}

	
    //-------------------------------------------------------------------------
    // Symbol's location in the source text
    //-------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    protected IMutableBlockSymbolTextBinding createSymbolTextBinding() {
        return new BlockSymbolTextBinding();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IMutableBlockSymbolTextBinding getTextBinding() {
        return (BlockSymbolTextBinding)super.getTextBinding();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ITextRegion> getNameTextRegions() {
        return getTextBinding().getNameTextRegions();
    }

    public void addNameTextRegion(ITextRegion region) {
        getTextBinding().addNameTextRegion(region);
    }
        
}
