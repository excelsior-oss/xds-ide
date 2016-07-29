package com.excelsior.xds.parser.modula;

import static com.excelsior.xds.parser.internal.modula.symbol.reference.ReferenceFactory.createStaticRef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Assert;

import com.excelsior.xds.core.builders.BuildSettings;
import com.excelsior.xds.core.sdk.XdsOptions;
import com.excelsior.xds.core.text.ITextRegion;
import com.excelsior.xds.core.text.TextPosition;
import com.excelsior.xds.core.text.TextRegion;
import com.excelsior.xds.core.utils.collections.Pair;
import com.excelsior.xds.parser.commons.IParserEventListener;
import com.excelsior.xds.parser.commons.symbol.IMutableBlockSymbolTextBinding;
import com.excelsior.xds.parser.internal.modula.nls.XdsMessages;
import com.excelsior.xds.parser.internal.modula.symbol.ConstantSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.DefinitionModuleSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.EnumElementSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.FinallyBodySymbol;
import com.excelsior.xds.parser.internal.modula.symbol.FormalParameterSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.ImplementationModuleSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.LocalModuleSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.MainModuleSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.ModulaSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.ModuleAliasSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.ModuleBodySymbol;
import com.excelsior.xds.parser.internal.modula.symbol.OberonMethodDeclarationSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.OberonMethodDefinitionSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.OberonMethodReceiverSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.ProcedureBodySymbol;
import com.excelsior.xds.parser.internal.modula.symbol.ProcedureDeclarationSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.ProcedureDefinitionSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.ProgramModuleSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.RecordFieldSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.RecordVariantSelectorSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.StandardModuleSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.UnknownModulaSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.UnresovedModuleSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.VariableSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.WithStatementScope;
import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceResolver;
import com.excelsior.xds.parser.internal.modula.symbol.reference.IStaticModulaSymbolReference;
import com.excelsior.xds.parser.internal.modula.symbol.reference.InternalReferenceUtils;
import com.excelsior.xds.parser.internal.modula.symbol.reference.ReferenceFactory;
import com.excelsior.xds.parser.internal.modula.symbol.reference.ReferenceLocation;
import com.excelsior.xds.parser.internal.modula.symbol.type.ArrayTypeSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.type.EnumTypeSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.type.EnumTypeSynonymSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.type.ForwardTypeSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.type.ForwardTypeSynonymSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.type.OberonMethodTypeSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.type.OpenArrayTypeSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.type.PointerTypeSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.type.ProcedureTypeSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.type.RecordTypeSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.type.SetTypeSymbol;
import com.excelsior.xds.parser.modula.ast.AstBody;
import com.excelsior.xds.parser.modula.ast.AstSimpleName;
import com.excelsior.xds.parser.modula.ast.AstSymbolRef;
import com.excelsior.xds.parser.modula.ast.types.AstPointerType;
import com.excelsior.xds.parser.modula.symbol.IBlockBodySymbol;
import com.excelsior.xds.parser.modula.symbol.IEnumElementSymbol;
import com.excelsior.xds.parser.modula.symbol.IFormalParameterSymbol;
import com.excelsior.xds.parser.modula.symbol.IImplemantationModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IInvalidModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbolScope;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IOberonMethodReceiverSymbol;
import com.excelsior.xds.parser.modula.symbol.IProcedureBodySymbol;
import com.excelsior.xds.parser.modula.symbol.IProcedureDeclarationSymbol;
import com.excelsior.xds.parser.modula.symbol.IProcedureDefinitionSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithDeclarations;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithDefinitions;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithProcedures;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithType;
import com.excelsior.xds.parser.modula.symbol.IVariableSymbol;
import com.excelsior.xds.parser.modula.symbol.SymbolAttribute;
import com.excelsior.xds.parser.modula.symbol.binding.IImportResolver;
import com.excelsior.xds.parser.modula.symbol.binding.ModulaSymbolCache;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.reference.IProxyReference;
import com.excelsior.xds.parser.modula.symbol.reference.ReferenceUtils;
import com.excelsior.xds.parser.modula.symbol.type.IArrayTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IEnumTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IForwardTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IOpaqueTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IOrdinalTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IPointerTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IRecordTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;
import com.excelsior.xds.parser.modula.utils.ModulaSymbolUtils;

/**
 * The parser layer to perform verification of compatibility rules and 
 * manipulate <code>IModulaSymbol</code>.
 */
public class XdsSymbolParser extends XdsPragmaParser 
{
	// used for unit tests
	public static final String REFERENCE_IS_RESOLVED_INCORRECTLY_MSG_BASE = "Reference is resolved incorrectly"; //$NON-NLS-1$
	
	private static final String REFERENCE_IS_RESOLVED_INCORRECTLY_MSG_DETAILS = " \n%s\nexpected: %s\nresolved: %s\n";  //$NON-NLS-1$

	/** Initial XDS compiler settings. They are used to parse imported modules. */
    private final BuildSettings buildSettings;
    
    private final IImportResolver importResolver;
    
    /** Current module is a definition module. */
    protected boolean isDefinitionModule;

    /** The top level module is being parsed. */
    protected IModuleSymbol hostModuleSymbol;

    /** Number of anonymous symbol to resolve collision of anonymous names */
    private int anonymousSymbolNumber;
    
    /** Number of symbol collisions to resolve collision of names */
    private int symbolCollisionNumber;
    
    private final List<IEnumTypeSymbol> foreignEnumTypeSynonyms;
    
    private List<IModulaSymbol> declaredSymbols = new ArrayList<IModulaSymbol>();
    
    private List<IProxyReference<IModulaSymbol>> symbolReferences = new ArrayList<IProxyReference<IModulaSymbol>>();
    
    protected IReferenceFactory refFactory = new LocalReferenceFactory();

    public XdsSymbolParser( IFileStore sourceFile, CharSequence chars
                          , XdsSettings xdsSettings
                          , IImportResolver importResolver
                          , IParserEventListener reporter 
                          , IXdsParserMonitor monitor )
    {
        super(sourceFile, chars, xdsSettings, reporter);
        this.foreignEnumTypeSynonyms = new ArrayList<IEnumTypeSymbol>(4);
        /** Initial XDS compiler settings. They are used to parse imported modules. */
        this.buildSettings = xdsSettings.getSettings();
        this.importResolver = importResolver;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        super.reset();
        anonymousSymbolNumber = 0;
        symbolCollisionNumber = 0;
        hostModuleSymbol = null;
        foreignEnumTypeSynonyms.clear();
        declaredSymbols.clear();
        symbolReferences.clear();
    }

    /**
     * Finalize construction of the parsed top level module.
     * Definition module exports enumeration type elements of synonyms to foreign
     * enumeration types. It is a feature of XDS sym-file reader.     
     */
    protected void finalizeHostModule() {
        if (isDefinitionModule) {
            for (IEnumTypeSymbol typeSymbol : foreignEnumTypeSynonyms) {
                addEnumConstants(hostModuleSymbol, typeSymbol);
            }
        }
        
        IReferenceResolver referenceResolver = InternalReferenceUtils.getReferenceResolver(hostModuleSymbol);
        if (referenceResolver != null) {
            for (IModulaSymbol symbol : declaredSymbols) {
                ReferenceLocation referenceLocation = referenceResolver.createReferenceLocation(symbol); 
                IModulaSymbol resolvedSymbol = referenceResolver.resolve(referenceLocation);
                if ((resolvedSymbol != null) & (symbol != resolvedSymbol)) {
                    markAsAlreadyDefined(symbol);
                }
                
                InternalReferenceUtils.addSymbolForResolving(hostModuleSymbol, symbol);
            }
        }
    }

    
    /**
     * Checks that a symbol is defined outside of the parsed module or its local modules.
     *  
     * @param symbol, a symbol to be checked
     * 
     * @return @true if symbol is defined inside given module.
     */
    protected boolean isForeignSymbol(IModulaSymbol symbol) {
        return !ModulaSymbolUtils.isSymbolFromModule(hostModuleSymbol, symbol);
    }

    
    /**
     * Returns language of the current module.
     */
    protected XdsLanguage getModuleLaguage() {
        XdsLanguage moduleLanguage = XdsLanguage.Modula2;
        if (hostModuleSymbol != null) {
            moduleLanguage = hostModuleSymbol.getLanguage();
        }
        else if (settings.isOberon()) {
            moduleLanguage = XdsLanguage.Oberon2;
        }
        return moduleLanguage;
    }
    
