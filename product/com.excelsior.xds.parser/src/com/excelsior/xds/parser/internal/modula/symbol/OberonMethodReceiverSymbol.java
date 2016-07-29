package com.excelsior.xds.parser.internal.modula.symbol;

import com.excelsior.xds.parser.modula.symbol.IOberonMethodReceiverSymbol;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.type.IForwardTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IOberonMethodTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IPointerTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IRecordTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;

public class OberonMethodReceiverSymbol extends    FormalParameterSymbol 
                                        implements IOberonMethodReceiverSymbol 
{
    public OberonMethodReceiverSymbol( String name
                                     , IOberonMethodTypeSymbol parentScope
                                     , IModulaSymbolReference<ITypeSymbol> typeSymbolRef ) 
    {
        super(name, -1, parentScope, typeSymbolRef);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IRecordTypeSymbol getBoundTypeSymbol() {
        ITypeSymbol receiverTypeSymbol = getTypeSymbol();
        
        if (receiverTypeSymbol instanceof IPointerTypeSymbol) {
            receiverTypeSymbol = ((IPointerTypeSymbol)receiverTypeSymbol).getBoundTypeSymbol();
        }

        if (receiverTypeSymbol instanceof IForwardTypeSymbol) {
            ITypeSymbol actialTypeSymbol = ((IForwardTypeSymbol)receiverTypeSymbol).getActualTypeSymbol();
            if (actialTypeSymbol != null) {
                receiverTypeSymbol = actialTypeSymbol; 
            }
        }
        
        if (receiverTypeSymbol instanceof IRecordTypeSymbol) {
            return (IRecordTypeSymbol)receiverTypeSymbol;
        }
        return null;
    }

	@Override
	protected void doAccept(ModulaSymbolVisitor visitor) {
		visitor.visit(this);
	}
	
}
