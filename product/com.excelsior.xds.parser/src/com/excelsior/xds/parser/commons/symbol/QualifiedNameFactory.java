package com.excelsior.xds.parser.commons.symbol;

import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IOberonMethodReceiverSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.SymbolAttribute;
import com.excelsior.xds.parser.modula.symbol.type.IRecordTypeSymbol;

/**
 * 
 * @author lsa80
 */
public final class QualifiedNameFactory {
	/**
	 * This class can contain only static methods 
	 */
	private QualifiedNameFactory(){
	}
	
	public static String getQualifiedName(String qualifiedName, SymbolAttribute attr) {
		StringBuilder qualifiedNameBuilder = new StringBuilder(qualifiedName);
		
		if (SymbolAttribute.FORWARD_DECLARATION.equals(attr)) {
			qualifiedNameBuilder.append("@FORWARD"); //$NON-NLS-1$
		}
		
		return qualifiedNameBuilder.toString();
	}
	
	public static String getQualifiedName(IModulaSymbol s, SymbolAttribute attr) {
		return getQualifiedName(s.getQualifiedName(), attr);
	}
	
	public static String getOberonQualifiedName( String memberName
			, IOberonMethodReceiverSymbol receiverSymbol
			, ISymbolWithScope parentScope ) 
	{
		String qualifiedName = null;

		if (receiverSymbol != null) {
			IRecordTypeSymbol boundTypeSymbol = receiverSymbol.getBoundTypeSymbol();
			if (boundTypeSymbol != null) {
				qualifiedName = boundTypeSymbol.getQualifiedName() + ".";   //$NON-NLS-1$;
			}
		}

		if (qualifiedName == null) {
			if (parentScope != null) {
				qualifiedName = parentScope.getQualifiedName() + ".";   //$NON-NLS-1$
			}
			else {
				qualifiedName = "";   //$NON-NLS-1$
			}
		}

		qualifiedName += memberName;
		return qualifiedName;
	}
}
