package com.excelsior.xds.parser.modula.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import com.excelsior.xds.core.project.SpecialFolderNames;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.parser.commons.pst.PstLeafNode;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.IAstSymbolRef;
import com.excelsior.xds.parser.modula.ast.IAstSymbolScope;
import com.excelsior.xds.parser.modula.ast.ModulaAst;
import com.excelsior.xds.parser.modula.symbol.ILocalModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbolScope;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IStandardModuleSymbol;

public abstract class ModulaSymbolUtils 
{
    /**
     * @param scope
     * @param symbolName
     * @param expectedClass expected class of found symbol
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends IModulaSymbol> T findSymbolInScope( IModulaSymbolScope scope
                                                               , String symbolName
                                                               , Class<T> expectedClass )
    {
        IModulaSymbol symbol = scope.findSymbolInScope(symbolName);
        if (symbol != null && !expectedClass.isAssignableFrom(symbol.getClass())) {
            symbol = null;
        }
        return (T)symbol;
    }
    
    
    /**
     * Returns the parent module of the given symbol or 
     * <tt>null</tt> if parent module was not found.
     * 
     * @param symbol a symbol to be analyzed
     * 
     * @return parent module symbol
     */
    public static IModuleSymbol getParentModule(IModulaSymbol symbol) {
        while (symbol != null) {
            symbol = symbol.getParentScope();
            if (symbol instanceof IModuleSymbol) {
                return (IModuleSymbol)symbol;
            }
        }
        return null;
    }

    /**
     * Checks that a symbol is defined in the given module or its local modules.
     *  
     * @param module, the module to operate on.
     * @param symbol, a symbol to be checked
     * 
     * @return @true if symbol is defined inside given module.
     */
    public static boolean isSymbolFromModule(IModuleSymbol module, IModulaSymbol symbol)
    {
        IModuleSymbol symbolParent = (symbol instanceof IModuleSymbol)
                                   ? (IModuleSymbol)symbol
                                   : getParentModule(symbol);
                                   
        if (module instanceof IStandardModuleSymbol) {
            return module.equals(symbolParent); 
        }
        else {
            while (symbolParent != null) {
                if (module.equals(symbolParent)) {
                    return true;
                }
                symbolParent = getParentModule(symbolParent);
            }
        }
        
        return false;
    }
    
    /**
     * Returns the source file of the given symbol or 
     * <tt>null</tt> if the source file was not defined for the symbol.
     *   
     * @return the path the file system, or 
     *         <tt>null</tt> if symbol has no associated source file 
     */
    public static File getSourceFile(IModulaSymbol symbol) {
        while (symbol != null) {
            if (symbol instanceof IModuleSymbol) {
            	try {
					return ResourceUtils.getAbsoluteFile(((IModuleSymbol)symbol).getSourceFile());
				} catch (CoreException e) {
					return null;
				}
            }
            symbol = symbol.getParentScope();
        }
        return null;
    }
    
    /**
     * Returns the source file of the given symbol or 
     * <tt>null</tt> if the source file was not defined for the symbol.
     *   
     * @return the path the file system, or 
     *         <tt>null</tt> if symbol has no associated source file 
     */
    public static IFileStore getSourceFileStore(IModulaSymbol symbol) {
        while (symbol != null) {
            if (symbol instanceof IModuleSymbol) {
            	return ((IModuleSymbol)symbol).getSourceFile();
            }
            symbol = symbol.getParentScope();
        }
        return null;
    }
    
    /**
     * Returns the handles of all files that are mapped to the source file of given symbol, filter by project, if it is not null
     * 
     * @return the corresponding files in the workspace, or 
     *         an empty array if none
     */
    public static IFile[] findIFilesForSymbol(IProject project, IModulaSymbol symbol) {
        IFile[] files; 
        IFileStore file = ModulaSymbolUtils.getSourceFileStore(symbol);
        if (file == null) {
            files = new IFile[0];
        }
        else {
            files = ResourceUtils.getWorkspaceRoot().findFilesForLocationURI(file.toURI());
        }
        List<IFile> resultIfiles = new ArrayList<IFile>();
    	for (IFile f : files) {
    		if ((project == null || f.getProject().equals(project)) && !SpecialFolderNames.isInsideIgnoredSpecialFolder(f)) {
    			resultIfiles.add(f);
    		}
    	}
        return resultIfiles.toArray(new IFile[0]);
    }
    
