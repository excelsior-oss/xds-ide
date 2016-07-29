package com.excelsior.xds.parser.modula;

import java.util.Iterator;

import org.eclipse.core.filesystem.IFileStore;

import com.excelsior.xds.core.sdk.XdsOptions;
import com.excelsior.xds.parser.commons.IParserEventListener;
import com.excelsior.xds.parser.commons.ast.TokenType;
import com.excelsior.xds.parser.internal.modula.nls.XdsMessages;
import com.excelsior.xds.parser.internal.modula.symbol.type.OpenArrayTypeSymbol;
import com.excelsior.xds.parser.modula.ast.AstDesignator;
import com.excelsior.xds.parser.modula.ast.AstModuleName;
import com.excelsior.xds.parser.modula.ast.AstQualifiedName;
import com.excelsior.xds.parser.modula.ast.AstSimpleName;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;
import com.excelsior.xds.parser.modula.ast.expressions.AstExpression;
import com.excelsior.xds.parser.modula.ast.imports.AstModuleAlias;
import com.excelsior.xds.parser.modula.ast.tokens.XdsParserTokenSets;
import com.excelsior.xds.parser.modula.symbol.IInvalidModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbolScope;
import com.excelsior.xds.parser.modula.symbol.IModuleAliasSymbol;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IOberonMethodSymbol;
import com.excelsior.xds.parser.modula.symbol.IRecordFieldSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.SymbolAttribute;
import com.excelsior.xds.parser.modula.symbol.binding.IImportResolver;
import com.excelsior.xds.parser.modula.symbol.type.IArrayTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IForwardTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.INumericalTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IProcedureTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IRangeTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IRecordTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ISetTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;

