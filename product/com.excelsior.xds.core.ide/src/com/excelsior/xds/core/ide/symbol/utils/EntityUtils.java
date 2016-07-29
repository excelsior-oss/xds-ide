package com.excelsior.xds.core.ide.symbol.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.eclipse.core.resources.IProject;

import com.excelsior.xds.core.builders.BuildSettings;
import com.excelsior.xds.core.compiler.compset.CompilationSetManager;
import com.excelsior.xds.core.ide.symbol.ParseTask;
import com.excelsior.xds.core.ide.symbol.ParseTaskFactory;
import com.excelsior.xds.core.ide.symbol.SymbolModelManager;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.utils.JavaUtils;
import com.excelsior.xds.core.utils.XdsFileUtils;
import com.excelsior.xds.parser.modula.symbol.IDefinitionModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IImplemantationModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IProcedureDeclarationSymbol;
import com.excelsior.xds.parser.modula.symbol.IProcedureDefinitionSymbol;
import com.excelsior.xds.parser.modula.symbol.IStandardModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.reference.ReferenceUtils;
import com.excelsior.xds.parser.modula.symbol.type.IForwardTypeSymbol;
import com.excelsior.xds.parser.modula.utils.ModulaSymbolUtils;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public final class EntityUtils {
	private EntityUtils() {
	}
	
	/**
	 * Get all symbols related to the given symbol. For the procedure, for example, this
	 * will be both IProcedureDefinitionSymbol, IProcedureDeclarationSymbol and all forward declarations to this procedure.
	 * Actually, this method find 'parts' of meta-entity. For example, procedure definition 'add_solution' and procedure declaration 'add_solution' are
	 * parts of 'add_solution' Procedure meta-entity. 
	 * 
	 * PLEASE NOTE : Method can take a while for execute. This because this method can result in synchronous call to the SymbolModelManager. Use with caution
	 *  
	 * @param project - project setting compilation set context for the symbols. Can be null if isExact false.
	 * @param isExact - search only in compilation set, so all symbols found (except startingSymbol) will be in the compilation set
	 * @param startingSymbol - symbol to start search from 
	 * @return
	 */
	public static Collection<IModulaSymbol> syncGetRelatedSymbols(IProject project, boolean isExact, IModulaSymbol startingSymbol) {
		Set<IModulaSymbolReference<IModulaSymbol>> refs = new HashSet<IModulaSymbolReference<IModulaSymbol>>();
		
    	Queue<IModulaSymbol> symbolsToVisit = new LinkedList<IModulaSymbol>();
    	symbolsToVisit.add(startingSymbol);
    	   	
    	while(!symbolsToVisit.isEmpty()) {
    		IModulaSymbol symbol = symbolsToVisit.poll();
    		if (symbol != null && addToResult(refs, project, isExact, startingSymbol, symbol)) {
    			if (symbol instanceof IProcedureDefinitionSymbol) {
    				IProcedureDefinitionSymbol procDefinition = (IProcedureDefinitionSymbol)symbol;
    				IProcedureDeclarationSymbol declarationSymbol = procDefinition.getDeclarationSymbol();
    				symbolsToVisit.add(declarationSymbol);
    			}
    			else if (symbol instanceof IProcedureDeclarationSymbol) {
    				IProcedureDeclarationSymbol procDeclaration = (IProcedureDeclarationSymbol)symbol;
    				symbolsToVisit.addAll(Lists.newArrayList(procDeclaration.getForwardDeclarations()));
    				
    				IProcedureDefinitionSymbol definitionSymbol = procDeclaration.getDefinitionSymbol();
    				symbolsToVisit.add(definitionSymbol);
    			}else if (symbol instanceof IDefinitionModuleSymbol) {
					IDefinitionModuleSymbol definitionModuleSymbol = (IDefinitionModuleSymbol) symbol;
					IImplemantationModuleSymbol implemantationModuleSymbol = syncGetImplementationModuleSymbol(project, isExact, definitionModuleSymbol);
					symbolsToVisit.add(implemantationModuleSymbol);
				}else if (symbol instanceof IImplemantationModuleSymbol) {
					IImplemantationModuleSymbol implemantationModuleSymbol = (IImplemantationModuleSymbol) symbol;
					IDefinitionModuleSymbol definitionModule = syncGetDefinitionModuleSymbol(project, isExact, implemantationModuleSymbol);
					symbolsToVisit.add(definitionModule);
				}
				else if (symbol instanceof IForwardTypeSymbol) {
					IForwardTypeSymbol forwardTypeSymbol = (IForwardTypeSymbol) symbol;
					symbolsToVisit.add(forwardTypeSymbol.getActualTypeSymbol());
				}
    		}
    	}
    	
    	List<IModulaSymbol> symbols = new ArrayList<IModulaSymbol>();
    	Iterables.addAll(symbols, ReferenceUtils.transformToSymbols(refs));
    	return symbols;
    }
	
	private static boolean addToResult(Set<IModulaSymbolReference<IModulaSymbol>> refs,
			IProject project, boolean isExact,
			IModulaSymbol startingSymbol, IModulaSymbol symbol) {
		IModulaSymbolReference<IModulaSymbol> ref = ReferenceUtils.createRef(symbol);
		if (refs.contains(ref)) {
			return false;
		}
		
		if (isExact && symbol != startingSymbol){
			File sourceFile = ModulaSymbolUtils.getSourceFile(symbol);
			if (sourceFile != null) {
				if (!isInCompilationSet(project, symbol)) {
					symbol = null;
				}
			}
			else{
				symbol = null;
			}
		}
		
		if (symbol != null) {
			return refs.add(ref);
		}
		
		return false;
	}
	
	public static boolean isInCompilationSet(IProject project, IModulaSymbol symbol) {
		if (project == null) {
			return false;
		}
		File sourceFile = ModulaSymbolUtils.getSourceFile(symbol);
		if (sourceFile != null) {
			String compilationSetFile = CompilationSetManager.getInstance().lookup(project.getName(), sourceFile.getName());
			return ResourceUtils.equalsPathesAsInFS(compilationSetFile, sourceFile.getAbsolutePath());
		}
		else {
			return symbol instanceof IStandardModuleSymbol;
		}
	}

	/**
	 * Looks for the IImplemantationModuleSymbol corresponding to the IDefinitionModuleSymbol. 
	 * PLEASE NOTE : Method can take a while for execute, use with caution.
	 * 
	 * @param isExact 
	 * @param project 
	 * 
	 * @param moduleSymbol
	 * @return
	 */
	public static IImplemantationModuleSymbol syncGetImplementationModuleSymbol(IProject project, boolean isExact, IDefinitionModuleSymbol moduleSymbol) {
		return JavaUtils.as(IImplemantationModuleSymbol.class, syncGetCoupledModuleSymbol(project, isExact, moduleSymbol));
	}
	
	/**
	 * Looks for the IDefinitionModuleSymbol corresponding to the IImplemantationModuleSymbol. 
	 * PLEASE NOTE : Method can take a while for execute, use with caution.
	 * @param isExact 
	 * @param projectName 
	 * 
	 * @param moduleSymbol
	 * @return
	 */
	public static IDefinitionModuleSymbol syncGetDefinitionModuleSymbol(IProject project, boolean isExact, IImplemantationModuleSymbol moduleSymbol) {
		return JavaUtils.as(IDefinitionModuleSymbol.class, syncGetCoupledModuleSymbol(project, isExact, moduleSymbol));
	}
	
	/**
	 * Looks for the IDefinitionModuleSymbol corresponding to the IImplemantationModuleSymbol (or vice versa). 
	 * 
	 * PLEASE NOTE : Method can take a while for execute, use with caution.
	 * @param isExact - this parameter constrains search to the compilation set. if  {@link false}, then lookups will be used to search.
	 * @param project 
	 * 
	 * @param moduleSymbol
	 * @return
	 */
	public static IModuleSymbol syncGetCoupledModuleSymbol(IProject project, boolean isExact, IModuleSymbol moduleSymbol) {
		String name = moduleSymbol.getName();
		if (name == null) {
			return null;
		}
		
		if (isExact && !isInCompilationSet(project, moduleSymbol)) {
			return null;
		}
		
		String moduleName = moduleSymbol.getName();
		String coupledModulePath = null;
		String coupledModuleFileName = null;
		if (moduleName != null) {
			if (JavaUtils.isOneOf(moduleSymbol, IDefinitionModuleSymbol.class)){
				coupledModuleFileName = XdsFileUtils.getProgramModuleFileName(moduleName);
			}
			else {
				coupledModuleFileName = XdsFileUtils.getDefinitionModuleFileName(moduleName);
			}
			coupledModulePath = CompilationSetManager.getInstance().lookup(project, coupledModuleFileName);
		}
		
		BuildSettings buildSettings = moduleSymbol.getBuildSettings();
		if (coupledModulePath == null && !isExact) {
			if (coupledModuleFileName != null) {
				coupledModulePath = ResourceUtils.getAbsolutePath(buildSettings.lookup(coupledModuleFileName));
			}
		}
		
		if (coupledModulePath != null) {
			File coupledModuleFile = new File(coupledModulePath);
			if (coupledModuleFile.exists()) {
				ParseTask parseTask = ParseTaskFactory.createTask(project, Arrays.asList(coupledModuleFile));
				parseTask.setBuildSettings(buildSettings);
				parseTask.setForce(false);
				return SymbolModelManager.instance().syncParseFirstSymbol(parseTask);
			}
		}
		
		return null;
	}
}
