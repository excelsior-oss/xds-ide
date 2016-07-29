package com.excelsior.xds.ui.editor.modula;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultTextDoubleClickStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.parser.commons.ast.TokenType;
import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenSets;
import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenTypes;
import com.excelsior.xds.parser.modula.scanner.jflex._XdsFlexScanner;

public class ModulaDoubleClickStrategy extends DefaultTextDoubleClickStrategy {
	@Override
	protected IRegion findExtendedDoubleClickSelection(IDocument document,
			int offset) {
		try {
			ITypedRegion region = TextUtilities.getPartition(document, IModulaPartitions.M2_PARTITIONING, offset, true);
			if (region.getType() == IModulaPartitions.M2_CONTENT_TYPE_DEFAULT) {
				_XdsFlexScanner lexer = new _XdsFlexScanner();
				IRegion lineReg = document.getLineInformationOfOffset(offset);
				lexer.reset(document.get(lineReg.getOffset(), lineReg.getLength()));

				List<TokenRegion> candidates = new ArrayList<>();
				TokenType tokenType;
				while ( ( tokenType = lexer.nextToken()) != ModulaTokenTypes.EOF) {
					if (ModulaTokenTypes.WHITE_SPACE.getDesignator().equals(tokenType.getDesignator())) {
						continue;
					}
					int tokenOffset = lineReg.getOffset() + lexer.getTokenOffset();
					if (tokenOffset <= offset && offset <= tokenOffset + lexer.yylength()) {
						candidates.add(new TokenRegion(new Region(tokenOffset, lexer.yylength()), tokenType));
					}
				}
				if (candidates.size() > 0){
					if (candidates.size() > 1){
						Collections.sort(candidates, new TokenRegionComparator());
					}
					return candidates.get(candidates.size() - 1);
				}
			}
		} catch (BadLocationException | IOException e) {
			LogHelper.logError(e);
		}
		return super.findExtendedDoubleClickSelection(document, offset);
	}

	private static final class TokenRegionComparator implements
			Comparator<TokenRegion>, ModulaTokenSets {
		private static final Set<TokenType> PUNCTUATION_SET = new HashSet<>(
				Arrays.asList(PLUS, MINUS, TIMES, COLON, SLASH, BAR, SEP, COMMA, DOT, RANGE, BECOMES, SEMICOLON)); 
		{
			PUNCTUATION_SET.addAll(BRACKETS_SET);
		}
		
		@Override
		public int compare(TokenRegion r1, TokenRegion r2) {
			return weight(r1) - weight(r2);
		}

		int weight(TokenRegion r) {
			TokenType tokenType = r.getTokenType();
			if (PUNCTUATION_SET.contains(tokenType)) {
				return 0;
			}
			else if (KEYWORD_SET.contains(tokenType)) {
				return 1;
			}
			else if (LITERAL_SET.contains(tokenType)) {
				return 2;
			}
			else if (IDENTIFIER.equals(tokenType)) {
				return 3;
			}
			return -1;
		}
	}

	private static class TokenRegion implements IRegion{
		private final IRegion region;
		private final TokenType tokenType;
		
		TokenRegion(IRegion region, TokenType tokenType) {
			this.region = region;
			this.tokenType = tokenType;
		}

		@Override
		public int getLength() {
			return region.getLength();
		}

		@Override
		public int getOffset() {
			return region.getOffset();
		}

		public TokenType getTokenType() {
			return tokenType;
		}
	}
}