package com.excelsior.xds.ui.editor.modula.contentassist2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.excelsior.xds.parser.commons.ast.TokenType;
import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenSets;
import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenTypes;
import com.excelsior.xds.parser.modula.ast.tokens.PragmaTokenType;
import com.excelsior.xds.parser.modula.scanner.jflex._XdsFlexScanner;

class ContentAssistUtils {
	private final static Predicate<TokenType> skipTokensPredicate = t -> isWhitespaceCommentOrPragma(t);
	
	static List<Token> tokenize(String text) {
		List<Token> tokens = new ArrayList<>();
		
		try {
			_XdsFlexScanner input = new _XdsFlexScanner(); 
			input.reset(text);
			while (true) {
			    TokenType t = input.nextToken();
			    if (ModulaTokenTypes.EOF == t) {
			        break;
			    }
			    tokens.add(new Token(t, input.getTokenOffset(), input.yylength()));
			}
		} catch (IOException e) {
			// will never happen
		}
		
		return tokens;
	}
	
	static boolean isWhitespaceCommentOrPragma(Token t) {
		return isWhitespaceCommentOrPragma(tokenType(t));
	}
	
	static boolean isWhitespaceCommentOrPragma(TokenType tt) {
		return ModulaTokenSets.WHITE_SPACE_AND_COMMENT_SET.contains(tt) || tt instanceof PragmaTokenType;
	}
	
	static TokenType tokenType(Token t) {
		return t != null? t.tokenType : null;
	}
	
	static boolean isBetween(List<Token> tokens, TokenType expectedBefore, TokenType expectedAfter, int offset) {
		return isBetween(tokens, expectedBefore, expectedAfter, offset, skipTokensPredicate);
	}
	
	static boolean isBetween(List<Token> tokens, TokenType expectedBefore, TokenType expectedAfter, int offset, Predicate<TokenType> skipTokensPredicate) {
		int i = 0; 
		for (;i < tokens.size(); i++) {
			Token t = tokens.get(i);
			if (t.contains(offset)) {
				break;
			}
		}
		
		return false;
	}
	
	static boolean isBefore(List<Token> tokens, int offset, TokenType expectedBefore) {
		return isBefore(tokens, offset, expectedBefore, skipTokensPredicate);
	}
	
	static boolean isBefore(List<Token> tokens, int offset, TokenType expectedBefore, Predicate<TokenType> skipTokensPredicate ) {
		return false;
	}
	
	static boolean isAfter(List<Token> tokens, int offset, TokenType expectedBefore) {
		return isAfter(tokens, offset, expectedBefore, skipTokensPredicate);
	}
	
	static boolean isAfter(List<Token> tokens, int offset, TokenType expectedAfter, Predicate<TokenType> skipTokensPredicate ) {
		return false;
	}
	
	static Token prevToken(List<Token> tokens, int offset) {
		return prevToken(tokens, offset, skipTokensPredicate);
	}
	
	static Token prevToken(List<Token> tokens, int offset, Predicate<TokenType> skipTokensPredicate) {
		if (offset < 0) {
			return null;
		}
		int i = 0;
		Token token = null;
		for (; i < tokens.size(); i++) {
			token = tokens.get(i);
			if (token.contains(offset)) {
				break;
			}
		}
		if (i != tokens.size()) {
			--i;
			while(i > -1 && skipTokensPredicate.test(tokens.get(i).tokenType)) {
				--i;
			}
			
			if (i > -1) {
				return tokens.get(i); 
			}
		}
		
		return null;
	}
	
	static Token nextToken(List<Token> tokens, int offset) {
		return nextToken(tokens, offset, skipTokensPredicate);
	}
	
	static Token nextToken(List<Token> tokens, int offset, Predicate<TokenType> skipTokensPredicate) {
		if (offset < 0) {
			return null;
		}
		int i = 0;
		Token token = null;
		for (; i < tokens.size(); i++) {
			token = tokens.get(i);
			if (token.contains(offset)) {
				break;
			}
		}
		if (i != tokens.size()) {
			++i;
			while(i < tokens.size() && skipTokensPredicate.test(tokens.get(i).tokenType)) {
				++i;
			}
			
			if (i < tokens.size()) {
				return tokens.get(i); 
			}
		}
		
		return null;
	}
	
	static String region(String s, Token t) {
		if (t == null) {
			return null;
		}
		return s.substring(t.offset, t.offset + t.length);
	}
}
