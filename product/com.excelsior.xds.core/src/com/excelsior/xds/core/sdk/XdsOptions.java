package com.excelsior.xds.core.sdk;

/**
 * Definition of XDS compiler options.
 * 
 * Options control the process of compilation, including language extensions, 
 * run-time checks and code generation. An option can be set ON (TRUE) or OFF (FALSE). 
 */
public interface XdsOptions 
{
    /** 
     * Enable Oberon-2 language extensions. 
     */
    String O2EXTENSIONS = "O2EXTENSIONS";   //$NON-NLS-1$  
    
    /** 
     * Enable Oberon-2 scientific language extensions, including
     * COMPLEX and LONGCOMPLEX types and the in-line exponentiation operator.
     */
    String O2NUMEXT = "O2NUMEXT";    //$NON-NLS-1$           
    
    /** 
     * Enable XDS Modula-2 language extensions, such as line comment ("--"), 
     * read-only parameters, etc., to be used in the source code.
     */
    String M2EXTENSIONS = "M2EXTENSIONS";    //$NON-NLS-1$  
    
    /** 
     * Enable a set of language extensions that makes the compiler more 
     * compatible with TopSpeed.
     */
    String TOPSPEED = "TOPSPEED";    //$NON-NLS-1$      

    /** 
     * Enable C++ style comments. 
     */
    String CPPCOMMENTS = "CPPCOMMENTS";    //$NON-NLS-1$
    
    /** 
     * Enable the ISO Modula-2 style pragmas <* *> to be used in Oberon-2.
     */
    String O2ISOPRAGMA = "O2ISOPRAGMA";    //$NON-NLS-1$
    
    /** 
     * Enable Modula-2 exceptions and finalization to be used in Oberon-2 programs, 
     * adding keywords EXCEPT, RETRY, and FINALLY. 
     */
    String O2ADDKWD = "O2ADDKWD";    //$NON-NLS-1$
    
    /** 
     * Private option. Enable support of GOTO statement. 
     */
    String M2GOTO = "M2GOTO";    //$NON-NLS-1$
    
    /**
     * Disable generation BEGIN-part of module. 
     */
    String NOMODULEINIT = "NOMODULEINIT";    //$NON-NLS-1$
    
    /**
     * Enable K26 language restrictions
     */
    String K26 = "K26";    //$NON-NLS-1$
    
    /**
     * Enable ASSERT checks
     */
    String ASSERT = "ASSERT";    //$NON-NLS-1$
    
    /**
     * Enable dynarr index checks
     */
    String CHECKDINDEX = "CHECKDINDEX";    //$NON-NLS-1$
    
    /**
     * DIV,MOD - positive divisor checks 
     */
    String CHECKDIV = "CHECKDIV";    //$NON-NLS-1$
     
    /**
     * Enable array index checks    
     */
    String CHECKINDEX = "CHECKINDEX";    //$NON-NLS-1$
    
    /**
     * Enable NIL pointer checks    
     */
    String CHECKNIL = "CHECKNIL";    //$NON-NLS-1$
    
    /**
     * Enable NIL procedure checks  
     */ 
    String CHECKPROC = "CHECKPROC";    //$NON-NLS-1$
    
    /**
     * Enable range checks          
     */ 
    String CHECKRANGE = "CHECKRANGE";    //$NON-NLS-1$
    
    /**
     * Enable set renge checks      
     */ 
    String CHECKSET = "CHECKSET";    //$NON-NLS-1$
    
    /**
     * Enable dynamic type checks   
     */ 
    String CHECKTYPE = "CHECKTYPE";    //$NON-NLS-1$
    
    /**
     * Enable cardinal overflow checks 
     */ 
    String COVERFLOW = "COVERFLOW";    //$NON-NLS-1$
    
    /**
     * Enable integer overflow checks 
     */ 
    String IOVERFLOW = "IOVERFLOW";    //$NON-NLS-1$
    
    /**
     * Enable float overflow checks 
     */ 
    String FOVERFLOW = "FOVERFLOW";    //$NON-NLS-1$
    

    /**
     * Enable default ALLOCATE & DEAL. 
     */ 
    String STORAGE = "STORAGE";    //$NON-NLS-1$
    
    /**
     * Add SHORT and LONG types     
     */ 
    String M2ADDTYPES = "M2ADDTYPES";    //$NON-NLS-1$
    
    /**
     * Use 16-bits INTEGER,CARDINAL,BITSET 
     */ 
    String M2BASE16 = "M2BASE16";    //$NON-NLS-1$
    
    /**
     * Compare symbol files in Modula-2 
     */ 
    String M2CMPSYM = "M2CMPSYM";    //$NON-NLS-1$
    
    /**
     * Enable browser               
     */ 
    String MAKEDEF = "MAKEDEF";    //$NON-NLS-1$
    
    /**
     * Browse: include all visible methods 
     */ 
    String BSCLOSURE = "BSCLOSURE";    //$NON-NLS-1$
    
    /**
     * Browse: include all redefined methods 
     */ 
    String BSREDEFINE = "BSREDEFINE";    //$NON-NLS-1$
    
    /**
     * Browse: sort by name         
     */ 
    String BSALPHA = "BSALPHA";    //$NON-NLS-1$

    /**
     * Permission to change symbol file 
     */ 
    String CHANGESYM = "CHANGESYM";    //$NON-NLS-1$
    
    /**
     * Oberon-2 main module         
     */ 
    String MAIN = "MAIN";    //$NON-NLS-1$
    
    /**
     * Preserve exported comments   
     */ 
    String XCOMMENTS = "XCOMMENTS";    //$NON-NLS-1$

    /**
     * Enable integer division by zero check 
     */ 
    String CHECKDZ = "CHECKDZ";    //$NON-NLS-1$
    
    /**
     * Enable type cast in the PIM style
     */
    String PIMCAST = "PIMCAST";    //$NON-NLS-1$
    
}
