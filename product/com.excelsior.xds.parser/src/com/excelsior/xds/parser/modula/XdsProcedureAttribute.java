package com.excelsior.xds.parser.modula;

public enum XdsProcedureAttribute {
    
    Default       (""),
    AlwaysInline  ("AlwaysInline"),
    NeverInline   ("NeverInline"),
    NoSaveRestore ("NoSaveRestore");

    public  final String designator;
    private final String altenativeDesignator;
    
    XdsProcedureAttribute(String text){
        designator = text;
        altenativeDesignator = text.toLowerCase();
    }

    public static XdsProcedureAttribute parseText(String text) {
        for(XdsProcedureAttribute attribute : XdsProcedureAttribute.values()) {
            if (attribute != Default) {
                if (attribute.designator.equals(text) || attribute.altenativeDesignator.equals(text)) {
                    return attribute;
                }               
            }
        }
        return null;
    }
    
}
