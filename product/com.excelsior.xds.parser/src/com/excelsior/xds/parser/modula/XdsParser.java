package com.excelsior.xds.parser.modula;

import static com.excelsior.xds.parser.internal.modula.symbol.reference.ReferenceFactory.createStaticRef;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;

import com.excelsior.xds.core.sdk.XdsOptions;
import com.excelsior.xds.core.text.TextPosition;
import com.excelsior.xds.core.utils.collections.Pair;
import com.excelsior.xds.parser.commons.IParserEventListener;
import com.excelsior.xds.parser.commons.ast.AstNode;
import com.excelsior.xds.parser.commons.ast.ElementType;
import com.excelsior.xds.parser.commons.ast.IAstFrameNode;
import com.excelsior.xds.parser.commons.ast.TokenType;
import com.excelsior.xds.parser.commons.pst.PstCompositeNode;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.commons.symbol.IMutableBlockSymbolTextBinding;
import com.excelsior.xds.parser.internal.modula.ModulaSymbolDeclarationTextRegionBuilder;
import com.excelsior.xds.parser.internal.modula.PstCommentsHandler;
import com.excelsior.xds.parser.internal.modula.nls.XdsMessages;
import com.excelsior.xds.parser.internal.modula.symbol.ConstantSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.EnumElementSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.FinallyBodySymbol;
import com.excelsior.xds.parser.internal.modula.symbol.FormalParameterSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.LocalModuleSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.ModuleAliasSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.ModuleBodySymbol;
import com.excelsior.xds.parser.internal.modula.symbol.OberonMethodDeclarationSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.OberonMethodDefinitionSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.OberonMethodReceiverSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.ProcedureBodySymbol;
import com.excelsior.xds.parser.internal.modula.symbol.ProcedureDeclarationSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.ProcedureDefinitionSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.RecordFieldSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.RecordVariantSelectorSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.VariableSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.type.EnumTypeSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.type.EnumTypeSynonymSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.type.ForwardTypeSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.type.InvalidTypeSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.type.OberonMethodTypeSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.type.OpaqueTypeSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.type.PointerTypeSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.type.ProcedureTypeSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.type.RangeTypeSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.type.RecordTypeSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.type.SetTypeSymbol;
import com.excelsior.xds.parser.modula.ast.AstBlock;
import com.excelsior.xds.parser.modula.ast.AstModuleName;
import com.excelsior.xds.parser.modula.ast.AstSimpleName;
import com.excelsior.xds.parser.modula.ast.AstStatementBlock;
import com.excelsior.xds.parser.modula.ast.ModulaAst;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;
import com.excelsior.xds.parser.modula.ast.constants.AstConstantDeclaration;
import com.excelsior.xds.parser.modula.ast.imports.AstImportFragment;
import com.excelsior.xds.parser.modula.ast.imports.AstImportStatement;
import com.excelsior.xds.parser.modula.ast.imports.AstModuleAlias;
import com.excelsior.xds.parser.modula.ast.modules.AstExportStatement;
import com.excelsior.xds.parser.modula.ast.modules.AstFinallyBody;
import com.excelsior.xds.parser.modula.ast.modules.AstModule;
import com.excelsior.xds.parser.modula.ast.modules.AstModuleBody;
import com.excelsior.xds.parser.modula.ast.procedures.AstFormalParameter;
import com.excelsior.xds.parser.modula.ast.procedures.AstOberonMethodDeclaration;
import com.excelsior.xds.parser.modula.ast.procedures.AstOberonMethodForwardDeclaration;
import com.excelsior.xds.parser.modula.ast.procedures.AstOberonMethodReceiver;
import com.excelsior.xds.parser.modula.ast.procedures.AstProcedure;
import com.excelsior.xds.parser.modula.ast.procedures.AstProcedureBody;
import com.excelsior.xds.parser.modula.ast.procedures.AstProcedureDeclaration;
import com.excelsior.xds.parser.modula.ast.procedures.AstProcedureDefinition;
import com.excelsior.xds.parser.modula.ast.procedures.AstProcedureForwardDeclaration;
import com.excelsior.xds.parser.modula.ast.procedures.AstResultType;
import com.excelsior.xds.parser.modula.ast.statements.AstCaseStatement;
import com.excelsior.xds.parser.modula.ast.statements.AstModulaWithStatement;
import com.excelsior.xds.parser.modula.ast.statements.AstStatement;
import com.excelsior.xds.parser.modula.ast.tokens.XdsParserTokenSets;
import com.excelsior.xds.parser.modula.ast.types.AstArrayType;
import com.excelsior.xds.parser.modula.ast.types.AstEnumElement;
import com.excelsior.xds.parser.modula.ast.types.AstEnumerationType;
import com.excelsior.xds.parser.modula.ast.types.AstFormalParameterType;
import com.excelsior.xds.parser.modula.ast.types.AstOpenArrayType;
import com.excelsior.xds.parser.modula.ast.types.AstPointerType;
import com.excelsior.xds.parser.modula.ast.types.AstProcedureType;
import com.excelsior.xds.parser.modula.ast.types.AstProcedureTypeOberon;
import com.excelsior.xds.parser.modula.ast.types.AstRangeType;
import com.excelsior.xds.parser.modula.ast.types.AstRecordField;
import com.excelsior.xds.parser.modula.ast.types.AstRecordType;
import com.excelsior.xds.parser.modula.ast.types.AstRecordVariantLabel;
import com.excelsior.xds.parser.modula.ast.types.AstRecordVariantSelector;
import com.excelsior.xds.parser.modula.ast.types.AstSetType;
import com.excelsior.xds.parser.modula.ast.types.AstTypeDeclaration;
import com.excelsior.xds.parser.modula.ast.variables.AstVariable;
import com.excelsior.xds.parser.modula.symbol.IDefinitionModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.ILocalModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbolScope;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IOberonMethodReceiverSymbol;
import com.excelsior.xds.parser.modula.symbol.IOberonMethodSymbol;
import com.excelsior.xds.parser.modula.symbol.IProcedureSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithDefinitions;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithProcedures;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.SymbolAttribute;
import com.excelsior.xds.parser.modula.symbol.binding.IImportResolver;
import com.excelsior.xds.parser.modula.symbol.binding.ModulaSymbolCache;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.type.IArrayTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IEnumTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IForwardTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IOpaqueTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IOrdinalTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IPointerTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IProcedureTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IRangeTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IRecordTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ISetTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;
import com.excelsior.xds.parser.modula.type.XdsStandardTypes;

/**
 * XDS Modula-2/Oberon-2 source code parser.
 * 
 * ISO/IEC DIS 10514:1994, Section 6.2 Definitions and Declarations 
 * Definitions and declarations serve to introduce the identifiers of a module or 
 * procedure into their scope. Definitions appear in definition modules; declarations 
 * appear in program modules, implementation modules, local modules and procedures. 
 */
