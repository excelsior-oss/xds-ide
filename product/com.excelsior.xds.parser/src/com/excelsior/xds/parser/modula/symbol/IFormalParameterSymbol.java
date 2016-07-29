package com.excelsior.xds.parser.modula.symbol;


public interface IFormalParameterSymbol extends ISymbolWithType 
{
    public boolean isVarParameter();
    
    public boolean isNilAllowed();

    public boolean isSeqParameter();
    
    public boolean isReadOnly();

    public boolean isDefaultValueEnabled();
    
}
