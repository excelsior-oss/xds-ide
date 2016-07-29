package com.excelsior.xds.parser.modula;

import org.eclipse.core.filesystem.IFileStore;

import com.excelsior.xds.parser.modula.ast.ModulaAst;

/**
 * Interface of an object that monitors a Modula-2 parsing..
 */
public interface IXdsParserMonitor
{
    /**
     * Notifies this listener that parsing of some module are finished.  
     * 
     * @param sourceFile the Modula-2 source file which has been parsed.
     * @param buildSettings the XDS compiler settings which have been used to parse source file.
     * @param modulaAst Modula-2 AST of the source file. 
     */
    public void endModuleParsing( IFileStore sourceFile
                                , ModulaAst modulaAst ); 

}
