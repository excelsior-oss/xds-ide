package com.excelsior.xds.ui.editor.commons.scanner.jflex;

import java.io.IOException;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

import com.excelsior.xds.ui.commons.syntaxcolor.TokenManager;

/**
 * A adapter for JFlex scanner which returns all unmatched characters at once.
 * 
 * NOTE: Adapted JFlex scanner must return <code>null</code> for unmatched characters.  
 *  
 * @author lion
 */
public class FlexAggregateAdapter extends FlexAdapter {
    
    /** The token to be returned by default if no rule fires */
    final protected IToken defaultReturnToken;
    
    private IToken token;
    private int tokenOffset;
    private int tokenLength;

	private TokenManager tokenManager;
    
    public FlexAggregateAdapter (IFlexScanner flex, IToken defaultReturnToken, TokenManager tokenManager) {
        super(flex, tokenManager);
        this.defaultReturnToken = defaultReturnToken;
        this.token = null;
        this.tokenManager = tokenManager;
    }

    @Override
    protected void reset( IDocument document, int offset, int length
                        , int start, int initialState ) 
    {
        super.reset(document, offset, length, start, initialState);
        token = null;
        tokenOffset = tokenLength = 0;
    }

    @Override
    public IToken nextToken() {
        try {
            IToken returnToken;
            if (token == null) {
                returnToken = tokenManager.createFrom(flex.nextToken());
                tokenOffset = rangeOffset + flex.getTokenOffset();
                tokenLength = flex.yylength();
                
                if (returnToken == null) {
                    token = tokenManager.createFrom(flex.nextToken());
                    while (token == null) {
                        tokenLength += flex.yylength();
                        token = tokenManager.createFrom(flex.nextToken());
                    }
                    returnToken = defaultReturnToken;
                }
            } else {
                returnToken = token;
                tokenOffset = rangeOffset + flex.getTokenOffset();
                tokenLength = flex.yylength();
                token = null; 
            }
            return returnToken;
        }
        catch (IOException e) { /*Can't happen*/ 
        }
        return Token.EOF;
    }

    @Override
    public int getTokenOffset() {
        return tokenOffset;
    }

    @Override
    public int getTokenLength() {
        return tokenLength;
    }

}