    /**
     * Returns symbol from the standard module: super and SYSTEM module.
     */
    protected IModulaSymbol getStandardSymbol(String name) {
        IModulaSymbol symbol = null;
        StandardModuleSymbol moduleSymbol = ModulaSymbolCache.getSuperModule(getModuleLaguage());
        symbol = moduleSymbol.resolveName(name);
        if (symbol == null) {
            moduleSymbol = ModulaSymbolCache.getSystemModule(getModuleLaguage());
            symbol = moduleSymbol.resolveName(name);
        }
        return symbol;
    }

    /**
     * Returns symbol from the standard module: super and SYSTEM module in the type-safe way.
     */
    @SuppressWarnings("unchecked")
    protected <T extends IModulaSymbol> T getStandardSymbol(String name, Class<T> targetClass) 
    {
        IModulaSymbol symbol = getStandardSymbol(name);
        if ((symbol != null) && targetClass.isAssignableFrom(symbol.getClass())) {
            return (T)symbol;
        }
        return null;
    }
    
    protected String createAnonymousName( String hostName, String qualifier
                                        , ISymbolWithScope parentScope )
    {
        String name;
        if (hostName != null) {
            name = hostName;
        }
        else {
            name = parentScope.getName() + IModulaSymbol.ANONYMOUS_NAME_TAG;
        }
        name += IModulaSymbol.ANONYMOUS_NAME_TAG + qualifier;
        
        if (parentScope.findSymbolInScope(name) != null) {
            anonymousSymbolNumber++;
            name = name + anonymousSymbolNumber;
        }
        return name;
    }

    
    protected void markAsAlreadyDefined(IModulaSymbol symbol) {
        symbol.addAttribute(SymbolAttribute.ALREADY_DEFINED);
        if (symbol instanceof ModulaSymbol) {
            symbolCollisionNumber++;
            ((ModulaSymbol)symbol).setNameCollosionId(symbolCollisionNumber);
        }
    }
        
    
    
    /**
     * Generates unique anonymous name, if the given one is <code>null</code>.
     * 
     * @param name - original name, my be <code>null</code>.
     * @param hostName - the name of host symbol, my be <code>null</code>,  used to construct name of anonymous type.
     * @param qualifier - suffix to construct anonymous name, must not be <code>null</code>  
     * @param parentScope - parent scope to check unique of anonymous name  
     * 
     * @return original or generated unique name.
     */
    protected String generateNameIfNull( String name, String hostName, String qualifier
                                       , ISymbolWithScope parentScope ) 
    {
        if (name == null) {
            name = createAnonymousName(hostName, qualifier, parentScope);
        }
//        if (name == null) {
//            anonymousEntityNumber++;
//            name = parentScope.getName() + "_" + qualifier  //$NON-NLS-1$
//                 + IModulaSymbol.ANONYMOUS_NAME_TAG 
//                 + anonymousEntityNumber;    
//        }
        return name;
    }

    
    /**
     * Adds enumeration type elements as constants to the given scope.
     * Elements of enumeration types will be added to the scope,  
     * if such identifier was not already registered. 
     *   
     * @param destinationScope - scope to be updated
     * @param typeSymbol - enumeration type to be processed.
     */
    private void addEnumConstants( ISymbolWithDefinitions destinationScope
                                 , IEnumTypeSymbol typeSymbol ) 
    {
        Collection<IEnumElementSymbol> elements = typeSymbol.getElements();
        for (IEnumElementSymbol element : elements) {
            // Elements of enumeration types should be imported in the module scope 
            // if such identifier was not already registered. 
            boolean alreadyDefined = destinationScope.findSymbolInScope(element.getName()) != null;
            if (! alreadyDefined) {
                destinationScope.addEnumElements(element);
            }
        }
    }

    /**
     * Adds enumeration type elements from synonym declaration of enumeration type, 
     * if it is required.
     *   
     * @param destinationScope - scope to be updated
     * @param typeSymbol - enumeration type to be processed.
     */
    protected void processEnumTypeSynonymDeclaration( ISymbolWithDefinitions destinationScope
                                                    , EnumTypeSynonymSymbol typeSymbol ) 
    {
        if (isForeignSymbol(typeSymbol.getOriginalSymbol())) {
            if (settings.topSpeedExtensions()) {
                addEnumConstants(destinationScope, typeSymbol);
            }
            else if (isDefinitionModule) {
                foreignEnumTypeSynonyms.add(typeSymbol);
            }
        }
    }
    
    
    /**
     * Returns the nearest parent scope with symbol definitions ability. 
     */
    protected static ISymbolWithDefinitions getParentScopeWithSymbolDefinitions(ISymbolWithScope parentSymbol)
    {
        do {
            if (parentSymbol instanceof ISymbolWithDefinitions) {
                return (ISymbolWithDefinitions) parentSymbol;
            }
            parentSymbol = parentSymbol.getParentScope();
        } while (parentSymbol != null);
        return null;
    }
    
    /**
     * Adds symbol to the import slot of the module's symbol.
     * 
     * @param importedSymbol - symbol to be added to the import slot.
     * @param moduleSymbol - scope to be updated
     */
    protected void addImport(IModulaSymbol importedSymbol, IModuleSymbol moduleSymbol) 
    {
        boolean alreadyDefined = checkSymbolAlreadyDefined(importedSymbol, moduleSymbol);
        if (! alreadyDefined) {
            moduleSymbol.addImport(createRef(importedSymbol));
            if (importedSymbol instanceof IEnumTypeSymbol) {
                IEnumTypeSymbol enumTypeSymbol = (IEnumTypeSymbol)importedSymbol;
                Collection<IEnumElementSymbol> elements = enumTypeSymbol.getElements();
                for (IEnumElementSymbol element : elements) {
                    // Elements of enumeration types should be imported in the module scope 
                    // if such identifier was not already registered. 
                    alreadyDefined = moduleSymbol.findSymbolInScope(element.getName()) != null;
                    if (! alreadyDefined) {
                        moduleSymbol.addImport(createRef((IModulaSymbol)element));
                    }
                }
            }
        }
    }

    /**
     * Appends the current token position to the list of usages of the specified symbol. 
     *
     * @param symbol the symbol with which the specified position is to be associated.
     */
    protected void addSymbolUsage(IModulaSymbol symbol) {
        addSymbolUsage(symbol, getTokenPosition());
    }
    
    /**
     * Appends the specified position to the list of usages of the specified symbol. 
     *
     * @param symbol the symbol with which the specified position is to be associated.
     * @param usagePosition position to be associated with the specified symbol.
     */
    protected void addSymbolUsage(IModulaSymbol symbol, TextPosition usagePosition) {
        if (symbol != null) {
            hostModuleSymbol.addSymbolUsage(symbol, usagePosition);
        }
    }
    
    
    protected void addSymbolForResolving(IModulaSymbol symbol) {
        if (symbol != null) {
            declaredSymbols.add(symbol);
        }
    }
    
    
    protected IModuleSymbol resolveModuleSymbol( String moduleName
                                               , XdsLanguage language
                                               , IXdsParserMonitor monitor )
    {
        IModuleSymbol moduleSymbol = importResolver.resolveModuleSymbol(language, moduleName, sourceFile);
        return moduleSymbol;
    }    

