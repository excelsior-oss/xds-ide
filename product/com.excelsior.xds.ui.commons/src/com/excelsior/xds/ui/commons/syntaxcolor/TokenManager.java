package com.excelsior.xds.ui.commons.syntaxcolor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;

import com.excelsior.xds.ui.commons.swt.resources.ResourceRegistry;

/**
 * Creates {@link Token} from tokenDescriptor, and remembers all colors (see {@link Color}) that were
 * created during this operation.  
 * 
 * Caches created tokens, holding Map<TokenDescriptor, Token>. 
 * 
 * Please note that this class should be used from UI thread, because {@link of Display}
 * 
 * @author lsa80
 */
public class TokenManager extends ResourceRegistry{
	private Map<TokenDescriptor, Token>  tokenDescriptor2Token = new HashMap<TokenDescriptor, Token>(); 
	
	public IToken createFrom(TokenDescriptor tokenDescriptor) {
		// if it is descriptor of the Token.WHITESPACE or Token.EOF return it immediately
		if (SpecialTokenDescriptors.isSpecial(tokenDescriptor)) {
			return SpecialTokenDescriptors.createFrom(tokenDescriptor);
		}
		
		// try reuse existing token
		Token token = tokenDescriptor2Token.get(tokenDescriptor);
		if (token == null) {
			TextAttributeDescriptor textAttrDesc = tokenDescriptor.getTextAttribute();
			Color backGroundColor = createColor(textAttrDesc.getBackground());
			Color foreGroundColor = createColor(textAttrDesc.getForeground());
			token = new Token(new TextAttribute(foreGroundColor, backGroundColor, textAttrDesc.getStyle()));
			tokenDescriptor2Token.put(tokenDescriptor, token);
		}
		
		return token;
	}
	
	/**
	 * Clears token cache. All managed resources like colors and fonts will be disposed only when {@link #dispose()} method is called.
	 */
	public void clear(){
		tokenDescriptor2Token.clear();
	}
}
