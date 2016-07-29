package com.excelsior.xds.parser.modula.type;

public abstract class Type {
    
    private final String name;

    public Type(String name) {
        this.name = name;
    }
    
    public String getName() {
		return name;
	}

	@Override
    public String toString() {
        return "Type " + name;    //$NON-NLS-1$
    }
}
