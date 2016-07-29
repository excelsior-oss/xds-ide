package com.excelsior.xds.parser.modula;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.eclipse.core.filesystem.IFileStore;

import com.excelsior.xds.core.text.ITextRegion;
import com.excelsior.xds.core.text.TextPosition;
import com.excelsior.xds.core.text.TextRegion;
import com.excelsior.xds.parser.commons.IParserEventListener;
import com.excelsior.xds.parser.commons.NullParseEventReporter;
import com.excelsior.xds.parser.commons.ast.IAstFrameChild;
import com.excelsior.xds.parser.commons.ast.IAstFrameNode;
import com.excelsior.xds.parser.commons.ast.TokenType;
import com.excelsior.xds.parser.commons.pst.PstCompositeNode;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.internal.modula.nls.XdsMessages;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;
import com.excelsior.xds.parser.modula.ast.pragmas.AstInactiveCode;
import com.excelsior.xds.parser.modula.ast.pragmas.AstPragma;
import com.excelsior.xds.parser.modula.ast.tokens.PragmaTokenTypes;

/**
 * Parser of XDS Modula-2/Oberon-2 source code directives  <* ... *>.
 *
 * <p>
 * Source code directives (or pragmas) are used to set compilation options in the source text and 
 * to select specific pieces of the source text to be compiled (conditional compilation).  
 * The ISO Modula-2 standard does not describe pragma syntax. XDS supports source code 
 * directives in both Modula-2 and Oberon-2. The syntax described in The Oakwood Guidelines 
 * for the Oberon-2 Compiler Developers is used.
 * </p>
 * 
 * <p>
 * NOTE: pragma nodes are rearranged by (<code>AstBuilder.endProduction(CompositeType<T>)</code>.
 * </p>
 */
