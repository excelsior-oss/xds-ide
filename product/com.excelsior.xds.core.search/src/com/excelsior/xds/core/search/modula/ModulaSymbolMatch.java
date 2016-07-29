package com.excelsior.xds.core.search.modula;

import org.eclipse.core.resources.IFile;
import org.eclipse.search.ui.text.Match;

import com.excelsior.xds.core.text.TextPosition;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;

/**
 * A search match with additional Modula-2 specific info.
 */
public class ModulaSymbolMatch extends Match 
{
    private final IModulaSymbol symbol;
    private final int line;
    private final int column;
    private String contextLine;

    public ModulaSymbolMatch(IFile iFile, IModulaSymbol symbol, TextPosition position ) 
    {
        super(iFile, Match.UNIT_CHARACTER, position.getOffset(), symbol.getName().length());
        this.symbol = symbol;
        this.line   = position.getLine();
        this.column = position.getColumn();
    }
    
    public void setContextLine(String contextLine) {
        this.contextLine = contextLine;
    }

    
    public IFile getFile() {
        return (IFile)getElement();
    }
    
    public IModulaSymbol getSymbol() {
        return symbol;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public String getContextLine() {
        return contextLine;
    }
    
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + column;
		result = prime * result + ((getFile() == null) ? 0 : getFile().hashCode());
		result = prime * result + line;
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
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
		ModulaSymbolMatch other = (ModulaSymbolMatch) obj;
		if (column != other.column)
			return false;
		if (getFile() == null) {
			if (other.getFile() != null)
				return false;
		} else if (!getFile().equals(other.getFile()))
			return false;
		if (line != other.line)
			return false;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String
				.format("ModulaSymbolMatch [iFile=%s, symbol=%s, line=%s, column=%s, contextLine=%s]",
						getFile(), symbol, line, column, contextLine);
	}
}
