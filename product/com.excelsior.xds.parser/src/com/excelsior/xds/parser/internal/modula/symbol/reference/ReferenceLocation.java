package com.excelsior.xds.parser.internal.modula.symbol.reference;

import org.apache.commons.lang.StringUtils;


public class ReferenceLocation {
    private String pathInModule;

    ReferenceLocation(String pathInModule) {
        this.pathInModule = pathInModule;
    }
    
    public boolean isEmpty() {
        return StringUtils.isEmpty(pathInModule);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((pathInModule == null) ? 0 : pathInModule.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ReferenceLocation other = (ReferenceLocation) obj;
        if (pathInModule == null) {
            if (other.pathInModule != null)
                return false;
        } else if (!pathInModule.equals(other.pathInModule))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ReferenceLocation [pathInModule=" + pathInModule + "]";
    }
}
