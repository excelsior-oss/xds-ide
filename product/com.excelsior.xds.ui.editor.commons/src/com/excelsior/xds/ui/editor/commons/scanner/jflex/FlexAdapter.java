package com.excelsior.xds.ui.editor.commons.scanner.jflex;

import java.io.IOException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.ui.commons.syntaxcolor.TokenDescriptor;
import com.excelsior.xds.ui.commons.syntaxcolor.TokenManager;

/**
 * A generic adapter for JFlex scanner to Eclipse IDocument scanner API.
 *  
 * @author lion
 */
public class FlexAdapter implements ITokenScanner {
    
    final protected IFlexScanner flex;
    
    protected int rangeOffset;

	private TokenManager tokenManager;
    
    
    public FlexAdapter (IFlexScanner flex, TokenManager tokenManager) {
        this.flex = flex;
        this.tokenManager = tokenManager;
    }
    
    public IFlexScanner getFlexScanner() {
    	return flex;
    }

    /**
     * Configures the scanner by providing access to the document range that should
     * be scanned.
     *
     * @param document the document to scan
     * @param offset the offset of the document range to scan
     * @param length the length of the document range to scan
     * @param start  the start offset in the document range to scan
     * @param initialState  initial lexical state
     */
    protected void reset( IDocument document, int offset, int length
                        , int start, int initialState ) 
    {
        Assert.isLegal(document != null);
        checkRange(offset, length, document.getLength());

        rangeOffset = offset;
        try {
            String buffer = document.get(offset, length);
//            System.out.println("=== begin="+offset+", start="+start+", end="+(length-start));
//            char[] source = buffer.toCharArray();
//            for (int i=0;  i< source.length; i++) {
//                System.out.println("  ["+i+"] ch=" + Character.getNumericValue(source[i]) + ",  val="+source[i] );
//            }
//            System.out.println("===");
            flex.reset(buffer, start, length-start, initialState);
        } catch (BadLocationException e) {
        }
    }

    @Override
    public void setRange(IDocument document, int offset, int length) {
        reset(document, offset, length, 0, IFlexScanner.INITIAL_STATE);    
    }
    
    @Override
    public IToken nextToken() {
        try {
            TokenDescriptor tokenDesc = flex.nextToken();
			return tokenManager.createFrom(tokenDesc);
        }
        catch (IOException e) { 
            LogHelper.logError(e); 
        }
        return Token.EOF;
    }

    @Override
    public int getTokenOffset() {
        return rangeOffset + flex.getTokenOffset();
    }

    @Override
    public int getTokenLength() {
        return flex.yylength();
    }

    /**
     * Checks that the given range is valid.
     * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=69292
     *
     * @param offset the offset of the document range to scan
     * @param length the length of the document range to scan
     * @param documentLength the document's length
     */
    private void checkRange(int offset, int length, int documentLength) {
        Assert.isLegal(offset > -1);
        Assert.isLegal(length > -1);
        Assert.isLegal(offset + length <= documentLength);
    }
    
}