public class XdsExpressionParser extends    XdsSymbolParser
                                 implements XdsParserTokenSets, ModulaElementTypes 
{
    protected ExpressionParser expressionParser;

    protected AstQualifiedName forwardTypeQualidentAst; 
    protected ITypeSymbol declaredType;

    public XdsExpressionParser( IFileStore sourceFile, CharSequence chars
                              , XdsSettings xdsSettings
                              , IImportResolver importResolver
                              , IParserEventListener reporter
                              , IXdsParserMonitor monitor )
    {
        super(sourceFile, chars, xdsSettings, importResolver, reporter, monitor);
        expressionParser = new ExpressionParser();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        super.reset();
        declaredType = null;
        forwardTypeQualidentAst = null;
    }
    
    protected IModulaSymbol parseDesignator(IModulaSymbolScope scope, ISymbolWithScope typeResolver) {
        AstDesignator designatorAst = builder.beginProduction(DESIGNATOR);
        IModulaSymbol designatior = null;
        
        if (token != IDENTIFIER) {
            error(XdsMessages.IdentifierExpected);
            expressionParser.parseExpression(scope, typeResolver);
        }
        else {
            designatior = parseQualident(scope, typeResolver);
            designatior = extendDesignator(designatior, scope, typeResolver);
            setAstSymbol(designatorAst, designatior);
        }
        
        builder.endProduction(designatorAst);
        return designatior;
    }
    
    protected IModulaSymbol extendDesignator( IModulaSymbol designator
                                            , IModulaSymbolScope scope, ISymbolWithScope typeResolver ) 
    {
        while (token != EOF) 
        {
            if (BAR == token) {
                if (designator instanceof IOberonMethodSymbol) {
//TODO check_super_call(designator)
                    nextToken();
                    break;
                }
                designator = makeDereference(designator);
                nextToken();
            }                
            else if (DOT == token) 
            {
                if (settings.isOberon()) {
                    designator = makeAutoDereference(designator);
                }
                nextToken();
                if (token == IDENTIFIER) {
                    AstSimpleName nameSymbol = builder.beginProduction(SIMPLE_NAME);
                    String accessedName = getTokenText();
                    designator = makeAccess(designator, accessedName);
                    setAstSymbol(nameSymbol, designator); 
                    addSymbolUsage(designator);
                    
                    nextToken();
                    builder.endProduction(nameSymbol);
                } 
                else {
                    designator = null;
                    error(XdsMessages.IdentifierExpected);
                }
            }
            else if (LBRACKET == token)
            {
                if (settings.isOberon()) {
                    designator = makeAutoDereference(designator);
                }
                nextToken();
                expressionParser.parseExpression(scope, typeResolver);
                designator = makeIndex(designator);
                if (token == COMMA) {
                    token = LBRACKET;
                }
                else {
                    parseToken(RBRACKET);
                }
            }                
            else if (LPARENTH == token)
            {
                designator = expressionParser.parseProcedureCall(designator, scope, typeResolver);
//                if (settings.isOberon()) {
//                    nextToken();
//                    parseTypeQualident(false);
//                    parseToken(RPARENTH);
//                }
//                else {
//                    return;
//                }
            }
            else {
                break; 
            }
        }
        return designator; 
    }
    
    
    
    protected ITypeSymbol parseTypeQualident( IModulaSymbolScope scope
                                            , ISymbolWithScope typeResolver, boolean isOpenArrayEnabled ) 
    {
        IModulaSymbol qualidentSumbol = parseQualident(scope, typeResolver);
        ITypeSymbol typeSymbol = null;
        if (qualidentSumbol instanceof ITypeSymbol) {
            typeSymbol = (ITypeSymbol) qualidentSumbol;
            if (!isOpenArrayEnabled) {
                if (typeSymbol instanceof OpenArrayTypeSymbol) {
                    error(XdsMessages.IllegalOpenArrayTypeUsage);
                }
            }
        }
        else {
// TODO process standard procedure pcM2.type_qualident()
//            error(XdsMessages.IdentifierDoesNotDenoteType);
        }
        return typeSymbol;
    }
    
    
    /**
     * QualifiedIdentifier = {ModuleIdentifier "."} identifier <br>
     * ModuleIdentifier    = identifier <br>
     * @param typeResolver 
     */
    protected IModulaSymbol parseQualident(IModulaSymbolScope scope, ISymbolWithScope typeResolver) {
        IModulaSymbol symbol = null;
        
        if (token == IDENTIFIER) {
            forwardTypeQualidentAst = null;
            boolean isExternal = false;
            IInvalidModulaSymbol invalidSymbol = null;
            AstQualifiedName qualidentAst = builder.beginProduction(QUALIFIED_NAME);
            
            String symbolName = getTokenText();
            symbol = typeResolver.resolveName(symbolName);

            IModuleSymbol moduleSymbol = null;
            if (symbol == null) {
                if ((declaredType != null) && symbolName.equals(declaredType.getName())) {
                    symbol = declaredType;
                }
            }
            else if (symbol instanceof IForwardTypeSymbol) {
                if ((declaredType != null) && symbolName.equals(declaredType.getName())) {
                    symbol = declaredType;
                }
            }
            else if (symbol instanceof IModuleSymbol) {
                AstModuleName moduleNameAst = builder.beginProduction(MODULE_NAME);
                
                isExternal   = true;
                moduleSymbol = (IModuleSymbol)symbol;
                setAstSymbol(moduleNameAst, moduleSymbol);

                builder.acceptLastToken();
                builder.endProduction(moduleNameAst);
            }
            else if (symbol instanceof IModuleAliasSymbol) {
                AstModuleAlias moduleAliasAst = builder.beginProduction(MODULE_ALIAS_NAME);

                isExternal   = true;
                moduleSymbol = ((IModuleAliasSymbol)symbol).getReference();
                if (moduleSymbol == null) {
                    // there is no chance to find anything in unresolved module
                    symbol = null;
                }
                else {
                    setAstSymbol(moduleAliasAst, (IModuleAliasSymbol)symbol);
                }
                
                builder.acceptLastToken();
                builder.endProduction(moduleAliasAst);
            }
            else if (symbol != null) {
                AstSimpleName simpleNameAst = builder.beginProduction(SIMPLE_NAME);
                setAstSymbol(simpleNameAst, symbol);
                builder.acceptLastToken();
                builder.endProduction(simpleNameAst);
            }
            
            if (symbol != null) {
                addSymbolUsage(symbol);
            }
            else {
                invalidSymbol = createUnknownSymbol(symbolName, scope);
            }
            nextToken();
            
            while (token == DOT) {
                if (symbol == null) {
                    nextToken();
                   
                    if (token != IDENTIFIER) {
                        error(XdsMessages.IdentifierExpected);
                    }
                    else {
                        invalidSymbol = createUnknownSymbol(getTokenText(), scope);
                        nextToken();
                    }
                }
                else if (moduleSymbol != null) {
                    nextToken();
                    
                    if (token != IDENTIFIER) {
                        error(XdsMessages.IdentifierExpected);
                        moduleSymbol = null;
                    }
                    else {
                        symbolName = getTokenText();
                        symbol = moduleSymbol.findSymbolInScope(symbolName, true);

                        if (symbol instanceof IModuleSymbol) {
                            AstModuleName moduleNameAst = builder.beginProduction(MODULE_NAME);

                            moduleSymbol = (IModuleSymbol)symbol;
                            setAstSymbol(moduleNameAst, moduleSymbol);

                            builder.acceptLastToken();
                            builder.endProduction(moduleNameAst);
                        }
                        else {
                            AstSimpleName simpleNameAst = builder.beginProduction(SIMPLE_NAME);
                            moduleSymbol = null;
                            if (symbol == null) {
//TODO support of all types is required                                 
//                                error(XdsMessages.UndeclaredIdentifier, symbolName);
                            }
                            else {
                                setAstSymbol(simpleNameAst, symbol);
                            }
                            builder.acceptLastToken();
                            builder.endProduction(simpleNameAst);
                        }
                        
                        addSymbolUsage(symbol);
                        nextToken();
                    }
                }
                else {
                    break;
                }
            } 
            
            if (symbol != null) {
                setAstSymbol(qualidentAst, symbol);
            }
            else if (!isExternal) {
                forwardTypeQualidentAst = qualidentAst;
            }

            if (symbol == null) {
                symbol = invalidSymbol;
            }
            builder.endProduction(qualidentAst);
        }
        
        return symbol;
    }
    
    
    /**
     * Parser of Modula-2/Oberon-2 expression statement.
     */
    final class ExpressionParser {

        private long value;
        
        // TODO : rename to getLongValue
        public long getIntegerValue() {
            return value;
        }


        public void parseBooleanExpression(IModulaSymbolScope scope, ISymbolWithScope typeResolver) {
            parseExpression(scope, typeResolver);
        }
        
        public IModulaSymbol parseExpression(IModulaSymbolScope scope, ISymbolWithScope typeResolver) {
            return parseExpression(EXPRESSION, scope, typeResolver);
        }
        
        public IModulaSymbol parseConstantExpression(IModulaSymbolScope scope, ISymbolWithScope typeResolver) {
            return parseExpression(CONSTANT_EXPRESSION, scope, typeResolver);
        }

        public <T extends AstExpression> IModulaSymbol parseExpression
              ( ModulaCompositeType<T> expression, IModulaSymbolScope scope, ISymbolWithScope typeResolver )
        {
            value = 0;
            IModulaSymbol resultSymbol = null;

            builder.beginProduction(expression);
            
            resultSymbol = parseSimpleExpression(scope, typeResolver);

            if ( (EQU  == token) 
              || (NEQ  == token) 
              || (LSS  == token)
              || (GTR  == token) 
              || (LTEQ == token) 
              || (GTEQ == token)) 
            {
                nextToken();
                parseSimpleExpression(scope, typeResolver);
                resultSymbol = getStandardSymbol(XdsStandardNames.BOOLEAN);
            } 
            else if (IN_KEYWORD == token) {
                nextToken();
                parseSimpleExpression(scope, typeResolver);
                resultSymbol = getStandardSymbol(XdsStandardNames.BOOLEAN);
            } 
            else if (IS_KEYWORD == token) {
                nextToken();
                parseTypeQualident(scope, typeResolver, false);
                resultSymbol = getStandardSymbol(XdsStandardNames.BOOLEAN);
            }
            
            builder.endProduction(expression);
            return resultSymbol;
        }
        
        private IModulaSymbol parseSimpleExpression(IModulaSymbolScope scope, ISymbolWithScope typeResolver) {
            IModulaSymbol resultSymbol = null;
            if (PLUS == token) {
                nextToken();
                resultSymbol = parseTerm(scope, typeResolver);
                value = +value;
            } 
            else if (MINUS == token) {
                nextToken();
                resultSymbol = parseTerm(scope, typeResolver);
                value = -value;
            } 
            else {
                resultSymbol = parseTerm(scope, typeResolver);
            }
            
            while (true) {
                if (PLUS == token) {
                    nextToken();
                    resultSymbol = parseTerm(scope, typeResolver);
                } 
                else if (MINUS == token) {
                    nextToken();
                    resultSymbol = parseTerm(scope, typeResolver);
                } 
                else if (OR_KEYWORD == token) {
                    nextToken();
                    resultSymbol = parseTerm(scope, typeResolver);
                } 
                else {
                    return resultSymbol;
                }
            }
        }

        
        private IModulaSymbol parseTerm(IModulaSymbolScope scope, ISymbolWithScope typeResolver) {
            IModulaSymbol resultSymbol = null;
            builder.beginProduction(TERM);
            
            resultSymbol = parseExponent(scope, typeResolver);
            while (token != EOF) {
                if (!TERM_LEVEL_OPERATION_SET.contains(token)) {
                    break;
                }
                nextToken();
                resultSymbol = parseExponent(scope, typeResolver);
            }
            
            builder.endProduction(TERM);
            return resultSymbol;
        }

        
        private IModulaSymbol parseExponent(IModulaSymbolScope scope, ISymbolWithScope typeResolver) {
            IModulaSymbol resultSymbol = null;
            resultSymbol = parseFactor(scope, typeResolver);
            if (token == EXPONENT) {
                nextToken();
                resultSymbol = parseExponent(scope, typeResolver);
            }
            return resultSymbol;
        }

        
        private IModulaSymbol parseFactor(IModulaSymbolScope scope, ISymbolWithScope typeResolver) {
            IModulaSymbol resultSymbol = null;
            builder.beginProduction(FACTOR);
            
            if (LITERAL_SET.contains(token)) {
                if (INTEGER_LITERAL_SET.contains(token)) {
                    try {
                        value = getTokenIntegerValue();
                    } catch (NumberFormatException e) {
                        value = 0;
                        logInternalError(e); 
                    }
                    resultSymbol = getStandardSymbol(XdsStandardNames.INTEGER);
                }
                nextToken();
            }
            else {
                if (LPARENTH == token) {
                    nextToken();
                    resultSymbol = parseExpression(scope, typeResolver);
                    parseToken(RPARENTH);
                } 
                else if (IDENTIFIER == token) {
                    resultSymbol = parseIdentifier(scope, typeResolver);
                } 
                else if (LBRACE == token) {
                    ISetTypeSymbol typeSymbol = getStandardSymbol(
                        XdsStandardNames.BITSET, ISetTypeSymbol.class
                    ); 
                    parseSetConstructor(typeSymbol, scope, typeResolver);
                } 
                else if (NOT == token) {
                    nextToken();
                    resultSymbol = parseFactor(scope, typeResolver);
                } 
                else if (ARRAY_KEYWORD == token) {
                    nextToken();
                    parseToken(OF_KEYWORD);
                    ITypeSymbol itemTypeSymbol = parseTypeQualident(scope, typeResolver, false);
                    parseOpenArrayConstructor(itemTypeSymbol, scope, typeResolver);
                } 
                else {
                    error(XdsMessages.ExpectedStartOfFactor);
                    skipToToken(FACTOR_SYNCHRONIZATION_SET);
                }
            }

            builder.endProduction(FACTOR);
            return resultSymbol;
        }


        private IModulaSymbol parseIdentifier(IModulaSymbolScope scope, ISymbolWithScope typeResolver) {
            IModulaSymbol resultSymbol = parseDesignator(scope, typeResolver);
            if (LPARENTH == token) 
            {
                if (resultSymbol instanceof ITypeSymbol) {
                    if (!(settings.xdsExtensions() || settings.topSpeedExtensions())) {
                        error(XdsMessages.ExtensionNotAllowed, XdsMessages.ObsoleteTypeCast);
                    }
                    else if (! settings.getOption(XdsOptions.PIMCAST)) {
                        warning(XdsMessages.ImplicitSystemCast);
                    }
                    
                    nextToken();
                    parseExpression(scope, typeResolver);
                    parseToken(RPARENTH);

                    if ((settings.isEnhancedDereference()) && (BAR == token)) {
                        resultSymbol = extendDesignator(resultSymbol, scope, typeResolver);
                    }
                }
                else {
                    resultSymbol = parseProcedureCall(resultSymbol, scope, typeResolver);
                    if (settings.xdsExtensions()) {
//TODO extract return type from the designator                    
                        resultSymbol = extendDesignator(resultSymbol, scope, typeResolver);
                    }
                }
            } 
            else if (LBRACE == token) {
                if (resultSymbol instanceof ITypeSymbol) {
                    parseConstructor((ITypeSymbol)resultSymbol, scope, typeResolver);
                }
                else {
                    if (resultSymbol != null) {
                        String symbolName = resultSymbol.getName();
                        error( resultSymbol.getPosition(), symbolName.length()
                             , XdsMessages.IdentifierDoesNotDenoteType, symbolName );
                    }
                    else {
                        error(XdsMessages.ExpectedType);
                    }
                    skipConstructor();
                }
            }
            return resultSymbol;
        }


        private void parseConstructor( ITypeSymbol typeSymbol
                                     , IModulaSymbolScope scope, ISymbolWithScope typeResolver  ) 
        {
            if (typeSymbol instanceof ISetTypeSymbol) {
                parseSetConstructor((ISetTypeSymbol)typeSymbol, scope, typeResolver);
            }
            else {
                if (settings.isOberon() && !settings.xdsExtensions()) {
                    error(XdsMessages.ExtensionNotAllowed, XdsMessages.ArrayRecotdSimpleAgregate);
                }
                if (typeSymbol instanceof IArrayTypeSymbol) {
                    IArrayTypeSymbol arrayTypeSymbol = (IArrayTypeSymbol)typeSymbol;
                    if (typeSymbol instanceof OpenArrayTypeSymbol) {
                        parseOpenArrayConstructor(arrayTypeSymbol.getElementTypeSymbol(), scope, typeResolver);
                    }
                    else {
                        parseArrayConstructor(arrayTypeSymbol, scope, typeResolver);
                    }
                }
                else if (typeSymbol instanceof IRecordTypeSymbol) {
                    parseRecordConstructor((IRecordTypeSymbol)typeSymbol, scope, typeResolver);
                }
                else if (typeSymbol instanceof INumericalTypeSymbol) {
                    if (typeSymbol instanceof IRangeTypeSymbol) {
                        typeSymbol = ((IRangeTypeSymbol)typeSymbol).getBaseTypeSymbol();
                    }
                    parseSimpleConstructor(typeSymbol, scope, typeResolver);

                }
                else {
                    skipConstructor();
                }
            }
        }
          
        private void skipConstructor() {
            int nestingLevel = 0;
            do {
                if (LBRACE == token) {
                    nestingLevel++;
                } 
                else if (RBRACE == token) {
                    nestingLevel--;
                }
                nextToken();
            } while ((nestingLevel > 0) && !SKIP_CONSTRUCTOR_SET.contains(token) && (token != EOF));
        }
      
      
        private void parseSimpleConstructor( ITypeSymbol typeSymbol
                                           , IModulaSymbolScope scope, ISymbolWithScope typeResolver )
        {
            if (LBRACE != token) {
                errorExpectedSymbol(LBRACE);
                return; 
            }
            nextToken();

            parseConstantExpression(scope, typeResolver);

            parseToken(RBRACE);
        }

        
        /*
         * See pcM2.Expr.record_constructor()
         */
        private void parseRecordConstructor( IRecordTypeSymbol typeSymbol
                                           , IModulaSymbolScope scope, ISymbolWithScope typeResolver )
        {
            if (LBRACE != token) {
                errorExpectedSymbol(LBRACE);
                return; 
            }
            nextToken();
            
//            while ((RBRACE != token) && (EOF != token)) {
//                if (LBRACE == token) {
//                    skipConstructor();
//                }
//                else {
//                    parseConstantExpression(scope);
//                }
//                if (COMMA == token) {
//                    nextToken();
//                }
//            }
            parseRecordFieldsConstructor(typeSymbol, scope, typeResolver);
            
            if (RBRACE == token) {
                nextToken();
            }
            else {
                error(XdsMessages.MoreExpressionsThanFieldsInRecord);
                skipConstructor();
            }
        }
        
        /*
         * See pcM2.Expr.fields_list()
         */
        private void parseRecordFieldsConstructor( IRecordTypeSymbol typeSymbol
                                                 , IModulaSymbolScope scope, ISymbolWithScope typeResolver )
        {
            Iterator<IRecordFieldSymbol> fieldsIterator = typeSymbol.getFields().iterator();
            if ((RBRACE != token) && fieldsIterator.hasNext()) {
                IRecordFieldSymbol fieldSymbol = fieldsIterator.next();

                if (typeSymbol.isAttributeSet(SymbolAttribute.VARIANT_RECORD)) {
                    // TODO: workaround for record variant fields
                    // unnamed variant selector doen't included in the record's field list 
                    fieldsIterator = null;
                    fieldSymbol    =  null;
                }
                do {
                    if (LBRACE == token) {
                        if (fieldSymbol != null) {
                            parseConstructor(fieldSymbol.getTypeSymbol(), scope, typeResolver);
                        }
                        else {
                            skipConstructor();  // workaround for record variant fields
                        }
                    }
                    else {
                        parseConstantExpression(scope, typeResolver);
                    }
                    // TODO check expression and field's type compatibility
                    
//                    if (fieldSymbol instanceof IRecordVariantSelectorSymbol) {
//                        // workaround for record variant fields
//                        fieldsIterator = null;
//                        fieldSymbol    =  null;
//                    }
                    if (COMMA != token) {
                        break;
                    }
                    if (fieldsIterator != null) {
                        if (fieldsIterator.hasNext()) {
                            fieldSymbol = fieldsIterator.next();
                        }
                        else {
                            break;
                        }
                    }
                    nextToken();
                } while ((RBRACE != token) && (EOF != token));
            }
            if ((fieldsIterator != null) && fieldsIterator.hasNext()) {
                IRecordFieldSymbol fieldSymbol = fieldsIterator.next();
                error(XdsMessages.ExpressionForFieldWasExpected, fieldSymbol.getName());
            }
        }
        
        
        private void parseArrayConstructor( IArrayTypeSymbol typeSymbol
                                          , IModulaSymbolScope scope, ISymbolWithScope typeResolver )
        {
            parseArrayLiteral(typeSymbol.getElementTypeSymbol(), scope, typeResolver);
            // TODO check the number of elements in the constructed array
        }
          
        private void parseOpenArrayConstructor( ITypeSymbol itemTypeSymbol
                                              , IModulaSymbolScope scope, ISymbolWithScope typeResolver )  
        {
            boolean isCompound = (itemTypeSymbol instanceof IArrayTypeSymbol)
                              || (itemTypeSymbol instanceof IRecordTypeSymbol);
            if (isCompound) {
                error(XdsMessages.BaseTypeOfOpenArrayAggregateShuldBeSimple);
            }
            parseArrayLiteral(itemTypeSymbol, scope, typeResolver);
        }

        private void parseArrayLiteral( ITypeSymbol itemTypeSymbol
                                      , IModulaSymbolScope scope, ISymbolWithScope typeResolver ) 
        {
            if (token != LBRACE) {
                errorExpectedSymbol(LBRACE);
                return; 
            }
            nextToken();

            while (token != EOF) 
            {
                parseRepeatedArrayLiteralItem(itemTypeSymbol, scope, typeResolver);
        
                if (RBRACE == token) {
                    nextToken();
                    break;
                } 
                else if (COMMA == token) {
                    nextToken();
                } 
                else {
                    errorExpectedSymbol(RBRACE);
                    break;
                }
            }
        }
        
        private void parseRepeatedArrayLiteralItem( ITypeSymbol itemTypeSymbol
                                                  , IModulaSymbolScope scope, ISymbolWithScope typeResolver ) 
        {
            if (LBRACE == token) {
                parseConstructor(itemTypeSymbol, scope, typeResolver);
            }
            else {
                parseExpression(scope, typeResolver);
            }
            
            if (BY_KEYWORD == token) {
                nextToken();
                parseExpression(scope, typeResolver);
            }
        }


        private void parseSetConstructor( ISetTypeSymbol typeSymbol
                                        , IModulaSymbolScope scope, ISymbolWithScope typeResolver ) 
        {
            nextToken();
            if (token != RBRACE) {
                while (token != EOF) 
                {
                    parseExpression(scope, typeResolver);
                    if (RANGE == token) {
                        nextToken();
                        parseExpression(scope, typeResolver);
                    }
                    
                    if (RBRACE == token) {
                        nextToken();
                        break;
                    } 
                    else if (COMMA == token) {
                        nextToken();
                    } 
                    else {
                        errorExpectedSymbol(RBRACE);
                        break;
                    }
                }
            }
            else {
                nextToken();
            }
        }


        
        private ITypeSymbol parseProcedureCall( IModulaSymbol callSymbol
                                              , IModulaSymbolScope scope, ISymbolWithScope typeResolver ) 
        {
            ITypeSymbol returnTypeSymbol = null;
            ITypeSymbol typeSymbol = getSymbolType(callSymbol);
            if (typeSymbol instanceof IProcedureTypeSymbol) {
                returnTypeSymbol = ((IProcedureTypeSymbol)typeSymbol).getReturnTypeSymbol();
                parseProcedureParameters(scope, typeResolver);
            } 
            else {
                skipParameters(scope, typeResolver);
            }
            return returnTypeSymbol;
        }

        private void parseProcedureParameters(IModulaSymbolScope scope, ISymbolWithScope typeResolver) {
            if (token == LPARENTH) {
                nextToken();
                if (token != RPARENTH) {
                    boolean exit = false;
                    while (!exit && (token != EOF)) {
                        if ((token != COMMA) && (token != RPARENTH)) {
                            parseExpression(scope, typeResolver);
                        }
                        if (token == COMMA) {
                            nextToken();
                        }
                        else {
                            exit = true;
                        }
                    }
                }
                parseToken(RPARENTH);
            }
        }

    }    

    
    //--------------------------------------------------------------------------
    //  Common Token Operation
    //--------------------------------------------------------------------------
    
    protected void skipParameters(IModulaSymbolScope scope, ISymbolWithScope typeResolver) {
        if (token == LPARENTH) {
            nextToken();

            while (token != EOF) {
                if ((token == RPARENTH) || (token == END_KEYWORD)) {
                    break;
                }
                
                expressionParser.parseExpression(scope, typeResolver);
                
                if (token == COMMA) {
                    nextToken();
                }
                else {
                    break;
                }
            }
            
            if (token == RPARENTH) {
                nextToken();
            }
        }
    }
    
    
    protected void recoverOnUnexpectedToken(TokenType expectedToken) {
        if (expectedToken == IDENTIFIER) {
            error(XdsMessages.IdentifierExpected);
        }
        else {
            errorExpectedSymbol(expectedToken);
        }
        
        if ((expectedToken != SEMICOLON) || !KEYWORD_SET.contains(token)) {
            nextToken();
        }
        if (token == expectedToken) {
            nextToken();
        }
    }

    
    protected void parseRepeatingToken(TokenType expectedToken) {
        if (token == expectedToken) {
            do {
                nextToken();
            } while (token == expectedToken);
        }
        else {
            recoverOnUnexpectedToken(expectedToken); 
        }
    }
    
    
    protected void parseToken(TokenType expectedToken) {
        if (token == expectedToken) {
            nextToken();
        }
        else {
            recoverOnUnexpectedToken(expectedToken); 
        }
    }

    
    protected void parseTokenStrictly(TokenType expectedToken) {
        if (token == expectedToken) {
            nextToken();
        }
        else {
            if (expectedToken == IDENTIFIER) {
                error(XdsMessages.IdentifierExpected);
            }
            else {
                errorExpectedSymbol(expectedToken);
            }
            skipToToken(expectedToken);
            if (token == expectedToken) {
                nextToken();
            }
        }
    }
    
}