    /**
     * Returns the handle of single file that is mapped to the source file of 
     * the given symbol.
     * 
     * @return the corresponding file in the workspace, or
     *         <tt>null<tt> if none or more than one. 
     */
    public static IFile findFirstFileForSymbol(IProject project, IModulaSymbol symbol) {
        IFile result = null;
        IFile[] files = findIFilesForSymbol(project, symbol);
        if (files.length > 0) {
            result = files[0];
        }
        return result;
    }
    
    /**
     * Returns the handle of single file that is mapped to the source file of 
     * the given symbol.
     * 
     * @return the corresponding file in the workspace, or
     *         <tt>null<tt> if none or more than one. 
     */
    public static IFile findFileForSymbol(IProject project, IModulaSymbol symbol) {
        IFile result = null;
        IFile[] files = findIFilesForSymbol(project, symbol);
        if (files.length == 1) {
            result = files[0];
        }
        return result;
    }
    
    public static IModulaSymbol getSymbol(PstNode node) {
        while (node != null) {
            if (node instanceof IAstSymbolRef) {
                return ((IAstSymbolRef)node).getSymbol();
            }
            node = node.getParent();
        }
        return null;
    }
    
    
    public static IModulaSymbolScope getParentScope(PstLeafNode leafNode) {
        if (leafNode != null) {
            PstNode parent = leafNode.getParent();
            while (parent != null) {
                if (parent instanceof IAstSymbolScope) {
                    return ((IAstSymbolScope)parent).getScope();
                }
                parent = parent.getParent();
            }
        }
    	return null;
    }
    
    /**
     * Returns a list of enclosed scopes, containing the given leaf node.
     * Last one is always super-module.
     *  
     * @param leafNode
     * @return list of enclosed scopes, containing the leaf node.
     */
    public static List<IModulaSymbolScope> getAllParentScopes(PstLeafNode leafNode) {
    	List<IModulaSymbolScope> scopes = new ArrayList<IModulaSymbolScope>();
    	
    	IModulaSymbolScope scope = getParentScope(leafNode);
    	while (scope != null) {
    		scopes.add(scope);
    		scope = scope.getParentScope();
    	}
    	
    	return scopes;
    }

    
    /**
     * Returns the host (non-local) module of the PST node.
     * 
     * @param node the PstNode for which the host module is looked for
     * @return host (non-local) module symbol
     */
    public static IModuleSymbol getHostModule(PstNode node)  {
        while (node != null) {
            if (node instanceof ModulaAst) {
                return ((ModulaAst)node).getModuleSymbol();
            }
            node = node.getParent();
        }
        return null;
    }
    
    
    /**
     * Returns name of a symbol. 
     * 
     * @param symbol the symbol to be processed, can be <tt>null</tt>
     * 
     * @return a symbol's name or <tt>null</tt>.
     */
    public static String getSymbolName(IModulaSymbol symbol) {
        String name = null;
        if (symbol != null) {
            name = symbol.getName();
        }
        return name;
    }
    
    
    /**
     * Finds top-level IModuleSymbol containing IModulaSymbol (via traversing of the Parent Scope).
     * 
     * @param symbol the symbol for which the host module is looked for
     * @return host (non-local) module symbol
     */
    public static IModuleSymbol getHostModule(IModulaSymbol symbol) {
        while (symbol != null) {
            if (isNonLocalModule(symbol)) {
                return (IModuleSymbol) symbol;
            }
            symbol = symbol.getParentScope();
        }
        return null;
    }

    
    /**
     * Checks that a symbol is not local module.
     *  
     * @param symbol, a symbol to be checked
     * 
     * @return @true if symbol is not local module.
     */
    public static boolean isNonLocalModule(IModulaSymbol symbol) {
        return (symbol instanceof IModuleSymbol) && !(symbol instanceof ILocalModuleSymbol);
    }
    
}
