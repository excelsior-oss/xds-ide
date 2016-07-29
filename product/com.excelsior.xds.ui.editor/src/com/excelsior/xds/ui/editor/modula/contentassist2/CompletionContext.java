package com.excelsior.xds.ui.editor.modula.contentassist2;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;

import com.excelsior.xds.parser.commons.pst.PstLeafNode;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.ui.editor.commons.contentassist.BaseCompletionContext;

/**
 * @author lsa80
 *
 */ 
public class CompletionContext extends BaseCompletionContext{
	private IFile editedFile;
	private String contentType;
	private IModuleSymbol moduleSymbol;
	private boolean isDotBeforeCursor;
	private boolean isDottedExpression;
	private PstLeafNode leafNode;
	private IModulaSymbol referencedSymbol;
	private String beforeCursorWordPart;
	private String currentStatement;
	private IRegion statementRegion;
	private String currentLineTail;
	private int replacementOffset;
	private int replacementLength;
	private RegionType regionType;
	private List<Token> tokens;
	
	private Token previousNonSpaceToken;
	private Token nextNonSpaceToken;

	CompletionContext(ITextViewer viewer, int offset) {
		super(viewer, offset);
	}

	boolean isDotBeforeCursor() {
		return isDotBeforeCursor;
	}

	void setDotBeforeCursor(boolean isDotBeforeCursor) {
		this.isDotBeforeCursor = isDotBeforeCursor;
	}

	boolean isDottedExpression() {
		return isDottedExpression;
	}

	void setDottedExpression(boolean isDottedExpression) {
		this.isDottedExpression = isDottedExpression;
	}

	void setLeafNode(PstLeafNode leafNode) {
		this.leafNode = leafNode;
	}

	PstLeafNode getLeafNode() {
		return leafNode;
	}

	String getBeforeCursorWordPart() {
		return beforeCursorWordPart;
	}

	void setBeforeCursorWordPart(String beforeCursorWordPart) {
		this.beforeCursorWordPart = beforeCursorWordPart;
	}
	
	String getCurrentLineTail() {
		return currentLineTail;
	}

	void setCurrentLineTail(String currentLineTail) {
		this.currentLineTail = currentLineTail;
	}
	
	String getCurrentStatement() {
		return currentStatement;
	}

	void setCurrentStatement(String currentStatement) {
		this.currentStatement = currentStatement;
	}

	void setStatementRegion(IRegion statementRegion) {
		this.statementRegion = statementRegion;
	}

	int getReplacementOffset() {
		return replacementOffset;
	}

	void setReplacementOffset(int replacementOffset) {
		this.replacementOffset = replacementOffset;
	}

	int getReplacementLength() {
		return replacementLength;
	}

	void setReplacementLength(int replacementLength) {
		this.replacementLength = replacementLength;
	}

	IModulaSymbol getReferencedSymbol() {
		return referencedSymbol;
	}

	void setReferencedSymbol(IModulaSymbol referencedSymbol) {
		this.referencedSymbol = referencedSymbol;
	}

	RegionType getRegionType() {
		return regionType;
	}

	void setRegionType(RegionType regionType) {
		this.regionType = regionType;
	}

	IFile getEditedFile() {
		return editedFile;
	}

	void setEditedFile(IFile editedFile) {
		this.editedFile = editedFile;
	}

	IModuleSymbol getModuleSymbol() {
		return moduleSymbol;
	}

	void setModuleSymbol(IModuleSymbol moduleSymbol) {
		this.moduleSymbol = moduleSymbol;
	}

	public String getContentType() {
		return contentType;
	}

	void setContentType(String contentType) {
		this.contentType = contentType;
	}

	List<Token> getTokens() {
		return tokens;
	}

	void setTokens(List<Token> tokens) {
		this.tokens = tokens;
	}

	Token getPreviousNonSpaceToken() {
		return previousNonSpaceToken;
	}

	void setPreviousNonSpaceToken(Token previousNonSpaceToken) {
		this.previousNonSpaceToken = previousNonSpaceToken;
	}

	Token getNextNonSpaceToken() {
		return nextNonSpaceToken;
	}

	void setNextNonSpaceToken(Token nextNonSpaceToken) {
		this.nextNonSpaceToken = nextNonSpaceToken;
	}
	
	int offsetInStatement() {
		if (tokens.isEmpty()) {
			return -1;
		}
		return getOffset() - statementRegion.getOffset();
	}
}