package com.excelsior.xds.ui.editor.commons.scanner.jflex;

/**
 * A JFlex partition token scanner.
 *   
 * @author lion
 */
public interface IFlexPartitionScanner extends IFlexScanner {

    /**
     * Returns the lexical state by content type.
     * 
     * @param contentType the content type 
     * @return the lexical token for given content type
     */
    int getState(String contentType);
    
}
