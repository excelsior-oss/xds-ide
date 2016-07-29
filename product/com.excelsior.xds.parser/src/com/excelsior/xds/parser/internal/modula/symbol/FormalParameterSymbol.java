package com.excelsior.xds.parser.internal.modula.symbol;

import com.excelsior.xds.parser.modula.symbol.IFormalParameterSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.SymbolAttribute;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.reference.ReferenceUtils;
import com.excelsior.xds.parser.modula.symbol.type.IProcedureTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;

public class FormalParameterSymbol extends    ModulaSymbol
                                   implements IFormalParameterSymbol 
{
    private IModulaSymbolReference<ITypeSymbol> typeSymbolRef;
	private int number;

    public FormalParameterSymbol(String name, int number, IProcedureTypeSymbol parentScope) {
        this(name, number, parentScope, null);
    }
    
    public FormalParameterSymbol( String name, int number, IProcedureTypeSymbol parentScope
                                , IModulaSymbolReference<ITypeSymbol> typeSymbolRef ) 
    {
        super(name, parentScope);
        this.typeSymbolRef = typeSymbolRef;
        this.number = number;
    }
    
    public int getNumber() {
		return number;
	}
    
    @Override
    public String getQualifiedName() {
        String qualifiedName = String.format("$param(%s)",  getNumber()) + getNameCollosionId();
        ISymbolWithScope parentScope = getParentScope();
        if (parentScope != null) {
            qualifiedName = parentScope.getQualifiedName() + "." + qualifiedName;   //$NON-NLS-1$
        }
        return qualifiedName;
    }

	/**
     * {@inheritDoc}
     */
    @Override
    public ITypeSymbol getTypeSymbol() {
        return ReferenceUtils.resolve(typeSymbolRef);
    }
    
    public void setTypeSymbol(IModulaSymbolReference<ITypeSymbol> typeSymbolRef) {
        this.typeSymbolRef = typeSymbolRef;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isVarParameter() {
        return isAttributeSet(SymbolAttribute.VAR_PARAMETER);
    }

//    public void markAsVarParameter() {
//        addAttribute(SymbolAttribute.VAR_PARAMETER);
//    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNilAllowed() {
        return isAttributeSet(SymbolAttribute.NIL_ALLOWED);
    }
    
//    public void markNilAllowed() {
//        addAttribute(SymbolAttribute.NIL_ALLOWED);
//    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSeqParameter() {
        return isAttributeSet(SymbolAttribute.SEQ_PARAMETER);
    }

//    public void markSeqParameter() {
//        addAttribute(SymbolAttribute.SEQ_PARAMETER);
//    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isReadOnly() {
        return isAttributeSet(SymbolAttribute.READ_ONLY);
    }

//    public void markReadOnly() {
//        addAttribute(SymbolAttribute.READ_ONLY);
//    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDefaultValueEnabled() {
        return isAttributeSet(SymbolAttribute.DEFAULT);
    }

//    public void markDefaultValueEnabled() {
//        addAttribute(SymbolAttribute.DEFAULT);
//    }

    
    public static String createAnonymousName(int parameterNumber) {
        return ANONYMOUS_NAME_TAG + "Arg" + parameterNumber;    //$NON-NLS-1$
    }

	@Override
	protected void doAccept(ModulaSymbolVisitor visitor) {
		visitor.visit(this);
	}
}