    /**
     * @param parentSymbol current module
     * @param importedModuleName - name of imported module  
     * @param monitor - handler of parsing events
     * @return symbol of imported module, or <tt>null</tt> if module was not found
     */
    protected IModuleSymbol resolveImport( IModuleSymbol parentSymbol
                                         , String importedModuleName
                                         , IXdsParserMonitor monitor ) 
    {
        IModuleSymbol moduleSymbol = null;
        if (parentSymbol.getName().equals(importedModuleName)) {
            error(XdsMessages.RecursiveImportDisabled);
        }
        else {            
            XdsLanguage language = parentSymbol.getLanguage();
            moduleSymbol = resolveModuleSymbol(importedModuleName, language, monitor);
            if (moduleSymbol == null) {
                error(XdsMessages.UnresolvedModuleName, importedModuleName);
                moduleSymbol = new UnresovedModuleSymbol(importedModuleName, language);
                setSymbolCurrentPosition(moduleSymbol);
            }
            else if (moduleSymbol.getLanguage() == XdsLanguage.Oberon2) {
            }
        }
        return moduleSymbol;
    }
    
    
    /**
     * Exports symbols from local module to the parent scope.
     * 
     * @param localModuleSymbol 
     * @param exportedIdentifiers identifiers to be exported
     */
    protected void processLocalModuleExport( LocalModuleSymbol localModuleSymbol
                                           , Map<String, Pair<AstSimpleName, TextPosition>> exportedIdentifiers )
    {
        for (Entry<String, Pair<AstSimpleName, TextPosition>> exportedIdentifierEntry : exportedIdentifiers.entrySet()) {
            String identifier = exportedIdentifierEntry.getKey();
            AstSimpleName identifierAst = exportedIdentifierEntry.getValue().getFirst();
            TextPosition position = exportedIdentifierEntry.getValue().getSecond();
            IModulaSymbol exportedSymbol = localModuleSymbol.findSymbolInScope(identifier);
            if (exportedSymbol != null) {
                exportedSymbol.addAttribute(SymbolAttribute.EXPORTED);
                localModuleSymbol.addSymbolInExport(exportedSymbol);
                addSymbolUsage(exportedSymbol, position);

                setAstSymbol(identifierAst, exportedSymbol);
                
                if (exportedSymbol instanceof IEnumTypeSymbol) {
                    // Elements of enumeration types should be exported in the module scope 
                    // if such identifier was not already registered. 
                    IEnumTypeSymbol enumTypeSymbol = (IEnumTypeSymbol)exportedSymbol; 
                    Collection<IEnumElementSymbol> elements = enumTypeSymbol.getElements();
                    for (IEnumElementSymbol element : elements) {
                        element.addAttribute(SymbolAttribute.EXPORTED);
                        localModuleSymbol.addSymbolInExport(element);
                    }
                }
            }
            else {
                error(XdsMessages.UnsatisfiedExportedObject, identifier);
            }
        }
    }
    
    protected void replaceForwardTypeSymbol( IForwardTypeSymbol forwardTypeSymbol
                                           , ITypeSymbol actualTypesymbol ) 
    {
        if (forwardTypeSymbol instanceof ForwardTypeSynonymSymbol) {
            actualTypesymbol = actualTypesymbol.createSynonym( actualTypesymbol.getName()
                                                             , actualTypesymbol.getParentScope(), refFactory );
        }
            
        Collection<IModulaSymbol> usagesInSymbols = forwardTypeSymbol.getUsages();
        for (IModulaSymbol usage : usagesInSymbols) {
            if (usage instanceof PointerTypeSymbol) {
                setBoundTypeSymbol((PointerTypeSymbol)usage, actualTypesymbol);
            }
            else if (usage instanceof VariableSymbol) {
                setTypeSymbol((VariableSymbol)usage, actualTypesymbol);
            }
            else if (usage instanceof FormalParameterSymbol) {
                setTypeSymbol((FormalParameterSymbol)usage, actualTypesymbol);
            }
            else if (usage instanceof ForwardTypeSynonymSymbol) {
                replaceForwardTypeSymbol((ForwardTypeSynonymSymbol)usage, actualTypesymbol);
            }
        }
        
        Collection<TextPosition> usagesInText = hostModuleSymbol.getSymbolUsages(forwardTypeSymbol);
        if (usagesInText != null) {
            for (TextPosition tokenPosition : usagesInText) {
                addSymbolUsage(actualTypesymbol, tokenPosition);
            }
        }
        
        forwardTypeSymbol.releaseUsages();
    }
    
    
    protected IForwardTypeSymbol createAndRegisterForwardType( String name
                                                             , ISymbolWithScope parentScope ) 
    {
        IModulaSymbolScope scope = parentScope;
        while (scope instanceof IRecordTypeSymbol) {
            scope = scope.getParentScope();
        }
        
        IForwardTypeSymbol typeSymbol = new ForwardTypeSymbol(name, parentScope);
        if (scope instanceof ISymbolWithDefinitions) {
            ((ISymbolWithDefinitions)scope).addType(typeSymbol);
        }
        
        return typeSymbol; 
    }
    

    
    /**
     * This routine must be called only when parsing program module
     */
    protected void registerProcedureForwardDeclarationSymbol( ProcedureDefinitionSymbol procDefSymbol
                                                            , TextPosition position  
                                                            , IModulaSymbol symbolInScope 
                                                            , ISymbolWithProcedures parentSymbol )
    {
        if (symbolInScope == null) {
            // it is the first FORWARD procedure declaration 
            ProcedureDeclarationSymbol procDeclSymbol = 
                    new ProcedureDeclarationSymbol(procDefSymbol, parentSymbol);
            setSymbolPosition(procDeclSymbol, position);
            parentSymbol.addProcedure(procDeclSymbol);
        }
        else if (symbolInScope instanceof ProcedureDeclarationSymbol) {
            // it seems that 'symbolInScope' is the FORWARD procedure declaration 
            ProcedureDeclarationSymbol procDeclSymbol = ((ProcedureDeclarationSymbol)symbolInScope); 
            if (procDeclSymbol.isForwardDeclaration()) {
                // TODO check procedure signature
                procDeclSymbol.addForwardDeclaration(createStaticRef(procDefSymbol));
            }
            else {
                errorIdentifierAlreadyDefined(symbolInScope); 
            }
        }
        else if (symbolInScope instanceof ProcedureDefinitionSymbol) {
            if (isForeignSymbol(symbolInScope)) {
                // it is the first FORWARD procedure declaration.
                // 'symbolInScope' comes from definition module 
                // FORWARD procedure declaration is redundant 
                // TODO check procedure signature
                ProcedureDeclarationSymbol procDeclSymbol = 
                        new ProcedureDeclarationSymbol(procDefSymbol, parentSymbol);
                setSymbolPosition(procDeclSymbol, position);
                parentSymbol.addProcedure(procDeclSymbol);

                procDefSymbol.addAttributes(symbolInScope.getAttributes());
                procDeclSymbol.addAttributes(symbolInScope.getAttributes());
                procDeclSymbol.setDefinitionSymbol(createRef((IProcedureDefinitionSymbol)symbolInScope));
                
                ((ProcedureDefinitionSymbol)symbolInScope).setDeclarationSymbol(createRef((IProcedureDeclarationSymbol)procDeclSymbol));
            }
        }
    }

