package com.excelsior.xds.core.compiler.driver;

import java.nio.charset.Charset;

/**
 * The xShell interface to handle XDS compiler output.  
 */
public interface IXShellListener {

    public static enum MessageType {
        COMPILE_ERROR,             // general error
        COMPILE_FATAL_ERROR,       // fatal error, further compilation is impossible 

        COMPILE_WARNING,           // warning, it does not affect results of compiling

        COMPILE_NOTICE,            // to display line/column and jump, but do not count as error
        COMPILE_TEXT               // do not display line/column and do not jump to it
    };
    
    // Compiler messages
    public void onMessage( MessageType messageType, int messageCode, String message
                         , String fileName, int line, int pos );

    
    // Compiler unformatted string to console window
    public void onConsoleString(String str);


    // Compiler job progress 
    public void onJobCaption(String caption);
    public void onJobStart(int progressLimit, String comment);
    public void onJobComment(String comment); // used for env.errors.SendIdeInfo(name, value) too (23.05.14, m2sparc compiler) 
    public void onJobProgress(int commentProgress, int progress);

    
    // Module list 
    public void onModuleListStart();
    public void onModuleListAppend(String fileName);
    public void onModuleListCommit();

    /**
     * Invokes in case of parsing error of compiler output. 
     * @param message - diagnostic message 
     */
    public void onParsingError(String message);
    
    public void onCompilerExit(int exitCode);
    
    /**
     * Listener must know what stream encoding used now.
     * This encoding may be switched 'on the fly' 
     * 
     * @return Charser (cp1251 or cp866 now)
     */
    public Charset getStreamCharset();

}
