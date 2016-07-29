// see org.eclipse.pde.internal.core.search.PluginSearchOperation
package com.excelsior.xds.core.search.modula;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.search.ui.text.Match;

import com.excelsior.xds.core.compiler.compset.CompilationSetManager;
import com.excelsior.xds.core.ide.symbol.ParseTask;
import com.excelsior.xds.core.ide.symbol.ParseTaskFactory;
import com.excelsior.xds.core.ide.symbol.SymbolModelManager;
import com.excelsior.xds.core.ide.symbol.utils.EntityUtils;
import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.IXdsSdkLibraryContainer;
import com.excelsior.xds.core.model.XdsModelManager;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.search.modula.utils.ModulaSearchUtils;
import com.excelsior.xds.core.text.ITextRegion;
import com.excelsior.xds.core.text.TextPosition;
import com.excelsior.xds.core.text.TextUtils;
import com.excelsior.xds.core.utils.JavaUtils;
import com.excelsior.xds.core.utils.XdsFileUtils;
import com.excelsior.xds.parser.commons.symbol.IBlockSymbolTextBinding;
import com.excelsior.xds.parser.modula.symbol.IConstantSymbol;
import com.excelsior.xds.parser.modula.symbol.IDefinitionModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IFormalParameterSymbol;
import com.excelsior.xds.parser.modula.symbol.IImplemantationModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.ILocalModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IMainModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModuleAliasSymbol;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IOberonMethodDeclarationSymbol;
import com.excelsior.xds.parser.modula.symbol.IProcedureDeclarationSymbol;
import com.excelsior.xds.parser.modula.symbol.IProcedureDefinitionSymbol;
import com.excelsior.xds.parser.modula.symbol.IProcedureSymbol;
import com.excelsior.xds.parser.modula.symbol.IProgramModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IRecordFieldSymbol;
import com.excelsior.xds.parser.modula.symbol.IStandardProcedureSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithDefinitions;
import com.excelsior.xds.parser.modula.symbol.IVariableSymbol;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.type.IEnumTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IRecordTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;
import com.excelsior.xds.parser.modula.utils.ModulaSymbolUtils;

public class ModulaSearchOperation
{
    public interface ISearchResultCollector
    {
        void accept(Object match);
    }
    
    protected ModulaSearchInput fInput;
    private ISearchResultCollector fCollector;
    
    /**
     *  Either fSymbolNamePattern or fSymbolQualifiedName should be null
     */
    private Pattern fSymbolNamePattern;
	private Set<String> fSymbolQualifiedNames;

    public ModulaSearchOperation(ModulaSearchInput input, ISearchResultCollector collector) {
        this.fInput = input;
        this.fCollector = collector;
        if (!StringUtils.isEmpty(input.getSearchString())) {
        	this.fSymbolNamePattern = TextUtils.createPattern(input.getSearchString(),
                    input.isCaseSensitive());
        }
        else if (input.getQualifiedNames() != null) {
        	this.fSymbolQualifiedNames = input.getQualifiedNames();
        }
    }

    public void execute(IProgressMonitor monitor, MultiStatus status) {
    	Map<IFile, IDocument> documentsCache = ModulaSearchUtils.evalNonFileBufferDocument();

        searchForSymbolDeclaration(documentsCache, monitor);

        searchForDeclarationsAndReferencesInScope(documentsCache, monitor, status);
    }

    
    private boolean isSearchForAnyElement() {
        return ModulaSearchInput.SEARCH_FOR_ANY_ELEMENT == fInput.getSearchFor();
    }

    private boolean isSearchFor(int searchForFlag) {
        return searchForFlag == fInput.getSearchFor();
    }


    private boolean isSymbolNameMatches(IModulaSymbol symbol) {
    	if (symbol == null || symbol.getName() == null) {
    		return false;
    	}
    	if (fSymbolNamePattern != null) {
    		return fSymbolNamePattern.matcher(symbol.getName()).matches();
    	}
    	else if (fSymbolQualifiedNames != null) {
    		return fSymbolQualifiedNames.contains(symbol.getQualifiedName());
    	}
    	
    	return false;
    }

    
    private ModulaSymbolMatch createMatchFromTokenPosition( IFile iFile
                                                          , IModulaSymbol symbol
                                                          , TextPosition position )
    {
    	if (position == null) {
    		return null;
    	}
        return new ModulaSymbolMatch(iFile, symbol, position);
    }