    /**
     * This routine must be called only when parsing program module
     */
    protected void registerOberonMethodForwardDeclarationSymbol( OberonMethodDefinitionSymbol procDefSymbol
                                                               , TextPosition position  
                                                               , IModulaSymbol symbolInScope
                                                               , IRecordTypeSymbol typeBoundSymbol
                                                               , ISymbolWithProcedures parentSymbol )
    {
        if (symbolInScope == null) {
            // this it the first FORWARD procedure declaration 
            OberonMethodDeclarationSymbol procDeclSymbol = 
                    new OberonMethodDeclarationSymbol(procDefSymbol, parentSymbol);
            setSymbolPosition(procDeclSymbol, position);
            if (typeBoundSymbol != null) {
                typeBoundSymbol.addProcedure(procDeclSymbol);
            }
        }
        else if (symbolInScope instanceof OberonMethodDeclarationSymbol) {
            // it seems that 'symbolInScope' is the FORWARD procedure declaration 
            OberonMethodDeclarationSymbol procDeclSymbol = ((OberonMethodDeclarationSymbol)symbolInScope); 
            if (procDeclSymbol.isForwardDeclaration()) {
                // TODO check procedure signature
                procDeclSymbol.addForwardDeclaration(createStaticRef((ProcedureDefinitionSymbol)procDefSymbol));
            }
            else {
                errorIdentifierAlreadyDefined(symbolInScope); 
            }
        }
    }

    
    /**
     * Called for procedures with body only. 
     * 
     * @param typeSymbol - type symbol of currently parsed procedure 
     * @param symbolInScope - symbol with the same name as procHeaderSymbol.getName()
     * @param parentSymbol - procedure or module symbol
     * @return
     */
    protected ProcedureDeclarationSymbol provideProcedureDeclarationSymbol
                                         ( String procedureName 
                                         , TextPosition position  
                                         , ProcedureTypeSymbol typeSymbol
                                         , IModulaSymbol symbolInScope 
                                         , ISymbolWithProcedures parentSymbol )
    {
        ProcedureDeclarationSymbol procDeclSymbol;

        if (symbolInScope instanceof ProcedureDeclarationSymbol) {
            // it seems that 'symbolInScope' is the FORWARD procedure declaration 
            procDeclSymbol = ((ProcedureDeclarationSymbol) symbolInScope);
            if (procDeclSymbol.isForwardDeclaration()) {
                // TODO check procedure signature
                procDeclSymbol.finalizeDeclaration(createRef(typeSymbol));
            }
            else {
                errorIdentifierAlreadyDefined(symbolInScope, position); 
                markAsAlreadyDefined(typeSymbol);
                procDeclSymbol = new ProcedureDeclarationSymbol( 
                    procedureName, createRef(typeSymbol), parentSymbol 
                );
                markAsAlreadyDefined(procDeclSymbol);
            }
        }
        else {
            // 'symbolInScope' is null or non procedure declaration symbol (the latter case is already reported)
            procDeclSymbol = new ProcedureDeclarationSymbol( 
                procedureName, createRef(typeSymbol), parentSymbol 
            );
            if (isDefinitionModule) {
                // we are in the definition module
                error(XdsMessages.NotAllowedInDefinitionModule);
            }
            else {
                // we are in the implementation module 
                if (symbolInScope == null) {
                    parentSymbol.addProcedure(procDeclSymbol);
                }
                else if (symbolInScope instanceof ProcedureDefinitionSymbol) {
                    // it seems that 'symbolInScope' comes from definition module 
                    // TODO check procedure signature
                    ProcedureDefinitionSymbol definitionSymbol = ((ProcedureDefinitionSymbol)symbolInScope);
                    procDeclSymbol.addAttributes(definitionSymbol.getAttributes());
                    definitionSymbol.setDeclarationSymbol(createRef((IProcedureDeclarationSymbol)procDeclSymbol));
                    procDeclSymbol.setDefinitionSymbol(createRef((IProcedureDefinitionSymbol)definitionSymbol));
                    parentSymbol.addProcedure(procDeclSymbol);
                }
            }
        }
        
        return procDeclSymbol;
    }
    
    
    /**
     * Called for procedures with body only. 
     * 
     * @param typeSymbol - currently parsed procedure 
     * @param symbolInScope - symbol with the same name as procHeaderSymbol.getName()
     * @param scope - parent procedure or module symbol
     * @return
     */
    protected OberonMethodDeclarationSymbol provideOberonMethodDeclarationSymbol
                                            ( String procedureName
                                            , TextPosition procedurePosition  
                                            , OberonMethodTypeSymbol typeSymbol
                                            , IModulaSymbol symbolInScope 
                                            , IRecordTypeSymbol typeBoundSymbol
                                            , ISymbolWithProcedures scope )
    {
        OberonMethodDeclarationSymbol procDeclSymbol;

        if (symbolInScope instanceof OberonMethodDeclarationSymbol) {
            // it seems that 'symbolInScope' is the FORWARD procedure declaration 
            procDeclSymbol = ((OberonMethodDeclarationSymbol) symbolInScope);
            if (procDeclSymbol.isForwardDeclaration()) {
                // TODO check procedure signature
                procDeclSymbol.finalizeDeclaration(createRef((ProcedureTypeSymbol)typeSymbol));
            }
            else {
                errorIdentifierAlreadyDefined(symbolInScope, procedurePosition); 
                markAsAlreadyDefined(typeSymbol);
                procDeclSymbol = new OberonMethodDeclarationSymbol( 
                    procedureName, createRef((ProcedureTypeSymbol)typeSymbol), scope 
                );
                markAsAlreadyDefined(procDeclSymbol);
            }
        }
        else {
            // 'symbolInScope' is null or non procedure declaration symbol (the latter case is already reported)
            procDeclSymbol = new OberonMethodDeclarationSymbol( 
                procedureName, createRef((ProcedureTypeSymbol)typeSymbol), scope 
            );
            if (symbolInScope == null) {
                if (typeBoundSymbol != null) {
                    typeBoundSymbol.addProcedure(procDeclSymbol);
                }
            }
        }

        return procDeclSymbol;
    }
    
    
    
    /**
     * Verifies whether a symbol was already defined in the given scope.
     * The error be reported if symbol was already defined.
     */
    protected boolean checkSymbolAlreadyDefined( IModulaSymbol symbol
                                               , ISymbolWithScope parentScope ) 
    {
        if (symbol != null) {
            return checkSymbolAlreadyDefined(symbol.getName(), parentScope);
        }
        return true;  // may be it prevent parser for other manipulation with <code>null</code>.
    }


    /**
     * Verifies whether a symbol with this name was already defined in the given scope.
     * The error be reported if symbol with this name was already defined.
     */
    protected boolean checkSymbolAlreadyDefined( String symbolName
                                               , ISymbolWithScope parentScope ) 
    {
        IModulaSymbol symbolInScope = parentScope.findSymbolInScope(symbolName);
        boolean alreadyDefined = (symbolInScope != null); 
        if (alreadyDefined) {
            errorIdentifierAlreadyDefined(symbolInScope); 
        }
        return alreadyDefined;
    }
    
    
    /**
     * Verifies whether a symbol with this name was already defined in 
     * the current declaration statement or in the given scope.
     * The error be reported if symbol with this name was already defined.
     */
    protected boolean checkSymbolAlreadyDefined( String symbolName
                                               , ISymbolWithScope parentScope 
                                               , List<? extends IModulaSymbol> symbolsInDeclaration )
    {
        for (IModulaSymbol declaredSymbol : symbolsInDeclaration) {
            if (symbolName.equals(declaredSymbol.getName())) {
                errorIdentifierAlreadyDefined(declaredSymbol);
                return true;
            }
        }
        return checkSymbolAlreadyDefined(symbolName, parentScope);
    }


    /**
     * Casts the type symbol to the <code>IOrdinalTypeSymbol</code> and reports  
     * a error if it is impossible.
     * 
     * @param typeSymbol - symbol to be verified
     * 
     * @return <code>IOrdinalTypeSymbol</code> or <code>null</code> 
     */
    protected IOrdinalTypeSymbol checkOrdinalTypeSymbol(ITypeSymbol typeSymbol) {
        if (typeSymbol instanceof IOrdinalTypeSymbol) {
            return (IOrdinalTypeSymbol)typeSymbol;
        }
        else {
            error(XdsMessages.ExpectedOrdinalType);
            return null;
        }
    }
    
    /**
     * Verifies and reports the presence of initialization problem of imported module.
     */
    protected void checkImport( IModuleSymbol currentModuleSymbol
                              , IModuleSymbol importedModule ) 
    {
        boolean raiseModuleInitIssue = ! currentModuleSymbol.hasConstructor() 
                                    && (importedModule != null) 
                                    && importedModule.hasConstructor(); 
        if (raiseModuleInitIssue) {   
            warning(XdsMessages.ModuleConstructorWillNotBeInvoked, importedModule.getName());
        }
    }


