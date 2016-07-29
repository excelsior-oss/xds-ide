package com.excelsior.xds.parser.modula.ast;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.filesystem.IFileStore;

import com.excelsior.xds.core.builders.DefaultBuildSettingsHolder;
import com.excelsior.xds.core.text.ITextRegion;
import com.excelsior.xds.parser.commons.ast.Ast;
import com.excelsior.xds.parser.commons.pst.PstCompositeNode;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.commons.symbol.ParsedModuleKey;
import com.excelsior.xds.parser.modula.ast.modules.AstModule;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;

/**
 * A root element of the Modula-2 AST
 */
public class ModulaAst extends Ast
{
    /** The module symbol corresponding to this AST */
    private IModuleSymbol moduleSymbol;
    
    /** The list of text locations ignored by the conditional compilation pragmas */
    private List<ITextRegion> inactiveTextRegions = Collections.emptyList(); 
    
    public ModulaAst(CharSequence chars, IFileStore sourceFile) {
        super(chars, sourceFile);
    }

    public AstModule getAstModule() {
        return (AstModule)getAstNode();
    }
    
    public IModuleSymbol getModuleSymbol() {
        if (moduleSymbol != null) {
            return moduleSymbol;
        }
        return getAstModule().getSymbol();
    }
    
    public ParsedModuleKey getParsedModuleKey() {
    	IModuleSymbol moduleSymbol = getModuleSymbol();
    	if (moduleSymbol != null) {
    		return moduleSymbol.getKey();
    	}
    	else{
    		if (getSourceFile() != null) {
    			return new ParsedModuleKey(getSourceFile());
    		}
    	}
    	return null;
    }
    
    
    public void setModuleSymbol(IModuleSymbol moduleSymbol) {
        this.moduleSymbol = moduleSymbol;
    }
    
    
    @SuppressWarnings("unchecked")
    public static <T extends PstCompositeNode, U extends PstCompositeNode> U 
           findParent(T node, ModulaCompositeType<U> parentType) 
    {
        PstNode parent = node.getParent();
        while(parent != null) {
            if (parent.getElementType() == parentType) {
                return (U)parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    
    /**
     * Sets the list of text locations ignored by the conditional compilation pragmas.
     * Sorts locations according to theirs offset. Locations should be non-overlapping.
     * 
     * @param inactiveTextRegions list of text locations ignored by the conditional compilation pragmas.
     */
    public void setInactiveCodeRegions(List<ITextRegion> inactiveTextRegions) {
        this.inactiveTextRegions = inactiveTextRegions;
        Collections.sort(this.inactiveTextRegions, new Comparator<ITextRegion>() {
			@Override
			public int compare(ITextRegion o1, ITextRegion o2) {
				return o1.getOffset() - o2.getOffset();
			}
		});
    }

    /**
     * Returns the list of text locations ignored by the conditional compilation pragmas.
     *
     * @return list of text locations ignored by the conditional compilation pragmas.
     */
    public List<ITextRegion> getInactiveCodeRegions() {
        return inactiveTextRegions; 
    }
}
