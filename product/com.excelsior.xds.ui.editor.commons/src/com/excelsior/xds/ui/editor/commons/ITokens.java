package com.excelsior.xds.ui.editor.commons;

public interface ITokens {
    /**
     * This coloring is 'default' (associated with plain text). It is used for 
     * elements with 'disabled' coloring
     */
    PersistentTokenDescriptor getDefaultColoring();
    
    
    PersistentTokenDescriptor getToken();
    
    String getCategoryName();
}