    protected ITypeSymbol getSymbolType(IModulaSymbol symbol) {
        ITypeSymbol typeSymbol = null;

        if (!(symbol instanceof IInvalidModulaSymbol)) {
            if (symbol instanceof ITypeSymbol) {
                typeSymbol = (ITypeSymbol)symbol;
            }
            else if (symbol instanceof ISymbolWithType) {
                typeSymbol = ((ISymbolWithType)symbol).getTypeSymbol();
            }
        }
            
        return typeSymbol;
    }
    
    
    protected IModulaSymbol makeAutoDereference(IModulaSymbol symbol) {
        ITypeSymbol typeSymbol = getSymbolType(symbol);
        boolean isDerefenceRequired = (typeSymbol instanceof IPointerTypeSymbol)
                                   || (typeSymbol instanceof IOpaqueTypeSymbol);
        if (isDerefenceRequired) {
            symbol = makeDereference(symbol);
        }
        return symbol;
    }
    
    protected ITypeSymbol makeDereference(IModulaSymbol symbol) {
        ITypeSymbol referedSymbol = null;
        if ((symbol != null) && !(symbol instanceof IInvalidModulaSymbol)) {
            ITypeSymbol typeSymbol = getSymbolType(symbol);
            if (typeSymbol instanceof IPointerTypeSymbol) {
//TODO Check that this pointer is not a forward type, error 97                
                referedSymbol = ((IPointerTypeSymbol)typeSymbol).getBoundTypeSymbol();
            }
            else {
//TODO support of expressions is required                    
//                error(XdsMessages.ObjectIsNotPointer);
            }
        }
        return referedSymbol;
    }
    
    
    protected IModulaSymbol makeAccess(IModulaSymbol symbol, String accessedName) {
        IModulaSymbol accessedSymbol = null;

        ITypeSymbol typeSymbol = getSymbolType(symbol);
        if ((typeSymbol != null) && !(typeSymbol instanceof IInvalidModulaSymbol)) {
            if (typeSymbol instanceof IRecordTypeSymbol) {
                accessedSymbol = ((IRecordTypeSymbol)typeSymbol).resolveName(accessedName);
                if (accessedSymbol != null) {
//TODO check PUBLIC attribute for symbols from foreign modules.
                }
                else {
//TODO support of expressions is required                    
//                    error(XdsMessages.UndeclaredIdentifier, accessedName);
                }
            }
            else {
//TODO support of expressions is required                    
//                error(XdsMessages.ObjectIsNotRecord);
            }
        }
        
        return accessedSymbol;
    }
    

    protected ITypeSymbol makeIndex(IModulaSymbol symbol) {
        ITypeSymbol elementTypeSymbol = null;

        ITypeSymbol typeSymbol = getSymbolType(symbol);
        if ((typeSymbol != null) && !(typeSymbol instanceof IInvalidModulaSymbol)) {
            if (typeSymbol instanceof IArrayTypeSymbol) {
//TODO check index type compatibility.
                elementTypeSymbol = ((IArrayTypeSymbol)typeSymbol).getElementTypeSymbol();
            }
            else {
//TODO support of expressions is required                    
//                error(XdsMessages.ObjectIsNotArray);                
            }
        }
        
        return elementTypeSymbol;
    }
    
    
    /**
     * Appends the text region to the collection of symbol name regions.
     */
    protected void addNameRegion( IMutableBlockSymbolTextBinding symbol
                                , String name, TextPosition position ) 
    {
        if ((symbol != null) && (position != null) && StringUtils.isNotEmpty(name)) 
        {
            ITextRegion region = new TextRegion(position.getOffset(), name.length());
            symbol.addNameTextRegion(region);
        }
    }
    
    
    /**
     * Sets position in which symbol is defined.
     * 
     * @param symbol symbol to be updated
     * @param position position of symbol's name
     */
    protected void setSymbolPosition(IModulaSymbol symbol, TextPosition position) {
        if (symbol instanceof ModulaSymbol) {
            ((ModulaSymbol) symbol).setPosition(position);
        }
    }

    /**
     * Sets the current position of token as position in which symbol is defined.
     * 
     * @param symbol symbol to be updated
     */
    protected void setSymbolCurrentPosition(IModulaSymbol symbol) {
        setSymbolPosition(symbol, getTokenPosition());
    }

    
    protected <T extends IModulaSymbol> void setAstSymbol(AstSymbolRef<T> nodeAst, T symbol) {
        if (nodeAst != null) {
            nodeAst.setSymbol(createRef(symbol));
        }
    }

    protected <T extends IBlockBodySymbol> void setAstSymbol(AstBody<T> nodeAst, T symbol) {
        if (nodeAst != null) {
            nodeAst.setSymbol(symbol);
        }
    }
    
    
    protected void setTypeSymbol(VariableSymbol variableSymbol, ITypeSymbol typeSymbol) {
        variableSymbol.setTypeSymbol(createRef(typeSymbol));
        if (typeSymbol instanceof IForwardTypeSymbol) {
            ((IForwardTypeSymbol)typeSymbol).addUsage(variableSymbol);
        }        
    }
    
    protected void setTypeSymbol(FormalParameterSymbol parameterSymbol, ITypeSymbol typeSymbol) {
        parameterSymbol.setTypeSymbol(createRef(typeSymbol));
        if (typeSymbol instanceof IForwardTypeSymbol) {
            ((IForwardTypeSymbol)typeSymbol).addUsage(parameterSymbol);
        }        
    }

    protected void setBoundTypeSymbol(PointerTypeSymbol pointerTypeSymbol, ITypeSymbol typeSymbol) {
        pointerTypeSymbol.setBoundType(createRef(typeSymbol));
        if (typeSymbol instanceof IForwardTypeSymbol) {
            ((IForwardTypeSymbol)typeSymbol).addUsage(pointerTypeSymbol);
        }        
    }
    
    
    /**
     * Searches for symbol in the given scope, and (recursively) in all parent scopes.
     * Reports the "undeclared identifier" error if there is no symbol with specified name.
     * 
     * @param symbolName the name of symbol which is to be searched 
     * @param parentScope - scope to be used to search the symbol
     * @return the first symbol with specified name in this and parent scope, or
     *         {@code null} if there is no symbol with specified name.
     */
    protected IModulaSymbol resolveName(String symbolName, IModulaSymbolScope parentScope) 
    {
        IModulaSymbol symbol = parentScope.resolveName(symbolName);
        if (symbol == null) {
            error(XdsMessages.UndeclaredIdentifier, symbolName);
        }
        return symbol;
    }
    
    /**
     * Searches for FOR-loop control variable in the given scope, and (recursively) 
     * in all parent scopes and verifies its correctness.
     * 
     * @param name the name of FOR-loop control variable which is to be searched 
     * @param parentScope - scope to be used to search the FOR-loop control variable
     * @return the first symbol with specified name in this and parent scope, or
     *         {@code null} if there is no symbol with specified name.
     */
    protected IModulaSymbol resolveForLoopControlVariable( String name
                                                         , IModulaSymbolScope parentScope )
    {
        IModulaSymbol controlVariableSymbol = resolveName(name, parentScope);
        if (controlVariableSymbol != null) {
            while (parentScope instanceof WithStatementScope) {
                parentScope = parentScope.getParentScope();
            }
            if (controlVariableSymbol instanceof IFormalParameterSymbol) {
                error(XdsMessages.ForLoopControlVariableMustNotBeFormalParameter);
            }
            else if (!(controlVariableSymbol instanceof IVariableSymbol)) {
                error(XdsMessages.ObjectIsNotVariable);
            }
            else if (parentScope != controlVariableSymbol.getParentScope()) {
                error(XdsMessages.ForLoopControlVariableMustBeLocal);
            }
            else if (controlVariableSymbol.isAttributeSet(SymbolAttribute.EXPORTED)) {
                error(XdsMessages.ForLoopControlVariableCannotBeExported);
            }
            else if (controlVariableSymbol.isAttributeSet(SymbolAttribute.READ_ONLY)) {
                error(XdsMessages.ReadOnlyDesignator);
            }
            else if (controlVariableSymbol.isAttributeSet(SymbolAttribute.VOLATILE)) {
                error(XdsMessages.ControlVariableCannotBeVolatile);
            }

            if (controlVariableSymbol instanceof IVariableSymbol) {
                IVariableSymbol variableSymbol = ((IVariableSymbol)controlVariableSymbol);
                checkOrdinalTypeSymbol(variableSymbol.getTypeSymbol());
            }
        }
        return controlVariableSymbol;
    }
    
