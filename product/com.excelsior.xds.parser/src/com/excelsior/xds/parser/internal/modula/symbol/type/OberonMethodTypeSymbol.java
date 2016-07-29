package com.excelsior.xds.parser.internal.modula.symbol.type;

import com.excelsior.xds.parser.commons.symbol.QualifiedNameFactory;
import com.excelsior.xds.parser.modula.symbol.IOberonMethodReceiverSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.SymbolAttribute;
import com.excelsior.xds.parser.modula.symbol.type.IOberonMethodTypeSymbol;
import com.excelsior.xds.parser.modula.type.OberonMethodType;
import com.excelsior.xds.parser.modula.type.ProcedureType;

public class OberonMethodTypeSymbol extends    ProcedureTypeSymbol 
                                    implements IOberonMethodTypeSymbol 
{
	private IOberonMethodReceiverSymbol receiverTypeSymbol;
    
    public OberonMethodTypeSymbol( String name
                                 , IOberonMethodReceiverSymbol receiverTypeSymbol
                                 , ISymbolWithScope parentScope ) 
    {
        super(name, parentScope);
        this.receiverTypeSymbol = receiverTypeSymbol;
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
    public OberonMethodType getType() {
        return (OberonMethodType)type;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected ProcedureType createType(String name) {
        return new OberonMethodType(name, this);
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public IOberonMethodReceiverSymbol getReceiverSymbol() {
        return receiverTypeSymbol;
    }
    
    public void setReceiverSymbol(IOberonMethodReceiverSymbol  receiverTypeSymbol) {
        this.receiverTypeSymbol = receiverTypeSymbol;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "TypeSymbol: " + getName() + " [type=" + type + "]";     //$NON-NLS-1$  //$NON-NLS-2$
    }

}
