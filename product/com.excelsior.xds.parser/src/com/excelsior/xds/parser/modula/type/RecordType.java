package com.excelsior.xds.parser.modula.type;

import com.excelsior.xds.parser.modula.symbol.type.IRecordTypeSymbol;

public class RecordType extends Type {

    private IRecordTypeSymbol typeSymbol;
    
    public RecordType(String debugName) {
        this(debugName, null);
    }

    public RecordType(String debugName, IRecordTypeSymbol typeSymbol) {
        super(debugName);
        this.setSymbol(typeSymbol);
    }

    public IRecordTypeSymbol getSymbol() {
        return typeSymbol;
    }

    public void setSymbol(IRecordTypeSymbol s) {
        this.typeSymbol = s;
    }
    
}