    /**
     * Reports the current identifier is already defined for the given symbol.
     * 
     * @param symbol already defined symbol 
     */
    protected void errorIdentifierAlreadyDefined (IModulaSymbol symbol) {
        errorIdentifierAlreadyDefined(symbol, getTokenPosition());
    }

    
    /**
     * Reports the identifier is already defined for the given symbol.
     * 
     * @param symbol the already defined symbol
     * @param position the position of duplicated identifier 
     */
    protected void errorIdentifierAlreadyDefined( IModulaSymbol symbol
                                                , TextPosition position ) 
    {
        String moduleFileName = "";
        IFileStore sourceFileStore = ModulaSymbolUtils.getSourceFileStore(symbol);
        if (sourceFileStore != null) {
            moduleFileName = sourceFileStore.getName();
        }
        
        int line   = 0;
        int column = 0; 
        TextPosition symbolPosition = symbol.getPosition();
        if (symbolPosition != null) {
            line   = symbolPosition.getLine();
            column = symbolPosition.getColumn();
        }
        String name = symbol.getName();
        error( position, name.length(), XdsMessages.IdentifierAlreadyDefined
             , name, moduleFileName, line, column ); 
    }

    /**
     * Checks and reports unresolved FORWARD declarations in the given scope.
     * 
     * @param scope - scope to be verify
     */
    protected void checkForwardSymbols(IModulaSymbolScope scope)
    {
        for (IModulaSymbol symbol : scope) {
            if (symbol instanceof ProcedureDeclarationSymbol) {
                ProcedureDeclarationSymbol procDecl = (ProcedureDeclarationSymbol)symbol;
                if (procDecl.isForwardDeclaration()) {
                    String procName = procDecl.getName();
                    error( procDecl.getPosition(), procName.length()
                         , XdsMessages.ProcedureNotImplemented, procName ); 
                    procDecl.removeAttribute(SymbolAttribute.FORWARD_DECLARATION);
                    addSymbolForResolving(procDecl);
                    procDecl.addAttribute(SymbolAttribute.FORWARD_DECLARATION);
                }
            }
            else if (symbol instanceof IForwardTypeSymbol) {
                IForwardTypeSymbol forwardType = (IForwardTypeSymbol)symbol;
                if (forwardType.getActualTypeSymbol() == null) {
                    String typeName = forwardType.getName();
                    error( forwardType.getPosition(), typeName.length()
                         , XdsMessages.UnsatisfiedForwardType, typeName ); 
                }
            }
            else if (symbol instanceof IRecordTypeSymbol) {
                IRecordTypeSymbol recordType = (IRecordTypeSymbol)symbol;
                checkForwardSymbols(recordType);
            }
        }
    }
    
    public static void clearGlobalTables() {
        ModulaSymbolCache.instance().clear();
    }
    
    @SuppressWarnings("unchecked")
    protected <T extends IModulaSymbol> IModulaSymbolReference<T> createRef(T symbol) {
        IStaticModulaSymbolReference<T> staticRef = ReferenceUtils.createStaticRef(symbol);
        
        IProxyReference<T> ref = ReferenceFactory.createProxyRef(staticRef);
        symbolReferences.add((IProxyReference<IModulaSymbol>) ref);
        return ref;
    }
    
    /**
     * Replaces static reference to the symbols by the dynamic ones.
     */
    protected void replaceStaticRefs() {
        for (IProxyReference<IModulaSymbol> proxyRef : symbolReferences) {
            Assert.isTrue(proxyRef.getReference() instanceof IStaticModulaSymbolReference<?>);
            
            IModulaSymbol staticSymbol = proxyRef.resolve();
            IModulaSymbolReference<IModulaSymbol> dynamicRef = ReferenceFactory.createRef(staticSymbol);
            proxyRef.setReference(dynamicRef);

            if (CHECK_REFERENCE_INTEGRITY) {
                IModulaSymbol dynamicSymbol = ReferenceUtils.resolve(dynamicRef); 
                if (!(staticSymbol instanceof IInvalidModulaSymbol) && !ObjectUtils.equals(staticSymbol, dynamicSymbol)) {
                    System.out.println("-- staticSymbol: " + staticSymbol.getQualifiedName() );
                    String message = String.format( 
                    		REFERENCE_IS_RESOLVED_INCORRECTLY_MSG_BASE + REFERENCE_IS_RESOLVED_INCORRECTLY_MSG_DETAILS, 
                        dynamicRef, staticSymbol, dynamicSymbol
                    );  
                    error(staticSymbol.getPosition(), staticSymbol.getName().length(), message);
                }
            }
        }
        symbolReferences.clear();
    }
    
    protected void createHostModule( String moduleName
                                   , boolean isImplementation
                                   , XdsLanguage moduleLanguage
                                   , IXdsParserMonitor parserMonitor
                                   , TextPosition modulePosition ) 
    {
        if (isDefinitionModule) {
            hostModuleSymbol = new DefinitionModuleSymbol( 
                moduleName, moduleLanguage, importResolver.createModuleKey(sourceFile), buildSettings, sourceFile, settings.isOdfSource() 
            );
        }
        else if (isImplementation) {
            hostModuleSymbol = new ImplementationModuleSymbol(moduleName, moduleLanguage, importResolver.createModuleKey(sourceFile), buildSettings, sourceFile);
            
            IModuleSymbol definitionModuleSymbol = resolveModuleSymbol(
                moduleName, moduleLanguage, parserMonitor
            );
            if (definitionModuleSymbol instanceof DefinitionModuleSymbol) {
                ImplementationModuleSymbol implementationModuleSymbol = ((ImplementationModuleSymbol)hostModuleSymbol);
                implementationModuleSymbol.setDefinitionModule((DefinitionModuleSymbol)definitionModuleSymbol);
                IModulaSymbolReference<IImplemantationModuleSymbol> ref = createRef((IImplemantationModuleSymbol)implementationModuleSymbol);
                ((DefinitionModuleSymbol)definitionModuleSymbol).setImplemantationModule(ref);
            } 
            else {
                error(XdsMessages.CannotFindDefinitionModule, moduleName);
            }
        }
        else if (settings.isOberon()) {
            if (settings.getOption(XdsOptions.MAIN)) {
                hostModuleSymbol = new MainModuleSymbol(moduleName, moduleLanguage, importResolver.createModuleKey(sourceFile), buildSettings, sourceFile);
            }
            else {
                hostModuleSymbol = new ProgramModuleSymbol(moduleName, moduleLanguage, importResolver.createModuleKey(sourceFile), buildSettings, sourceFile);
            }
        }
        else {
            hostModuleSymbol = new MainModuleSymbol(moduleName, moduleLanguage, importResolver.createModuleKey(sourceFile), buildSettings, sourceFile);
        }
        
        setSymbolPosition(hostModuleSymbol, modulePosition);
        if (settings.getOption(XdsOptions.NOMODULEINIT)) {
            hostModuleSymbol.addAttribute(SymbolAttribute.NOMODULEINIT);
        }
        
        if (hostModuleSymbol instanceof IMutableBlockSymbolTextBinding) {
            if (StringUtils.isNotEmpty(moduleName)) {
                ITextRegion region = new TextRegion(modulePosition.getOffset(), moduleName.length());
                ((IMutableBlockSymbolTextBinding)hostModuleSymbol).setNameTextRegion(region);
                ((IMutableBlockSymbolTextBinding)hostModuleSymbol).addNameTextRegion(region);
            }
        }
    }
    
