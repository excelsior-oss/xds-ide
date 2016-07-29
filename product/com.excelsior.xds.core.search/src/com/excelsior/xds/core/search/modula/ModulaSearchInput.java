/**
 * see org.eclipse.pde.internal.core.search.PluginSearchInput.
 */
package com.excelsior.xds.core.search.modula;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.text.FileTextSearchScope;

import com.excelsior.xds.core.search.internal.nls.Messages;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IForwardTypeSymbol;

public class ModulaSearchInput 
{
    public static final int SEARCH_FOR_TYPE = 1;
    public static final int SEARCH_FOR_VARIABLE = 2;
    public static final int SEARCH_FOR_PROCEDURE = 3;
    public static final int SEARCH_FOR_FIELD = 4;
    public static final int SEARCH_FOR_CONSTANT = 5;
    public static final int SEARCH_FOR_MODULE = 6;
    public static final int SEARCH_FOR_ANY_ELEMENT = 0;

    public static final int LIMIT_TO_DECLARATIONS   = 0x0001;
    public static final int LIMIT_TO_USAGES     = 0x0002;
    public static final int LIMIT_TO_ALL_OCCURENCES = 0xFFFF;
    
    public static final int MODIFIER_ALL_NAME_OCCURENCES = 0x0001; // search for all name occurences, like << $PROCEDURE_NAME$ END. >> for the procedure, beside usual << PROCEDURE $PROCEDURE_NAME$ >>
    
    public static final int SEARCH_IN_COMP_SET      = 0x0001;
    public static final int SEARCH_IN_ALL_SOURCES   = 0x0002;
    public static final int SEARCH_IN_SDK_LIBRARIES = 0x0004;
    
    private IProject project; // project giving the scope of search, if any
    private String searchString = null;
    private Set<String> qualifiedNames;
    private boolean caseSensitive = true;
    private int searchFor = 0;
    private int limitTo = 0;
    private int searchModifiers = 0;
    private int searchInFlags = 0;
    private FileTextSearchScope searchScope;
    private int searchScopeId;
    private IModulaSymbol symbolToSearchFor;
    
    public ModulaSearchInput(IProject project) {
    	this.project = project;
	}
    
	/**
	 * project giving the scope of search, if any
	 */
	public IProject getProject() {
		return project;
	}
	
	/**
	 * project giving the scope of search, if any
	 */
	public void setProject(IProject project) {
		this.project = project;
	}

	/**
	 * Qualified symbol names to find
	 */
	public Set<String> getQualifiedNames() {
		return qualifiedNames;
	}

	/**
	 * Qualified symbol names to find
	 */
	public void setSymbolQualifiedNames(Set<String> qualifiedNames) {
		this.searchString = null;
		this.qualifiedNames = qualifiedNames;
	}

	public String getSearchString() {
        return searchString;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean value) {
        caseSensitive = value;
    }

    public void setSearchString(String name) {
        searchString = name;
        qualifiedNames = null;
    }

    public int getSearchFor() {
        return searchFor;
    }

    public void setSearchFor(int element) {
        searchFor = element;
    }

    public int getLimitTo() {
        return limitTo;
    }

    public void setLimitTo(int limit) {
        limitTo = limit;
    }

    public int getSearchInFlags() {
        return searchInFlags;
    }

    public void setSearchInFlags(int bits) {
        searchInFlags = bits;
    }
    
    public int getSearchModifiers() {
		return searchModifiers;
	}

	public void setSearchModifiers(int searchModifiers) {
		this.searchModifiers = searchModifiers;
	}

	public FileTextSearchScope getSearchScope() {
        return searchScope;
    }

    public void setSearchScope(FileTextSearchScope scope) {
        searchScope = scope;
    }

    public int getSearchScopeId() {
        return searchScopeId;
    }

    public void setSearchScopeId(int scopeId) {
        searchScopeId = scopeId;
    }

	public IModulaSymbol getSymbolToSearchFor() {
		return symbolToSearchFor;
	}
	
	public boolean isSearchOnlyInCompilationSet() {
		return (searchInFlags & ModulaSearchInput.SEARCH_IN_COMP_SET) != 0;
	}

	public void setSymbolToSearchFor(IModulaSymbol symbolToSearchFor) {
	    while (symbolToSearchFor instanceof IForwardTypeSymbol) {
	        IModulaSymbol actualTypeSymbol = ((IForwardTypeSymbol)symbolToSearchFor).getActualTypeSymbol();
	        if (actualTypeSymbol == null)
	            break;
	        symbolToSearchFor = actualTypeSymbol;
	    }
		this.symbolToSearchFor = symbolToSearchFor;
	}

	
	/**
	 * Constructs something like:
     *   "Declarations of variables 'abc*' in project 'zz.prj'"
     *   "Использование процедуры 'my_proc' in file 'foo.mod'"
	 */
	public String getSearchDescription() {
	    String what = Messages.ModulaSearchInput_AllOccurencies;
	    if (getLimitTo() == LIMIT_TO_DECLARATIONS) {
	        what = Messages.ModulaSearchInput_Declarations;
	    } else if (getLimitTo() == LIMIT_TO_USAGES) {
	        what = Messages.ModulaSearchInput_Usages;
	    }

	    String ofWhat = Messages.ModulaSearchInput_Names;
	    switch (getSearchFor()) {
	    case SEARCH_FOR_TYPE:     ofWhat = Messages.ModulaSearchInput_Types; break;
	    case SEARCH_FOR_VARIABLE: ofWhat = Messages.ModulaSearchInput_Variables; break;
	    case SEARCH_FOR_PROCEDURE:ofWhat = Messages.ModulaSearchInput_Procedures; break;
	    case SEARCH_FOR_FIELD:    ofWhat = Messages.ModulaSearchInput_Fields; break;
	    case SEARCH_FOR_CONSTANT: ofWhat = Messages.ModulaSearchInput_Constants; break;
	    case SEARCH_FOR_MODULE:   ofWhat = Messages.ModulaSearchInput_Modules; break;
	    default:                  ofWhat = ""; break; //$NON-NLS-1$
	    }
	    if (!ofWhat.isEmpty()) ofWhat += ' '; 
	    
        String where = ""; //$NON-NLS-1$
        if (getSearchScopeId() == ISearchPageContainer.WORKSPACE_SCOPE) {
                where = Messages.ModulaSearchInput_Workspace;
        } else {
            where = getSearchScope().getDescription();
        }
        
	    return String.format("%s %s'%s' в  %s", what, ofWhat, getSearchString(), where); //$NON-NLS-1$
	}
}
