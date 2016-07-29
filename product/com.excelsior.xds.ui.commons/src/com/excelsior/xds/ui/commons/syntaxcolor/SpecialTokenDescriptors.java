package com.excelsior.xds.ui.commons.syntaxcolor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * static descriptors of the WHITESPACE and EOF tokens from class {@link Token}
 * @author lsa80
 */
public class SpecialTokenDescriptors {
	public static final TokenDescriptor WHITESPACE = new TokenDescriptor();
	public static final TokenDescriptor EOF = new TokenDescriptor();
	
	private static final Map<TokenDescriptor, IToken> specialsTokenDescs = new HashMap<TokenDescriptor, IToken>();
	
	static
	{
		specialsTokenDescs.put(WHITESPACE, Token.WHITESPACE);
		specialsTokenDescs.put(EOF, Token.EOF);
	}
	
	public static boolean isEof(TokenDescriptor tokenDesc) {
		return EOF == tokenDesc;
	}
	
	static boolean isSpecial(TokenDescriptor tokenDesc) {
		return specialsTokenDescs.containsKey(tokenDesc);
	}
	
	static IToken createFrom(TokenDescriptor tokenDesc) {
		return specialsTokenDescs.get(tokenDesc);
	}
}
