package com.excelsior.xds.ui.editor.modula.contentassist2;

import com.excelsior.xds.parser.commons.ast.TokenType;

class Token {
	final TokenType tokenType;
	final int offset;
	final int length;
	
	Token(TokenType tokenType, int offset, int length) {
		this.tokenType = tokenType;
		this.offset = offset;
		this.length = length;
	}
	
	boolean contains(int offset) {
		return this.offset <= offset && offset < this.offset + length;
	}

	@Override
	public String toString() {
		return "Token [tokenType=" + tokenType + ", offset=" + offset
				+ ", length=" + length + "]";
	}
}