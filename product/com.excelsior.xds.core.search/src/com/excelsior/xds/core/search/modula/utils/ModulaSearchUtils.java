package com.excelsior.xds.core.search.modula.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.ITextEditor;

import com.excelsior.xds.core.compiler.driver.CompileDriver;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.search.SearchCorePlugin;
import com.excelsior.xds.core.search.modula.ModulaSearchInput;
import com.excelsior.xds.core.text.TextEncoding;
import com.excelsior.xds.core.utils.XdsFileUtils;
import com.excelsior.xds.parser.commons.symbol.QualifiedNameFactory;
import com.excelsior.xds.parser.modula.symbol.IConstantSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IProcedureSymbol;
import com.excelsior.xds.parser.modula.symbol.IRecordFieldSymbol;
import com.excelsior.xds.parser.modula.symbol.IVariableSymbol;
import com.excelsior.xds.parser.modula.symbol.SymbolAttribute;
import com.excelsior.xds.parser.modula.symbol.type.IForwardTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;

public final class ModulaSearchUtils 
{
	private ModulaSearchUtils(){
	}
	
    /**
     * @return IDocument for IFile if it is opened in editor and it is dirty
     *         or null
     */
    public static IDocument evalNonFileBufferDocument(IFile ifile) {
        Map<IFile, IDocument> map = evalDirtyDocs(ifile);
        return map.get(ifile);
    }

    /**
     * @return returns a map from IFile to IDocument for all open, dirty editors
     */
    public static Map<IFile, IDocument> evalNonFileBufferDocument() {
        return evalDirtyDocs(null);
    }
    
   
    /**
     * @param iFile file to get IDocument for
     * @param documentsCache null or contains known file documents (f.ex. see evalNonFileBufferDocument())
     *                       Out: newly found <IFile, IDocument> will be added here if any 
     * @return AST or null
     */
    public static IDocument getDocumentForIFile(IFile iFile, Map<IFile, IDocument> documentsCache) 
    {
        IDocument document = null;
        if (documentsCache != null) {
            document = documentsCache.get(iFile);
        }
        if (document == null) { 
            if (iFile.exists()) {
                StringBuilder sb = new StringBuilder();
                try {
                	if (XdsFileUtils.isSymbolFile(iFile.getName()) && iFile.getLocation() != null) {
                		String symFileText = CompileDriver.decodeSymFile(ResourceUtils.getAbsolutePath(iFile));
                		if (symFileText == null){
                			return null;
                		}
						sb.append(symFileText);
                    }
                    else {
                    	try(InputStream stream = iFile.getContents()){
							TextEncoding.readFileAndCodepage(stream, sb, null);
						} catch (CoreException e) {
							LogHelper.logError(e);
						}
                    }
					document = new Document(sb.toString());
	                if (documentsCache != null) {
	                    documentsCache.put(iFile, document);
	                }
				} catch (IOException e) {
					LogHelper.logError(e);
				}
            }
        }
        return document;
    }

    
    public static Map<IFile, IDocument> evalDirtyDocs(IFile filter) {
        Map<IFile, IDocument> result = new HashMap<IFile, IDocument>();
        IWorkbench workbench = SearchCorePlugin.getDefault().getWorkbench();
        IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
        for (int i = 0; i < windows.length; i++) {
            IWorkbenchPage[] pages = windows[i].getPages();
            for (int x = 0; x < pages.length; x++) {
                IEditorReference[] editorRefs = pages[x].getEditorReferences();
                for (int z = 0; z < editorRefs.length; z++) {
                    IEditorPart ep = editorRefs[z].getEditor(false);
                    if (ep instanceof ITextEditor && ep.isDirty()) { // only
                                                                     // dirty
                                                                     // editors
                        evaluateTextEditor(result, ep, filter);
                    }
                }
            }
        }
        return result;
    }
    
    /**
	 * @param symbol
	 * @return names of the symbols related to given symbol (now only forward declaration), including itself
	 */
	public static Set<String> getSymbolQualifiedNames(IModulaSymbol symbol) {
		List<String> qualifiedNames = new ArrayList<String>();
		qualifiedNames.add(symbol.getQualifiedName());
		if (symbol instanceof IForwardTypeSymbol) {
			IForwardTypeSymbol forwardTypeSymbol = (IForwardTypeSymbol)symbol;
			ITypeSymbol actualTypeSymbol = forwardTypeSymbol.getActualTypeSymbol();
			if (actualTypeSymbol != null) {
				qualifiedNames.add(actualTypeSymbol.getQualifiedName());
			}
		}
		else{
			String forwardDeclQualifiedName = QualifiedNameFactory.getQualifiedName(symbol.getQualifiedName(), SymbolAttribute.FORWARD_DECLARATION);
			qualifiedNames.add(forwardDeclQualifiedName);
		}
		return new HashSet<String>(qualifiedNames);
	}

	public static  int getSearchForConstant(IModulaSymbol symbol) {
		int searchForConstant = ModulaSearchInput.SEARCH_FOR_ANY_ELEMENT;
		if (symbol instanceof ITypeSymbol) {
			searchForConstant = ModulaSearchInput.SEARCH_FOR_TYPE;
		}
		else if (symbol instanceof IVariableSymbol) {
			searchForConstant = ModulaSearchInput.SEARCH_FOR_VARIABLE;
		}
		else if (symbol instanceof IProcedureSymbol) {
			searchForConstant = ModulaSearchInput.SEARCH_FOR_PROCEDURE;
		}
		else if (symbol instanceof IRecordFieldSymbol) {
			searchForConstant = ModulaSearchInput.SEARCH_FOR_FIELD;
		}
		else if (symbol instanceof IConstantSymbol) {
			searchForConstant = ModulaSearchInput.SEARCH_FOR_CONSTANT;
		}
		else if (symbol instanceof IModuleSymbol) {
			searchForConstant = ModulaSearchInput.SEARCH_FOR_MODULE;
		}
		return searchForConstant;
	}
    
    private static void evaluateTextEditor(Map<IFile, IDocument> result, IEditorPart ep, IFile filter) {
        IEditorInput input = ep.getEditorInput();
        if (input instanceof IFileEditorInput) {
            IFile file = ((IFileEditorInput) input).getFile();
            if (filter == null || filter.equals(file)) { 
                if (!result.containsKey(file)) { // take the first editor found
                    ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
                    ITextFileBuffer textFileBuffer = bufferManager
                            .getTextFileBuffer(file.getFullPath(), LocationKind.IFILE);
                    if (textFileBuffer != null) {
                        // file buffer has precedence
                        result.put(file, textFileBuffer.getDocument());
                    }
                    else {
                        // use document provider
                        IDocument document = ((ITextEditor) ep).getDocumentProvider().getDocument(input);
                        if (document != null) {
                            result.put(file, document);
                        }
                    }
                }
            }
        }
    }

    
}
