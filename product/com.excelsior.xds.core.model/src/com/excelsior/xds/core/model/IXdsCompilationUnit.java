package com.excelsior.xds.core.model;

import org.eclipse.core.filesystem.IFileStore;

import com.excelsior.xds.parser.modula.ast.ModulaAst;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;

public interface IXdsCompilationUnit extends IXdsElement
                                           , IXdsElementWithSymbol 
                                           , IXdsContainer
{
    CompilationUnitType getCompilationUnitType();
    
    IXdsModule getModuleElement();
    
    @Override
    IModuleSymbol getSymbol();
    
    boolean isInCompilationSet();

    ModulaAst getModulaAst();
    
    public IFileStore getAbsoluteFile();
}
