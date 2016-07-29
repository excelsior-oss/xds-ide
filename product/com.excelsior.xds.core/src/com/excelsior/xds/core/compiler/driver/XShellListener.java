package com.excelsior.xds.core.compiler.driver;

import java.nio.charset.Charset;

import com.excelsior.xds.core.text.TextEncoding;

/**
 * Default implementation of xShell interface.  
 */
public class XShellListener implements IXShellListener {
    
    private Charset charset = TextEncoding.whatCharsetToUse("Cp1251"); //$NON-NLS-1$

    @Override
    public void onMessage( MessageType messageType, int messageCode, String message
                         , String fileName, int line, int pos ) {
    }

    @Override
    public void onConsoleString(String str) {
    }

    @Override
    public void onJobCaption(String caption) {
    }

    @Override
    public void onJobStart(int progressLimit, String comment) {
    }

    @Override
    public void onJobComment(String comment) {
    }

    @Override
    public void onJobProgress(int commentProgress, int progress) {
    }

    @Override
    public void onModuleListStart() {
    }

    @Override
    public void onModuleListAppend(String fileName) {
    }

    @Override
    public void onModuleListCommit() {
    }

    @Override
    public void onParsingError(String message) {
    }

    @Override
    public void onCompilerExit(int exitCode) {
    }

    @Override
    public Charset getStreamCharset() {
        return charset;
    }

    protected void setStreamCharset(Charset cs) {
        charset = cs;
    }
    
}
