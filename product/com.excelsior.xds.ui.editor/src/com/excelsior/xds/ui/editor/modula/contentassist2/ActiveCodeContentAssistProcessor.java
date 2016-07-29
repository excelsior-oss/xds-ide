package com.excelsior.xds.ui.editor.modula.contentassist2;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionListenerExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

import com.excelsior.xds.builder.buildsettings.BuildSettingsCache;
import com.excelsior.xds.core.builders.BuildSettings;
import com.excelsior.xds.core.ide.symbol.IdeImportResolver;
import com.excelsior.xds.core.project.XdsProjectSettings;
import com.excelsior.xds.core.project.XdsProjectSettingsManager;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.core.utils.JavaUtils;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;
import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenTypes;
import com.excelsior.xds.parser.modula.symbol.IConstantSymbol;
import com.excelsior.xds.parser.modula.symbol.IEnumElementSymbol;
import com.excelsior.xds.parser.modula.symbol.IFormalParameterSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbolScope;
import com.excelsior.xds.parser.modula.symbol.IModuleAliasSymbol;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IProcedureSymbol;
import com.excelsior.xds.parser.modula.symbol.IStandardProcedureSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithImports;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.IVariableSymbol;
import com.excelsior.xds.parser.modula.symbol.binding.IImportResolver;
import com.excelsior.xds.parser.modula.symbol.type.IEnumTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IPointerTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IRecordTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;
import com.excelsior.xds.parser.modula.utils.ModulaSymbolUtils;
import com.excelsior.xds.ui.editor.commons.contentassist.ICompletionContextUser;
import com.excelsior.xds.ui.editor.internal.nls.Messages;
import com.excelsior.xds.ui.editor.modula.ModulaContentAssistant;
import com.excelsior.xds.ui.editor.modula.contentassist2.ModuleImportContextualCompletionProposal.CommaPosition;
import com.excelsior.xds.ui.images.ImageUtils;
import com.excelsior.xds.ui.viewers.ModulaSymbolDescriptions;
import com.excelsior.xds.ui.viewers.ModulaSymbolImages;
import com.google.common.collect.Iterables;

