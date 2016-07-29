package com.excelsior.xds.parser.modula;

import java.io.IOException;
import java.util.Set;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;

import com.excelsior.xds.core.sdk.XdsOptions;
import com.excelsior.xds.core.text.TextPosition;
import com.excelsior.xds.parser.commons.IParserEventListener;
import com.excelsior.xds.parser.commons.ast.TokenType;
import com.excelsior.xds.parser.internal.modula.nls.XdsMessages;
import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenSets;
import com.excelsior.xds.parser.modula.ast.tokens.PragmaTokenTypes;
import com.excelsior.xds.parser.modula.scanner.jflex._XdsFlexScanner;

/**
 * The first stage of the source code parsing. It interacts with tokens' scanner,
 * converts tokens according to the build settings and processes reports of errors.
 */
public class XdsTokenParser extends    XdsParserState
                            implements ModulaTokenSets, PragmaTokenTypes 
{
    /** The token of the matched text region */
    protected TokenType token;
    
    /** Cached text representation of the current token */
    private String text;

    /** Low-level handler of a matched tokens */
    private TokenListener tokenListener;
    
    /** Input source text */
    private final _XdsFlexScanner input;


    public XdsTokenParser( IFileStore sourceFile, CharSequence chars
                         , XdsSettings settings
                         , IParserEventListener reporter )
    {
        super(sourceFile, chars, settings, reporter);

        tokenListener = new DefaultTokenListener();
        
        input = new _XdsFlexScanner(); 
        input.reset(chars);
    }

    /** 
     * Set the given listener for token match events.
     *  
     * @param tokenHandler the listener of new tokens
     */
    protected void setTokenListener(TokenListener tokenHandler) {
        this.tokenListener = tokenHandler;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        super.reset();
        token = null;
        input.reset(chars);
    }
    
    /**
     * Returns the token of the last matched text region.
     * 
     * @return the last matched token.
     */
    protected TokenType getToken() {
        return token;
    }

    
    /**
     * Returns the number of characters up to the start of the matched text.
     */
    protected int getTokenOffset() {
        return input.getTokenOffset();
    }
    
    /**
     * Returns the length of the matched text region.
     */
    protected int getTokenLength() {
        return input.yylength();
    }
    
    /**
     * Returns the position of the last matched token.
     * 
     * @return position of the last matched token.
     */
    protected TextPosition getTokenPosition() {
        return input.getTokenPosition();
    }

    
    /**
     * Returns the processed text of the last matched token.
     * 
     * @return the processed text of the last matched token.
     */
    protected String getTokenText() {
        if (text == null) {
            CharSequence tokenText = input.yytext();

            if (token == STRING_LITERAL) 
            {
                // text of the string literal without quotes
                text = tokenText.subSequence(1, tokenText.length() - 1).toString();
                
            } 
            else if ( (OCT_INTEGER_LITERAL  == token)
                   || (HEX_INTEGER_LITERAL  == token)
                   || (COMPLEX_LITERAL      == token)
                   || (LONG_COMPLEX_LITERAL == token)
                   || (CHAR_HEX_LITERAL     == token) )
            {
                // text without last character
                text = tokenText.subSequence(0, tokenText.length() - 1).toString();
                
            } 
            else if (token == CHAR_OCT_LITERAL) 
            {
                // the character by its octal ASCII code
                tokenText = tokenText.subSequence(0, tokenText.length() - 1);
                int charCode = Integer.parseInt(tokenText.toString(), 8);
                text = Character.toString((char)charCode);
                
            } 
            else {
                text = tokenText.toString();
            }
        }
        return text;
    }
    
    
    protected long getTokenIntegerValue() throws NumberFormatException {
        if (DEC_INTEGER_LITERAL == token) 
        {
            return Long.parseLong(getTokenText(), 10);
            
        } 
        else if (OCT_INTEGER_LITERAL == token) 
        {
            return Long.parseLong(getTokenText(), 8);
            
        } 
        else if ( (HEX_INTEGER_LITERAL == token) 
               || (CHAR_HEX_LITERAL    == token) ) 
        {
            return Long.parseLong(getTokenText(), 16);
            
        } 
        else {
            throw new NumberFormatException("Wrong number format");
        }
    }
    

    
    /**
     * Resumes scanning until the next token is matched,
     * the end of input is encountered or an I/O-Error occurs.
     * 
     * @return the next token in the input char sequence.
     * @throws CoreException 
     */
    protected TokenType nextToken() {
        text = null;
        try {
            token = input.nextToken();
        } catch (IOException e) {
            logInternalError(e);
            return EOF;
        }

        if (WHITE_SPACE != token) {
            if (END_OF_LINE_COMMENT == token) 
            {            
                if (settings.getOption(XdsOptions.CPPCOMMENTS))
                    error(XdsMessages.ExtensionNotAllowed, XdsMessages.UseCppLineComments);
                else if (!settings.xdsExtensions())
                    error(XdsMessages.ExtensionNotAllowed, "");    //$NON-NLS-1$
            
            }
            else if (BLOCK_COMMENT == token) 
            {            
                if (settings.getOption(XdsOptions.CPPCOMMENTS))
                    error(XdsMessages.ExtensionNotAllowed, XdsMessages.UseCppBlockComments);

            }
            else if ( (CPP_BLOCK_COMMENT == token)
                   || (CPP_END_OF_LINE_COMMENT == token) ) 
            {            
                if (!settings.getOption(XdsOptions.CPPCOMMENTS))
                    error(XdsMessages.ExtensionNotAllowed, XdsMessages.UseOptionCppComments);

            }
            else if (OCT_INTEGER_LITERAL == token) 
            {            
                if (settings.isOberon() && !settings.xdsExtensions())
                    error(XdsMessages.IllegalNumber);
                
            }
            else if ( (LONG_REAL_LITERAL    == token)            
                   || (COMPLEX_LITERAL      == token)           
                   || (LONG_COMPLEX_LITERAL == token) )
            {            
                if (!settings.isOberon())
                    error(XdsMessages.IllegalNumber);
                
            }
            else if (CHAR_HEX_LITERAL == token) 
            {            
                if (!settings.isOberon() && !settings.xdsExtensions())
                    error(XdsMessages.IllegalNumber);
                            
            }
            else if (CHAR_OCT_LITERAL == token) 
            {            
                if (settings.isOberon() && !settings.xdsExtensions())
                    error(XdsMessages.IllegalNumber);
                            
            }
            else if ( (LEFT_SHIFT  == token)            
                   || (RIGHT_SHIFT == token) ) 
            {            
                if (!settings.xdsExtensions() && !settings.topSpeedExtensions())
                    error(XdsMessages.ExtensionNotAllowed, "");    //$NON-NLS-1$);
                            
            }
            else if (ALIAS == token) 
            {            
                if (!settings.topSpeedExtensions())
                    error(XdsMessages.ExtensionNotAllowed, "");    //$NON-NLS-1$ 
                
            }
            else if (EXPONENT == token) 
            {            
                if (  (!settings.isOberon() && !settings.xdsExtensions()) 
                   || ( settings.isOberon() && !settings.isOberonScientificExtensions())
                   )
                    error(XdsMessages.ExtensionNotAllowed, XdsMessages.ExponentiationOperator);
                
            }
            else if ( (PRAGMA_BEGIN == token)            
                   || (PRAGMA_POP   == token)            
                   || (PRAGMA_PUSH  == token) ) 
            {            
                if (!settings.isIsoPragma())
                    error(XdsMessages.ExtensionNotAllowed, XdsMessages.IsoPragmaSyntax);
                
            }
            else if (AND_KEYWORD == token) 
            {            
                if (settings.isOberon() && !settings.xdsExtensions())
                    token = IDENTIFIER;
                else
                    token = AND;
                
            }
            else if (ASM_KEYWORD == token) 
            {            
                if (!settings.xdsExtensions())
                    token = IDENTIFIER;
                
            }
            else if (NON_OBERON_KEYWORD_SET.contains(token))            
            {
                if (settings.sourceType == XdsSourceType.Oberon)
                    token = IDENTIFIER;
                
            }
            else if ( (EXCEPT_KEYWORD  == token)
                   || (FINALLY_KEYWORD == token) 
                   || (FROM_KEYWORD    == token) 
                   || (RETRY_KEYWORD   == token) ) 
            {            
                if (settings.isOberon() && !settings.getOption(XdsOptions.O2ADDKWD))
                    token = IDENTIFIER;
                            
            }
            else if ( (GOTO_KEYWORD == token) 
                   || (LABEL_KEYWORD == token) ) 
            {            
                if ((settings.isOberon()) || !settings.isGotoExtension())
                    token = IDENTIFIER;
                            
            }
            else if (IS_KEYWORD == token) 
            {            
                if (!settings.isOberon())
                    token = IDENTIFIER;
                
            }
            else if (SET_KEYWORD == token) 
            {            
                if (settings.isOberon())
                    token = IDENTIFIER;
                
            }
            else if (NOT_KEYWORD == token) 
            {            
                if (settings.isOberon() && !settings.xdsExtensions())
                    token = IDENTIFIER;
                else
                    token = NOT;
                
            }
            else if (PACKEDSET_KEYWORD == token) 
            {            
                if (settings.isOberon() && !settings.xdsExtensions())
                    token = IDENTIFIER;
                            
            }
            else if (SEQ_KEYWORD == token) 
            {            
                if (!settings.xdsExtensions())
                    token = IDENTIFIER;
            }
        }
        
        tokenListener.addToken(token, getTokenOffset(), getTokenLength());
        
        return token; 
    }
    
    
    protected TokenType skipToToken (TokenType expectedToken) {
        while ((token != expectedToken) && (token != EOF)) {
            token = nextToken();
        } 
        return token;
    }

    
    protected TokenType skipToToken (Set<TokenType> expectedTokens) {
        while (!expectedTokens.contains(token) && (token != EOF)) {
            token = nextToken();
        }
        return token;
    }
    
    
    /**
     * Reports the specified warning for the current position of source code.
     * 
     * @param message text of the warning message
     * @param arguments arguments of the given warning message
     * @throws CoreException 
     */
    protected void warning(String message, Object... arguments) {
        warning(getTokenPosition(), getTokenLength(), message, arguments);
    }
    
    /**
     * Reports the specified warning for the given position in the source code.
     * 
     * @param position position of the warning
     * @param length length of invalid area 
     * @param message text of the warning message
     * @param arguments arguments of the given warning message
     * @throws CoreException 
     */
    protected void warning(TextPosition position, int length, String message, Object... arguments) {
        reporter.warning(sourceFile, chars, position, length, message, arguments);
    }
    
    
    /**
     * Reports the specified error for the current position of source code.
     * It is assumed the invalid area ends in the position of the current token.  
     * 
     * @param message text of the error message
     * @param arguments arguments of the given error message
     * @throws CoreException 
     */
    protected void error(String message, Object... arguments) {
        error(getTokenPosition(), getTokenLength(), message, arguments);
    }
    
    /**
     * Reports the specified error for the given position in the source code.
     * 
     * @param position position of the error
     * @param message text of the error message
     * @param arguments arguments of the given error message
     * @throws CoreException 
     */
    protected void error(TextPosition position, String message, Object... arguments) {
        int length = getTokenPosition().getOffset() - position.getOffset(); 
        error(position, length, message, arguments);
    }
    
    /**
     * Reports the specified error for the given position and length in the source code.
     * 
     * @param position position of the error
     * @param length length of invalid area 
     * @param message text of the error message
     * @param arguments arguments of the given error message
     * @throws CoreException 
     */
    protected void error(TextPosition position, int length, String message, Object... arguments) {
        reporter.error(sourceFile, chars, position, length, message, arguments);
    }
    
    
    /**
     * Reports the expected symbol for the given token.
     * 
     * @param token expected token
     * @throws CoreException 
     */
    protected void errorExpectedSymbol (TokenType token) {
        error(XdsMessages.ExpectedSymbol, token.getDesignator());
    }

    
    /**
     * Log the specified internal error of parser and scanner.
     * 
     * @param exception, a low-level exception.
     */
    protected void logInternalError(Throwable exception) {
        reporter.logInternalError(sourceFile, exception);
    }

    /**
     * Log the specified internal error of parser and scanner.
     * 
     * @param message, a human-readable message, localized to the
     *           current locale.
     */
    protected void logInternalError(String message) {
        reporter.logInternalError(sourceFile, message);
    }

    /**
     * Log the specified internal error of parser and scanner.
     * 
     * @param message, a human-readable message, localized to the
     *           current locale.
     * @param exception, a low-level exception, or <code>null</code>
     *           if not applicable.
     */
    protected void logInternalError(String message, Throwable exception) {
        reporter.logInternalError(sourceFile, message, exception);
    }
    
    /** 
     * A token listener is notified of a new token is matched. 
     */
    protected interface TokenListener {
        public void addToken(TokenType token, int offset, int length);
    }

    protected class DefaultTokenListener implements TokenListener {
        public void addToken(TokenType token, int offset, int length) {
            builder.addToken(token, offset, length);
        }
    }
    
    protected static class NullTokenListener implements TokenListener {
        public static final NullTokenListener INSTANCE = new NullTokenListener(); 

        private NullTokenListener() { 
        }
        public void addToken(TokenType token, int offset, int length) {
            // do nothing
        }
    }
    
}
