package com.excelsior.xds.parser.indents;

/**
 * Descriptor of source code vertical indent guide. 
 */
public class IndentGuideDescriptor {
    
    public final int indentLevel;
    public final int startLine;
    public final int endLine;

    public IndentGuideDescriptor (int indentLevel, int startLine, int endLine) {
        this.indentLevel = indentLevel;
        this.startLine   = startLine;
        this.endLine     = endLine;
    }

    @Override
    public boolean equals (Object obj) {
        IndentGuideDescriptor other = (IndentGuideDescriptor) obj;
        return indentLevel == other.indentLevel 
            && startLine   == other.startLine
            && endLine     == other.endLine;
    }

    @Override
    public String toString() {
        return "IndentGuideDescriptor [indentLevel=" + indentLevel
             + ", startLine=" + startLine 
             + ", endLine=" + endLine + "]";
    }
    
}