public class XdsParser extends    XdsExpressionParser
                       implements XdsParserTokenSets, ModulaElementTypes 
{
	public static boolean IS_DEBUG_PRINT = false; 
	
    /** Handler of parsing events */
    private final IXdsParserMonitor parserMonitor;

    private final ModifierParser modifierParser;
    
    // there was IMPORT SYSTEM in current module
    private boolean isSystemImported;
    
    private boolean isOberonMethod;
    
    public XdsParser( IFileStore sourceFile, CharSequence chars
                    , XdsSettings xdsSettings
                    , IImportResolver importResolver
                    , IParserEventListener reporter
                    , IXdsParserMonitor monitor )
    {
        super(sourceFile, chars, xdsSettings, importResolver, reporter, monitor);
        parserMonitor  = monitor;
        modifierParser = new ModifierParser();
        settings.updateHeaderOptions();
        setParseCommentÑontents(false);
    }


    public XdsParser( IFileStore sourceFile, CharSequence chars
                    , XdsSettings xdsSettings
                    , IImportResolver importResolver
                    , IParserEventListener reporter )
    {
        this(sourceFile, chars, xdsSettings, importResolver, reporter, null);
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        super.reset();
        isDefinitionModule = false;
        isSystemImported   = false;
    }
    
    /**
     * Parses first several lines of the source fail and returns module name.
     * 
     * @return module name
     */
    public String getModuleName() {
        reset();
        ModulaAst ast = new ModulaAst(chars, sourceFile);
        builder.beginProduction(ast);
        builder.beginProduction(PROGRAM_MODULE);
        
        nextToken();
        settings.updateHeaderOptions();
        
        if (settings.isOdfSource()) {
            if (DEFINITION_KEYWORD == token) {
                isDefinitionModule = true;
                nextToken();
            } 
            else {
                skipToToken(DEFINITION_KEYWORD);
            }
        } else {
            if (! settings.isOberon()) {
                if (DEFINITION_KEYWORD == token) {
                    isDefinitionModule = true;
                    nextToken();
                }
                else {
                    if (IMPLEMENTATION_KEYWORD == token) {
                        nextToken();
                    }
                }
            }
            parseTokenStrictly(MODULE_KEYWORD);
        }
        
        modifierParser.setDefaultModuleLanguage();
        if (isDefinitionModule) {
			modifierParser.parseDirectLanguageSpec(ModulaSymbolCache.getModulaSuperModule(),
					ModulaSymbolCache.getModulaSuperModule(),
					modifierParser.defaultModuleLanguage());
            settings.setLanguage(modifierParser.language);
        }

        String moduleName = null;   
        if (token == IDENTIFIER) {
            moduleName = getTokenText();
        }
        
        return moduleName;
    }

    
    /**
     * CompilationModule = DefinitionModule | ["IMPLEMENTATION"] ProgramModule <br>
     * 
     * ProgramModule =  "MODULE" ModuleIdentifier [Protection] ";" <br>
     *                  ImportLists ModuleBlock  ModuleIdentifier "." <br>
     * 
     * DefinitionModule = "DEFINITION" "MODULE" ModuleIdentifier ";" <br>
     *                    ImportLists Definitions "END" ModuleIdentifier "." <br>
     *                    
     * ModuleIdentifier = Identifier
     * @throws CoreException 
     */
    public ModulaAst parseModule() throws CoreException {
        reset();
        if (IS_DEBUG_PRINT) {
        	System.out.println("<<Core parse>> " + sourceFile.getName());
        }
        
        ModulaAst ast = new ModulaAst(chars, sourceFile);
        builder.beginProduction(ast);
        AstModule moduleAst = builder.beginProduction(PROGRAM_MODULE);
        
        nextToken();
        settings.updateHeaderOptions();
        
        boolean isImplementation = false;
        PstNode defOrImplKeywordNode = null;
        PstNode moduleKeywordNode = null;
        if (settings.isOdfSource()) {
            if (DEFINITION_KEYWORD == token) {
                isDefinitionModule = true;
                defOrImplKeywordNode = builder.getLastNode();
                nextToken();
            } 
            else {
                errorExpectedSymbol(DEFINITION_KEYWORD);
                skipToToken(DEFINITION_KEYWORD);
            }
        } else {
            if (! settings.isOberon()) {
                if (DEFINITION_KEYWORD == token) {
                    isDefinitionModule = true;
                    defOrImplKeywordNode = builder.getLastNode();
                    nextToken();
                }
                else {
                    if (IMPLEMENTATION_KEYWORD == token) {
                        isImplementation = true;
                        defOrImplKeywordNode = builder.getLastNode();
                        nextToken();
                    }
                    if (settings.getOption(XdsOptions.NOMODULEINIT))
                        warning(XdsMessages.DefinitionModuleOption, XdsOptions.NOMODULEINIT);
                }
            }

            moduleKeywordNode = builder.getLastNode();
            parseTokenStrictly(MODULE_KEYWORD);
        }
        if (isDefinitionModule) {
            moduleAst = builder.changeProduction(DEFINITION_MODULE);
        }

        if (defOrImplKeywordNode != null) {
            ((IAstFrameNode)(builder.getCurrentProduction())).addFrameNode(defOrImplKeywordNode);
        }
        if (moduleKeywordNode != null) {
            ((IAstFrameNode)(builder.getCurrentProduction())).addFrameNode(moduleKeywordNode);
        }
        
        modifierParser.setDefaultModuleLanguage();
        if (isDefinitionModule) {
			modifierParser.parseDirectLanguageSpec(
					ModulaSymbolCache.getModulaSuperModule(),
					ModulaSymbolCache.getModulaSuperModule(),
					modifierParser.defaultModuleLanguage());
            settings.setLanguage(modifierParser.language);
        }
        XdsLanguage moduleLanguage = modifierParser.language;

        String moduleName;   
        final TextPosition modulePosition = getTokenPosition();
        if (token == IDENTIFIER) {
            builder.beginProduction(MODULE_IDENTIFIER);
            moduleName = getTokenText();
            nextToken();
            builder.endProduction(MODULE_IDENTIFIER);
        }
        else { 
            moduleName = "";   //$NON-NLS-1$
            error(XdsMessages.IdentifierExpected);
        }
        settings.addEquation(XdsEquations.MODULE, moduleName);
        
        createHostModule( moduleName, isImplementation, moduleLanguage
                        , parserMonitor, modulePosition );
        
        setAstSymbol(moduleAst, hostModuleSymbol);
        ast.setModuleSymbol(hostModuleSymbol);
        
        parseToken(SEMICOLON);
        parseImport(hostModuleSymbol);

//        debugPrintln(chars.toString());
        
        parseModuleBlock(hostModuleSymbol, hostModuleSymbol);
        parseToken(DOT);

        builder.endProduction(moduleAst);
        skipToToken(EOF);
        
        builder.endProduction(ast);
        
        finalizeHostModule();
        
        PstCommentsHandler.arrangeComments(chars, ast.getAstNode(), IS_TRAILING_WHITE_SPACE_ALLOWED);
        ast.accept(new ModulaSymbolDeclarationTextRegionBuilder());
        ast.setInactiveCodeRegions(getInactiveCodeRegions());

        if (hostModuleSymbol.getSourceFile() != null) {
            ModulaSymbolCache.instance().addModule(hostModuleSymbol);
        }
        replaceStaticRefs();
        hostModuleSymbol = null;

        if (parserMonitor != null) {
            parserMonitor.endModuleParsing(sourceFile, ast);
        }
        if (reporter != null) {
        	reporter.endFileParsing(sourceFile);
        }
        
        return ast;
    }
    
    
    private void parseModuleBlock(IModuleSymbol parentScope, ISymbolWithScope typeResolver) {
        parseBlock(parentScope, typeResolver, true);
    }

    private void parseProcedureBlock(ISymbolWithDefinitions parentScope, ISymbolWithScope typeResolver) {
        parseBlock(parentScope, typeResolver, false);
    }
    
    private void parseBlock(ISymbolWithDefinitions parentScope, ISymbolWithScope typeResolver, boolean isModuleBlock) 
    {
        AstBlock blockAst = isDefinitionModule ? builder.beginProduction(DEFINITIONS)
                                               : builder.beginProduction(DECLARATIONS);
        int procCount = 0;
        boolean wasDeclaration = false;
        while (true) 
        {
            boolean invalidDeclOrder = settings.isOberon() & (procCount > 0)
                                     & ! settings.xdsExtensions()
                                     & CONST_VAR_TYPE_SET.contains(token);
            if (invalidDeclOrder) {
                error(XdsMessages.IllegalDeclarationOrder);
                procCount = Integer.MIN_VALUE;   // to suppress duplication of error 
            }
            
            wasDeclaration &= DECLARATION_SET.contains(token);
                    
            if (EOF == token) {
                break;
            }
            else if (PROCEDURE_KEYWORD == token) {
                procCount++;
                parseProcedureDeclaration(parentScope, typeResolver);
            }
            else if ((BEGIN_KEYWORD == token) || (END_KEYWORD == token))
            {
                break;
            }
            else if (VAR_KEYWORD == token) {
                parseVariableDeclaration(parentScope, typeResolver);
            }
            else if (CONST_KEYWORD == token) {
                parseConstantDeclaration(parentScope, typeResolver);
            }
            else if (TYPE_KEYWORD == token) {
                parseTypeDeclaration(parentScope, typeResolver);
            }
            else if (MODULE_KEYWORD == token) {
                parseLocalModuleDeclaration(parentScope, typeResolver);
            }
            else if (LABEL_KEYWORD == token) { 
                parseLabelDeclaration();
            }
            else {
                error(XdsMessages.ExpectedDeclarationStart);
                if (isModuleBlock) {
                    skipToToken(MODULE_DECLARATION_SYNCHRONIZATION_SET);
                    if ((token == IMPORT_KEYWORD) || (token == FROM_KEYWORD)) {
                        if (wasDeclaration) {
                            error(XdsMessages.IllegalDeclarationOrder);
                        }
                        if (parentScope instanceof ILocalModuleSymbol) {
                            parseLocalModuleImport((ILocalModuleSymbol)parentScope);
                        }
                        else if (parentScope instanceof IModuleSymbol) {
                            parseImport((IModuleSymbol)parentScope);
                        }
                    }
                }
                else {
                    skipToToken(DECLARATION_SYNCHRONIZATION_SET);
                }
            }
        }
        builder.endProduction(blockAst);

        checkForwardSymbols(parentScope);
        if (EOF == token) {
            return;
        }
        
        AstStatementBlock blockBodyAst = null;
        if (BEGIN_KEYWORD == token) {
            String bodyName = BEGIN_KEYWORD.getDesignator();
            if (isModuleBlock) {
                AstModuleBody moduleBodyAst = builder.beginProduction(MODULE_BODY);
                addModuleFrameNode(BEGIN_KEYWORD);
                ModuleBodySymbol bodySymbol = createModuleBodySymbol(parentScope, bodyName);
                blockBodyAst = moduleBodyAst;
                setAstSymbol(moduleBodyAst, bodySymbol);
                addSymbolForResolving(bodySymbol);
            }
            else {
                AstProcedureBody procedureBodyAst = builder.beginProduction(PROCEDURE_BODY);
                addProcedureFrameNode(BEGIN_KEYWORD);
                ProcedureBodySymbol bodySymbol = createProcedureBodySymbol(parentScope, bodyName);
                setAstSymbol(procedureBodyAst, bodySymbol);
                addSymbolForResolving(bodySymbol);
                blockBodyAst = procedureBodyAst;
            }
            
            parseBlockBody(parentScope, typeResolver, isModuleBlock);
        }

        if (FINALLY_KEYWORD == token) {
            if (isModuleBlock) {
                addModuleFrameNode(FINALLY_KEYWORD);
            }
            AstFinallyBody finallyBodyAst = builder.beginProduction(FINALLY_BODY);
            FinallyBodySymbol bodySymbol = createFinallyBodySymbol(parentScope);
            setAstSymbol(finallyBodyAst, bodySymbol);
            addSymbolForResolving(bodySymbol);
            parseBlockBody(parentScope , typeResolver, isModuleBlock);
            builder.endProduction(finallyBodyAst);
        }

        if (isModuleBlock) {
            addModuleFrameNode(END_KEYWORD);
        } else {
            addProcedureFrameNode(END_KEYWORD);
        }
        parseToken(END_KEYWORD);
        
        if (blockBodyAst != null) {
        	builder.endProduction(blockBodyAst);
        }

        if (IDENTIFIER == token) {
            if (parentScope instanceof IMutableBlockSymbolTextBinding) {
                addNameRegion( (IMutableBlockSymbolTextBinding)parentScope
                             , getTokenText(), getTokenPosition() );
            }
            ElementType nameType = (isModuleBlock) ? MODULE_IDENTIFIER
                                                   : PROCEDURE_IDENTIFIER;  
            builder.beginProduction(nameType);
            nextToken();
            builder.endProduction(nameType);
        }
        else {
            error(XdsMessages.IdentifierExpected);
        }
    }
    
    private void parseBlockBody(IModulaSymbolScope parentSymbol, ISymbolWithScope typeResolver, boolean isModuleBlock) {
        if (token == FINALLY_KEYWORD) {
            if (settings.getOption(XdsOptions.K26)) {
                warning(XdsMessages.KeywordDisabledByK26Option, FINALLY_KEYWORD.getDesignator());
            }
        }
        if (isDefinitionModule) {
            error(XdsMessages.NotAllowedInDefinitionModule);
        }
        
        nextToken();
        parseStamentSequence(parentSymbol, typeResolver);
        
        if (token == EXCEPT_KEYWORD) {
            if (isModuleBlock) {
                addModuleFrameNode(EXCEPT_KEYWORD);
            } else {
                addProcedureFrameNode(EXCEPT_KEYWORD);
            }

            builder.beginProduction(EXCEPT_BLOCK);

            if (settings.getOption(XdsOptions.K26)) {
                warning(XdsMessages.KeywordDisabledByK26Option, EXCEPT_KEYWORD.getDesignator());
            }
            nextToken();
            parseStamentSequence(parentSymbol, typeResolver);

            builder.endProduction(EXCEPT_BLOCK);
        }
    }


    private void parseStamentSequence(IModulaSymbolScope parentSymbol, ISymbolWithScope typeResolver) {
        builder.beginProduction(STATEMENT_LIST);
        ElementType statementType = null;
        AstStatement statementAst = null;
        
        while (token != EOF) {
            if (SEMICOLON != token) {
                if (IDENTIFIER == token) 
                {
                    statementType = ASSIGMENT_STATEMENT;
                    builder.beginProduction(statementType);
                    parseDesignator(parentSymbol, typeResolver);
                    if (BECOMES == token) {
                        parseAssignment(parentSymbol, typeResolver);
                    }                    
                    else if (COLON == token) {
                        nextToken();
                    }                    
                    else if (EQU == token) {
                        error(XdsMessages.StringExpected, BECOMES.getDesignator());
                        nextToken();
                        expressionParser.parseExpression(parentSymbol, typeResolver);
                    }                    
                    else {
                        statementType = CALL_EXPRESSION;
                        skipParameters(parentSymbol, typeResolver);
                    }
                    
                }
                else if (IF_KEYWORD == token) 
                {
                    statementAst = builder.beginProduction(IF_STATEMENT);
                    addFrameNode(IF_KEYWORD, IF_STATEMENT);
                    nextToken();
                    while (token != EOF) 
                    {
                        expressionParser.parseBooleanExpression(parentSymbol, typeResolver);
                        addFrameNode(THEN_KEYWORD, IF_STATEMENT);
                        parseToken(THEN_KEYWORD);
                        parseStamentSequence(parentSymbol, typeResolver);
                        if (token == ELSIF_KEYWORD) {
                            addFrameNode(ELSIF_KEYWORD, IF_STATEMENT);
                            nextToken();
                        }
                        else {
                            if (token == ELSE_KEYWORD) {
                                addFrameNode(ELSE_KEYWORD, IF_STATEMENT);
                                nextToken();
                                parseStamentSequence(parentSymbol, typeResolver);
                            }
                            break;
                        }
                    } 
                    addFrameNode(END_KEYWORD, IF_STATEMENT);
                    nextToken();
                    
                }                
                else if (WHILE_KEYWORD == token)
                {
                    statementAst = builder.beginProduction(WHILE_STATEMENT);
                    addFrameNode(WHILE_KEYWORD, WHILE_STATEMENT);
                    nextToken();
                    expressionParser.parseBooleanExpression(parentSymbol, typeResolver);
                    addFrameNode(DO_KEYWORD, WHILE_STATEMENT);
                    parseToken(DO_KEYWORD);
                    parseStamentSequence(parentSymbol, typeResolver);
                    addFrameNode(END_KEYWORD, WHILE_STATEMENT);
                    parseToken(END_KEYWORD);
                    
                }                
                else if (REPEAT_KEYWORD == token) 
                {
                    statementAst = builder.beginProduction(REPEAT_STATEMENT);
                    addFrameNode(REPEAT_KEYWORD, REPEAT_STATEMENT);
                    nextToken();
                    parseStamentSequence(parentSymbol, typeResolver);
                    addFrameNode(UNTIL_KEYWORD, REPEAT_STATEMENT);
                    parseToken(UNTIL_KEYWORD);
                    expressionParser.parseBooleanExpression(parentSymbol, typeResolver);
                    
                }                
                else if (LOOP_KEYWORD == token)
                {
                    statementAst = builder.beginProduction(LOOP_STATEMENT);
                    addFrameNode(LOOP_KEYWORD, LOOP_STATEMENT);
                    nextToken();
                    parseStamentSequence(parentSymbol, typeResolver);
                    addFrameNode(END_KEYWORD, LOOP_STATEMENT);
                    parseToken(END_KEYWORD);
                    
                }                
                else if (EXIT_KEYWORD == token)
                {
                    statementType = EXIT_STATEMENT;
                    builder.beginProduction(statementType);
                    addFrameNode(EXIT_KEYWORD, LOOP_STATEMENT);
                    nextToken();
                    
                }                
                else if (RETURN_KEYWORD == token)
                {
                    statementType = RETURN_STATEMENT;
                    builder.beginProduction(statementType);
                    addProcedureFrameNode(RETURN_KEYWORD);
                    nextToken();
                    if (!RETURN_TERMINATION_SET.contains(token)) {
                        expressionParser.parseExpression(parentSymbol, typeResolver);
                    }
                    
                }                
                else if (GOTO_KEYWORD == token)
                {
                    statementType = GOTO_STATEMENT;
                    builder.beginProduction(statementType);
                    parseGotoStatement();
                    
                }
                else if (FOR_KEYWORD == token)
                {
                    statementAst = builder.beginProduction(FOR_STATEMENT);
                    parseForStatement(parentSymbol, typeResolver);
                    
                }
                else if (CASE_KEYWORD == token)
                {
                    statementAst = parseCaseStatement(parentSymbol, typeResolver);
                    
                }
                else if (WITH_KEYWORD == token)
                {
                    if (settings.isOberon()) {
                        statementType = OBERON_WITH_STATEMENT;
                        builder.beginProduction(statementType);
                        parseOberonWithStatement(parentSymbol, typeResolver);
                    } 
                    else {
                        statementType = MODULA_WITH_STATEMENT;
                        AstModulaWithStatement withStmtAst = builder.beginProduction(MODULA_WITH_STATEMENT);
                        parseModulaWithStatement(withStmtAst, parentSymbol, typeResolver);
                    }
                    
                }                
                else if (RETRY_KEYWORD == token)
                {
                    statementType = RETRY_STATEMENT;
                    builder.beginProduction(statementType);
                    nextToken();
                    
                }                
                else if (ASM_KEYWORD == token)
                {
                    statementAst = builder.beginProduction(ASM_STATEMENT);
                    addFrameNode(ASM_KEYWORD, ASM_STATEMENT);
                    parseAsmBlock();
                    nextToken();
                    
                }                
                else if (STATEMENT_TERMINATION_SET.contains(token)) 
                {
                    break;
                    
                }
                else {
                    error(XdsMessages.ExpectedStartOfStatement);
                    nextToken();
                }
            }

            if (SEMICOLON == token) {
                nextToken();
            }
            else if (STATEMENT_TERMINATION_SET.contains(token)) {
                if (statementAst != null) {
                    builder.endProduction(statementAst);
                    statementAst = null;
                }
                else if (statementType != null) {
                    builder.endProduction(statementType);
                    statementType = null;
                }
                break;
            }
            else {
                errorExpectedSymbol(SEMICOLON);
            }

            if (statementAst != null) {
                builder.endProduction(statementAst);
                statementAst = null;
            }
            else if (statementType != null) {
                builder.endProduction(statementType);
                statementType = null;
            }
        }
        
        builder.endProduction(STATEMENT_LIST);
    }


    private void parseAsmBlock() {
        if (!settings.xdsExtensions()) {
            error(XdsMessages.ExtensionNotAllowed, "");
        }
        skipToToken(END_KEYWORD);
        addFrameNode(END_KEYWORD, ASM_STATEMENT);
    }


    /**
     * WithStatement    = "WITH" RecordDesignator "DO" StatementSequence "END" <br>
     * RecordDesignator = VariableDesignator | ValueDesignator <br>
     */
    private void parseModulaWithStatement( AstModulaWithStatement withStmtAst
                                         , IModulaSymbolScope parentScope, ISymbolWithScope typeResolver ) 
    {
        Assert.isTrue(token == WITH_KEYWORD);
        addFrameNode(WITH_KEYWORD, MODULA_WITH_STATEMENT);
        nextToken();
        IModulaSymbol recordDesignatorSymbol = expressionParser.parseExpression(parentScope, typeResolver);
        parentScope = createWithStatementScope(parentScope, recordDesignatorSymbol);
        if (withStmtAst != null) {
            withStmtAst.setScope(parentScope);
        }
        
        addFrameNode(DO_KEYWORD, MODULA_WITH_STATEMENT);
        parseToken(DO_KEYWORD);
        parseStamentSequence(parentScope, typeResolver);
        addFrameNode(END_KEYWORD, MODULA_WITH_STATEMENT);
        parseToken(END_KEYWORD);
    }
    
    private void parseOberonWithStatement(IModulaSymbolScope parentSymbol, ISymbolWithScope typeResolver) {
        nextToken();
        boolean exit = false;
        while (!exit && (token != EOF)) {
            parseOberonWithVariant(parentSymbol , typeResolver);
            if (token == SEP) {
                nextToken();
            }
            else {
                exit = true;
            }
        }
        if (token == ELSE_KEYWORD) {
            nextToken();
            parseStamentSequence(parentSymbol, typeResolver);
        }
        nextToken();
    }


    private void parseOberonWithVariant(IModulaSymbolScope parentSymbol, ISymbolWithScope typeResolver) {
        parseDesignator(parentSymbol, typeResolver);
        parseToken(COLON);
        parseTypeQualident(parentSymbol, typeResolver, false);
        parseToken(DO_KEYWORD);
        parseStamentSequence(parentSymbol, typeResolver);
    }


    /**
     * CaseStatement = "CASE" CaseSelector "OF" CaseVariantList "END" <br>
     * CaseSelector = OrdinalExpression <br>
     * CaseVariantList = CaseVariant {"|" CaseVariant} [CaseElsePart] <br>
     * 
     * CaseVariant = [CaseLabelList ":" StatementSequence] <br>
     * CaseLabelList = CaseLabel {"," CaseLabel} <br>
     * CaseLabel = ConstantExpression [".." ConstantExpression] <br>
     */
    private AstCaseStatement parseCaseStatement(IModulaSymbolScope parentSymbol, ISymbolWithScope typeResolver) {
        Assert.isTrue(token == CASE_KEYWORD);
        AstCaseStatement statementAst = builder.beginProduction(CASE_STATEMENT);
        addFrameNode(CASE_KEYWORD, CASE_STATEMENT);

        nextToken();
        expressionParser.parseExpression(CASE_VARIANT_SELECTOR, parentSymbol, typeResolver);
        
        addFrameNode(OF_KEYWORD, CASE_STATEMENT);
        parseToken(OF_KEYWORD);
        
        builder.beginProduction(CASE_VARIANT_LIST);
        while ((token != END_KEYWORD) && (token != ELSE_KEYWORD) && (token != EOF)) {
            if (token == SEP) {
                addFrameNode(SEP, CASE_STATEMENT);
                nextToken();
            }
            else {
                parseCaseStatementVariant(parentSymbol, typeResolver);
            }
        }

        if (token == ELSE_KEYWORD) {
            addFrameNode(ELSE_KEYWORD, CASE_STATEMENT);
            builder.beginProduction(CASE_ELSE_PART);
            nextToken();
            parseStamentSequence(parentSymbol, typeResolver);
            builder.endProduction(CASE_ELSE_PART);
        }
        else {
// KIDE-333: Ëèøíåå ïðåäóïðåæäåíèå 'CASE statement without ELSE'             
// TODO: to show this warning we must be sure that there is at last one untreated CASE-variant.
//            warning(XdsMessages.CaseWithoutElsePart);
        }
        builder.endProduction(CASE_VARIANT_LIST);
        
        addFrameNode(END_KEYWORD, CASE_STATEMENT);
        parseToken(END_KEYWORD);
        return statementAst;
    }


    /**
     * CaseVariant = [CaseLabelList ":" StatementSequence] <br>
     * CaseLabelList = CaseLabel {"," CaseLabel} <br>
     * CaseLabel = ConstantExpression [".." ConstantExpression] <br>
     */
    private void parseCaseStatementVariant(IModulaSymbolScope parentSymbol, ISymbolWithScope typeResolver) {
        builder.beginProduction(CASE_VARIANT);

        builder.beginProduction(CASE_LABEL_LIST);
        while (token != EOF) {
            builder.beginProduction(CASE_LABEL);
            expressionParser.parseConstantExpression(parentSymbol, typeResolver);
            if (token == RANGE) {
                nextToken();
                expressionParser.parseConstantExpression(parentSymbol, typeResolver);
            }
            builder.endProduction(CASE_LABEL);

            if (token == COMMA) {
                nextToken();
            }
            else {
                break;
            }
        }
        builder.endProduction(CASE_LABEL_LIST);
        
        addFrameNode(COLON, CASE_STATEMENT);
        parseToken(COLON);
        parseStamentSequence(parentSymbol, typeResolver);

        builder.endProduction(CASE_VARIANT);
        if ((token != ELSE_KEYWORD) && (token != END_KEYWORD)) {
            addFrameNode(SEP, CASE_STATEMENT);
            parseToken(SEP); 
        }
    }


    private void parseForStatement(IModulaSymbolScope parentScope, ISymbolWithScope typeResolver) {
        Assert.isTrue(token == FOR_KEYWORD);
        addFrameNode(FOR_KEYWORD, FOR_STATEMENT);
        nextToken();

        AstSimpleName controlVariableAst = builder.beginProduction(SIMPLE_NAME);
        if (token == IDENTIFIER) {
            String controlVariableName = getTokenText();
            IModulaSymbol controlVariableSymbol = resolveForLoopControlVariable(
                controlVariableName, parentScope
            );
            if (controlVariableSymbol != null) {
                setAstSymbol(controlVariableAst, controlVariableSymbol);
                addSymbolUsage(controlVariableSymbol);
            }
            nextToken();
        }
        else {
            error(XdsMessages.IdentifierExpected);
        }
        builder.endProduction(controlVariableAst);
        
        parseToken(BECOMES);
        expressionParser.parseExpression(parentScope, typeResolver);

        addFrameNode(TO_KEYWORD, FOR_STATEMENT);
        parseToken(TO_KEYWORD);
        expressionParser.parseExpression(parentScope, typeResolver);
        
        if (token == BY_KEYWORD) {
            addFrameNode(BY_KEYWORD, FOR_STATEMENT);
            nextToken();
            expressionParser.parseExpression(parentScope, typeResolver);
        }

        addFrameNode(DO_KEYWORD, FOR_STATEMENT);
        parseToken(DO_KEYWORD);
        parseStamentSequence(parentScope, typeResolver);
        addFrameNode(END_KEYWORD, FOR_STATEMENT);
        parseToken(END_KEYWORD);
    }


    private void parseGotoStatement() {
        nextToken();
        if (token == IDENTIFIER) {
            nextToken();
        }
        else {
            error(XdsMessages.IdentifierExpected);
        }
    }


    private void parseAssignment(IModulaSymbolScope parentSymbol, ISymbolWithScope typeResolver) {
        nextToken();
        expressionParser.parseExpression(parentSymbol, typeResolver);
    }


    private void parseLabelDeclaration() {
        Assert.isTrue(token == LABEL_KEYWORD);

        if (isDefinitionModule) {
            error(XdsMessages.NotAllowedInDefinitionModule);
        }
        
        nextToken();
        while (token == IDENTIFIER) {
            @SuppressWarnings("unused")
			String labelName = getTokenText();
            nextToken();
            if (SEMICOLON == token) 
            {
                nextToken();
                break;
            }
            else if (COMMA == token) {
                nextToken();
            }            
            else if (IDENTIFIER == token) {
                errorExpectedSymbol(SEMICOLON);
            }                
            else {
                errorExpectedSymbol(SEMICOLON);
                break;
            }
        }
    }


    private void parseConstantDeclaration(ISymbolWithDefinitions parentSymbol, ISymbolWithScope typeResolver) {
        Assert.isTrue(token == CONST_KEYWORD);
        builder.beginProduction(CONSTANT_DECLARATION_BLOCK);
        
        nextToken();
        while (token == IDENTIFIER) {
            AstConstantDeclaration constantAst;
            constantAst = builder.beginProduction(CONSTANT_DECLARATION);

            builder.beginProduction(DECORATED_IDENTIFIER);
            
            final String constantName = getTokenText();
// TODO : ---- BEGIN Code below should be moved to the XdsSymbolParser?
            ConstantSymbol<ITypeSymbol> constantSymbol = createConstantSymbol(parentSymbol, constantName);

            boolean alreadyDefined = checkSymbolAlreadyDefined(constantName, parentSymbol);
            
            nextToken();
			modifierParser.parseDirectLanguageSpec(parentSymbol, typeResolver);
            constantSymbol.setLanguage(modifierParser.language);
            
            EnumSet<SymbolAttribute> attributes = parseExportMarker(true);  
            constantSymbol.addAttributes(attributes);
            
            setAstSymbol(constantAst, constantSymbol);
// TODO : ---- END Code below should be moved to the XdsSymbolParser?

            builder.endProduction(DECORATED_IDENTIFIER);
         
            if (EQU == token)
            {
                nextToken();
                setTypeOfConstantSymbol(constantSymbol, parentSymbol, typeResolver);
            }                
            else if (ALIAS == token)
            {
                nextToken();
                parseQualident(parentSymbol, typeResolver);
            }
            else 
            {
                errorExpectedSymbol(EQU);
                builder.endProduction(CONSTANT_DECLARATION);
                break;
            }

            if (alreadyDefined) {
                markAsAlreadyDefined(constantSymbol);
            } else {
                parentSymbol.addConstant(constantSymbol);                
            }
            addSymbolForResolving(constantSymbol);
            
            if (SEMICOLON == token) {
                nextToken();
            }                
            else if (IDENTIFIER == token) {
                errorExpectedSymbol(SEMICOLON);
            }
            else {
                errorExpectedSymbol(SEMICOLON);
                builder.endProduction(CONSTANT_DECLARATION);
                break;
            }
            builder.endProduction(CONSTANT_DECLARATION);
        } 

        builder.endProduction(CONSTANT_DECLARATION_BLOCK);
    }
    

    private void parseVariableDeclaration(ISymbolWithDefinitions parentSymbol, ISymbolWithScope typeResolver) {
        Assert.isTrue(token == VAR_KEYWORD);
        builder.beginProduction(VARIABLE_DECLARATION_BLOCK);
        nextToken();
        
        boolean wasSlash = false;
        while ((token == IDENTIFIER) || (token == SLASH)) {
            if (token == SLASH) {
                wasSlash = true;
                nextToken();
            }
            else {
                builder.beginProduction(VARIABLE_DECLARATION);
                builder.beginProduction(VARIABLE_LIST);

                String hostName = null;
                List<VariableSymbol> allVariableSymbols = new ArrayList<VariableSymbol>(32);                
                while (token != EOF) 
                {
                    if (token == IDENTIFIER) {
                        AstVariable variableAst = builder.beginProduction(VARIABLE);

                        String variableName = getTokenText();
                        if (hostName == null) {
                            hostName = variableName;
                        }
                        VariableSymbol variableSymbol = createVariableSymbol(variableName, parentSymbol);
                        
                        if (wasSlash) {
                            variableSymbol.addAttribute(SymbolAttribute.EXTERNAL);
                            wasSlash = false;
                        }
                        
                        boolean alreadyDefined = checkSymbolAlreadyDefined(variableName, parentSymbol);
                        if (alreadyDefined) {
                            markAsAlreadyDefined(variableSymbol);
                        } 
                        else {
                            parentSymbol.addVariable(variableSymbol);
                        }
                        
                        allVariableSymbols.add(variableSymbol);
                        setAstSymbol(variableAst, variableSymbol);
                        
                        nextToken();
                        modifierParser.parseAbsoluteAddress(parentSymbol, typeResolver);

                        modifierParser.parseDirectLanguageSpec(parentSymbol, typeResolver);
                        variableSymbol.setLanguage(modifierParser.language);
                        
                        EnumSet<SymbolAttribute> attributes = parseExportMarker(true);
                        variableSymbol.addAttributes(attributes);
                        
                        addSymbolForResolving(variableSymbol);
                        builder.endProduction(variableAst);
                    } 
                    else {
                        parseToken(IDENTIFIER);
                    }
                    
                    if (token == COMMA) {
                        nextToken();
                    }
                    else if (IDENTIFIER == token) {
                        errorExpectedSymbol(COMMA);
                    }
                    else {
                        break;
                    }
                }
                builder.endProduction(VARIABLE_LIST);

                if (settings.xdsExtensions() && (token == SEP)) {
                    nextToken();
                }

                boolean needType = true;
                ITypeSymbol typeSymbol;
                
                if (settings.xdsExtensions() && (token == BECOMES)) {
                    nextToken();
                    expressionParser.parseExpression(parentSymbol, typeResolver);
                    if (token == COLON) {
                        nextToken();
                    }
                    else {
                        needType = false;
                    }
                }
                else {
                    parseToken(COLON);
                }

                if (needType) {
                    typeSymbol = parseTypeDefinition(null, hostName, parentSymbol, parentSymbol, false, false);
                }
                else {
                    // TODO TypeSymbol must be extracted from expression
                    typeSymbol = null; 
                }

                for (VariableSymbol variableSymbol : allVariableSymbols) {
                    setTypeSymbol(variableSymbol, typeSymbol);
                }
                
                parseToken(SEMICOLON);
                builder.endProduction(VARIABLE_DECLARATION);
                wasSlash = false;
            }
        }

        builder.endProduction(VARIABLE_DECLARATION_BLOCK);
    }
    

    /**
     * TypeDefinition = SimpleType | ArrayType | RecordType | SetType |  PointerType | ProcedureType
     * 
     * @param typeName - the name of defined type, may be <tt>null</tt>
     * @param hostName - the name of host symbol, used to construct name of anonymous type
     */
    private ITypeSymbol parseTypeDefinition( String typeName, String hostName
                                           , ISymbolWithScope parentSymbol
                                           , ISymbolWithScope typeResolver
                                           , boolean isOpenArrayEnabled
                                           , boolean isForwardDeclarationEnabled )
    {
        return parseTypeDefinition( typeName, hostName, parentSymbol, typeResolver
                                  , isOpenArrayEnabled, isForwardDeclarationEnabled
                                  , TYPE_ELEMENT );
    }


    /**
     * TypeDefinition = SimpleType | ArrayType | RecordType | SetType |  PointerType | ProcedureType
     * 
     * @param typeName - the name of defined type, may be <tt>null</tt>
     * @param hostName - the name of host symbol, used to construct name of anonymous type
     */
    private ITypeSymbol parseTypeDefinition( String typeName, String hostName
                                           , ISymbolWithScope parentScope
                                           , ISymbolWithScope typeResolver
                                           , boolean isOpenArrayEnabled
                                           , boolean isForwardDeclarationEnabled
                                           , ModulaCompositeType<? extends AstNode> astNodeType ) 
    {
        builder.beginProduction(astNodeType);
        ITypeSymbol typeSymbol = null;
        if (IDENTIFIER == token) 
        {
            final String identifier = getTokenText();
            final TextPosition forwardTypePosition = getTokenPosition();
            
            AstRangeType typeAst = builder.beginProduction(RANGE_TYPE_DEFINITION);
            typeSymbol = parseTypeQualident(parentScope, typeResolver, isOpenArrayEnabled);
            if (token == LBRACKET) {
                IOrdinalTypeSymbol baseTypeSymbol;
                if (typeSymbol instanceof IOrdinalTypeSymbol) {
                    baseTypeSymbol = (IOrdinalTypeSymbol)typeSymbol;
                }
                else {
                    baseTypeSymbol = null;
                    error(XdsMessages.ExpectedOrdinalType);
                }
                IRangeTypeSymbol rangeTypeSymbol = parseRangeTypeDefinition(
                    typeName, hostName, parentScope, typeResolver, baseTypeSymbol
                );

                builder.endProduction(typeAst);
                setAstSymbol(typeAst, rangeTypeSymbol);
                addSymbolForResolving(rangeTypeSymbol);
                typeSymbol = rangeTypeSymbol;
            } 
            else {
                builder.dropProduction(RANGE_TYPE_DEFINITION);
                if (typeSymbol == null) {
                    if (isForwardDeclarationEnabled) {
                        IForwardTypeSymbol forwardTypeSymbol = createAndRegisterForwardType(identifier, parentScope);
                        setSymbolPosition(forwardTypeSymbol, forwardTypePosition);
                        addSymbolUsage(forwardTypeSymbol, forwardTypePosition);
                        if (forwardTypeQualidentAst != null) {
                            setAstSymbol(forwardTypeQualidentAst, forwardTypeSymbol);
                            addSymbolForResolving(forwardTypeSymbol);
                        }
                        typeSymbol = forwardTypeSymbol;
                    } 
                    else {
// TODO                         
//                        error(XdsMessages.TypeIsNotDefined, identifier);
                    }
                }
                else {
                    if (typeName != null) {
                        typeSymbol = typeSymbol.createSynonym(typeName, parentScope, refFactory);
                    }                    
                }
            }
        } 
        else if (LBRACKET == token) {
            AstRangeType typeAst; 
            typeAst = builder.beginProduction(RANGE_TYPE_DEFINITION);
            
            IRangeTypeSymbol rangeTypeSymbol = parseRangeTypeDefinition(
                typeName, hostName, parentScope, typeResolver, null
            );
            setAstSymbol(typeAst, rangeTypeSymbol);
            addSymbolForResolving(rangeTypeSymbol);
            typeSymbol = rangeTypeSymbol;
            
            builder.endProduction(typeAst);
        } 
        else if (LPARENTH == token) {
            typeSymbol = parseEnumerationTypeDefinition(typeName, hostName, parentScope);
        } 
        else if (PROCEDURE_KEYWORD == token) 
        {
            if (settings.isOdfSource()) {
                typeSymbol = parseProcedureTypeDefinition(typeName, hostName, parentScope, typeResolver);
            }
            else if (settings.isOberon()) {
                typeSymbol = parseProcedureTypeOberonDefinition(typeName, hostName, parentScope, typeResolver);
            } 
            else {
                typeSymbol = parseProcedureTypeDefinition(typeName, hostName, parentScope, typeResolver);
            }
        } 
        else if (RECORD_KEYWORD == token) {
            typeSymbol = parseRecordTypeDefinition(typeName, hostName, parentScope, typeResolver);
        } 
        else if (ARRAY_KEYWORD == token) {
            typeSymbol = parseArrayTypeDefinition(typeName, hostName, isOpenArrayEnabled, parentScope, typeResolver);
        } 
        else if (SET_KEYWORD == token) {
            typeSymbol = parseSetTypeDefinition(typeName, hostName, false, parentScope, typeResolver);
        } 
        else if (PACKEDSET_KEYWORD == token) {
            typeSymbol = parseSetTypeDefinition(typeName, hostName, true, parentScope, typeResolver);
        } 
        else if (POINTER_KEYWORD == token) {
            typeSymbol = parsePointerTypeDefinition(typeName, hostName, parentScope, typeResolver);
        } 
        else {
            error(XdsMessages.ExpectedTypeStart);
        }
        
        if (typeSymbol != null && typeSymbol.isAnonymous() ) {
            addSymbolForResolving(typeSymbol);
        }
     
        builder.endProduction(astNodeType);
        return typeSymbol;
    }
    
    
    /**
     * PointerType = "POINTER" [DirectLanguageSpec] "TO" BoundType <br>
     * BoundType   = TypeIdentifier | NewType <br>
     */
    private IPointerTypeSymbol parsePointerTypeDefinition( String typeName
                                                         , String hostName  
                                                         , ISymbolWithScope scope, ISymbolWithScope typeResolver ) 
    {
        Assert.isTrue(token == POINTER_KEYWORD);
        AstPointerType typeAst = builder.beginProduction(POINTER_TYPE_DEFINITION);

        boolean isTypeDeclaration = (typeName != null);
        
        PointerTypeSymbol typeSymbol = createPointerTypeSymbol(
            typeName, hostName, scope, typeAst
        );
        typeName = typeSymbol.getName();
        setAstSymbol(typeAst, typeSymbol);
        addSymbolForResolving(typeSymbol);
        
        nextToken();
        
        modifierParser.parseDirectLanguageSpec(scope, typeResolver);
        typeSymbol.setLanguage(modifierParser.language);
        
        parseToken(TO_KEYWORD);

        if (isTypeDeclaration) {
            declaredType = typeSymbol;
        }
        
        ITypeSymbol boundTypeSymbol = parseTypeDefinition(null, typeName, scope, typeResolver, true, true);
        
        setBoundTypeSymbol(typeSymbol, boundTypeSymbol);
        
        builder.endProduction(typeAst);
        return typeSymbol;
    }


    /**
     * SetType  = "SET" "OF" BaseType <br>
     * BaseType = OrdinalTypeIdentifier | NewOrdinalType <br>
     * 
     * OrdinalTypeIdentifier =  TypeIdentifier <br>
     * NewOrdinalType = EnumerationType | SubrangeType <br>
     */
    private ISetTypeSymbol parseSetTypeDefinition( String typeName
                                                 , String hostName
                                                 , boolean isPacked
                                                 , ISymbolWithScope scope, ISymbolWithScope typeResolver ) 
    {
        Assert.isTrue((token == SET_KEYWORD) || (token == PACKEDSET_KEYWORD));
        AstSetType typeAst;
        typeAst = builder.beginProduction(SET_TYPE_DEFINITION);
        
        SetTypeSymbol typeSymbol = null;
        if (settings.isOberon() && !isPacked) {
            typeSymbol = (SetTypeSymbol)ModulaSymbolCache.getOberonSuperModule().resolveName("SET");  //$NON-NLS-1$
            nextToken();
        }
        else {
            nextToken();
            parseToken(OF_KEYWORD);
            
            typeName = generateNameIfNull(typeName, hostName, "SetType", scope);    //$NON-NLS-1$
            ITypeSymbol baseTypeSymbol = parseTypeDefinition(null, typeName, scope, typeResolver, false, false);

            typeSymbol = createSetTypeSymbol(
                typeName, hostName, isPacked, baseTypeSymbol, scope
            );
        }
        setAstSymbol(typeAst, typeSymbol);
        addSymbolForResolving(typeSymbol);

        builder.endProduction(typeAst);
        return typeSymbol;
    }

    /**
     * ModulaArrayType = "ARRAY" IndexType {"," IndexType} "OF" ComponentType <br>
     * ModulaOpenArrayFormalType = "ARRAY" "OF" {"ARRAY" "OF"} TypeIdentifier <br>
     *
     * IndexType = Ordinal TypeDenoter <br>
     *
     * OberonArrayType = "ARRAY" [ConstExpr {"," ConstExpr}] "OF" Type <br>
     */
    private IArrayTypeSymbol parseArrayTypeDefinition( String typeName
                                                     , String hostName
                                                     , boolean openArrayEnabled
                                                     , ISymbolWithScope scope, ISymbolWithScope typeResolver ) 
    {
        AstArrayType typeAst = builder.beginProduction(ARRAY_TYPE_DEFINITION);
        typeName = generateNameIfNull(typeName, hostName, "ArrayType", scope);    //$NON-NLS-1$ 
       
        nextToken();

        IArrayTypeSymbol typeSymbol = null;
        if (token == OF_KEYWORD) {
            typeAst = builder.changeProduction(OPEN_ARRAY_TYPE_DEFINITION);
            if (!settings.isOberon() && !settings.xdsExtensions()) {
                error(XdsMessages.ExtensionNotAllowed, XdsMessages.ArrayOfType);
            }
            if (!openArrayEnabled) {
                error(XdsMessages.IllegalOpenArrayTypeUsage);
            }
            nextToken();
            ITypeSymbol elementTypeSymbol = parseTypeDefinition(null, typeName, scope, typeResolver, true, false);
            
            typeSymbol = createOpenArrayTypeSymbol(
                typeName, hostName, elementTypeSymbol, scope
            );
        }
        else {
            IOrdinalTypeSymbol indexTypeSymbol = null;
            if (settings.isOberon() && !settings.isOdfSource()) {
                expressionParser.parseConstantExpression(scope, typeResolver);
                
                indexTypeSymbol = (IOrdinalTypeSymbol)ModulaSymbolCache.getOberonSystemModule().resolveName(XdsStandardNames.INDEX);
            }
            else {
                ITypeSymbol parsedIndexTypeSymbol = parseTypeDefinition(null, typeName, scope, typeResolver, false, false, INDEX_TYPE);
                
                if (parsedIndexTypeSymbol instanceof IOrdinalTypeSymbol) {
                    indexTypeSymbol = (IOrdinalTypeSymbol)parsedIndexTypeSymbol; 
                }
            }
            
            if (token == COMMA) {
                builder.acceptLastToken();
                token = ARRAY_KEYWORD;
            } else {
                parseToken(OF_KEYWORD); 
            }

            ITypeSymbol elementTypeSymbol = parseTypeDefinition(null, typeName, scope, typeResolver, false, false);

            typeSymbol = createArrayTypeSymbol( 
                typeName, hostName, elementTypeSymbol, indexTypeSymbol, scope 
            );
        }
        setAstSymbol(typeAst, typeSymbol);
        addSymbolForResolving(typeSymbol);
        
        builder.endProduction(typeAst);
        return typeSymbol;
    }


    /**
     * RecordType = "RECORD" [BaseTypeDeclaration] FieldListSequence "END" <br>
     * BaseTypeDeclaration = "(" RecordType ")" <br> 
     */
    private IRecordTypeSymbol parseRecordTypeDefinition( String typeName
                                                       , String hostName  
                                                       , ISymbolWithScope parentSymbol, ISymbolWithScope typeResolver ) 
    {
        Assert.isTrue(token == RECORD_KEYWORD);
        AstRecordType typeAst = builder.beginProduction(RECORD_TYPE_DEFINITION);
        addFrameNode(RECORD_KEYWORD, RECORD_TYPE_DEFINITION);

        boolean isTypeDeclaration = (typeName != null);
        
        nextToken();
        
        modifierParser.parseDirectLanguageSpec(parentSymbol, typeResolver);
        XdsLanguage language = modifierParser.language;
        
        RecordTypeSymbol typeSymbol = null;
        if ((token == LPARENTH) && (settings.isOberon() || settings.xdsExtensions())) {
            IRecordTypeSymbol baseTypeSymbol = parseBaseTypeDeclaration(parentSymbol, typeResolver); 
            typeSymbol = createRecordTypeSymbol(
                typeName, hostName, baseTypeSymbol, "OberonRecord", parentSymbol   //$NON-NLS-1$
            );
        }
        else {
            typeSymbol = createRecordTypeSymbol(
                typeName, hostName, null, "Record", parentSymbol   //$NON-NLS-1$
            );
        }
        
        typeSymbol.setLanguage(language);
        setAstSymbol(typeAst, typeSymbol);
        addSymbolForResolving(typeSymbol);

        if (isTypeDeclaration) {
            declaredType = typeSymbol;
        }
        
        parseRecordFieldsDefintion(language, typeSymbol, typeResolver);
        addFrameNode(END_KEYWORD, RECORD_TYPE_DEFINITION);
        parseToken(END_KEYWORD);
        
        builder.endProduction(typeAst);
        return typeSymbol;
    }


    /**
     * FieldListSequence = FieldList {";" FieldList} <br>
     * FieldList = SimpleFields | VariantFields  <br>
     */
    private void parseRecordFieldsDefintion( XdsLanguage language
                                           , RecordTypeSymbol parentScope, ISymbolWithScope typeResolver ) 
    {
        builder.beginProduction(RECORD_FIELD_BLOCK_LIST);
        while (token != EOF) 
        {
            if (IDENTIFIER == token) {
                parseRecordSimpleFieldDefinition(parentScope, typeResolver);
            } 
            else if (CASE_KEYWORD == token) {
                parseRecordVariantFieldDefinition(language, parentScope, typeResolver);
            } 
            else if (END_ELSE_SEP_SET.contains(token)) {
                break;
            } 
            else if (settings.isOdfSource() && (PROCEDURE_KEYWORD == token)) {
                parseProcedureDeclaration(parentScope, typeResolver);
            }
            else if (token != SEMICOLON) {
                errorExpectedSymbol(END_KEYWORD);
                skipToToken(END_KEYWORD);
                break;
            }
            else {
                parseToken(SEMICOLON);
            }
        }
        builder.endProduction(RECORD_FIELD_BLOCK_LIST);
    }


    /**
     * VariantFields = "CASE" [Identifier] ":" OrdinalTypeidentifier "OF" VariantList "END" <br>
     * VariantList   = Variant {"|" Variant} <br>
     */
    private void parseRecordVariantFieldDefinition( XdsLanguage language
                                                  , RecordTypeSymbol parentSymbol, ISymbolWithScope typeResolver ) 
    {
        Assert.isTrue(token == CASE_KEYWORD);
        builder.beginProduction(RECORD_VARIANT_FIELD_BLOCK);
        addFrameNode(CASE_KEYWORD, RECORD_VARIANT_FIELD_BLOCK);

        parentSymbol.addAttribute(SymbolAttribute.VARIANT_RECORD);
        if (XdsLanguage.Oberon2 == language) {
            error(XdsMessages.VariantFieldsNotAllowedInOberonRecord);                        
        }
        
        nextToken();

        AstRecordVariantSelector selectorAst = builder.beginProduction(RECORD_VARIANT_SELECTOR);
        String selectorName = null;
        boolean alreadyDefined = true;
        
        final TextPosition selectorPosition = getTokenPosition();
        if (token == COLON) {
            // unnamed tag
        }
        else if (token == IDENTIFIER) {
            selectorName = getTokenText();
            alreadyDefined = checkSymbolAlreadyDefined(selectorName, parentSymbol);

            nextToken();
            modifierParser.parseDirectLanguageSpec(parentSymbol, typeResolver);
            parseExportMarker(true);    
        }
        else {
            error(XdsMessages.IdentifierExpected);
            builder.endProduction(selectorAst);
            builder.endProduction(RECORD_VARIANT_FIELD_BLOCK);
            skipToToken(END_KEYWORD);
            return;
        }
        
        parseToken(COLON);
        ITypeSymbol selectorTypeSymbol = parseTypeQualident(parentSymbol, typeResolver, false);

        RecordVariantSelectorSymbol selectorSymbol = createRecordVariantSelectorSymbol(
            selectorName, selectorTypeSymbol, selectorPosition, parentSymbol
        );
        
        if (alreadyDefined) {
            markAsAlreadyDefined(selectorSymbol);
        }
        else {
            parentSymbol.addField(selectorSymbol);
        }
        
        setAstSymbol(selectorAst, selectorSymbol);
        addSymbolForResolving(selectorSymbol);
        builder.endProduction(selectorAst);
        
        addFrameNode(OF_KEYWORD, RECORD_VARIANT_FIELD_BLOCK);
        parseToken(OF_KEYWORD);
        
        builder.beginProduction(RECORD_VARIANT_LIST);
        while ((token != END_KEYWORD) && (token != EOF)) {
            if (token == SEP) {
                addFrameNode(SEP, RECORD_VARIANT_FIELD_BLOCK);
                nextToken();
            }
            else {
                parseRecordVariant(language, parentSymbol, typeResolver);
            }
        }
        addFrameNode(END_KEYWORD, RECORD_VARIANT_FIELD_BLOCK);
        builder.endProduction(RECORD_VARIANT_LIST);

        nextToken();

        if (!END_ELSE_SEP_SET.contains(token)) {
            parseRepeatingToken(SEMICOLON);
        }
        
        builder.endProduction(RECORD_VARIANT_FIELD_BLOCK);
    }


    /**
     * RecordVariant = "ELSE" FieldListSequence | RecordVariantLabelList ":" FieldListSequence <br>
     * RecordVariantLabelList = RecordVariantLabels {"," CaseLabels} <br>
     * RecordVariantLabels    = ConstExpression [".." ConstExpression] <br>     
     */
    private void parseRecordVariant( XdsLanguage language
                                   , RecordTypeSymbol parentSymbol, ISymbolWithScope typeResolver) 
    {
        if (token == ELSE_KEYWORD) {
            addFrameNode(ELSE_KEYWORD, RECORD_VARIANT_FIELD_BLOCK);
            builder.beginProduction(RECORD_VARIANT_ELSE_PART);
            nextToken();
            parseRecordFieldsDefintion(language, parentSymbol, typeResolver);
            builder.endProduction(RECORD_VARIANT_ELSE_PART);
        }
        else {
            builder.beginProduction(RECORD_VARIANT);
            builder.beginProduction(RECORD_VARIANT_LABEL_LIST);
            while (token != EOF) 
            {
                parseVariantLabel(parentSymbol, typeResolver);
                if (COLON == token) {
                    addFrameNode(COLON, RECORD_VARIANT_FIELD_BLOCK);
                    break;
                } 
                else if (COMMA == token) {
                    nextToken();
                } 
                else {
                    parseToken(COMMA);
                    break;
                }
            }
            builder.endProduction(RECORD_VARIANT_LABEL_LIST);

            nextToken();
            parseRecordFieldsDefintion(language, parentSymbol, typeResolver);
            
            if ((token != ELSE_KEYWORD) && (token != END_KEYWORD)) {
               addFrameNode(SEP, RECORD_VARIANT_FIELD_BLOCK);
               parseToken(SEP);
            }
            builder.endProduction(RECORD_VARIANT);
        }
    }


    /**
     * RecordVariantLabel = ConstantExpression [".." ConstantExpression] <br>
     */
    private void parseVariantLabel(IModulaSymbolScope parentSymbol, ISymbolWithScope typeResolver) {
        AstRecordVariantLabel labelAst = builder.beginProduction(RECORD_VARIANT_LABEL);
        expressionParser.parseExpression(CONSTANT_EXPRESSION, parentSymbol, typeResolver);
        if (token == RANGE) {
            nextToken();
            expressionParser.parseExpression(CONSTANT_EXPRESSION, parentSymbol, typeResolver);
        }
        if (labelAst != null) {
            String labelText = chars.subSequence( 
                labelAst.getOffset(), labelAst.getOffset() + labelAst.getLength() 
            ).toString();
            labelAst.setText(labelText);
        }
        builder.endProduction(labelAst);
    }

    /**
     * SimpleFields   = IdentifierList ":" TypeDefinition <br>
     * IdentifierList = Identifier {"," Identifier} <br>
     */
    private void parseRecordSimpleFieldDefinition(IRecordTypeSymbol parentSymbol, ISymbolWithScope typeResolver) {
        builder.beginProduction(RECORD_SIMPLE_FIELD_BLOCK);
        builder.beginProduction(RECORD_FIELD_LIST);
        
        List<RecordFieldSymbol> declaredSymbols = new ArrayList<RecordFieldSymbol>(8);
        String hostName = null;
        while (true) {
            if (IDENTIFIER == token) {
                AstRecordField fieldAst = builder.beginProduction(RECORD_FIELD);

                String fieldName = getTokenText();
                if (hostName == null) {
                    hostName = fieldName;
                }
                RecordFieldSymbol fieldSymbol = createRecordFieldSymbol(fieldName, parentSymbol);
                setAstSymbol(fieldAst, fieldSymbol);

                boolean alreadyDefined = checkSymbolAlreadyDefined(fieldName, parentSymbol, declaredSymbols);
                if (alreadyDefined) {
                    markAsAlreadyDefined(fieldSymbol);
                }
                declaredSymbols.add(fieldSymbol);
                
                nextToken();
                
                modifierParser.parseDirectLanguageSpec(parentSymbol, typeResolver);
                fieldSymbol.setLanguage(modifierParser.language);
                
                EnumSet<SymbolAttribute> attributes = parseExportMarker(true);
                fieldSymbol.addAttributes(attributes);

                addSymbolForResolving(fieldSymbol);
                builder.endProduction(fieldAst);
            }
            else {
                recoverOnUnexpectedToken(IDENTIFIER);
            }
            
            if (COMMA == token) {
                nextToken();
            } 
            else if (IDENTIFIER == token) {
                errorExpectedSymbol(COMMA);
            } 
            else {
                break;
            }
        }
        builder.endProduction(RECORD_FIELD_LIST);

        parseToken(COLON);

        ITypeSymbol typeSymbol = parseTypeDefinition(null, hostName, parentSymbol, typeResolver, false, false);
        
        for (RecordFieldSymbol fieldSymbol : declaredSymbols) {
            fieldSymbol.setTypeSymbol(createRef(typeSymbol));
            if (!fieldSymbol.isAttributeSet(SymbolAttribute.ALREADY_DEFINED)) {
                parentSymbol.addField(fieldSymbol);
            }
        }
        
        if (!END_ELSE_SEP_SET.contains(token)) {
            parseRepeatingToken(SEMICOLON);
        }
        
        builder.endProduction(RECORD_SIMPLE_FIELD_BLOCK);
    }

    /**
     * BaseTypeDeclaration = "(" RecordType ")" <br> 
     */
    private IRecordTypeSymbol parseBaseTypeDeclaration(IModulaSymbolScope parentSymbol, ISymbolWithScope typeResolver)
    {
        builder.beginProduction(BASE_TYPE);

        nextToken();
        ITypeSymbol typeSymbol = parseTypeQualident(parentSymbol, typeResolver, true);
        
        IRecordTypeSymbol recordTypeSymbol = null;
        if (typeSymbol instanceof IRecordTypeSymbol) {
            recordTypeSymbol = (IRecordTypeSymbol)typeSymbol;
        }
        else {
            error(XdsMessages.ObjectIsNotRecord);
        }
        parseToken(RPARENTH);
        
        builder.endProduction(BASE_TYPE);
        return recordTypeSymbol;
    }


  
    /**
     * ProcedureType = "PROCEDURE" ["(" [FormalParameterTypeList] ")"] [":" ResultType] <br>
     * FormalParameterTypeListt = FormalParameterType {"," FormalParameterType} <br>
     * FormalParameterType = ["VAR" ["[NIL]"]] FormalType <br>
     */ 
    private IProcedureTypeSymbol parseProcedureTypeDefinition( String typeName
                                                             , String hostName  
                                                             , ISymbolWithScope parentScope, ISymbolWithScope typeResolver ) 
    {
        Assert.isTrue(token == PROCEDURE_KEYWORD);
        AstProcedureType typeAst = builder.beginProduction(PROCEDURE_TYPE_DEFINITION);

        ProcedureTypeSymbol typeSymbol = createProcedureTypeSymbol(typeName, hostName, parentScope);
        
        nextToken();
        
        modifierParser.parseDirectLanguageSpec(parentScope, typeResolver);
        XdsLanguage language = modifierParser.language;
                
        if (token == LPARENTH) {
            builder.beginProduction(FORMAL_PARAMETER_TYPE_LIST);

            nextToken();

            if (token != RPARENTH) {
                int parameterCount = typeSymbol.getParameters().size() + 1;
                while (token != EOF) {
                    if (SEQ_KEYWORD == token) {
                        break;
                    }
                    else {
                        AstFormalParameterType parameterAst = builder.beginProduction(FORMAL_PARAMETER_TYPE);
                        EnumSet<SymbolAttribute> attributes = SymbolAttribute.createEmptySet();
                        
                        if (VAR_KEYWORD == token) {
                            attributes.add(SymbolAttribute.VAR_PARAMETER);
                            
                            nextToken();
                            parseVarParameterAttributes(attributes);
                        }
                        
                        if ( (IDENTIFIER    == token) 
                          || (ARRAY_KEYWORD == token) ) 
                        {
                            String parameterName = FormalParameterSymbol.createAnonymousName(parameterCount);
                            ITypeSymbol parameterTypeSymbol = parseFormalType(parameterName, typeSymbol, typeResolver);

                            FormalParameterSymbol parameterSymbol = createFormalParameterSymbol(
                                    parameterName, parameterCount, parameterTypeSymbol, typeSymbol
                            );
                            parameterCount++;
                            parameterSymbol.addAttributes(attributes);                                                                 
                            setAstSymbol(parameterAst, parameterSymbol);
                            addSymbolForResolving(parameterSymbol);
                        } 
                        else {
                            error(XdsMessages.IdentifierExpected);
                        }

                        if ((RPARENTH == token) || (SEQ_KEYWORD == token)) {
                            builder.endProduction(parameterAst);
                            break;
                        } 
                        else {
                            parseToken(COMMA);
                            builder.endProduction(parameterAst);
                        }                    
                    }
                }
                
                if (SEQ_KEYWORD == token) {
                    AstFormalParameterType parameterAst = builder.beginProduction(FORMAL_PARAMETER_TYPE);

                    if (!settings.xdsExtensions()) {
                        error(XdsMessages.ExtensionNotAllowed, XdsMessages.SeqParameter);                        
                    }
                    nextToken();
                    
                    ITypeSymbol parameterTypeSymbol = parseTypeQualident(parentScope, typeResolver, true);

                    FormalParameterSymbol parameterSymbol = createFormalParameterSymbol(
                            FormalParameterSymbol.createAnonymousName(parameterCount), parameterCount, parameterTypeSymbol, typeSymbol);
                    parameterCount++;
                    
                    parameterSymbol.addAttribute(SymbolAttribute.SEQ_PARAMETER);                                                                 
                    setAstSymbol(parameterAst, parameterSymbol);
                    addSymbolForResolving(parameterSymbol);

                    builder.endProduction(parameterAst);
                }
            }
            parseToken(RPARENTH);
            
            builder.endProduction(FORMAL_PARAMETER_TYPE_LIST);
            
            if (COLON == token) {
                nextToken();
                ITypeSymbol returnTypeSymbol = parseTypeQualident(parentScope, typeResolver, false);
                typeSymbol.setReturnTypeSymbol(createRef(returnTypeSymbol));
            }  
        }

        typeSymbol.setLanguage(language);
        setAstSymbol(typeAst, typeSymbol);
        addSymbolForResolving(typeSymbol);
        
        builder.endProduction(typeAst);
        return typeSymbol; 
    }


    private IProcedureTypeSymbol parseProcedureTypeOberonDefinition( String typeName
                                                                   , String hostName  
                                                                   , ISymbolWithScope scope, ISymbolWithScope typeResolver ) 
    {
        Assert.isTrue(token == PROCEDURE_KEYWORD);
        AstProcedureTypeOberon typeAst = builder.beginProduction(PROCEDURE_TYPE_OBERON_DEFINITION);

        ProcedureTypeSymbol typeSymbol = createProcedureTypeSymbol(typeName, hostName, scope);
        
        nextToken();
        
        modifierParser.parseDirectLanguageSpec(scope, typeResolver);
        parseProcedureHeader(typeSymbol, typeResolver);
        
        setAstSymbol(typeAst, typeSymbol);
        addSymbolForResolving(typeSymbol);
        
        builder.endProduction(typeAst);
        return typeSymbol;
    }


    /**
     * ProcedureHeader     = FormalParameters ":" TypeQualident <br>
     * FormalParameters    = "(" FormalParameterList ")" <br>
     * FormalParameterList = FormalParameter {";" FormalParameter} <br>
     * FormalParameter     = ["VAR"] IdentifierList ":" FormalType <br>
     */
    private void parseProcedureHeader(ProcedureTypeSymbol parentSymbol, ISymbolWithScope typeResolver) {
        if (token == LPARENTH) {
            builder.beginProduction(FORMAL_PARAMETER_BLOCK);
            nextToken();
            
            
            if (token != RPARENTH) {
            	int parameterNo = parentSymbol.getParameters().size() + 1;
                while (token != EOF) 
                {
                    if (SEQ_KEYWORD == token) {
                        break;
                    } 
                    else if (RPARENTH == token) {
                        error(XdsMessages.IdentifierExpected);
                        break;
                    }
                    
                    builder.beginProduction(FORMAL_PARAMETER_DECLARATION);
                    EnumSet<SymbolAttribute> attributes = SymbolAttribute.createEmptySet();
                    
                    if (VAR_KEYWORD == token) {
                        attributes.add(SymbolAttribute.VAR_PARAMETER);

                        nextToken();
                        parseVarParameterAttributes(attributes);
                    } 

                    builder.beginProduction(FORMAL_PARAMETER_LIST);

                    List<FormalParameterSymbol> declaredSymbols = new ArrayList<FormalParameterSymbol>(8);
                    String hostName = null;
                    while (true) 
                    {
                        if (IDENTIFIER == token) {
                            AstFormalParameter parameterAst = builder.beginProduction(FORMAL_PARAMETER);
                            
                            String parameterName = getTokenText();
                            if (hostName == null) {
                                hostName = parameterName;
                            }
                            FormalParameterSymbol parameterSymbol = createFormalParameterSymbol(parameterName, parameterNo++, parentSymbol);
                            parameterSymbol.addAttributes(attributes);   
                            
                            setAstSymbol(parameterAst, parameterSymbol);
                            
                            boolean alreadyDefined = checkSymbolAlreadyDefined(parameterName, parentSymbol, declaredSymbols);
                            if (alreadyDefined) {
                                markAsAlreadyDefined(parameterSymbol);
                            }
                            declaredSymbols.add(parameterSymbol);
                            
                            nextToken();
                            modifierParser.parseDirectLanguageSpec(parentSymbol, typeResolver);
                            parameterSymbol.setLanguage(modifierParser.language);
                            
                            if (token == MINUS) {
                                parameterSymbol.addAttribute(SymbolAttribute.READ_ONLY);
                                if (!settings.xdsExtensions()) {
                                    error(XdsMessages.ExtensionNotAllowed, XdsMessages.ReadOnlyParameters);
                                }
                                if (isDefinitionModule) {
                                    error(XdsMessages.NotAllowedInDefinitionModule);
                                }
                                nextToken();
                            }
                            if (NEQ == token) {
                                if (!settings.xdsExtensions()) {
                                    error(XdsMessages.ExtensionNotAllowed, XdsMessages.UnusedParameters);
                                }
                                if (isDefinitionModule) {
                                    error(XdsMessages.NotAllowedInDefinitionModule);
                                }
                                nextToken();
                            }
                            if (token == BECOMES) {
                                parameterSymbol.addAttribute(SymbolAttribute.DEFAULT);
                                if (!settings.xdsExtensions()) {
                                    error(XdsMessages.ExtensionNotAllowed, XdsMessages.ParameterValueByDefault);
                                }
                                nextToken();
                                expressionParser.parseExpression(parentSymbol, typeResolver);
                            }

                            addSymbolForResolving(parameterSymbol);
                            builder.endProduction(parameterAst);
                        }
                        else {
                            parseToken(IDENTIFIER);
                        }
                        
                        if (COMMA == token) {
                            nextToken();
                        } 
                        else if (IDENTIFIER == token) {
                            errorExpectedSymbol(COMMA);
                        } 
                        else {
                            break;
                        }
                    }
                    builder.endProduction(FORMAL_PARAMETER_LIST);
                    
                    parseToken(COLON);
                    ITypeSymbol parameterTypeSymbol;
                    if (settings.isOberon()) {
                        parameterTypeSymbol = parseTypeDefinition(null, hostName, parentSymbol, typeResolver,  true, false);
                    }
                    else {
                        parameterTypeSymbol = parseFormalType(hostName, parentSymbol, typeResolver);
                    }
                    
                    for (FormalParameterSymbol parameterSymbol : declaredSymbols) {
                        setTypeSymbol(parameterSymbol, parameterTypeSymbol);
                        if (!parameterSymbol.isAttributeSet(SymbolAttribute.ALREADY_DEFINED)) {
                            parentSymbol.addParameter(parameterSymbol);
                        }
                    }

                    if (token == RPARENTH) {
                        builder.endProduction(FORMAL_PARAMETER_DECLARATION);
                        break;
                    }
                    else {
                        parseToken(SEMICOLON);
                    }
                    builder.endProduction(FORMAL_PARAMETER_DECLARATION);
                } 
                
                if (SEQ_KEYWORD == token) {
                    builder.beginProduction(FORMAL_PARAMETER_DECLARATION);

                    if (!settings.xdsExtensions()) {
                        error(XdsMessages.ExtensionNotAllowed, XdsMessages.SeqParameter);
                    }
                    nextToken();
                    if (IDENTIFIER == token) {
                        AstFormalParameter parameterAst = builder.beginProduction(FORMAL_PARAMETER);

                        String parameterName = getTokenText();
                        
                        FormalParameterSymbol parameterSymbol = createFormalParameterSymbol(parameterName, parameterNo++,  parentSymbol);
                        setSymbolCurrentPosition(parameterSymbol);
                        parameterSymbol.addAttribute(SymbolAttribute.SEQ_PARAMETER);

                        setAstSymbol(parameterAst, parameterSymbol);
                        boolean alreadyDefined = checkSymbolAlreadyDefined(parameterName, parentSymbol);
                        if (alreadyDefined) {
                            markAsAlreadyDefined(parameterSymbol);
                        } 
                        else {
                            parentSymbol.addParameter(parameterSymbol);
                        }
                        
                        nextToken();
                        modifierParser.parseDirectLanguageSpec(parentSymbol, typeResolver);
                        parameterSymbol.setLanguage(modifierParser.language);

                        addSymbolForResolving(parameterSymbol);
                        builder.endProduction(parameterAst);
                        
                        parseToken(COLON);

                        ITypeSymbol parameterTypeSymbol;
                        if (settings.isOberon()) {
                            parameterTypeSymbol = parseTypeDefinition(null, parameterName, parentSymbol, typeResolver, false, false);
                        }
                        else {
                            parameterTypeSymbol = parseTypeQualident(parentSymbol, typeResolver, false);
                        }
                        parameterSymbol.setTypeSymbol(createStaticRef(parameterTypeSymbol));
                    }
                    else {
                        error(XdsMessages.IdentifierExpected);
                    }
                    builder.endProduction(FORMAL_PARAMETER_DECLARATION);
                }
            }
            
            parseToken(RPARENTH);
            builder.endProduction(FORMAL_PARAMETER_BLOCK);
            
            if (COLON == token) {
                AstResultType resultTypeAst = builder.beginProduction(RESULT_TYPE);
                
                nextToken();
                
                ITypeSymbol returnTypeSymbol;
                if (settings.isOberon()) {
                    returnTypeSymbol = parseTypeDefinition(null, parentSymbol.getName(), parentSymbol, typeResolver, false, false);
                }
                else {
                    returnTypeSymbol = parseTypeQualident(parentSymbol, typeResolver, false);
                }
                parentSymbol.setReturnTypeSymbol(createRef(returnTypeSymbol));
                setAstSymbol(resultTypeAst, returnTypeSymbol);
                addSymbolForResolving(returnTypeSymbol);
                
                builder.endProduction(resultTypeAst);
            }
        }
    }

    
    /**
     * FormalType = TypeQualident | OpenArrayFormalType <br>
     * OpenArrayFormalType = "ARRAY" "OF" {"ARRAY" "OF"} TypeQualident <br>
     */
    private ITypeSymbol parseFormalType(String hostName, ISymbolWithScope scope, ISymbolWithScope typeResolver) 
    {
        builder.beginProduction(FORMAL_TYPE);
        
        List<AstOpenArrayType> typeAstList = new ArrayList<AstOpenArrayType>();
        @SuppressWarnings("unused")
		int dim1 = 0;
        
        while (ARRAY_KEYWORD == token) {
            AstOpenArrayType typeAst = builder.beginProduction(OPEN_ARRAY_TYPE_DEFINITION);
            typeAstList.add(typeAst);
            
            nextToken();
            parseToken(OF_KEYWORD);
            dim1++;
        }
        ITypeSymbol typeSymbol = parseTypeQualident(scope, typeResolver, true);

        for (int i = typeAstList.size() - 1; i >= 0; i--) {
            IArrayTypeSymbol arrayTypeSymbol = createOpenArrayTypeSymbol(
                null, hostName, typeSymbol, scope
            ); 
            AstArrayType typeAst = typeAstList.get(i);
            setAstSymbol(typeAst, arrayTypeSymbol);
            addSymbolForResolving(arrayTypeSymbol);
            builder.endProduction(typeAst);

            typeSymbol = arrayTypeSymbol;
        }

        builder.endProduction(FORMAL_TYPE);
        return typeSymbol;
    }


    private void parseVarParameterAttributes(EnumSet<SymbolAttribute> attributes) {
        if (token == LBRACKET) {
            nextToken();
            if (!isSystemImported && !settings.xdsExtensions()) {
                error(XdsMessages.ExtensionNotAllowed, XdsMessages.SpecialKindsParameters);
            }
            if (token == IDENTIFIER) {
                String name = getTokenText();
                if ("NIL".equals(name)) {
                    // object is special VAR parameter, may be NIL
                    attributes.add(SymbolAttribute.NIL_ALLOWED);
                }
                else {
                    error(XdsMessages.InvalidParameterSpecificationExpectedNIL);
                }
                nextToken();
            }
            else {
                error(XdsMessages.IdentifierExpected);
            }
            parseToken(RBRACKET);
        }
    }


    /**
     * EnumerationType = "(" IdentifierList ")" <br>
     * IdentifierList  = Identifier {"," Identifier} <br>
     * 
     * @param typeSymbolResult 
     */
    private IEnumTypeSymbol parseEnumerationTypeDefinition( String typeName
                                                          , String hostName  
                                                          , ISymbolWithScope parentScope ) 
    {
        Assert.isTrue(token == LPARENTH);
        AstEnumerationType typeAst = builder.beginProduction(ENUMERATION_TYPE_DEFINITION);

        if (settings.isOberon() && !settings.xdsExtensions()) {
            error(XdsMessages.NotAllowedInOberon);
        }

        EnumTypeSymbol typeSymbol = createEnumTypeSymbol(typeName, hostName, parentScope);
        setAstSymbol(typeAst, typeSymbol);
        addSymbolForResolving(typeSymbol);
        
        nextToken();
        if (token == RPARENTH) {
            error(XdsMessages.IdentifierExpected);
        }
        else {
            ISymbolWithDefinitions symbolWithDefinitions = getParentScopeWithSymbolDefinitions(parentScope);
            
            while (true) 
            {
                if (token == IDENTIFIER) {
                    AstEnumElement enumElementAst;
                    enumElementAst = builder.beginProduction(ENUM_ELEMENT);
                    
                    String enumElementName = getTokenText();
                    EnumElementSymbol enumElementSymbol = createEnumElementSymbol(enumElementName, typeSymbol);
                    
                    setAstSymbol(enumElementAst, enumElementSymbol);
                    
                    boolean alreadyDefined = checkSymbolAlreadyDefined(enumElementName, parentScope);
                    if (alreadyDefined) {
                        markAsAlreadyDefined(enumElementSymbol);
                    }
                    else if (symbolWithDefinitions != null) {
                        symbolWithDefinitions.addEnumElements(enumElementSymbol);                
                    }
                    
                    nextToken();

                    addSymbolForResolving(enumElementSymbol);
                    builder.endProduction(enumElementAst);
                }
                else {
                    recoverOnUnexpectedToken(IDENTIFIER);
                }
                
                if (COMMA == token) {
                    nextToken();
                } 
                else if (RPARENTH == token) {
                    break;
                } 
                else if (IDENTIFIER == token) {
                    errorExpectedSymbol(COMMA);
                } 
                else {
                    break;
                }
            } 
        }
        parseToken(RPARENTH);

        builder.endProduction(typeAst);
        typeSymbol.finalizeDefinition();
        return typeSymbol;
    }


    /**
     * RangeType = [OrdinalTypeIdentifier] "[" ConstExpression ".." ConstExpression "]" <br>
     * OrdinalTypeIdentifier = TypeIdentifier <br>
     */
    private IRangeTypeSymbol parseRangeTypeDefinition( String typeName
                                                     , String hostName
                                                     , ISymbolWithScope scope
                                                     , ISymbolWithScope typeResolver
                                                     , IOrdinalTypeSymbol baseTypeSymbol )
    {
        if (settings.isOberon() && !settings.xdsExtensions()) {
            error(XdsMessages.NotAllowedInOberon);
        }
        
        if (baseTypeSymbol != null) {
            checkOrdinalTypeSymbol(baseTypeSymbol);
        }

        nextToken();
        expressionParser.parseConstantExpression(scope, typeResolver);
        Long lowBound = expressionParser.getIntegerValue();
        
        parseToken(RANGE);
        
        expressionParser.parseConstantExpression(scope, typeResolver);
        Long highBound = expressionParser.getIntegerValue();
        
        parseToken(RBRACKET);
        
        if (baseTypeSymbol == null) {
            //TODO process case when bounds is a CHAR. 
            if (lowBound < 0) {
                if (!settings.xdsExtensions()) {
                    baseTypeSymbol = (IOrdinalTypeSymbol) scope.resolveName(XdsStandardNames.INTEGER);
                }
                else {
                    String baseTypeName;
                    if (highBound > XdsStandardTypes.INT32.getMaxValue().longValue()) {
                        baseTypeName = XdsStandardNames.INT64;
                    }
                    else if (highBound > XdsStandardTypes.INT16.getMaxValue().longValue()) {
                        baseTypeName = XdsStandardNames.INT32;
                    }
                    else if (highBound > XdsStandardTypes.INT8.getMaxValue().longValue()) {
                        baseTypeName = XdsStandardNames.INT16;
                    }
                    else {
                        baseTypeName = XdsStandardNames.INT8;
                    }
                    baseTypeSymbol = (IOrdinalTypeSymbol)getStandardSymbol(baseTypeName);
                }
            }
            else {
                if (!settings.xdsExtensions()) {
                    baseTypeSymbol = (IOrdinalTypeSymbol)getStandardSymbol(XdsStandardNames.CARDINAL);
                }
                else {
                    String baseTypeName;
                    if (highBound > XdsStandardTypes.CARD32.getMaxValue().longValue()) {
                        baseTypeName = XdsStandardNames.CARD64;
                    }
                    else if (highBound > XdsStandardTypes.CARD16.getMaxValue().longValue()) {
                        baseTypeName = XdsStandardNames.CARD32;
                    }
                    else if (highBound > XdsStandardTypes.CARD8.getMaxValue().longValue()) {
                        baseTypeName = XdsStandardNames.CARD16;
                    }
                    else {
                        baseTypeName = XdsStandardNames.CARD8;
                    }
                    baseTypeSymbol = (IOrdinalTypeSymbol)getStandardSymbol(baseTypeName);
                }
            }
            
            boolean isOutOfBounds = (lowBound  < baseTypeSymbol.getType().getMinValue().longValue())
                                 || (highBound > baseTypeSymbol.getType().getMaxValue().longValue());
            if (isOutOfBounds) {
                error(XdsMessages.ExpressionOutOfBounds);
            }
        }
        else {
            //TODO check that bounds are compatible with the base type. 
        }
        
        if (lowBound > highBound) {
//TODO expression value is required             
//            error(XdsMessages.LowBoundGreaterThanHighBound);
        }
            
        typeName = generateNameIfNull(typeName, hostName, "Range", scope);    //$NON-NLS-1$
        RangeTypeSymbol typeSymbol = new RangeTypeSymbol(typeName, scope, baseTypeSymbol, lowBound, highBound);    
        return typeSymbol;
    }


    private void parseLocalModuleDeclaration(ISymbolWithDefinitions parentScope, ISymbolWithScope typeResolver) {
        if (isDefinitionModule) {
            error(XdsMessages.NotAllowedInDefinitionModule);
        }
        if (settings.isOberon()) {
            error(XdsMessages.NotAllowedInOberon);
        }
        
        AstModule localModuleAst = builder.beginProduction(LOCAL_MODULE);
        addModuleFrameNode(MODULE_KEYWORD);

        nextToken();

        String moduleName = "";
        boolean alreadyDefined = false;
        final TextPosition modulePosition = getTokenPosition();
        
        if (IDENTIFIER == token) {
            builder.beginProduction(MODULE_IDENTIFIER);
            
            moduleName = getTokenText();  
            alreadyDefined = checkSymbolAlreadyDefined(moduleName, parentScope);

            nextToken();
            builder.endProduction(MODULE_IDENTIFIER);
        } 
        else {
            error(XdsMessages.IdentifierExpected);            
        }
        
        modifierParser.parseProtection(parentScope, typeResolver);
        parseToken(SEMICOLON);
        
        LocalModuleSymbol localModuleSymbol = createLocalModuleSymbol(
            moduleName, modulePosition, alreadyDefined, parentScope
        );
        if (alreadyDefined) {
            markAsAlreadyDefined(localModuleSymbol);
        }
        
        setAstSymbol(localModuleAst, localModuleSymbol);
        addSymbolForResolving(localModuleSymbol);
        
        parseLocalModuleImport(localModuleSymbol); 

        // ExportList = UnqualifiedExport | QualifiedExport 
        // UnqualifiedExport = "EXPORT" IdentifierList semicolon 
        // QualifiedExport   = "EXPORT" "QUALIFIED" IdentifierList ";"
        AstExportStatement exportAst = builder.beginProduction(UNQUALIFIED_EXPORT);
        Map<String, Pair<AstSimpleName, TextPosition>> exportedIdentifiers = new HashMap<String, Pair<AstSimpleName, TextPosition>>();
        if (EXPORT_KEYWORD == token) {
            nextToken();

            boolean isExportQualifed = false;
            if (QUALIFIED_KEYWORD == token) {
                exportAst = builder.changeProduction(QUALIFIED_EXPORT);
                localModuleSymbol.addAttribute(SymbolAttribute.QUALIFIED_EXPORT);
                isExportQualifed = true;
                nextToken();
            }
            
            builder.beginProduction(IDENTIFIER_LIST);            
            while (token != EOF) {
                if (IDENTIFIER == token) {
                    AstSimpleName exportNameAst = builder.beginProduction(SIMPLE_NAME);            

                    String identifier = getTokenText();
                    
                    Pair<AstSimpleName, TextPosition> pair = Pair.create(exportNameAst, getTokenPosition());
                    exportedIdentifiers.put(identifier, pair);
                    if (!isExportQualifed) {
                        checkSymbolAlreadyDefined(identifier, parentScope);
                    }
                    
                    nextToken();
                    builder.endProduction(exportNameAst);
                }
                else {
                    parseToken(IDENTIFIER);
                }
                
                if (COMMA == token) {
                    nextToken();
                } 
                else if (SEMICOLON == token) {
                    break;
                } 
                else if (IDENTIFIER == token) {
                    errorExpectedSymbol(COMMA);
                } 
                else {
                    break;
                }            
            }
            builder.endProduction(IDENTIFIER_LIST);
            
            parseToken(SEMICOLON);
        }
        builder.endProduction(exportAst);
        
        parseModuleBlock(localModuleSymbol, localModuleSymbol);

        processLocalModuleExport(localModuleSymbol, exportedIdentifiers);
        
        parseToken(SEMICOLON);
        builder.endProduction(LOCAL_MODULE);
    }

    
    /**
     * ProcDecl = PROCEDURE [Receiver] IdentDef [FormalPars] ";" DeclSeq ["BEGIN" StatementSeq] "END" identifier <br>
     * Receiver = "(" ["VAR"] identifier ":" identifier ")" <br>
     */
    private OberonReceiverInfo parseOberonMethodReceiver (ISymbolWithProcedures parentSymbol, ISymbolWithScope typeResolver) 
    {
        isOberonMethod = false;
        if (settings.isOdfSource() && (token == SLASH)) {
            nextToken();
        }
        if (!settings.isOberon() || (token != LPARENTH)) {
            return null;
        }

        AstOberonMethodReceiver receiverAst = builder.beginProduction(OBERON_METHOD_RECEIVER);
        boolean isVarInstance = false; 

        nextToken();
        if (token == VAR_KEYWORD) {
            isVarInstance = true;
            nextToken();
        }

        IOberonMethodReceiverSymbol receiverSymbol; 
        if (token == IDENTIFIER) {
            final String receiverName = getTokenText();
            final TextPosition receiverPosition = getTokenPosition();
        
            nextToken();
            parseToken(COLON);
            ITypeSymbol typeSymbol = parseTypeQualident(parentSymbol, typeResolver, false);

            if (typeSymbol != null) {
                if (isVarInstance) {
                    if (!(typeSymbol instanceof IRecordTypeSymbol)) {
                        error(XdsMessages.ObjectIsNotRecord);
                    }
                }
                else if (typeSymbol instanceof IPointerTypeSymbol) {
                    if (typeSymbol.getLanguage() == XdsLanguage.Oberon2) {
                        IPointerTypeSymbol pointerTypeSymbol = (IPointerTypeSymbol)typeSymbol;
                        ITypeSymbol boundTypeSymbol = pointerTypeSymbol.getBoundTypeSymbol();
                        
                        if (settings.isOdfSource() && boundTypeSymbol instanceof IForwardTypeSymbol) {
                            // old format of odf-files allows declare methods inside record type declaration
                            // TYPE ListenerObj*  = RECORD 
                            //        PROCEDURE (this: Listener) TextChanged*(from,to: INTEGER);
                            //      END;
                            if (parentSymbol instanceof IRecordTypeSymbol) {
                                String name = parentSymbol.getName();
                                boolean isParentActualType = StringUtils.isNotEmpty(name)
                                                          && StringUtils.equals(name, boundTypeSymbol.getName())
                                                          && parentSymbol.getParentScope() == boundTypeSymbol.getParentScope();
                                if (isParentActualType) {
                                    ITypeSymbol actualTypeSymbol = (IRecordTypeSymbol)parentSymbol;
                                    ((ForwardTypeSymbol)boundTypeSymbol).setActualTypeSymbol(createRef(actualTypeSymbol));
                                    boundTypeSymbol = actualTypeSymbol;
                                }
                            }
                        }
                        
                        if (!(boundTypeSymbol instanceof IRecordTypeSymbol)) {
                            error(XdsMessages.PointerNotBoundRecord);
                        }
                    }
                    else {
                        error(XdsMessages.OberonTypeIsRequired);
                    }
                }
                else {
                    error(XdsMessages.ObjectIsNotPointerOrRecord);
                }
            }
            
            // this is temporary symbol, it will not resolve correctly, since it has no parentScope.
            receiverSymbol = createOberonMethodReceiverSymbol(
                receiverName, receiverPosition, isVarInstance, typeSymbol
            );
            
            isOberonMethod = true;
        }
        else {
            receiverSymbol = null;
            error(XdsMessages.IdentifierExpected);
            skipToToken(OBERON_PROCEDURE_SYNCHRONIZATION_SET);
        }
        
        parseToken(RPARENTH); 

        builder.endProduction(receiverAst);
        return new OberonReceiverInfo(receiverAst, receiverSymbol);
    }

    
    /**
     * ProcedureOberonDeclaration  = "PROCEDURE" [Receiver] IdentDef [FormalPars] ";" ProcedureBlock Identifier <br>
     * ProcedureOberonForwardDeclaration = "PROCEDURE" "^" [Receiver] IdentDef [FormalPars] <br>
     * IdentDef = Identifier [" * " | "-"] <br>
     */
    private AstProcedure<? extends IProcedureSymbol> 
            parseOberonMethodDeclaration( ISymbolWithProcedures parentSymbol
            							, ISymbolWithScope typeResolver
                                        , OberonReceiverInfo receviverInfo
                                        , XdsLanguage language
                                        , boolean isForwardDeclaration
                                        , PstNode procKeyword ) 
    {
        AstProcedure<? extends IProcedureSymbol> procedureAst;
        AstOberonMethodReceiver receiverAst = receviverInfo.getAstNode();
        IOberonMethodReceiverSymbol receiverSymbol = receviverInfo.getSymbol();
        IRecordTypeSymbol typeBoundSymbol = null;
        if (receiverSymbol != null) {
            typeBoundSymbol = receiverSymbol.getBoundTypeSymbol();
        }
        IModulaSymbol symbolInScope = null;
        String procedureName;
        final TextPosition procedurePosition = getTokenPosition();
        
        if (token == IDENTIFIER) {
            builder.beginProduction(PROCEDURE_IDENTIFIER);
            
            procedureName = getTokenText();
            if (typeBoundSymbol != null) {
                symbolInScope = typeBoundSymbol.findSymbolInScope(procedureName);
            } 

            if (symbolInScope != null) {
                if (!(symbolInScope instanceof IOberonMethodSymbol)) {
                    errorIdentifierAlreadyDefined(symbolInScope); 
                }
            }            

            nextToken();
            
            builder.endProduction(PROCEDURE_IDENTIFIER);
        }
        else {
            error(XdsMessages.IdentifierExpected);
            procedureName = null; 
            symbolInScope = null;
        }
        
        EnumSet<SymbolAttribute> attributes = parseExportMarker(false);

        modifierParser.parseProtection(parentSymbol, typeResolver);

        OberonMethodTypeSymbol procedureTypeSymbol = createOberonMethodTypeSymbol(
                procedureName, language, attributes, 
                receiverSymbol, parentSymbol
        );
        
        // Oberon-2 method scope is not defined when receiver symbol was parsed
        if (receiverSymbol != null) {
            OberonMethodReceiverSymbol newReceiverSymbol = recreateOberonMethodReceiverSymbol(
                receiverSymbol, procedureTypeSymbol
            );
            setAstSymbol(receiverAst, newReceiverSymbol);
            receiverSymbol = newReceiverSymbol;
        }
        
        parseProcedureHeader(procedureTypeSymbol, typeResolver);
        
        parseToken(SEMICOLON);
        
        if (isForwardDeclaration) {
        	procedureTypeSymbol.addAttribute(SymbolAttribute.FORWARD_DECLARATION);
            AstOberonMethodForwardDeclaration procedureForwarDeclAst; 
            procedureForwarDeclAst = builder.changeProduction(OBERON_METHOD_FORWARD_DECLARATION); 
            procedureAst = procedureForwarDeclAst;

            OberonMethodDefinitionSymbol procedureDefSymbol = createOberonMethodDefinitionSymbol(
                    procedureName, procedurePosition, procedureTypeSymbol, parentSymbol
            );
            procedureDefSymbol.addAttribute(SymbolAttribute.FORWARD_DECLARATION);
            
            setAstSymbol(procedureForwarDeclAst, procedureDefSymbol);
            addSymbolForResolving(procedureDefSymbol);
            if (procedureName != null) {
                registerOberonMethodForwardDeclarationSymbol( 
                    procedureDefSymbol, procedurePosition, symbolInScope, typeBoundSymbol, 
                    parentSymbol 
                );
            }
        }
        else {
            // parse procedure body
            AstOberonMethodDeclaration procedureDeclAst; 
            procedureDeclAst = builder.changeProduction(OBERON_METHOD_DECLARATION); 
            procedureAst = procedureDeclAst;
            addProcedureFrameNode(procedureDeclAst, procKeyword);

            OberonMethodDeclarationSymbol procedureDeclSymbol;
            procedureDeclSymbol = provideOberonMethodDeclarationSymbol( 
                procedureName, procedurePosition, procedureTypeSymbol, 
                symbolInScope, typeBoundSymbol, parentSymbol 
            );
            setSymbolPosition(procedureDeclSymbol, procedurePosition);
            addNameRegion(procedureDeclSymbol, procedureName, procedurePosition);

            setAstSymbol(procedureDeclAst, procedureDeclSymbol);
            addSymbolForResolving(procedureDeclSymbol);
            
            // only odf-files include method declarations without body
            if (!settings.isOdfSource()) {
                parseProcedureBlock(procedureDeclSymbol, procedureDeclSymbol); 
                parseToken(SEMICOLON);
            }
        }
        
        return procedureAst;
    }
    

    /**
     * ProcedureDeclaration = ProcedureHeading ";" <br>
     *                      ( ProcedureBlock ProcedureIdentifier | "FORWARD" ) ";" <br>
     *                      
     * ProcedureHeading = "PROCEDURE" ProcedureIdentifier <br>
     *                    ( [FormalParameters] | FormalParameters ":" ResultType ) <br>
     * ProcedureIdentifier = identifier <br>
     * 
     * ProcedureOberonDeclaration  = "PROCEDURE" [Receiver] IdentDef [FormalPars] ";" ProcedureBlock Identifier <br>
     * ProcedureOberonForwardDeclaration = "PROCEDURE" "^" [Receiver] IdentDef [FormalPars] <br>
     * IdentDef = Identifier [" * " | "-"] <br>
     */
    private void parseProcedureDeclaration(ISymbolWithProcedures parentScope, ISymbolWithScope typeResolver) {
        Assert.isTrue(PROCEDURE_KEYWORD == token);
        PstNode procKeyword = builder.getLastNode(); 
        boolean hasBody = !isDefinitionModule;
        boolean isOberonForwardDeclaration = false;
        
        AstProcedureDefinition procedureDefAst = builder.beginProduction(PROCEDURE_DEFINITION);
        AstProcedure<? extends IProcedureSymbol> procedureAst = procedureDefAst; 

        nextToken();

        modifierParser.parseProcedureModifiers(parentScope, typeResolver);
        XdsLanguage language = modifierParser.language;
        if (language == XdsLanguage.Oberon2) {
            language = XdsLanguage.Modula2;
        }

        if ((BAR == token) && settings.isOberon()) {
            isOberonForwardDeclaration = true;
            hasBody = false;
            nextToken();
        }
        
        OberonReceiverInfo receviverInfo = parseOberonMethodReceiver(parentScope, typeResolver);
        
        if (isOberonMethod) {
            procedureAst = parseOberonMethodDeclaration( 
                parentScope, typeResolver, receviverInfo, language, 
                isOberonForwardDeclaration, procKeyword
            );
        }
        else {
            boolean isExtenal = false;
            // parse general procedure declaration
            if (MINUS == token) {
                // TODO mark as code procedure
                nextToken();
            }
            else if (PLUS == token) {
                error(XdsMessages.InterruptProceduresNotImplemented);
                nextToken();
            } 
            else if (SLASH == token) {
                if (!settings.xdsExtensions() && !XdsLanguage.EXTERNAL_PROCEDURES.contains(language)) {
                    error(XdsMessages.LanguageIsNotValidForExternalProcedure);
                }
                procedureDefAst = builder.changeProduction(PROCEDURE_EXTERNAL_SPECIFICATION);
                procedureAst = procedureDefAst;

                isExtenal = true;
                hasBody = false;
                nextToken();
            } 
            else if (TIMES == token) {
                nextToken();
            }
            
            IModulaSymbol symbolInScope;
            String procedureName;
            final TextPosition procedurePosition = getTokenPosition();
            if (token == IDENTIFIER) {
                builder.beginProduction(PROCEDURE_IDENTIFIER);
                
                procedureName = getTokenText();
                symbolInScope = parentScope.findSymbolInScope(procedureName);

                if (symbolInScope != null) {
                    if (isDefinitionModule || !(symbolInScope instanceof IProcedureSymbol)) {
// TODO required to support of Oberon methods                    
// TODO actual position is required
//                      error(XdsMessages.IdentifierAlreadyDefined, procedureName, "???", -1, -1); 
                    }
                }            

                nextToken();
                
                builder.endProduction(PROCEDURE_IDENTIFIER);
            }
            else {
                error(XdsMessages.IdentifierExpected);
                procedureName = null; 
                symbolInScope = null;
            }
            
            EnumSet<SymbolAttribute> attributes = parseExportMarker(false);

            modifierParser.parseProtection(parentScope, typeResolver);
            
            ProcedureTypeSymbol procedureTypeSymbol = createProcedureTypeSymbol(
                    procedureName, procedurePosition, language, attributes, parentScope
            );

            parseProcedureHeader(procedureTypeSymbol, typeResolver);
            
            parseToken(SEMICOLON);

            if ((FORWARD_KEYWORD == token) || isOberonForwardDeclaration) {
                if (isDefinitionModule) {
                    error(XdsMessages.NotAllowedInDefinitionModule);            
                }
                procedureTypeSymbol.addAttribute(SymbolAttribute.FORWARD_DECLARATION);
                AstProcedureForwardDeclaration procedureForwardDeclAst;
                procedureForwardDeclAst = builder.changeProduction(PROCEDURE_FORWARD_DECLARATION);
                procedureAst = procedureForwardDeclAst;

                ProcedureDefinitionSymbol procedureForwardDeclSymbol = createProcedureDefinitionSymbol(
                    procedureName, procedurePosition, procedureTypeSymbol, parentScope
                );
                procedureForwardDeclSymbol.addAttribute(SymbolAttribute.FORWARD_DECLARATION);
                
                setAstSymbol(procedureForwardDeclAst, procedureForwardDeclSymbol);
                addSymbolForResolving(procedureForwardDeclSymbol);
                if (procedureName != null) {
                    registerProcedureForwardDeclarationSymbol( 
                        procedureForwardDeclSymbol, procedurePosition, symbolInScope, parentScope 
                    );
                }
                
                if (!isOberonForwardDeclaration) {
                    nextToken();
                    parseToken(SEMICOLON);
                }
            } 
            else if (hasBody) {  
                // parse procedure body
                AstProcedureDeclaration procedureDeclAst = builder.changeProduction(PROCEDURE_DECLARATION); 
                procedureAst = procedureDeclAst;
                addProcedureFrameNode(procedureDeclAst, procKeyword);

                ProcedureDeclarationSymbol procedureDeclSymbol = provideProcedureDeclarationSymbol( 
                    procedureName, procedurePosition, procedureTypeSymbol, symbolInScope, parentScope 
                );
                setSymbolPosition(procedureDeclSymbol, procedurePosition);
                addNameRegion(procedureDeclSymbol, procedureName, procedurePosition);
                
                setAstSymbol(procedureDeclAst, procedureDeclSymbol);
                addSymbolForResolving(procedureDeclSymbol);
                
                parseProcedureBlock(procedureDeclSymbol, procedureDeclSymbol); 
                parseToken(SEMICOLON);
            } 
            else {
                ProcedureDefinitionSymbol procedureDefSymbol = createProcedureDefinitionSymbol(
                    procedureName, procedurePosition, procedureTypeSymbol, parentScope
                );
                setAstSymbol(procedureDefAst, procedureDefSymbol);
                addSymbolForResolving(procedureDefSymbol);
                
                if (procedureName != null) {
                    if (isDefinitionModule) {
                        if (symbolInScope == null) {
                            parentScope.addProcedure(procedureDefSymbol);
                        }
                    }
                    else if (isExtenal) {
                        procedureDefSymbol.addAttribute(SymbolAttribute.EXTERNAL);
                        if (symbolInScope == null) {
                            parentScope.addProcedure(procedureDefSymbol);
                        }
                    }
                }
            }
        }
        
        builder.endProduction(procedureAst);
    }


    /**
     * TypeDeclaration = "TYPE" identifier "=" TypeDefinition  |  OpaqueTypeDefinition <br>
     * OpaqueTypeDefinition = identifier <br>
     */
    private void parseTypeDeclaration(ISymbolWithDefinitions parentScope, ISymbolWithScope typeResolver) {
        Assert.isTrue(token == TYPE_KEYWORD);
        builder.beginProduction(TYPE_DECLARATION_BLOCK);
        declaredType = null;
        
        nextToken();
        
        while (token == IDENTIFIER) {
            AstTypeDeclaration typeAst = builder.beginProduction(TYPE_DECLARATION);
            
            builder.beginProduction(DECORATED_IDENTIFIER);

            final String typeName = getTokenText();
            final TextPosition typePosition = getTokenPosition();
            
            IModulaSymbol symbolInScope = parentScope.findSymbolInScope(typeName);
            boolean isNewType = (symbolInScope == null);
            if (!isNewType) {
                if (!(symbolInScope instanceof ForwardTypeSymbol) && !(symbolInScope instanceof OpaqueTypeSymbol)) {
                    errorIdentifierAlreadyDefined(symbolInScope); 
                }
            }
            
            nextToken();

            modifierParser.parseDirectLanguageSpec(parentScope, typeResolver);
            XdsLanguage language = modifierParser.language;
            
            EnumSet<SymbolAttribute> attributes = parseExportMarker(false);
            
            builder.endProduction(DECORATED_IDENTIFIER);
            
            ITypeSymbol typeSymbol = null;
            if (token == EQU) {
                if (isNewType) {
                    nextToken();
                    typeSymbol = parseTypeDefinition(typeName, null, parentScope, parentScope, true, false);
                    if (typeSymbol instanceof EnumTypeSynonymSymbol) {
                        processEnumTypeSynonymDeclaration(parentScope, (EnumTypeSynonymSymbol)typeSymbol);
                    }
                }
                else if (symbolInScope instanceof ForwardTypeSymbol) {
                    nextToken();
                    typeSymbol = parseTypeDefinition(typeName, null, parentScope, parentScope, true, false);

                    if (typeSymbol instanceof IOpaqueTypeSymbol) {
                        error(XdsMessages.ForwardTypeCannotBeOpaque);
                        typeSymbol = null;
                    }
                    else {
                        ((ForwardTypeSymbol)symbolInScope).setActualTypeSymbol(createRef(typeSymbol));
                        replaceForwardTypeSymbol((ForwardTypeSymbol)symbolInScope, typeSymbol);
                        parentScope.addType(typeSymbol);                
                    }
                }
                else if (symbolInScope instanceof OpaqueTypeSymbol) {
                    if (isDefinitionModule) {
                        error(XdsMessages.NotAllowedInDefinitionModule);
                    }
                    nextToken();
                    typeSymbol = parseTypeDefinition(typeName, null, parentScope, parentScope, false, false);
                    
                    if ((typeSymbol instanceof IPointerTypeSymbol) || (typeSymbol instanceof IOpaqueTypeSymbol))  {
                        ((OpaqueTypeSymbol)symbolInScope).setActualTypeSymbol(typeSymbol);
                        parentScope.addType(typeSymbol);                
                    }
                    else {
                        error(XdsMessages.ObjectIsNotPointer);
                    }
                }
                else {
                    nextToken();
                    typeSymbol = parseTypeDefinition(typeName, null, parentScope, parentScope, true, false);
                }
            } 
            else if ((token == SEMICOLON) && isDefinitionModule && isNewType) {
                typeSymbol = new OpaqueTypeSymbol(typeName, parentScope);
            }
            else {
                errorExpectedSymbol(EQU);
            }

            if (typeSymbol == null) {
                typeSymbol = new InvalidTypeSymbol(typeName, parentScope);
            }
            typeSymbol.setLanguage(language);
            typeSymbol.addAttributes(attributes);
            setSymbolPosition(typeSymbol, typePosition);
            setAstSymbol(typeAst, typeSymbol);
            addSymbolForResolving(typeSymbol);
            
            if (isNewType) {
                parentScope.addType(typeSymbol);                
            }
            
            parseToken(SEMICOLON);
            declaredType = null;
            
            builder.endProduction(typeAst);
        }
        
        declaredType = null;
        builder.endProduction(TYPE_DECLARATION_BLOCK);
    }


    /**
     * Import            = SimpleImport | UnqualifiedImport  <br>
     * SimpleImport      = "IMPORT" ModuleIdentList ";"  <br>
     * UnqualifiedImport = "FROM" identifier "IMPORT" IdentList ";"  <br>
     *  <br>
     * ModuleIdentList   = ModuleIdent {"," ModuleIdent}  <br>
     * ModuleIdent       = [ModuleAlias ":="] identifier   <br>
     * ModuleAlias       = identifier  <br>
     *  <br>
     * IdentList         = identifier {"," identifier}  <br>
     * 
     * @param parentSymbol current module
     */
    private void parseImport(IModuleSymbol parentSymbol) {
        builder.beginProduction(IMPORTS);

        while ((token == IMPORT_KEYWORD) || (token == FROM_KEYWORD)) {
            AstImportStatement importStatementAst   = null;
            IModuleSymbol      importedModuleSymbol = null;

            boolean isFromClause = (FROM_KEYWORD == token);
            if (isFromClause) {
                importStatementAst = builder.beginProduction(UNQUALIFIED_IMPORT);
                
                nextToken();
                if (token == IDENTIFIER) {
                    AstModuleName importedModuleAst;
                    importedModuleAst = builder.beginProduction(MODULE_NAME);

                    final String importedModuleName = getTokenText(); 
                    importedModuleSymbol = resolveImport(
                        parentSymbol, importedModuleName, parserMonitor
                    );
                    checkImport(parentSymbol, importedModuleSymbol);
                    setAstSymbol(importedModuleAst, importedModuleSymbol);
                    addSymbolUsage(importedModuleSymbol);
             
                    nextToken();
                    
                    builder.endProduction(importedModuleAst);
                }
                else {
                    recoverOnUnexpectedToken(IDENTIFIER);
                }
            }
            else {
                importStatementAst = builder.beginProduction(SIMPLE_IMPORT);
            }
            
            parseToken(IMPORT_KEYWORD);
            builder.beginProduction(IMPORT_FRAGMENT_LIST);
            
            while (true) {
                if (token == IDENTIFIER) {
                    AstImportFragment importFragmentAst; 
                    if (isFromClause) {
                        importFragmentAst = builder.beginProduction(UNQUALIFIED_IMPORT_FRAGMENT);
                        
                        AstSimpleName importedEntityAst; 
                        importedEntityAst = builder.beginProduction(SIMPLE_NAME);
                        
                        final String importedEntityName = getTokenText();
                        if (importedModuleSymbol != null) {
                            IModulaSymbol symbol = importedModuleSymbol.findSymbolInScope(importedEntityName, true);
                            if (symbol != null) {
                                addImport(symbol, parentSymbol);
                            }
                            else {
                            	symbol = createUnresolvedModuleSymbol(importedEntityName);
                            }
                            setAstSymbol(importFragmentAst, symbol);
                            setAstSymbol(importedEntityAst, symbol);
                            addSymbolUsage(symbol);
                        }
                        
                        nextToken();
             
                        builder.endProduction(importedEntityAst);
                    }
                    else {
                        importFragmentAst = builder.beginProduction(SIMPLE_IMPORT_FRAGMENT);
                        
                        builder.beginProduction(ALIAS_DECLARATION);
                        AstModuleAlias aliasNameAst = builder.beginProduction(MODULE_ALIAS_NAME);
                        
                        String importedModuleName = getTokenText();
                        final TextPosition identPosition = getTokenPosition();
                        
                        nextToken();
                        
                        if (token == BECOMES) {
                            if (!(settings.xdsExtensions() || settings.isOberon())) {
                                error(XdsMessages.ExtensionNotAllowed, XdsMessages.RenamingInImport);
                            }
                            String aliasModuleName = importedModuleName; 
                            builder.endProduction(aliasNameAst);
                            
                            nextToken();
                            
                            AstModuleName importedModuleAst = builder.beginProduction(MODULE_NAME);
                            
                            if (token == IDENTIFIER) {
                                importedModuleName = getTokenText(); 

                                if (parentSymbol.getName().equals(importedModuleName)) {
                                    error(XdsMessages.RecursiveImportDisabled);
                                }              
                                isSystemImported = isSystemImported 
                                                || XdsStandardNames.SYSTEM.equals(importedModuleName);
                                importedModuleSymbol = resolveImport(
                                    parentSymbol, importedModuleName, parserMonitor
                                );
                                checkImport(parentSymbol, importedModuleSymbol);
                                setAstSymbol(importedModuleAst, importedModuleSymbol);
                                addSymbolUsage(importedModuleSymbol);
                            }

                            ModuleAliasSymbol moduleAliasSymbol = createModuleAliasSymbol(
                                aliasModuleName, identPosition, importedModuleSymbol, parentSymbol
                            );
                            setAstSymbol(aliasNameAst, moduleAliasSymbol);
                            addSymbolForResolving(moduleAliasSymbol);
                            addImport(moduleAliasSymbol, parentSymbol);

                            parseToken(IDENTIFIER);
                            
                            builder.endProduction(importedModuleAst);
                            builder.endProduction(ALIAS_DECLARATION);
                        } 
                        else {
                            AstModuleName importedModuleAst = builder.changeProduction(MODULE_NAME);
                            
                            if (parentSymbol.getName().equals(importedModuleName)) {
                                error(XdsMessages.RecursiveImportDisabled);
                            }
                            isSystemImported = isSystemImported 
                                            || XdsStandardNames.SYSTEM.equals(importedModuleName);
                            importedModuleSymbol = resolveImport(
                                parentSymbol, importedModuleName, parserMonitor
                            );
                            checkImport(parentSymbol, importedModuleSymbol);
                            
                            setAstSymbol(importedModuleAst, importedModuleSymbol);
                            addImport(importedModuleSymbol, parentSymbol);
                            addSymbolUsage(importedModuleSymbol, identPosition);

                            builder.endProduction(importedModuleAst);
                            builder.dropProduction(ALIAS_DECLARATION);
                        }
                        
                        setAstSymbol(importFragmentAst, importedModuleSymbol);
                    }
                    builder.endProduction(importFragmentAst);
                }
                else {
                    recoverOnUnexpectedToken(IDENTIFIER);
                }
                
                if (COMMA == token) {
                    nextToken();
                } 
                else if (IDENTIFIER == token) {
                    errorExpectedSymbol(COMMA);
                } 
                else {
                    break;
                }
            }
            
            builder.endProduction(IMPORT_FRAGMENT_LIST);
            
            parseToken(SEMICOLON);
            builder.endProduction(importStatementAst);
        }
        builder.endProduction(IMPORTS);
    }

    
    private void parseLocalModuleImport(ILocalModuleSymbol parentSymbol) {
        boolean isFromClause; 

        while ((token == IMPORT_KEYWORD) || (token == FROM_KEYWORD)) {
            AstImportStatement importStatementAst = null;
            ISymbolWithScope   importScope = null;

            isFromClause = (FROM_KEYWORD == token);
            if (isFromClause) {
                importStatementAst = builder.beginProduction(UNQUALIFIED_IMPORT);

                nextToken();
                if (token == IDENTIFIER) {
                    AstSimpleName importScopeAst; 
                    importScopeAst = builder.beginProduction(SIMPLE_NAME);
                    
                    final String importScopeName = getTokenText();
                    IModulaSymbol symbol = parentSymbol.getParentScope().findSymbolInScope(importScopeName);
                    if (symbol == null) {
                        error(XdsMessages.UndeclaredIdentifier, importScopeName);
                    }
                    else {
                        setAstSymbol(importScopeAst, symbol);
                        addSymbolUsage(symbol);
                        if (symbol instanceof ISymbolWithScope) {
                            importScope = (ISymbolWithScope)symbol;
                        }
                        else {
                            error(XdsMessages.ImportFromObjectIllegal, importScopeName);
                        }
                    }
                    nextToken();
                    
                    builder.endProduction(importScopeAst);
                }
                else {
                    recoverOnUnexpectedToken(IDENTIFIER);
                }
            }
            else {
                importStatementAst = builder.beginProduction(SIMPLE_IMPORT);
            }

            parseToken(IMPORT_KEYWORD);
            builder.beginProduction(IMPORT_FRAGMENT_LIST);
            
            while (true) {
                if (token == IDENTIFIER) {
                    IModulaSymbol symbol = null;
                    AstImportFragment importFragment; 
                    AstSimpleName importedEntityAst; 

                    String importedEntityName = getTokenText();

                    if (isFromClause) {
                        importFragment = builder.beginProduction(UNQUALIFIED_IMPORT_FRAGMENT);
                        importedEntityAst = builder.beginProduction(SIMPLE_NAME);

                        if (importScope != null) {
                            symbol = (importScope instanceof IDefinitionModuleSymbol)
                                   ? importScope.findSymbolInScope(importedEntityName, true)
                                   : importScope.findSymbolInScope(importedEntityName);
                        }
                    }
                    else {
                        importFragment = builder.beginProduction(SIMPLE_IMPORT_FRAGMENT);
                        importedEntityAst = builder.beginProduction(SIMPLE_NAME);

                        symbol = parentSymbol.getParentScope().resolveName(importedEntityName);
                    }

                    if (symbol != null) {
                        parentSymbol.addImport(createRef(symbol));
                        setAstSymbol(importFragment, symbol);
                        setAstSymbol(importedEntityAst, symbol);
                        addSymbolUsage(symbol);
                    }
                    else if (importScope != null) {
                        error(XdsMessages.UndeclaredIdentifier, importedEntityName);
                    }
                    
                    nextToken();

                    builder.endProduction(importedEntityAst);
                    builder.endProduction(importFragment);
                }
                else {
                    recoverOnUnexpectedToken(IDENTIFIER);
                }

                if (COMMA == token) {
                    nextToken();
                } 
                else if (IDENTIFIER == token) {
                    errorExpectedSymbol(COMMA);
                } 
                else {
                    break;
                }
            }

            builder.endProduction(IMPORT_FRAGMENT_LIST);
            
            parseToken(SEMICOLON);
            builder.endProduction(importStatementAst);
        }
    }

    
    private EnumSet<SymbolAttribute> parseExportMarker(boolean m2ReadOnly) {
        EnumSet<SymbolAttribute> attributes = SymbolAttribute.createEmptySet();
        builder.beginProduction(EXPORT_MARKER);
        
        if (isDefinitionModule) {
            attributes.add(SymbolAttribute.PUBLIC);
        }

        if (settings.isOberon() || settings.isOdfSource()) {
            if (TIMES == token) {
                attributes.add(SymbolAttribute.PUBLIC);
                nextToken();
            } 
            else if (MINUS == token) {
                attributes.add(SymbolAttribute.PUBLIC);
                attributes.add(SymbolAttribute.READ_ONLY);
                nextToken();
            }            
        }
        else {
            if (m2ReadOnly) {
                // check Modula-2 "READ ONLY" mark
                if (token == MINUS) {
                    if (!settings.xdsExtensions()) {
                        error(XdsMessages.ExtensionNotAllowed, XdsMessages.ReadOnlyTag);                        
                    }
                    if (isDefinitionModule) {
                        attributes.add(SymbolAttribute.PUBLIC);
                        attributes.add(SymbolAttribute.READ_ONLY);
                    } 
                    else {
                        error(XdsMessages.AllowedOnlyInGlobalScope);                        
                    }
                    nextToken();
                }
            }
            else if ((token == TIMES) && settings.xdsExtensions()) {
                attributes.add(SymbolAttribute.PUBLIC);
                nextToken();
            }
        }
        
        builder.endProduction(EXPORT_MARKER);
        return attributes;
    }

    
    /**
     * Parser of protection expression and direct language specification.
     */
    private final class ModifierParser {

        XdsLanguage language;
        @SuppressWarnings("unused")
		long        protection;
        @SuppressWarnings("unused")
		XdsProcedureAttribute procedureAttribute;

        XdsLanguage defaultModuleLanguage() {
            return settings.isOberon() ?  XdsLanguage.Oberon2
                                           :  XdsLanguage.Modula2;
        }
        
        void setDefaultModuleLanguage() {
            language = defaultModuleLanguage();
        }
        
        XdsLanguage languageOf(IModulaSymbolScope parentSymbol) {
        	XdsLanguage language = null;
        	while(parentSymbol != null)
        	{
        		if (parentSymbol instanceof ISymbolWithScope) {
    				ISymbolWithScope scopedSymbol = (ISymbolWithScope) parentSymbol;
    				language = scopedSymbol.getLanguage();
    				break;
    			}
        		parentSymbol = parentSymbol.getParentScope();
        	}
        	return language;
        }
        
        void parseDirectLanguageSpec(IModulaSymbolScope parentSymbol, ISymbolWithScope typeResolver) {
        	parseDirectLanguageSpec(parentSymbol, typeResolver, null);
        }
        
        /**
         * The desired language can be specified as "[" language "]", where 
         * language can be a string or integer constant expression.
         * @param parentSymbol 
         */
        void parseDirectLanguageSpec(IModulaSymbolScope parentSymbol, ISymbolWithScope typeResolver, XdsLanguage defaultLanguage) {
            XdsLanguage language = null;
            
            if (token == LBRACKET) {
                builder.beginProduction(DIRECT_LANGUAGE_SPEC);
                nextToken();

                if (!isSystemImported && !settings.xdsExtensions())
                    error(XdsMessages.ExtensionNotAllowed, XdsMessages.DirectLanguageSpecification);
        
                if (token == STRING_LITERAL) {
                    String text = getTokenText();
                    language = XdsLanguage.NAME_TO_LANGUAGE.get(text);
                    if (language == null) {
                        error(XdsMessages.InvalidLanguageValue);
                    }
                    nextToken();
                }
                else if (INTEGER_LITERAL_SET.contains(token)) {
                    TextPosition expressionPosition = getTokenPosition();
                    expressionParser.parseConstantExpression(parentSymbol, typeResolver);
                    long index = expressionParser.getIntegerValue();
                    language = XdsLanguage.INDEX_TO_LANGUAGE.get(index);
                    if (language == null) {
                        error(expressionPosition, XdsMessages.InvalidLanguageValue);
                    }
                }
                else { 
                    expressionParser.parseConstantExpression(parentSymbol, typeResolver);
//TODO                        error(XdsMessages.IncompatibleTypes);
                }
                
                parseToken(RBRACKET);
                builder.endProduction(DIRECT_LANGUAGE_SPEC);
            }
            
            if (language != null) {
                this.language = language;
            }
            else {
            	this.language = defaultLanguage != null? defaultLanguage : languageOf(parentSymbol);
            }
        }
        
        void parseProcedureModifiers(IModulaSymbolScope parentSymbol, ISymbolWithScope typeResolver) {
        	setDefaultModuleLanguage();
            procedureAttribute = XdsProcedureAttribute.Default;
            
            if (token == LBRACKET) {
                nextToken();
                
                if (!isSystemImported && !settings.xdsExtensions())
                    error(XdsMessages.ExtensionNotAllowed, XdsMessages.DirectLanguageSpecification);

                do {
                    if (token == STRING_LITERAL) {
                        String text = getTokenText();
                        if (XdsLanguage.NAME_TO_LANGUAGE.containsKey(text)) {
                            language = XdsLanguage.NAME_TO_LANGUAGE.get(text);
                        }
                        else {
                            XdsProcedureAttribute attribute = XdsProcedureAttribute.parseText(text);
                            if (attribute != null) {
                                if (isDefinitionModule) {
                                    error(XdsMessages.NotAllowedInDefinitionModule);
                                }
                                procedureAttribute = attribute;
                            } 
                            else {
                                error(XdsMessages.InvalidLanguageValue);
                            }
                        }
                        
                        nextToken();
                    }
                    else if (INTEGER_LITERAL_SET.contains(token)) {
                        TextPosition expressionPosition = getTokenPosition();
                        expressionParser.parseConstantExpression(parentSymbol, typeResolver);
                        long index = expressionParser.getIntegerValue();
                        language = XdsLanguage.INDEX_TO_LANGUAGE.get(index);
                        if (language == null) {
                            error(expressionPosition, XdsMessages.InvalidLanguageValue);
                        }
                    }
                    else {
                        expressionParser.parseConstantExpression(parentSymbol, typeResolver);
//TODO                        error(XdsMessages.IncompatibleTypes);
                    }
                    
                    
                } while (token == COMMA);
                
                parseToken(RBRACKET);
            }
        }
        
        
        /**
         * A program module, implementation module or local module may specify, by including  
         * protection in its heading, that execution of the enclosed statement sequence is 
         * protected.
         * 
         * ModuleHeading = MODULE ident [ Protection ] ";" <br>
         * Protection    = "[" ConstExpression "]" <br>
         * 
         * The protection expression should be of the PROTECTION type. The PROTECTION type  
         * is an elementary type with at least two values: INTERRUPTIBLE and UNINTERRUPTIBLE.
         *  
         * @param parentSymbol 
         */
        void parseProtection(IModulaSymbolScope parentSymbol, ISymbolWithScope typeResolver) {
            if (token == LBRACKET) {
                nextToken();
                if (token != RBRACKET) {
                    expressionParser.parseConstantExpression(parentSymbol, typeResolver);
                    protection = expressionParser.getIntegerValue();
                }
                parseToken(RBRACKET);
            }
            if (token == LBRACKET) {
                parseDirectLanguageSpec(parentSymbol, typeResolver);
            }
        }
        
        
        /**
         * In ISO Modula-2, an absolute address may be specified for a variable 
         * after its name in square brackets.
         * 
         * AbsoluteAddress  = "[" AddressTypeValue "]" <br>
         * AddressTypeValue = ConstExpression <br>
         * 
         * @param parentSymbol 
         */
        public void parseAbsoluteAddress(IModulaSymbolScope parentSymbol, ISymbolWithScope typeResolver) {
            if (token == LBRACKET) {
                nextToken();
                if (token != RBRACKET) {
                    expressionParser.parseExpression(parentSymbol, typeResolver);
                }
                parseToken(RBRACKET);
            }            
        }
        
        
    }
    
    private void setTypeOfConstantSymbol( ConstantSymbol<ITypeSymbol> constantSymbol
                                        , ISymbolWithDefinitions parentSymbol, ISymbolWithScope typeResolver ) 
    {
        IModulaSymbol valueSymbol = expressionParser.parseConstantExpression(parentSymbol, typeResolver);
        ITypeSymbol constantTypeSymbol = getSymbolType(valueSymbol); 
        if (constantTypeSymbol != null) {
            IModulaSymbolReference<ITypeSymbol> ref = createRef(constantTypeSymbol);
            constantSymbol.setTypeSymbol(ref);
        }
    }
    


    /**
     * If last node is 'expectedToken' and it is inside 'nodeType' parent
     * then adds this node to this parent frame list.
     *     
     * @param expectedToken
     * @param nodeType - must be instance of IAstFrameNode 
     */
    private void addFrameNode(TokenType expectedToken, ModulaCompositeType<? extends PstCompositeNode> nodeType) {
        Object nodeClass = nodeType.getNodeClass();
        PstNode node = builder.getLastNode();
        if ((node != null) && (node.getElementType() == expectedToken)) {
            PstNode parent = builder.getCurrentProduction();
            while (parent != null) {
                if (parent.getClass().equals(nodeClass) && parent instanceof IAstFrameNode) {
                    ((IAstFrameNode)parent).addFrameNode(node);
                    return;
                }
                parent = parent.getParent();
            }
        }
    }


    private void addProcedureFrameNode(TokenType token) {
        PstNode node = builder.getLastNode();
        if ((node != null) && (node.getElementType() == token)) {
            PstNode parent = builder.getCurrentProduction();
            while (parent != null) {
                if (parent instanceof AstProcedureDeclaration) {
                    AstProcedureDeclaration hostAst = (AstProcedureDeclaration)parent;
                    hostAst.addFrameNode(node);
                    return;
                }
                else if (parent instanceof AstOberonMethodDeclaration) {
                    AstOberonMethodDeclaration hostAst = (AstOberonMethodDeclaration)parent;
                    hostAst.addFrameNode(node);
                    return;
                }
                parent = parent.getParent();
            }
        }
    }
    
    private void addModuleFrameNode(TokenType token) {
        PstNode node = builder.getLastNode();
        if ((node != null) && (node.getElementType() == token)) {
            PstNode parent = builder.getCurrentProduction();
            while (parent != null) {
                if (parent instanceof AstModule) {
                    AstModule hostAst = (AstModule)parent;
                    hostAst.addFrameNode(node);
                    return;
                }
                parent = parent.getParent();
            }
        }
    }


    private void addProcedureFrameNode( AstProcedureDeclaration parent
                                      , PstNode node ) 
    {
        if (parent != null) {
            parent.addFrameNode(node);
        }
    }
    
    private void addProcedureFrameNode( AstOberonMethodDeclaration parent
                                      , PstNode node ) 
    {
        if (parent != null) {
            parent.addFrameNode(node);
        }
    }

    
    private static class OberonReceiverInfo 
    {
        private final AstOberonMethodReceiver astNode;
        private final IOberonMethodReceiverSymbol symbol;
        
        public OberonReceiverInfo( AstOberonMethodReceiver astNode
                                 , IOberonMethodReceiverSymbol symbol ) 
        {
            this.astNode = astNode;
            this.symbol  = symbol;
        }

        public AstOberonMethodReceiver getAstNode() {
            return astNode;
        }

        public IOberonMethodReceiverSymbol getSymbol() {
            return symbol;
        }
    }
}