public class ActiveCodeContentAssistProcessor implements
		IContentAssistProcessor, ICompletionContextUser<CompletionContext> {
	private boolean IS_DEBUG_PRINT = false;
	private static final char[] PROPOSAL_ACTIVATION_CHARS = new char[] { '.' };
	private static final ICompletionProposalComparator completionProposalComparator = new ICompletionProposalComparator();
	
	private CompletionContext context;
	private int proposalCategoryIteration;
	private boolean isAssistSessionRestarted;
	
	private final CompletionListener completionListener = new CompletionListener();
	private final ModulaContentAssistant contentAssistant;
	
	ActiveCodeContentAssistProcessor(ModulaContentAssistant contentAssistant) {
		this.contentAssistant = contentAssistant;
		contentAssistant.addCompletionListener(completionListener);
	}
	
	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int offset) {
		if (IS_DEBUG_PRINT) {
			System.out.println(String.format("proposalCategoryIteration=%s isAssistSessionRestarted=%s contentAssistant.isAssistSessionRestarted()=%s", proposalCategoryIteration, isAssistSessionRestarted,contentAssistant.isAssistSessionRestarted()));
		}
		contentAssistant.enableAutoInsert(true);
		isAssistSessionRestarted = isAssistSessionRestarted || contentAssistant.isAssistSessionRestarted();
		if (isAssistSessionRestarted) {
            isAssistSessionRestarted = false;
        }
        else {
            ++proposalCategoryIteration;
        }
		
		List<ICompletionProposal> proposals = new ArrayList<>();
		Set<ICompletionProposal> proposalsSet = new HashSet<>();
		if (context.getReplacementLength() >= 0) {
			if (context.getRegionType() == RegionType.IMPORT_STATEMENT) {
				Token prevToken = ContentAssistUtils.prevToken(context.getTokens(), context.offsetInStatement());
	            Token nextToken = ContentAssistUtils.nextToken(context.getTokens(), context.offsetInStatement());
	            context.setPreviousNonSpaceToken(prevToken);
	            context.setNextNonSpaceToken(nextToken);
				if (isFromImport(context.getLeafNode())) {
					fromImportProposals(proposals, proposalsSet);
				}
				else {
					importProposals(proposals, proposalsSet);
				}
			}
			else {
				if (context.isDottedExpression()) {
					dottedExpressionProposals(context, proposals, proposalsSet);
					if (proposals.size() == 1 && isNoChangeProposal(proposals.get(0))) {
						contentAssistant.enableAutoInsert(false);
					}
				}
				else {
					boolean addMinimum = proposalCategoryIteration % 2  == 1;
					scopeProposals(context, proposals, proposalsSet, addMinimum);
					if (addMinimum && proposals.size() == 1 && !isOnlyProposalPossible()) {
						contentAssistant.enableAutoInsert(false);
					}
				}
			}
		}
		
		return proposals.toArray(new ICompletionProposal[0]);
	}
	
	private boolean isNoChangeProposal(ICompletionProposal p) {
		if (p instanceof ModulaContextualCompletionProposal) {
			ModulaContextualCompletionProposal mp = (ModulaContextualCompletionProposal) p;
			return mp.isNoChangeProposal(context.getViewer().getDocument());
		}
		return false;
	}
	
	private boolean isOnlyProposalPossible() {
		List<ICompletionProposal> proposals = new ArrayList<>();
		Set<ICompletionProposal> proposalsSet = new HashSet<>();
		scopeProposals(context, proposals, proposalsSet, false);
		return proposals.size() == 1;
	}
	
	private void importProposals(List<ICompletionProposal> proposals, Set<ICompletionProposal> proposalsSet) {
		Set<String> alreadyImportedNames = computeAlreadyImportedQualifiedNames(context.getModuleSymbol()) ;
		
		BuildSettings buildSettings =  BuildSettingsCache.createBuildSettings(context.getEditedFile());
		Collection<File> lookupDirs = getLookupDirs(buildSettings);
		
		Collection<ModuleInfo> modules = computeModuleInfos(context, lookupDirs, alreadyImportedNames);
		createCompletionProposals(context, modules, proposals, proposalsSet);
	}
	
	private void fromImportProposals(List<ICompletionProposal> proposals,
			Set<ICompletionProposal> proposalsSet) {	
		
		BuildSettings buildSettings =  BuildSettingsCache.createBuildSettings(context.getEditedFile());
		
		FromImportStatementParser parser = FromImportStatementParser.createParser(buildSettings, context.getModuleSymbol(), context.getCurrentStatement());
		FromImportStatementParser.Statement statement = parser.parse();
		
		if (statement.moduleName == null) {
			if (ContentAssistUtils.tokenType(context.getPreviousNonSpaceToken()) == ModulaTokenTypes.FROM_KEYWORD) {
				importProposals(proposals, proposalsSet);
			}
			return;
		}
		
		IModuleSymbol moduleSymbol = resolve(statement.moduleName);
		List<String> names = new ArrayList<String>();
		if (moduleSymbol != null) {
			Predicate<IModulaSymbol> symbolFilter = s1 -> !statement.symbolNames.contains(s1.getName()) && JavaUtils.isOneOf(s1,
					ITypeSymbol.class, IConstantSymbol.class,
					IVariableSymbol.class, IProcedureSymbol.class);
			addCompletionProposalsFromScope(moduleSymbol, proposals, proposalsSet, symbolFilter);
			for (IModulaSymbol s : moduleSymbol) {
				names.add(s.getName());
			}
		}
	}
	
	private IModuleSymbol resolve(String moduleName) {
		IModuleSymbol hostModule = context.getModuleSymbol();
		BuildSettings buildSettings =  BuildSettingsCache.createBuildSettings(context.getEditedFile());
		
		IModuleSymbol moduleSymbol = null;
		if (hostModule != null && buildSettings != null) {
			IImportResolver importResolver = new IdeImportResolver(buildSettings, null, null);
			moduleSymbol =  importResolver.resolveModuleSymbol(hostModule.getLanguage(), moduleName, hostModule.getSourceFile());
		}
		return moduleSymbol;
	}

	private static boolean isFromImport(PstNode n) {
    	return fromImportNode(n) != null;
    }
	
	private static PstNode fromImportNode(PstNode n) {
		while(n != null) {
    		if (n.getElementType() == ModulaElementTypes.UNQUALIFIED_IMPORT) {
    			return n;
    		}
    		n = n.getParent();
    	}
		return null;
	}
	
	private static void createCompletionProposals(CompletionContext context, Collection<ModuleInfo> modules, List<ICompletionProposal> proposals, Set<ICompletionProposal> proposalsSet) {
		IProject iProject = context.getEditedFile().getProject();
		XdsProjectSettings xps = XdsProjectSettingsManager.getXdsProjectSettings(iProject);
        Sdk sdk = xps.getProjectSdk();
        String sdkName = sdk != null? sdk.getName() : StringUtils.EMPTY;
        
        List<ICompletionProposal> completionProposals = new ArrayList<>();
        
		// Add completion proposals for modules:
        for (ModuleInfo mi : modules) {
            File modFile = new File(mi.absPath);
            String modname = FilenameUtils.getBaseName(modFile.getName());

            String filterPrefix = context.getBeforeCursorWordPart();
            if (filterPrefix != null && !modname.toLowerCase().startsWith(filterPrefix.toLowerCase())) {
                continue;
            }

            String decorator = ""; //$NON-NLS-1$
            Image img = ImageUtils.getImage(ImageUtils.OBJ_DEF_MODULE_16x16);
            switch (mi.location) {
            case EXTDEP: // - /MyPrj/External Dependencies [c:\path\aa.def]
                decorator = Messages.format(Messages.ModulaContextualCompletionProcessor_ExternalDeps, 
                        new Object[]{iProject.getName(), mi.absPath});
                img = ImageUtils.getImage(ImageUtils.EXTERNAL_DEPENDENCIES_FOLDER_IMAGE_NAME);
                break;
            case LIB:    // - /MyPrj/SDK Library [XDS Sdk]/sym/x86/CCtlRTL.sym
                decorator = Messages.format(Messages.ModulaContextualCompletionProcessor_SdkLibrary, 
                                            new Object[]{iProject.getName(), sdkName, FilenameUtils.separatorsToUnix(mi.dispPath)});
                img = ImageUtils.getImage(ImageUtils.SDK_LIBRARY_FOLDER_IMAGE_NAME);
                break;
            default:                     // - /MyPrj/src/my.def
                decorator = Messages.format(Messages.ModulaContextualCompletionProcessor_inProjectPath, new Object[]{iProject.getName(), FilenameUtils.separatorsToUnix(mi.dispPath)}); 
            }

            StyledString sstring = new StyledString();
            sstring.append(modname);
            sstring.append(decorator, StyledString.DECORATIONS_STYLER);
            
            
            EnumSet<CommaPosition> commas = EnumSet.of(CommaPosition.NONE); 
            
            if (ContentAssistUtils.tokenType(context.getPreviousNonSpaceToken()) == ModulaTokenTypes.IDENTIFIER) {
            	commas.add(CommaPosition.BEFORE);
            }

            if (ContentAssistUtils.tokenType(context.getNextNonSpaceToken()) == ModulaTokenTypes.IDENTIFIER) {
            	commas.add(CommaPosition.AFTER);
            }

            ICompletionProposal proposal = new ModuleImportContextualCompletionProposal(
            		context.getReplacementOffset(), 
            		context.getReplacementLength(),
            		commas,
                    img, 
                    sstring, 
                    modFile,
                    mi.location);

            if (!proposalsSet.contains(proposal)) {
            	proposalsSet.add(proposal);
                completionProposals.add(proposal);
            }
        }
        
        sort(completionProposals);
        proposals.addAll(completionProposals);
	}
	
	private static Collection<ModuleInfo> computeModuleInfos(CompletionContext context, Collection<File> lookupDirs, Set<String> alreadyImportedNames) {
		Collection<ModuleInfo> modules = new ArrayList<>();
		String prjRoot = null;
        String libRoot = null;
        
        IProject iProject = context.getEditedFile().getProject();
		if (iProject != null) {
            prjRoot = ResourceUtils.getAbsolutePath(iProject);
            XdsProjectSettings xps = XdsProjectSettingsManager.getXdsProjectSettings(iProject);
            Sdk sdk = xps.getProjectSdk();
            if (sdk != null) {
                String sdkh = sdk.getSdkHomePath(); // no getLibraryDefinitionsPath(): .sym files are out form it
                if (sdkh != null && !sdkh.isEmpty()) {
                	libRoot = ResourceUtils.getAbsolutePathAsInFS(sdkh);
                }
            }
        }
		for (File fdir : lookupDirs) {
            String pathPref = prjRoot;
            ModuleLocation loc = ModuleLocation.PROJECT;
            String dir = fdir.getAbsolutePath();
            if (libRoot != null && dir.startsWith(libRoot)) {
                loc = ModuleLocation.LIB;
                pathPref = libRoot;
            } else if (prjRoot != null && !dir.startsWith(prjRoot)) {
                loc = ModuleLocation.EXTDEP;
                pathPref = ""; //$NON-NLS-1$
            }
            
            File files[] = fdir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return FilenameUtils.isExtension(name.toLowerCase(), new String[]{"def", "ob2", "sym"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
            });
            
            if (files == null) {
            	continue;
            }
            
            for (File f : files) {
                if (alreadyImportedNames.add(FilenameUtils.getBaseName(f.getName()).toLowerCase())) {
                    String absPath = new File(dir, f.getName()).getAbsolutePath();
                    String dispPath = absPath;
                    if (pathPref != null && !pathPref.isEmpty() && absPath.startsWith(pathPref)) {
                        dispPath = absPath.substring(pathPref.length());
                        if (dispPath.startsWith("\\") || dispPath.startsWith("/")) { //$NON-NLS-1$ //$NON-NLS-2$
                            dispPath = dispPath.substring(1);
                        }
                    }
                    modules.add(new ModuleInfo(absPath, dispPath, loc));
                }
            }
        }
		
		return modules;
	}
	
	private static Collection<File> getLookupDirs(BuildSettings bs) {
		Collection<File> dirs = new HashSet<File>();

		for (String name : new String[]{"zz.def", "zz.ob2", "zz.sym"}) {
			dirs.addAll(bs.getLookupDirs(name));
		}
		return dirs;
	}
	
	private static Set<String> computeAlreadyImportedQualifiedNames(IModuleSymbol moduleSymbol) {
		if (moduleSymbol == null) {
			return Collections.emptySet();
		}
		Set<String> names = new HashSet<>();
		
		Iterable<IModulaSymbol> symbols = Iterables.concat(
				Collections.singletonList(moduleSymbol),
				moduleSymbol.getImports());
		
		for (IModulaSymbol s : symbols) {
			String name = null;
			if (s instanceof IModuleAliasSymbol) {
				IModuleAliasSymbol aliasSymb = (IModuleAliasSymbol) s;
				IModuleSymbol aliased = aliasSymb.getReference();
				if (aliased != null) {
					name = aliased.getName();
				}
			}
			else {
				name = s.getName();
			}
			if (name == null){
				continue;
			}
			names.add(StringUtils.lowerCase(name));
		}
		
		return names;
	}
    
    enum ModuleLocation {
    	PROJECT,
    	EXTDEP,
    	LIB
    }

    private static final class ModuleInfo {
        public final String absPath;
        public final String dispPath;
        public final ModuleLocation location;
        
        public ModuleInfo(String absPath, String dispPath, ModuleLocation location) {
            this.absPath = absPath; 
            this.dispPath = dispPath;
            this.location = location; 
        }
    }
    
	private void scopeProposals(CompletionContext context,
			List<ICompletionProposal> proposals, Set<ICompletionProposal> proposalsSet, boolean addMinimum) {
		List<IModulaSymbolScope> ancestorScopes = ModulaSymbolUtils.getAllParentScopes(context.getLeafNode());
		IModulaSymbolScope superModuleScope = null;
        if (ancestorScopes.size() > 1) {
            superModuleScope = ancestorScopes.remove(ancestorScopes.size() - 1); // drop super-module
        }
        
        final Predicate<IModulaSymbol> symbolFilter = createSymbolFilter(context);
        
        while (true) {
            int maxScopes = addMinimum ? 1 : Integer.MAX_VALUE;
            maxScopes = Math.min(maxScopes, ancestorScopes.size());
    
            for (int idx = 0; idx < maxScopes; ++idx) {
                addCompletionProposalsFromScope(ancestorScopes.get(idx), proposals, proposalsSet, symbolFilter);
            }
            
            if (proposals.isEmpty() && addMinimum) {
                // turn off addMinimum and try again
                addMinimum = false; 
                continue;
            }
            break;
        }
        
        sort(proposals);

        if (!addMinimum) {
            // separately add names from the super-module
            List<ICompletionProposal> completionProposalsFromSuperModule = new ArrayList<ICompletionProposal>();
            addCompletionProposalsFromScope(superModuleScope, completionProposalsFromSuperModule, proposalsSet, symbolFilter);
            sort(completionProposalsFromSuperModule);
            proposals.addAll(completionProposalsFromSuperModule);
        }
	}
	
	private static void sort(List<ICompletionProposal> proposals) {
        Collections.sort(proposals, completionProposalComparator);
    }

    private static class ICompletionProposalComparator implements Comparator<ICompletionProposal> {
        @Override
        public int compare(ICompletionProposal o1, ICompletionProposal o2) {
            int cc = 0;
            if (o1 instanceof ModuleImportContextualCompletionProposal &&
                o2 instanceof ModuleImportContextualCompletionProposal) 
            {
                cc = ((ModuleImportContextualCompletionProposal)o1).getLocation().ordinal() -
                      ((ModuleImportContextualCompletionProposal)o2).getLocation().ordinal();
            }
            if (cc != 0) {
                return cc;
            } else {
                return o1.getDisplayString().toLowerCase().compareTo(o2.getDisplayString().toLowerCase());
            }
        }
    }
	
	private static Predicate<IModulaSymbol> createSymbolFilter(CompletionContext context) {
		return s -> {
			boolean test = true;
			RegionType regionType = context.getRegionType();
			switch (regionType) {
			case PROCEDURE_PARAMETERS:
			case DECLARATIONS:
				test = s instanceof ITypeSymbol || s instanceof IModuleSymbol || s instanceof IModuleAliasSymbol;
				break;
			case IMPORT_STATEMENT:
				test = s instanceof IModuleSymbol || s instanceof IModuleAliasSymbol;
				break;
			case MODULE:
				// TODO : this is workaround before we have more precise context determination.
				if (s instanceof IVariableSymbol) {
					IVariableSymbol varSymbol = (IVariableSymbol) s;
					test = !varSymbol.isLocal();
				}
				else if (s instanceof IFormalParameterSymbol) {
					test = false;
				}
				break;
			default:
			}
			return test;
		};
	}
	
	private void addCompletionProposalsFromScope( IModulaSymbolScope scope
            , List<ICompletionProposal> completionProposals, Set<ICompletionProposal> proposalsSet, Predicate<IModulaSymbol> symbolFilter) 
    {
        if (scope != null) {
            for (IModulaSymbol s : scope) {
                addModulaSymbolToCompletionProposals(completionProposals, proposalsSet, s, symbolFilter);            
            }
            if (scope instanceof IModuleSymbol) {
                for (IModulaSymbol s : ((IModuleSymbol)scope).getExportedSymbols()) {
                    addModulaSymbolToCompletionProposals(completionProposals, proposalsSet, s, symbolFilter);            
                }
            }
            if (scope instanceof ISymbolWithImports) {
                for (IModulaSymbol s : ((ISymbolWithImports)scope).getImports()) {
                    addModulaSymbolToCompletionProposals(completionProposals, proposalsSet, s, symbolFilter);            
                }
            }
        }
    }

	private void addModulaSymbolToCompletionProposals(List<ICompletionProposal> completionProposals, Set<ICompletionProposal> proposalsSet,
            IModulaSymbol s, Predicate<IModulaSymbol> symbolFilter) 
    {
        if (s == null || s.getName() == null) {
            return; // parser sometimes may produce it 
        }

        if (symbolFilter != null && !symbolFilter.test(s)) {
            return;
        }
        
        String proposalText =  s.getName();
        
        String filterPrefix = context.getBeforeCursorWordPart();
        if (filterPrefix != null && !StringUtils.startsWithIgnoreCase(proposalText, filterPrefix)) {
            return;
        }

        addProposal(completionProposals, proposalsSet, s);
    }

	private void addProposal(List<ICompletionProposal> completionProposals, Set<ICompletionProposal> proposalsSet,
			IModulaSymbol s) {
		String proposalText =  s.getName();
		StyledString sstring = new StyledString(proposalText);
        sstring.append(ModulaSymbolDescriptions.getSymbolDescription(s), StyledString.DECORATIONS_STYLER);
        
        String replacementString;
        if (s instanceof IProcedureSymbol && context.getRegionType() != RegionType.IMPORT_STATEMENT) {
            replacementString = createReplacementStringFromProcedureSymbol((IProcedureSymbol) s);
        } else {
        	StringBuilder sb = new StringBuilder();
        	if (context.getRegionType() == RegionType.IMPORT_STATEMENT) {
        		if (ContentAssistUtils.tokenType(context.getPreviousNonSpaceToken()) == ModulaTokenTypes.IDENTIFIER) {
        			sb.append(',');
        		}
        	}
        	sb.append(s.getName());
        	if (context.getRegionType() == RegionType.IMPORT_STATEMENT) {
        		if (ContentAssistUtils.tokenType(context.getNextNonSpaceToken()) == ModulaTokenTypes.IDENTIFIER) {
        			sb.append(',');
        		}
        	}
        	replacementString = sb.toString();
        }

        int cursorPosition = Math.max(context.getReplacementLength(), replacementString.length()); //XXX ??

        ICompletionProposal proposal = new ModulaContextualCompletionProposal(
                replacementString, 
                context.getReplacementOffset(), 
                context.getReplacementLength(),
                cursorPosition, 
                ModulaSymbolImages.getImage(s), 
                sstring, 
                null, ""); //$NON-NLS-1$

        if (!proposalsSet.contains(proposal)) {
        	proposalsSet.add(proposal);
            completionProposals.add(proposal);
        }
	}
	
	private String createReplacementStringFromProcedureSymbol(IProcedureSymbol ps) {
        StringBuilder sb = new StringBuilder();
        sb.append(ps.getName());
        if (context.getRegionType() == RegionType.PROCEDURE_BODY) {
        	String lineTail = context.getCurrentLineTail();
        	boolean isParamsExists = lineTail.startsWith("(") && !lineTail.startsWith("(*"); //$NON-NLS-1$ //$NON-NLS-2$
        	if (!isParamsExists) {
        		// no (<arguments>) exists in the target text => add default 
        		sb.append('(');
        		Collection<IFormalParameterSymbol> parameters = ps.getParameters();
        		int i = 0;
        		for (Iterator<?> iterator = parameters.iterator(); iterator
        				.hasNext();) {
        			if (i++ > 0) {
        				sb.append(", "); //$NON-NLS-1$
        			}
        			IFormalParameterSymbol parameterSymbol = (IFormalParameterSymbol) iterator.next();
        			sb.append(parameterSymbol.getName());
        		}
        		sb.append(')');
        	}
        }
        return sb.toString();
    }

	private void dottedExpressionProposals(CompletionContext context,
			List<ICompletionProposal> proposals, Set<ICompletionProposal> proposalsSet) {
		Predicate<IModulaSymbol> symbolFilter = createSymbolFilter(context);
		IModulaSymbol symbol = context.getReferencedSymbol();
		if (symbol == null) {
			return;
		}
		if (symbol instanceof IModuleAliasSymbol) {
			symbol = ((IModuleAliasSymbol)symbol).getReference();
        }
		ITypeSymbol typeSymbol = JavaUtils.as(ITypeSymbol.class, symbol);
		if (typeSymbol != null) {
			addCompletionProposalsFromTypeSymbol(proposals, proposalsSet, typeSymbol, symbolFilter);
		}
		else {
			IModuleSymbol moduleSymbol = JavaUtils.as(IModuleSymbol.class, symbol);
			if (moduleSymbol != null) {
				addCompletionProposalsFromDefinitionModule(proposals, proposalsSet, moduleSymbol, symbolFilter);
			}
		}
	}
	
	private void addCompletionProposalsFromDefinitionModule( List<ICompletionProposal> completionProposals, Set<ICompletionProposal> proposalsSet, IModuleSymbol module, Predicate<IModulaSymbol> symbolFilter )
    {
        List<IModulaSymbol> al = new ArrayList<IModulaSymbol>();
        for (IModulaSymbol symbol : module.getExportedSymbols()) {
            if (symbol.getName() != null) {
                al.add(symbol);
            }
        }
        Collections.sort(al, new Comparator<IModulaSymbol>() {
            @Override
            public int compare(IModulaSymbol s1, IModulaSymbol s2) {
                int ord1 = getTypeOrder(s1);
                int ord2 = getTypeOrder(s2);
                if (ord1 != ord2) {
                    return ord1 - ord2;
                }
                return s1.getName().compareToIgnoreCase(s2.getName());
            }

            private int getTypeOrder(IModulaSymbol s) {
                if (s instanceof IProcedureSymbol || s instanceof IStandardProcedureSymbol) {
                    return 1;
                } 
                if (s instanceof IVariableSymbol) {
                    return 2;
                } 
                if (s instanceof ITypeSymbol) {
                    return 3;
                } 
                if (s instanceof IEnumTypeSymbol) {
                    return 4;
                } 
                if (s instanceof IEnumElementSymbol) {
                    return 5;
                } 
                if (s instanceof IConstantSymbol) {
                    return 6;
                } 
                return 7;
            }
        });

        for (IModulaSymbol s : al) {
            addModulaSymbolToCompletionProposals(completionProposals, proposalsSet, s, symbolFilter);       
        }
    }
	
	private void addCompletionProposalsFromTypeSymbol(List<ICompletionProposal> completionProposals, Set<ICompletionProposal> proposalsSet, ITypeSymbol symbol, Predicate<IModulaSymbol> symbolFilter) 
    {
        if (symbol instanceof IPointerTypeSymbol) {
            ITypeSymbol its = ((IPointerTypeSymbol)symbol).getBoundTypeSymbol();
            if (its instanceof ISymbolWithScope) {
                symbol = its;
            }
        }
        if (symbol instanceof ISymbolWithScope) {
            for (IModulaSymbol modulaSymbol : getSymbolsFromSuperType((ISymbolWithScope)symbol)) {
                addModulaSymbolToCompletionProposals(completionProposals, proposalsSet, modulaSymbol, symbolFilter);
            }
        }
    }
	
	private Iterable<IModulaSymbol> getSymbolsFromSuperType(ISymbolWithScope scope) {
		Iterable<IModulaSymbol> iterable = scope;
		if (scope instanceof IRecordTypeSymbol) {
			Iterable<IRecordTypeSymbol> hierarchy = () -> new Iterator<IRecordTypeSymbol>() {
				IRecordTypeSymbol current = (IRecordTypeSymbol)scope;
				@Override
				public IRecordTypeSymbol next() {
					IRecordTypeSymbol next = current;
					current = current.getBaseTypeSymbol();
					return next;
				}
				
				@Override
				public boolean hasNext() {
					return current != null;
				}
			};
			iterable = () -> StreamSupport.stream(hierarchy.spliterator(), false).flatMap(r -> StreamSupport.stream(r.spliterator(), false)).iterator();
		}
		
		return iterable;
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer,
			int offset) {
		return null;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return PROPOSAL_ACTIVATION_CHARS;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	@Override
	public void setCompletionContext(CompletionContext context) {
		this.context = context;
	}
	
	private final class CompletionListener implements ICompletionListener, ICompletionListenerExtension  {
        @Override
        public void selectionChanged(ICompletionProposal proposal,
                boolean smartToggle) {
        }

        @Override
        public void assistSessionStarted(ContentAssistEvent e) {
            proposalCategoryIteration = 0;
            if (IS_DEBUG_PRINT) {
            	System.out.println("assistSessionStarted:::");
            }
        }

        @Override
        public void assistSessionEnded(ContentAssistEvent e) {
        	if (IS_DEBUG_PRINT) {
        		System.out.println("assistSessionEnded:::");
        	}
        }

        @Override
        public void assistSessionRestarted(ContentAssistEvent e) {
            isAssistSessionRestarted = true;
            if (IS_DEBUG_PRINT) {
            	System.out.println("assistSessionRestarted:::");
            }
        }
    }
}