    private ModulaSymbolMatch createMatchFromSymbol(IFile f, IModulaSymbol symbol) {
        return createMatchFromTokenPosition(f, symbol, symbol.getPosition());
    }
    
    private Collection<Match> createMatchesFromSymbol(IFile f, IModulaSymbol symbol) {
    	Collection<Match> matches = new ArrayList<Match>();
    	
    	boolean isSearchForAllNameOccurrences = JavaUtils.areFlagsSet(fInput.getSearchModifiers(), ModulaSearchInput.MODIFIER_ALL_NAME_OCCURENCES);
    	if (isSearchForAllNameOccurrences && (symbol instanceof IBlockSymbolTextBinding)) {
			IBlockSymbolTextBinding textBinding = (IBlockSymbolTextBinding)symbol;
			Collection<ITextRegion> nameTextRegions = textBinding.getNameTextRegions();
			for (ITextRegion textRegion : nameTextRegions) {
				Match match = createMatchFromTokenPosition(f, symbol, new TextPosition(-1, -1, textRegion.getOffset()));
				matches.add(match);
			}
		}
		else {
			Match match = createMatchFromSymbol(f, symbol);
			matches.add(match);
		}
    	
    	return matches;
    }

    private void createMatchesFromUsages( IModuleSymbol moduleSymbol
                                        , IModulaSymbol modulaSymbol
                                        , IFile iFile
                                        , List<Match> matches )
    {
        Collection<TextPosition> symbolUsages = moduleSymbol.getSymbolUsages(modulaSymbol);
        if (symbolUsages != null) {
            for (TextPosition tokenPosition : symbolUsages) {
                matches.add(createMatchFromTokenPosition(iFile, modulaSymbol, tokenPosition));
            }
        }
    }    

    
    private void searchForDeclarationsAndReferencesInScope( Map<IFile, IDocument> documentsCache, IProgressMonitor monitor
                                                          , MultiStatus status )
    {
        IFile farr[] = fInput.getSearchScope().evaluateFilesInScope(status);
        monitor.beginTask("", farr.length); //$NON-NLS-1$
        
        try {
            for (IFile f : farr) {
                try{
                    if (monitor.isCanceled()) {
                        break;
                    }
                    
                    if (!XdsFileUtils.isCompilationUnitFile(f.getName())) {
                        continue;
                    }
                    
                    IXdsElement xdsEl = XdsModelManager.getModel().getXdsElement(f);
                    if (xdsEl == null) {
                        continue;
                    }
                    
                    // Search in:
                    boolean searchInOk = false;
                    {
                        int searchIn = fInput.getSearchInFlags();
                        
                        boolean isInSdk = false;
                        // TODO : remove isInSdk calculation using IXdsSdkLibraryContainer. Try using LibraryFileSetManager instead.
                        IXdsContainer xCont = xdsEl.getParent();
                        while (xCont != null) {
                            if (xCont instanceof IXdsSdkLibraryContainer) {
                                isInSdk = true;
                                break;
                            }
                            xCont = xCont.getParent();
                        }
                        
                        if (isInSdk) {
                            searchInOk = (searchIn & ModulaSearchInput.SEARCH_IN_SDK_LIBRARIES) != 0;
                        }
                        else {
                            if ((searchIn & ModulaSearchInput.SEARCH_IN_ALL_SOURCES) != 0) {
                                searchInOk = true;
                            }
                            else if ((searchIn & ModulaSearchInput.SEARCH_IN_COMP_SET) != 0) {
                                searchInOk = CompilationSetManager.getInstance()
                                        .isInCompilationSet(f.getProject().getName(),
                                                ResourceUtils.getAbsolutePath(f));
                            }
                        }
                    }
                    if (!searchInOk) {
                        continue;
                    }
                    
                    // Do search:
                    ArrayList<Match> matches = findMatchesInFile(f, documentsCache);
                    for (Match m : matches) {
                    	acceptMatch(documentsCache, m);
                    }
                }
                finally{
                    monitor.worked(1);
                }
            }
        } finally {
            monitor.done();
        }
    }