public class XdsPragmaParser extends    XdsCommentParser 
                             implements PragmaTokenTypes, ModulaElementTypes 
{
    /** The list of text locations ignored by the conditional compilation pragmas */
    private final List<ITextRegion> inactiveCodeRegions = new ArrayList<ITextRegion>(8); 

    private ConditionalCompilationState ccState;
    private final Stack<ConditionalCompilationState> ccStack = new Stack<ConditionalCompilationState>();
    
    private PragmaExpressionParser pragmaExpressionParser;

    private boolean modeScan;
    
    private TextPosition pragmaBeginPosition;
    
    
    public XdsPragmaParser( IFileStore sourceFile, CharSequence chars
                          , XdsSettings settings
                          , IParserEventListener reporter )
    {
        super(sourceFile, chars, settings, reporter);
        pragmaExpressionParser = new PragmaExpressionParser();
        modeScan = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        super.reset();
        modeScan = true;
        ccState  = null;
        ccStack.clear();
        pragmaBeginPosition = null;
    }
    
    /**
     * Returns the next non-pragma token in the input char sequence.
     * 
     * @return the next token
     */
    @Override
    protected TokenType nextToken() {
        while (true) {
            super.nextToken();
        
            if (PRAGMA_BEGIN == token) {
                pragmaBeginPosition = getTokenPosition();
                parsePragmaBlock(false);
            }
            else if (PRAGMA_POP == token) {
                builder.beginProduction(PRAGMA_INLINE_SETTINGS);
                popSettings();
                endPragmaProduction(PRAGMA_INLINE_SETTINGS);
            }
            else if (PRAGMA_PUSH == token) {
                builder.beginProduction(PRAGMA_INLINE_SETTINGS);
                pushSettings();
                endPragmaProduction(PRAGMA_INLINE_SETTINGS);
            }
            else if (PRAGMA_ARGS_BEGIN  == token) {
                parsePragmaArgs();
            }
            else if (PRAGMA_ARGS_POP_BEGIN == token) {
                popSettings();
                parsePragmaArgs();
            }
            else if (PRAGMA_ARGS_PUSH_BEGIN == token) {
                pushSettings();
                parsePragmaArgs();
            }
            else if (PRAGMA_ARGS_POPPUSH_BEGIN == token) {
                popSettings();
                pushSettings();
                parsePragmaArgs();
            }
            else if (EOF == token) {
                return token;
            }
            else if (BAD_CHARACTER == token) {
                error(XdsMessages.IllegalCharacter);
            }
            else {
                return token;
            }
        }
    }

    
    /**
     * Checks consistency of conditional compilation state.
     */
    public void checkConsistency() {
        if (ccState != null) {
            error( XdsMessages.IllegalConditionCompilationAtPosition
                 , NullParseEventReporter.positionToString(ccState.position) );
            ccState = null;
        }
    }

    
    /**
     * Returns the list of text locations ignored by the conditional compilation pragmas.
     *
     * @return list of text locations ignored by the conditional compilation pragmas.
     */
    public List<ITextRegion> getInactiveCodeRegions() {
        return inactiveCodeRegions; 
    }
    
    
    protected void onInactiveBlock(AstInactiveCode inactiveCodeAst) {
        if (inactiveCodeAst != null) {
            ITextRegion textRegion = new TextRegion (
                inactiveCodeAst.getOffset(), inactiveCodeAst.getLength() 
            );
            inactiveCodeRegions.add(textRegion);
        }
    }
    
    /**
     * "<*$" ["<" | ">" | "|"] ... "*>"
     *                         ^ start from this position
     */
    private void parsePragmaArgs() {
        builder.beginProduction(PRAGMA_INLINE_SETTINGS);
        do {
            nextPragmaToken();
            if (token == IDENTIFIER) {
                String optionName = getTokenText();
                nextPragmaToken();

                if (PLUS == token) {
                    setOption(optionName, true);
                }
                else if (MINUS == token) {
                    setOption(optionName, false);
                }
                else {
                    errorInvalidPragmaSyntax();
                    break;
                }
            } 
            else {
                break;
            }
        } while (true);
            
        if (token != PRAGMA_END) {
            errorInvalidPragmaSyntax();
            skipToToken(PRAGMA_END);
        }
        endPragmaProduction(PRAGMA_INLINE_SETTINGS);
    }
    
    
    /**
     * "<*" ... "*>"
     *      ^ start from this position
     */
    protected boolean parsePragmaBlock(boolean insedIgnoredText) {
        boolean isSkipTokenRequired   = false;
        boolean endConditionStatement = false;
        
        AstPragma pragmaAst = builder.beginProduction(PRAGMA);
        nextPragmaToken();
        
        if ((PLUS == token) || (MINUS == token))
        {
            pragmaAst = builder.changeProduction(PRAGMA_INLINE_SETTINGS);
            boolean enabled = (token == PLUS); 
            nextPragmaToken();
            if (token == IDENTIFIER) {
                setOption(getTokenText(), enabled);
            } else {
                error(XdsMessages.IdentifierExpected);
            }
            nextPragmaToken();
        }
        else if (IF_KEYWORD == token)
        {
            pragmaAst = builder.changeProduction(PRAGMA_IF_STATEMENT);
            parseConditionStatement(true);
            isSkipTokenRequired = true;
        }
        else if (ELSIF_KEYWORD == token)
        {
            pragmaAst = builder.changeProduction(PRAGMA_ELSIF_STATEMENT);
            setFrameNode();
            if (insedIgnoredText) {
                endParentProduction_INACTIVE_CODE();
            }
            parseConditionStatement(false);
            isSkipTokenRequired   = true;
            endConditionStatement = true;
        }
        else if (ELSE_KEYWORD == token)
        { 
            pragmaAst = builder.changeProduction(PRAGMA_ELSE_STATEMENT);
            setFrameNode();
            addFrameNode(ELSE_KEYWORD);
            if (insedIgnoredText) {
                endParentProduction_INACTIVE_CODE();
            }
            processPragmaStatementELSE();
            nextPragmaToken();
            isSkipTokenRequired   = true;
            endConditionStatement = true;
        }
        else if (END_KEYWORD == token)
        {
            pragmaAst = builder.changeProduction(PRAGMA_END_STATEMENT);
            setFrameNode();
            addFrameNode(END_KEYWORD);
            if (insedIgnoredText) {
                endParentProduction_INACTIVE_CODE();
            }
            processPragmaStatementEND();
            nextPragmaToken();
            endConditionStatement = true;
        }
        else if (POP_PRAGMA_KEYWORD == token) 
        {
            pragmaAst = builder.changeProduction(PRAGMA_INLINE_SETTINGS);
            popSettings();
            nextPragmaToken();
        }
        else if (PUSH_PRAGMA_KEYWORD == token) 
        {
            pragmaAst = builder.changeProduction(PRAGMA_INLINE_SETTINGS);
            pushSettings();
            nextPragmaToken();
        }
        else if (NEW_PRAGMA_KEYWORD == token)
        {
            // [ NEW ] identifier [ "+" | "-" | "=" string ]    
            pragmaAst = builder.changeProduction(PRAGMA_INLINE_SETTINGS);
            nextPragmaToken();
            if (token == IDENTIFIER) {
                parseNewStyleInlineSetting(getTokenText(), true);
            } else {
                error(XdsMessages.IdentifierExpected);
            }
        }
        else if (IDENTIFIER == token)
        {
            // identifier [ "+" | "-" | "=" string ]    
            pragmaAst = builder.changeProduction(PRAGMA_INLINE_SETTINGS);
            parseNewStyleInlineSetting(getTokenText(), false);
        }
        else {
            error(XdsMessages.IdentifierExpected);
        }

        if (token != PRAGMA_END) {
            errorInvalidPragmaSyntax();
            skipToToken(PRAGMA_END);
        }
        
        if (isSkipTokenRequired) {
            skipTokens();
        }

        if (pragmaAst != null) {
            builder.acceptLastToken();
            builder.endProduction(pragmaAst);
        }
        return endConditionStatement;
    }


    /**
     * The syntax of the conditional compilation IF statement follows: 
     * 
     *   IfStatement      = <* IF Expression THEN *> text
     *                    { <* ELSIF Expression THEN *> text }
     *                    [ <* ELSE *> text ]
     *                      <* END *>
     *   Expression       = SimpleExpression
     *                    [ ("=" | "#") SimpleExpression].
     *   SimpleExpression = Term { "OR" Term}.
     *   Term             = Factor { "&" Factor}.
     *   Factor           = Ident | string |
     *                      "DEFINED" "(" Ident ")" |
     *                      "(" Expression ")" |
     *                      "~" Factor | "NOT" Factor.
     *   Ident            = option | equation.
     * 
     */
    private void parseConditionStatement(boolean newLevel) {
        if (newLevel) {
            if (ccState != null) {
                ccStack.push(ccState);
            }
            ccState = new ConditionalCompilationState(modeScan, builder.getCurrentProduction());
            addFrameNode(IF_KEYWORD);
        } 
        else if (ccState == null) {
            error(XdsMessages.IllegalConditionCompilation);
            ccState = new ConditionalCompilationState(modeScan, null);
            ccState.setClause(ELSIF_KEYWORD);
        }
        else {
            if ((ccState.clause != IF_KEYWORD) && (ccState.clause != ELSIF_KEYWORD)) {
                error( XdsMessages.IllegalConditionCompilationAtPosition
                     , NullParseEventReporter.positionToString(ccState.position) );
            }
            else {
                ccState.setClause(ELSIF_KEYWORD);
                if (modeScan && ccState.isActive) {
                    ccState.isActive = false;
                }
                addFrameNode(ELSIF_KEYWORD);
            }
            
            modeScan = ccState.modeScan;
        }

        nextPragmaToken();
        
        pragmaExpressionParser.parseExpression();
        boolean value = pragmaExpressionParser.getBooleanValue();
        
        if (token == THEN_KEYWORD) {
            addFrameNode(THEN_KEYWORD);
        }
        nextPragmaToken(THEN_KEYWORD);
        
        modeScan = value && (ccState != null) && ccState.isActive;
    }
    
    
    protected void processPragmaStatementELSE() {
        if (ccState == null) {
            error(XdsMessages.IllegalConditionCompilation);
        }
        else if ((ccState.clause != IF_KEYWORD) && (ccState.clause != ELSIF_KEYWORD)) {
            error( XdsMessages.IllegalConditionCompilationAtPosition
                 , NullParseEventReporter.positionToString(ccState.position) );
        }
        else {
            ccState.setClause(ELSE_KEYWORD);
            modeScan = ! modeScan && ccState.isActive;
        }
    }

    protected void processPragmaStatementEND() {
        if (ccState != null) {
            modeScan = ccState.modeScan;
            ccState = ccStack.isEmpty() ? null : ccStack.pop(); 
        }
        else {
            error(XdsMessages.IllegalConditionCompilation);
        }
    }

       
    
    /**
     * Parses an inline options and equations which is defined in the NewStyle. 
     * NewStyle is proposed as the Oakwood standard for Oberon-2.
     * 
     * XDS allows options to be changed in the source text by using standard 
     * ISO pseudo comments <* ... *>.
     *
     * The format of an inline option or equation setting is described by the following syntax: 
     *
     * Pragma     = "<*" PragmaBody "*>"
     * PragmaBody = PUSH | POP | NewStyle | OldStyle
     * NewStyle   = [ NEW ] name [ "+" | "-" | "=" string ]
     * OldStyle   = ("+" | "-") name
     *
     * @param name the name of inline option and equation. 
     * @param create option and equation is defined as NEW
     */
    private void parseNewStyleInlineSetting(String name, boolean create) {
        nextPragmaToken();
        if ((PLUS == token) || (MINUS == token))
        {
            boolean enabled = (token == PLUS);
            if (create) {
                addOption(name, enabled);
            }
            else { 
                setOption(name, enabled);
            }
            nextPragmaToken();
        }
        else if (EQU == token)
        {
            nextPragmaToken();
            if (STRING_LITERAL_SET.contains(token)) {
                if (create) {
                    addEquation(name, getTokenText());
                }
                else { 
                    setEquation(name, getTokenText());
                }
                nextPragmaToken();
            } 
            else { 
                error(XdsMessages.StringExpected);
            }
        }
        else 
        {
            if (create) {
                addOption(name, false);
            } else if (token == PRAGMA_END) {
                errorInvalidPragmaSyntax();
            }
        }
    }

    
    private void setOption(String name, boolean enabled) {
        if (modeScan) {
            if (! settings.isOptionDefined(name)) {
                warning(XdsMessages.UnknownOption, name);
            }
            settings.addOption(name, enabled);
        }
    }

    private void addOption(String name, boolean enabled) {
        if (modeScan) {
            if (settings.isOptionDefined(name)) {
                warning(XdsMessages.OptionAlreadyDefined, name);
            }
            settings.addOption(name, enabled);
        }
    }

    private void setEquation(String name, String value) {
        if (modeScan) {
            if (! settings.isEquationDefined(name)) {
                warning(XdsMessages.UnknownEquation, name);
            }
            settings.addEquation(name, value);
        }
    }

    private void addEquation(String name, String value) {
        if (modeScan) {
            if (settings.isEquationDefined(name)) {
                warning(XdsMessages.EquationAlreadyDefined, name);
            }
            settings.addEquation(name, value);
        }
    }

    private boolean isDefined(String name) {
        return settings.isOptionDefined(name)
            || settings.isEquationDefined(name);
    }
    
    
    private void popSettings() {
        if (modeScan) {
            settings.popSettings();
        }
    }

    private void pushSettings() {
        if (modeScan) {
            settings.pushSettings();
        }
    }

        
    private TokenType nextPragmaToken() {
        while (true) {
            super.nextToken();

            if (BAD_CHARACTER == token) {
                error(XdsMessages.IllegalCharacter);
            }
            else {
                return token;
            }
        }
    }

    private TokenType nextPragmaToken(TokenType expectedToken) {
        if (token == expectedToken)
            return nextPragmaToken();
        else
            errorExpectedSymbol(expectedToken);
        return token;
    }
    
    
    private void skipTokens() {
        if (!modeScan) {
            AstInactiveCode ignoredTextAst = beginProduction_INACTIVE_CODE();
            
            while (!modeScan) {
                super.nextToken();

                if (PRAGMA_BEGIN == token) 
                {
                    pragmaBeginPosition = getTokenPosition();
                    if (parsePragmaBlock(true)) {
                        break;
                    }
                }
                else if (EOF == token) 
                {
                    if (ignoredTextAst != null) {
                        builder.endProduction(INACTIVE_CODE);
                        onInactiveBlock(ignoredTextAst);
                    }
                    break;
                }
            }
        }
    }
    
    
    private AstInactiveCode beginProduction_INACTIVE_CODE() {
        AstInactiveCode ignoredTextAst = null;
        if (ccState != null && ccState.modeScan) {
            builder.acceptLastToken();
            ignoredTextAst = builder.beginProduction(INACTIVE_CODE);
        }
        return ignoredTextAst;
    }
    
    private void endParentProduction_INACTIVE_CODE() {
        if (ccState != null && ccState.modeScan) {
            AstInactiveCode ignoredTextAst = builder.endParentProduction(INACTIVE_CODE);
            onInactiveBlock(ignoredTextAst);
        }
    }
    
    private <T extends AstPragma> void endPragmaProduction(ModulaCompositeType<T> pragma) 
    {
        builder.acceptLastToken();
        builder.endProduction(pragma);
    }
    
    
    private final class ConditionalCompilationState {
        boolean modeScan;
        boolean isActive;

        TokenType clause;
        TextPosition position;
        PstCompositeNode ifNode;
        
        ConditionalCompilationState (boolean modeScan, PstCompositeNode ifNode) {
            this.modeScan = modeScan;
            this.ifNode = ifNode;
            this.isActive = modeScan;
            clause   = IF_KEYWORD;
            position = getTokenPosition();
        }
        
        void setClause(TokenType newClause) {
            clause   = newClause;
            position = getTokenPosition();
        }
    }

    
    /**
     * Parser of XDS pragma expression statement.
     */
    private final class PragmaExpressionParser {
        
        private String value;

        public boolean getBooleanValue() {
            return toBoolean(value, true);
        }
        
        public void parseExpression() {
            parseExpression(true);
        }
     
        /**
         *  Expression = SimpleExpression [ ("=" | "#") SimpleExpression ].
         */
        private void parseExpression(boolean resolve) {
            parseSimpleExpression(resolve);
            String leftValue = value;
            if ( (EQU == token) 
              || (NEQ == token) )
            {
                boolean not = (token == NEQ);
                nextPragmaToken();
                parseSimpleExpression(resolve);
                String rightValue = value;
                value = Boolean.toString(not != leftValue.equals(rightValue));
            }
        }
        
        
        /**
         *   SimpleExpression = Term { "OR" Term}.
         */
        private void parseSimpleExpression(boolean resolve) {
            parsTerm(resolve);
            String leftValue = value;
            while (token == OR_KEYWORD) {
                nextPragmaToken();
                boolean res = toBoolean(leftValue, resolve);
                parsTerm(!res);
                String rightValue = value;
                leftValue = Boolean.toString(res || toBoolean(rightValue, resolve));
            }
            value = leftValue;
        }
        
        /**
         *  Term = Factor { "&" Factor}.
         */
        private void parsTerm(boolean resolve) {
            parseFactor(resolve);
            String leftValue = value;
            while (token == AND) {
                nextPragmaToken();
                boolean res = toBoolean(leftValue, resolve);
                parseFactor(res);
                String rightValue = value;
                leftValue = Boolean.toString(res && toBoolean(rightValue, resolve));
            }
            value = leftValue;
        }

        /**
         *   Factor = Ident | string |
         *            "DEFINED" "(" Ident ")" |
         *            "(" Expression ")" |
         *            "~" Factor | "NOT" Factor.
         */
        private void parseFactor(boolean resolve) {
            if (LPARENTH == token)
            {
                nextPragmaToken();
                parseExpression(resolve);
                nextPragmaToken(RPARENTH);

            }
            else if ( (NOT == token)
                   || (NOT_KEYWORD == token) )
            {
                nextPragmaToken();
                parseFactor(resolve);
                value = Boolean.toString(! toBoolean(value, true));
                
            }
            else if (DEFINED_PRAGMA_KEYWORD == token)
            {
                nextPragmaToken();
                nextPragmaToken(LPARENTH);
                
                if (token == IDENTIFIER) {
                    value = Boolean.toString(isDefined(getTokenText()));
                    nextPragmaToken();
                }
                else {
                    value = "";
                    error(XdsMessages.IdentifierExpected);
                }
                
                nextPragmaToken(RPARENTH);
                
            }
            else 
            {
                if (STRING_LITERAL_SET.contains(token)) {
                    value = getTokenText();
                    nextPragmaToken();
                }
                else
                    parseIndentifier(resolve);
            }
        }

        /**
         * Ident = option | equation.
         */
        private void parseIndentifier(boolean resolve) {
            value = "";
            if (token == IDENTIFIER) {
                String name = getTokenText();
                if (settings.isOptionDefined(name)) {
                    value = Boolean.toString(settings.getOption(name));
                }
                else if (settings.isEquationDefined(name)){
                    value = settings.getEquation(name);
                    if (value == null)
                        value = "";
                }
                else {
                    value = Boolean.toString(false);
                    if (modeScan && resolve) 
                        warning(XdsMessages.UnknownOption, name);
                }
                nextPragmaToken();
            } 
            else {
                error(XdsMessages.IdentifierExpected);
            }
        }
        
        private boolean toBoolean(String value, boolean resolve) {
            if ("false".equals(value))
                return false;
            if ("true".equals(value))
                return true;
            if (modeScan && resolve)
                error(XdsMessages.IncompatibleTypes);
            return false;
        }
        
    }
        
    private void addFrameNode(TokenType expectedToken) {
        PstNode node = builder.getLastNode();
        if ((node != null) && (node.getElementType() == expectedToken)) {
            if (ccState != null && (ccState.ifNode instanceof IAstFrameNode)) {
                ((IAstFrameNode)(ccState.ifNode)).addFrameNode(node);
            }
        }
    }
    
    private void setFrameNode() {
        PstCompositeNode n = builder.getCurrentProduction();
        if (n instanceof IAstFrameChild) {
            if (ccState != null && (ccState.ifNode instanceof IAstFrameNode)) {
                ((IAstFrameChild)n).setAstFrameNode((IAstFrameNode)(ccState.ifNode));
            }
        }
    }

    
    private void errorInvalidPragmaSyntax() {
        int length = (getTokenPosition().getOffset() - pragmaBeginPosition.getOffset())
                   + getTokenLength(); 
        error(pragmaBeginPosition, length, XdsMessages.InvalidPragmaSyntax);
        
    }

}