    protected ModuleBodySymbol createModuleBodySymbol( ISymbolWithDefinitions parentScope
                                                     , String bodyName ) 
    {
        ModuleBodySymbol bodySymbol = new ModuleBodySymbol(bodyName, parentScope);
        if (parentScope instanceof ProgramModuleSymbol) {
            ((ProgramModuleSymbol)parentScope).setModuleBodySymbol(bodySymbol);
        }
        return bodySymbol;
    }
    
    protected ProcedureBodySymbol createProcedureBodySymbol( ISymbolWithDefinitions parentScope
                                                           , String bodyName ) 
    {
        ProcedureBodySymbol bodySymbol = new ProcedureBodySymbol(bodyName, parentScope);
        setSymbolCurrentPosition(bodySymbol);
        if (parentScope instanceof ProcedureDeclarationSymbol) {
            IModulaSymbolReference<IProcedureBodySymbol> ref = createRef((IProcedureBodySymbol)bodySymbol);
            ((ProcedureDeclarationSymbol)parentScope).setProcedureBodySymbol(ref);
        }
        return bodySymbol;
    }
    
    protected FinallyBodySymbol createFinallyBodySymbol(ISymbolWithDefinitions parentScope) 
    {
        FinallyBodySymbol bodySymbol = new FinallyBodySymbol(
            FINALLY_KEYWORD.getDesignator(), parentScope
        );
        if (parentScope instanceof ProgramModuleSymbol) {
            ((ProgramModuleSymbol)parentScope).setFinallyBodySymbol(bodySymbol);
        }
        return bodySymbol;
    }
    
    protected IModulaSymbolScope createWithStatementScope( IModulaSymbolScope parentScope
                                                         , IModulaSymbol recordDesignatorSymbol ) 
    {
        ITypeSymbol recordTypeSymbol = getSymbolType(recordDesignatorSymbol);
        if (recordTypeSymbol instanceof IRecordTypeSymbol) {
            parentScope = new WithStatementScope(createRef((IRecordTypeSymbol)recordTypeSymbol), parentScope);
        }
        return parentScope;
    }
    
    protected ConstantSymbol<ITypeSymbol> createConstantSymbol
                                          ( ISymbolWithDefinitions parentScope
                                          , String constantName ) 
    {
        ConstantSymbol<ITypeSymbol> constantSymbol = new ConstantSymbol<ITypeSymbol>(
            constantName, parentScope
        );
        setSymbolCurrentPosition(constantSymbol);
        return constantSymbol;
    }
    
    protected VariableSymbol createVariableSymbol( String variableName
                                                 , ISymbolWithDefinitions scope ) 
    {
        VariableSymbol variableSymbol = new VariableSymbol(variableName, scope);
        setSymbolCurrentPosition(variableSymbol);
        return variableSymbol;
    }
    
    protected PointerTypeSymbol createPointerTypeSymbol( String typeName
                                                       , String hostName 
                                                       , ISymbolWithScope parentScope
                                                       , AstPointerType typeAst) 
    {
        String  symbolTypeName = generateNameIfNull(
            typeName, hostName, "PointerType", parentScope    //$NON-NLS-1$
        );    
        PointerTypeSymbol typeSymbol = new PointerTypeSymbol(symbolTypeName, parentScope);
        setSymbolCurrentPosition(typeSymbol);
        return typeSymbol;
    }
    
    protected SetTypeSymbol createSetTypeSymbol( String typeName
                                               , String hostName
                                               , boolean isPacked
                                               , ITypeSymbol baseTypeSymbol
                                               , ISymbolWithScope parentScope ) 
    {
        SetTypeSymbol typeSymbol = new SetTypeSymbol(typeName, parentScope, isPacked);
        typeSymbol.setBaseTypeSymbol(checkOrdinalTypeSymbol(baseTypeSymbol));
        setSymbolCurrentPosition(typeSymbol);
        return typeSymbol;
    }
    
    protected IArrayTypeSymbol createOpenArrayTypeSymbol( String typeName
                                                        , String hostName
                                                        , ITypeSymbol elementTypeSymbol
                                                        , ISymbolWithScope parentScope ) 
    {
        typeName = generateNameIfNull(typeName, hostName, "OpenArrayType", parentScope);    //$NON-NLS-1$ 
        IArrayTypeSymbol typeSymbol = new OpenArrayTypeSymbol( 
            typeName, parentScope, getModuleLaguage(), createRef(elementTypeSymbol) 
        );
        setSymbolCurrentPosition(typeSymbol);
        return typeSymbol;
    }
    
    protected IArrayTypeSymbol createArrayTypeSymbol( String typeName
                                                    , String hostName
                                                    , ITypeSymbol elementTypeSymbol
                                                    , IOrdinalTypeSymbol indexTypeSymbol
                                                    , ISymbolWithScope parentScope ) 
    {
        IArrayTypeSymbol typeSymbol;
        typeName = generateNameIfNull(typeName, hostName, "ArrayType", parentScope);    //$NON-NLS-1$ 
        typeSymbol = new ArrayTypeSymbol(typeName, parentScope, createRef(indexTypeSymbol), createRef(elementTypeSymbol));
        setSymbolCurrentPosition(typeSymbol);
        return typeSymbol;
    }
    
    protected RecordTypeSymbol createRecordTypeSymbol( String typeName
                                                     , String hostName
                                                     , IRecordTypeSymbol baseTypeSymbol
                                                     , String qualifier
                                                     , ISymbolWithScope parentScope ) 
    {
        RecordTypeSymbol typeSymbol;
        typeName = generateNameIfNull(typeName, hostName, qualifier, parentScope);    //$NON-NLS-1$
        typeSymbol = new RecordTypeSymbol(typeName, createRef(baseTypeSymbol), parentScope);
        setSymbolCurrentPosition(typeSymbol);
        return typeSymbol;
    }
    
    protected RecordVariantSelectorSymbol createRecordVariantSelectorSymbol
                                          ( String name
                                          , ITypeSymbol typeSymbol
                                          , TextPosition position
                                          , RecordTypeSymbol parentScope ) 
    {
        RecordVariantSelectorSymbol selectorSymbol = new RecordVariantSelectorSymbol(name, parentScope);
        setSymbolPosition(selectorSymbol, position);
        if (isDefinitionModule) {
            selectorSymbol.addAttribute(SymbolAttribute.PUBLIC);
        }
        selectorSymbol.setTypeSymbol(createStaticRef(typeSymbol));
        setSymbolCurrentPosition(selectorSymbol);
        return selectorSymbol;
    }
    
    protected RecordFieldSymbol createRecordFieldSymbol( String name
                                                       , IRecordTypeSymbol parentScope ) 
    {
        RecordFieldSymbol fieldSymbol = new RecordFieldSymbol(name, parentScope);
        setSymbolCurrentPosition(fieldSymbol);
        return fieldSymbol;
    }
    
    protected FormalParameterSymbol createFormalParameterSymbol
                                    ( String name
                                    , int number
                                    , ITypeSymbol parameterTypeSymbol
                                    , ProcedureTypeSymbol parentScope ) 
    {
        FormalParameterSymbol parameterSymbol = new FormalParameterSymbol( 
            name, number, parentScope, createRef(parameterTypeSymbol) 
        );
        parentScope.addParameter(parameterSymbol);
        setSymbolCurrentPosition(parameterSymbol);
        return parameterSymbol;
    }

    protected FormalParameterSymbol createFormalParameterSymbol
                                   ( String name
                                   , int number
                                   , ProcedureTypeSymbol parentScope ) 
    {
        FormalParameterSymbol parameterSymbol = new FormalParameterSymbol(name, number, parentScope);
        setSymbolCurrentPosition(parameterSymbol);
        return parameterSymbol;
    }
    