    private void searchForSymbolDeclaration(Map<IFile, IDocument> documentsCache2, IProgressMonitor monitor) {
    	if (JavaUtils.areFlagsSet(fInput.getLimitTo(), ModulaSearchInput.LIMIT_TO_DECLARATIONS)) {
    		Map<IFile, IDocument> documentsCache = ModulaSearchUtils.evalNonFileBufferDocument();
    		IModulaSymbol symbolToSearchFor = fInput.getSymbolToSearchFor();
    		if (symbolToSearchFor != null) {
    			monitor.beginTask("", 1); //$NON-NLS-1$
    			try {
    				IFile symbolFile = ModulaSymbolUtils.findFirstFileForSymbol(fInput.getProject(), symbolToSearchFor);
    				if (symbolFile != null) {
    					Collection<IModulaSymbol> sameEntitySymbols = EntityUtils.syncGetRelatedSymbols(symbolFile.getProject(), fInput.isSearchOnlyInCompilationSet(), symbolToSearchFor);
    					for (IModulaSymbol sameEntitySymbol : sameEntitySymbols) {
    						symbolFile = ModulaSymbolUtils.findFirstFileForSymbol(fInput.getProject(), sameEntitySymbol);
    						if (symbolFile != null) {
    							Collection<Match> matches = createMatchesFromSymbol(symbolFile, sameEntitySymbol);
    							for (Match match : matches) {
    								acceptMatch(documentsCache, match);
								}
    						}
    					}
    				}
    			} finally {
    				monitor.done();
    			}
    		}
    	}
    }

    private ArrayList<Match> findMatchesInFile( IFile f
                                              , Map<IFile, IDocument> documentsCache ) 
    {
    	final ArrayList<Match> matches = new ArrayList<Match>();
    	if (f.getProject().isOpen()) {
    		ParseTask task = ParseTaskFactory.create(f);
    		task.setForce(false);
    		IModuleSymbol moduleSymbol = SymbolModelManager.instance().syncParseFirstSymbol(task);
    		if (moduleSymbol != null) {
    			IModulaSymbol symbolToSearchFor = fInput.getSymbolToSearchFor();
    			if (symbolToSearchFor != null) {
    				boolean isInputAccepted = JavaUtils.areFlagsSet(fInput.getLimitTo(), ModulaSearchInput.LIMIT_TO_USAGES);
    				if (isInputAccepted) {
    					Collection<IModulaSymbol> sameEntitySymbols = EntityUtils.syncGetRelatedSymbols(f.getProject(), fInput.isSearchOnlyInCompilationSet(), symbolToSearchFor);
    					for (IModulaSymbol sameEntitySymbol : sameEntitySymbols) {
    						createMatchesFromUsages(moduleSymbol, sameEntitySymbol, f, matches);
    					}
    				}
    			}
    			else {
    				findMatchesInFileWithPattern(f, matches, moduleSymbol);
    			}
    		}
    	}
    	return matches;
    }
    
