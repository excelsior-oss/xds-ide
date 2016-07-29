package com.excelsior.xds.parser.modula;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Languages and calling conventions are supported by XDS compiler. 
 *
 * The compiler must know the implementation language of a module to take into 
 * account different semantics of different languages and to produce correct code.
 *  
 * In some cases, it is necessary for a procedure or data type to be implemented 
 * according to the rules of a language other than that of the whole module. 
 * In XDS, it is possible to explicitly specify the language of a type or object.
 *
 * Language can be a string or integer constant expression.
 */
public enum XdsLanguage {
    Oberon2 ("Oberon", 0),    //$NON-NLS-1$
    Modula2 ("Modula", 1),    //$NON-NLS-1$
    C       ("C",      2),    //$NON-NLS-1$
//  Java,
//  JBC,                                       // used only in project system to distinguish Java FEs
//  SL1,
//  Pascal,
//  BNRPascal,
    StdCall ("StdCall", 8),   //$NON-NLS-1$    // Win32 system call
    SysCall ("SysCall", 9),   //$NON-NLS-1$    // OS/2 system call
    OSCall  ("OSCall", 10),   //$NON-NLS-1$    // K26 system call
    RtsCall ("RtsCall", 11),  //$NON-NLS-1$    // XDS run-time call
    ;
    
    private final String name;
    private final long   index;
    
    private XdsLanguage(String name, long index) {
        this.name  = name;
        this.index = index;
    }
    
    public static final Map<String, XdsLanguage> NAME_TO_LANGUAGE = new HashMap<String, XdsLanguage>();
    static {
        for (XdsLanguage language: XdsLanguage.values()) {
            NAME_TO_LANGUAGE.put(language.name, language);
        }
    };
    
    public static final Map<Long, XdsLanguage> INDEX_TO_LANGUAGE = new HashMap<Long, XdsLanguage>();
    static {
        for (XdsLanguage language: XdsLanguage.values()) {
            INDEX_TO_LANGUAGE.put(language.index, language);
        }
    };
    
    public static final Set<XdsLanguage> EXTERNAL_PROCEDURES = new HashSet<XdsLanguage>(Arrays.asList(
            C,   StdCall,   SysCall,   OSCall
    ));

}