    protected EnumElementSymbol createEnumElementSymbol( String elementName
                                                       , EnumTypeSymbol enumTypeSymbol ) 
    {
        EnumElementSymbol enumElementSymbol = new EnumElementSymbol(
            elementName, enumTypeSymbol.getParentScope(), 
            createRef((IEnumTypeSymbol)enumTypeSymbol), 
            enumTypeSymbol.getElementCount()
        );
        setSymbolCurrentPosition(enumElementSymbol);
        if (enumTypeSymbol.findSymbolInScope(elementName) == null) {
            enumTypeSymbol.addElement(enumElementSymbol);
        }
        return enumElementSymbol;
    }

    protected EnumTypeSymbol createEnumTypeSymbol( String typeName
                                                 , String hostName
                                                 , ISymbolWithScope parentScope ) 
    {
        EnumTypeSymbol typeSymbol;
        typeName = generateNameIfNull(typeName, hostName, "EnumType", parentScope);    //$NON-NLS-1$ 
        typeSymbol = new EnumTypeSymbol(typeName, parentScope);
        setSymbolCurrentPosition(typeSymbol);
        return typeSymbol;
    }
    
    protected LocalModuleSymbol createLocalModuleSymbol
                                ( String moduleName
                                , TextPosition modulePosition
                                , boolean alreadyDefined
                                , ISymbolWithDefinitions scope ) 
    {
        LocalModuleSymbol localModuleSymbol = new LocalModuleSymbol(moduleName, scope);
        setSymbolPosition(localModuleSymbol, modulePosition);
        
        if (!alreadyDefined && !StringUtils.isEmpty(moduleName)) {
            if (scope instanceof ISymbolWithDeclarations) {
                ((ISymbolWithDeclarations)scope).addLocalModule(localModuleSymbol);
            }
        }
        
        if (settings.getOption(XdsOptions.NOMODULEINIT)) {
            localModuleSymbol.addAttribute(SymbolAttribute.NOMODULEINIT);
        }
        return localModuleSymbol;
    }
    
    protected IOberonMethodReceiverSymbol createOberonMethodReceiverSymbol
                                         ( String name
                                         , TextPosition position
                                         , boolean isVarInstance
                                         , ITypeSymbol typeSymbol ) 
    {
        IOberonMethodReceiverSymbol receiverSymbol = new OberonMethodReceiverSymbol(
            name, null, createRef(typeSymbol)
        );
        setSymbolPosition(receiverSymbol, position);
        if (isVarInstance) {
            receiverSymbol.addAttribute(SymbolAttribute.VAR_PARAMETER);
        }
        return receiverSymbol;
    }
    
    protected OberonMethodDefinitionSymbol createOberonMethodDefinitionSymbol
                                          ( String procedureName
                                          , TextPosition position
                                          , OberonMethodTypeSymbol procedureTypeSymbol
                                          , ISymbolWithProcedures parentScope ) 
    {
        OberonMethodDefinitionSymbol procedureDefSymbol;
        procedureDefSymbol = new OberonMethodDefinitionSymbol( 
            procedureName, parentScope, 
            createStaticRef((ProcedureTypeSymbol)procedureTypeSymbol)
        );
        setSymbolPosition(procedureDefSymbol, position);
        return procedureDefSymbol;
    }


    protected OberonMethodReceiverSymbol recreateOberonMethodReceiverSymbol
                                         ( IOberonMethodReceiverSymbol receiverSymbol
                                         , OberonMethodTypeSymbol procedureTypeSymbol ) 
    {
        OberonMethodReceiverSymbol newReceiverSymbol = new OberonMethodReceiverSymbol( 
            receiverSymbol.getName(), procedureTypeSymbol, 
            createRef(receiverSymbol.getTypeSymbol()) 
        );
        setSymbolPosition(newReceiverSymbol, receiverSymbol.getPosition());
        newReceiverSymbol.setAttributes(receiverSymbol.getAttributes());
        addSymbolForResolving(newReceiverSymbol);
        procedureTypeSymbol.setReceiverSymbol(newReceiverSymbol);
        return newReceiverSymbol;
    }


    protected OberonMethodTypeSymbol createOberonMethodTypeSymbol
                                     ( String procedureName
                                     , XdsLanguage language
                                     , EnumSet<SymbolAttribute> attributes
                                     , IOberonMethodReceiverSymbol receiverSymbol
                                     , ISymbolWithProcedures parentScope ) 
    {
        String procedureTypeName = createAnonymousName(
            procedureName, "OberonMethodType", parentScope    //$NON-NLS-1$
        );
        OberonMethodTypeSymbol procedureTypeSymbol = new OberonMethodTypeSymbol(
            procedureTypeName, receiverSymbol, parentScope
        );
        setSymbolCurrentPosition(procedureTypeSymbol);
        procedureTypeSymbol.setLanguage(language);
        procedureTypeSymbol.addAttributes(attributes);
        addSymbolForResolving(procedureTypeSymbol);
        return procedureTypeSymbol;
    }

    protected ProcedureDefinitionSymbol createProcedureDefinitionSymbol
                                        ( String procedureName 
                                        , TextPosition position
                                        , ProcedureTypeSymbol typeSymbol
                                        , ISymbolWithProcedures parentScope ) 
    {
        ProcedureDefinitionSymbol symbol = new ProcedureDefinitionSymbol(
            procedureName, parentScope, createStaticRef(typeSymbol)
        );
        setSymbolPosition(symbol, position);
        return symbol;
    }


    protected ProcedureTypeSymbol createProcedureTypeSymbol( String typeName
                                                           , String hostName  
                                                           , ISymbolWithScope parentScope ) 
      {
          typeName = generateNameIfNull(typeName, hostName, "ProcedureType", parentScope);    //$NON-NLS-1$ 
          ProcedureTypeSymbol typeSymbol = new ProcedureTypeSymbol(typeName, parentScope);
          setSymbolCurrentPosition(typeSymbol);
          return typeSymbol;
      }
      
    protected ProcedureTypeSymbol createProcedureTypeSymbol( String hostSymbolName
                                                           , TextPosition procedurePosition
                                                           , XdsLanguage language
                                                           , EnumSet<SymbolAttribute> attributes
                                                           , ISymbolWithProcedures parentScope )
    {
        ProcedureTypeSymbol procedureTypeSymbol = createProcedureTypeSymbol(
            null, hostSymbolName, parentScope
        ); 
        setSymbolPosition(procedureTypeSymbol, procedurePosition);
        procedureTypeSymbol.setLanguage(language);
        procedureTypeSymbol.addAttributes(attributes);
        addSymbolForResolving(procedureTypeSymbol);
        return procedureTypeSymbol;
    }
    
    protected ModuleAliasSymbol createModuleAliasSymbol( String aliasModuleName
                                                       , TextPosition position
                                                       , IModuleSymbol importedModuleSymbol
                                                       , IModuleSymbol parentScope ) 
    {
        ModuleAliasSymbol moduleAliasSymbol = new ModuleAliasSymbol(aliasModuleName, parentScope);
        IModulaSymbolReference<IModuleSymbol> ref = createRef(importedModuleSymbol);
        moduleAliasSymbol.setReference(ref);
        setSymbolPosition(moduleAliasSymbol, position);
        return moduleAliasSymbol;
    }


    protected IModulaSymbol createUnresolvedModuleSymbol(String importedEntityName)    
    {
        IModulaSymbol symbol;
        symbol = new UnresovedModuleSymbol(importedEntityName, getModuleLaguage());
        setSymbolCurrentPosition(symbol);
        return symbol;
    }
    
    protected UnknownModulaSymbol createUnknownSymbol( String symbolName
                                                     , IModulaSymbolScope scope ) 
    {
        UnknownModulaSymbol symbol = new UnknownModulaSymbol(symbolName, scope);
        setSymbolCurrentPosition(symbol);
        return symbol;
    }
    

    private class LocalReferenceFactory implements IReferenceFactory {
    	@Override
    	public <T extends IModulaSymbol> IModulaSymbolReference<T> createRef(
    			T symbol) {
    		return XdsSymbolParser.this.createRef(symbol);
    	}

    }
}
