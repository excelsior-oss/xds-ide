package com.excelsior.xds.parser.modula;

/**
 * Definition of XDS compiler equations.
 * 
 * An equation is a pair (name,value), where value is in general case an arbitrary 
 * string. Some equations have a limited set of valid values, some may not have 
 * the empty string as a value. 
 */
public interface XdsEquations {

    /**
     * Internal compiler equation holds compiled module name. 
     */
    String MODULE = "MODULE";    //$NON-NLS-1$ 

    /**
     * Internal compiler equation holds compiled file name with path and extension. 
     */
    String FILE = "FILE";    //$NON-NLS-1$ 

}
