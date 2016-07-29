package com.excelsior.xds.core.compiler.compset;

import java.util.Collection;

/**
 * Notifies about the changes in Compilation Set. If file in compilation set, but external its addition or removal from compilation
 * set will generate appropriate event, but it is possible that external resource will not be mapped to workspace at the moment.
 * Thus, listeners who need corresponding workspace resource, must also listen to workspace deltas. 
 * TODO : fix this, so 
 * 
 * @author lsa80
 */
public interface ICompilationSetListener {
    /**
     * @param projectName
     * @param pathes - the following paths were added to the compilationSet of the project
     */
    public void added(String projectName, Collection<String> pathes); 
    
    /**
     * @param projectName
     * @param pathes - the following paths were removed from the compilationSet of the project
     */
    public void removed(String projectName, Collection<String> compilationSet);
}