    private void findMatchesInFileWithPattern( final IFile iFile
                                             , final ArrayList<Match> matches
                                             , IModuleSymbol moduleSymbol )
    {
        boolean isDeclarationRequired = JavaUtils.areFlagsSet(fInput.getLimitTo(), ModulaSearchInput.LIMIT_TO_DECLARATIONS);
        if (isDeclarationRequired) {
            moduleSymbol.accept(new DeclarationVisitor(iFile, matches));
        }
        
        boolean isUsageRequired = JavaUtils.areFlagsSet(fInput.getLimitTo(), ModulaSearchInput.LIMIT_TO_USAGES);
        if (isUsageRequired) {
            @SuppressWarnings("rawtypes")List<Class> expectedSymbolClasses;
            switch (fInput.getSearchFor()) {
            case ModulaSearchInput.SEARCH_FOR_TYPE:
                expectedSymbolClasses = classList(ITypeSymbol.class);
                break;
            case ModulaSearchInput.SEARCH_FOR_VARIABLE:
                expectedSymbolClasses = classList(IVariableSymbol.class, IFormalParameterSymbol.class);
                break;
            case ModulaSearchInput.SEARCH_FOR_PROCEDURE:
                expectedSymbolClasses = classList(IProcedureSymbol.class, IStandardProcedureSymbol.class);
                break;
            case ModulaSearchInput.SEARCH_FOR_FIELD:
                expectedSymbolClasses = classList(IRecordFieldSymbol.class);
                break;
            case ModulaSearchInput.SEARCH_FOR_CONSTANT:
                expectedSymbolClasses = classList(IConstantSymbol.class);
                break;
            case ModulaSearchInput.SEARCH_FOR_MODULE:
                expectedSymbolClasses = classList(IModuleSymbol.class);
                break;
            case ModulaSearchInput.SEARCH_FOR_ANY_ELEMENT:
                expectedSymbolClasses = null;
                break;
            default:
                return;
            }
            
            Iterable<IModulaSymbol> usedSymbols = moduleSymbol.getUsedSymbols();
            for (IModulaSymbol modulaSymbol : usedSymbols) {
                matchSymbolReferences( moduleSymbol, modulaSymbol, expectedSymbolClasses
                                     , iFile, matches );
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private List<Class> classList(Class... classes) {
        List<Class> classesList = new ArrayList<Class>();
        classesList.addAll(Arrays.asList(classes));
        return classesList;
    }

    private void matchSymbolReferences( IModuleSymbol moduleSymbol
                                      , IModulaSymbol modulaSymbol
                                      , @SuppressWarnings("rawtypes") List<Class> expectedSymbolClasses
                                      , IFile f
                                      , List<Match> matches )
    {
        boolean isOneOfTheExpected = expectedSymbolClasses == null;
        if (!isOneOfTheExpected) {
            for (Class<?> expectedSymbolClass : expectedSymbolClasses) {
                if (expectedSymbolClass.isAssignableFrom(modulaSymbol.getClass())) {
                    isOneOfTheExpected = true;
                    break;
                }
            }
        }
        if (isOneOfTheExpected && isSymbolNameMatches(modulaSymbol)) {
            createMatchesFromUsages(moduleSymbol, modulaSymbol, f, matches);
        }
    }

    private void setMatchContextLine(ModulaSymbolMatch match, Map<IFile, IDocument> documentsCache) 
    {
        IDocument document = ModulaSearchUtils.getDocumentForIFile(match.getFile(), documentsCache);
        if (document == null)
            return;
            
        IRegion contextLineRegion;
        try {
            contextLineRegion = document.getLineInformationOfOffset(match.getOffset());
            String contextLine = document.get(contextLineRegion.getOffset(), contextLineRegion.getLength());
            match.setContextLine(contextLine);
        } catch (BadLocationException e) {
        }
    }

    
    private class DeclarationVisitor extends ModulaSymbolVisitor 
    {
        private final IFile iDeclarationFile;
        private final File iDeclarationFilePath;
        private final ArrayList<Match> matches;
    
        public DeclarationVisitor(IFile iDeclarationFile, ArrayList<Match> matches) {
            this.iDeclarationFile   = iDeclarationFile;
            this.iDeclarationFilePath = ResourceUtils.getAbsoluteFile(iDeclarationFile);
            this.matches = matches;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean preVisit(IModulaSymbol symbol) {
            return super.preVisit(symbol) && isSameFileAsDeclarationFile(symbol);
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean visit(IProcedureDefinitionSymbol symbol) {
            searchFor(symbol.getParameters(), ModulaSearchInput.SEARCH_FOR_VARIABLE);
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean visit(IProcedureDeclarationSymbol symbol) {
            searchFor(symbol.getParameters(), ModulaSearchInput.SEARCH_FOR_VARIABLE);
            searchFor(symbol.getLocalModules(), ModulaSearchInput.SEARCH_FOR_MODULE);
            searchInSymbolWithDefinitions(symbol);
            return true;
        }
        
		@Override
		public boolean visit(IOberonMethodDeclarationSymbol symbol) {
			searchFor(symbol.getParameters(), ModulaSearchInput.SEARCH_FOR_VARIABLE);
			matchSymbol(symbol.getReceiverSymbol(), ModulaSearchInput.SEARCH_FOR_VARIABLE);
            searchFor(symbol.getLocalModules(), ModulaSearchInput.SEARCH_FOR_MODULE);
            searchInSymbolWithDefinitions(symbol);
			return true;
		}

		/**
         * {@inheritDoc}
         */
        @Override
        public boolean visit(IRecordTypeSymbol symbol) {
            searchFor(symbol.getFields(), ModulaSearchInput.SEARCH_FOR_FIELD);
            searchFor(symbol.getProcedures(), ModulaSearchInput.SEARCH_FOR_PROCEDURE);
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean visit(IEnumTypeSymbol symbol) {
            searchFor(symbol.getElements(), ModulaSearchInput.SEARCH_FOR_CONSTANT);
            return true;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean visit(IDefinitionModuleSymbol symbol) {
            searchForAliases(symbol.getImports());
            searchInSymbolWithDefinitions(symbol);

            matchSymbol(symbol, ModulaSearchInput.SEARCH_FOR_MODULE);
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean visit(IImplemantationModuleSymbol symbol) {
            searchInProgramModuleSymbol(symbol);
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean visit(ILocalModuleSymbol symbol) {
            searchInProgramModuleSymbol(symbol);
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean visit(IMainModuleSymbol symbol) {
            searchInProgramModuleSymbol(symbol);
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean visit(IProgramModuleSymbol symbol) {
            searchInProgramModuleSymbol(symbol);
            return true;
        }

    
        private void searchFor( Iterable<? extends IModulaSymbol> symbols
                              , int searchForFlag )
        {
            if (isSearchForAnyElement() || isSearchFor(searchForFlag)) {
                for (IModulaSymbol symbol : symbols) {
                    if (isSymbolMatches(symbol)) {
                    	matches.addAll(createMatchesFromSymbol(iDeclarationFile, symbol));
                    }
                }
            }
        }

        private void searchForAliases(Iterable<? extends IModulaSymbol> symbols)
        {
            if (isSearchForAnyElement() || isSearchFor(ModulaSearchInput.SEARCH_FOR_MODULE)) {
                for (IModulaSymbol symbol : symbols) {
                    if (symbol instanceof IModuleAliasSymbol && isSymbolMatches(symbol)) {
                    	matches.addAll(createMatchesFromSymbol(iDeclarationFile, symbol));
                    }
                }
            }
        }


        private void searchInSymbolWithDefinitions(ISymbolWithDefinitions symbolWithDefinitions)
        {
            searchFor(symbolWithDefinitions.getProcedures(), ModulaSearchInput.SEARCH_FOR_PROCEDURE);
            searchFor(symbolWithDefinitions.getConstants(), ModulaSearchInput.SEARCH_FOR_CONSTANT);
            searchFor(symbolWithDefinitions.getTypes(), ModulaSearchInput.SEARCH_FOR_TYPE);
            searchFor(symbolWithDefinitions.getVariables(), ModulaSearchInput.SEARCH_FOR_VARIABLE);
        }

        private void searchInProgramModuleSymbol(IProgramModuleSymbol symbol) {
            searchForAliases(symbol.getImports());
            searchInSymbolWithDefinitions(symbol);
            searchFor(symbol.getLocalModules(), ModulaSearchInput.SEARCH_FOR_MODULE );

            matchSymbol(symbol, ModulaSearchInput.SEARCH_FOR_MODULE);
        }

        
        private void matchSymbol(IModulaSymbol symbol, int searchForFlag) {
            if (isSearchForAnyElement() || isSearchFor(searchForFlag)) {
                if (isSymbolMatches(symbol)) {
                	matches.addAll(createMatchesFromSymbol(iDeclarationFile, symbol));
                }
            }
        }

        private boolean isSymbolMatches(IModulaSymbol symbol) {
        	return isSameFileAsDeclarationFile(symbol) && isSymbolNameMatches(symbol);
        }
        
        private boolean isSameFileAsDeclarationFile(IModulaSymbol symbol) {
			return ResourceUtils.equalsPathesAsInFS(iDeclarationFilePath, ModulaSymbolUtils.getSourceFile(symbol));
		}
    }

    private void acceptMatch(Map<IFile, IDocument> documentsCache, Match match) {
		if (match != null) {
			setMatchContextLine((ModulaSymbolMatch)match, documentsCache);
			fCollector.accept(match);
		}
	}
}
