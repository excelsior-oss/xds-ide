package com.excelsior.xds.parser.modula;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.filesystem.IFileStore;

import com.excelsior.xds.core.utils.IPredicate;
import com.excelsior.xds.parser.commons.IParserEventListener;
import com.excelsior.xds.parser.commons.NullParseEventReporter;
import com.excelsior.xds.parser.commons.ast.AstBuilder;
import com.excelsior.xds.parser.commons.ast.IAstBuilder;
import com.excelsior.xds.parser.commons.ast.IElementType;
import com.excelsior.xds.parser.commons.ast.NullAstBuilder;
import com.excelsior.xds.parser.commons.ast.TokenTypes;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;

class XdsParserState
{
    protected static boolean CHECK_REFERENCE_INTEGRITY = true;

    /* --------------------------------------------------------------------------
     * Immutable states
     * --------------------------------------------------------------------------
     */
    
    /** The source file to be parsed. */
    protected final IFileStore sourceFile;

    /** Buffer containing the text to be parsed */
    protected final CharSequence chars;

    
    /** Parser configuration settings */
    protected final XdsSettings settings;

    
    /** Handler of parsing errors, warnings etc. */
    protected final IParserEventListener reporter;
    
    
    /** Program Structure Tree builder */
    protected final IAstBuilder builder;

    XdsParserState( IFileStore sourceFile, CharSequence chars
    			  , XdsSettings settings
                  , IParserEventListener reporter )
    {
        this.sourceFile = sourceFile;
        this.chars      = chars;

        if (reporter == null) {
            reporter = NullParseEventReporter.getInstance();
        }
        this.reporter = reporter;

        this.settings = settings;
        
        this.builder  = settings.isBuildAst()? new AstBuilder(
            sourceFile, reporter, WHITE_SPACE_PREDICATE, IS_TRAILING_WHITE_SPACE_ALLOWED
        ) : NullAstBuilder.getInstance();
    }

    
    /**
     * Resets the mutable states of this parser.
     * Resetting a parser discards all of its explicit state information.
     */
    protected void reset() {
        builder.reset();
    }
    
    
    protected static IPredicate<PstNode> IS_TRAILING_WHITE_SPACE_ALLOWED = new IPredicate<PstNode>() 
    {
        /** {@inheritDoc} */
        @Override
        public boolean evaluate(PstNode node) {
            return (node == null) 
                || (ModulaElementTypes.INACTIVE_CODE == node.getElementType());
        }
    };  

    protected static IPredicate<PstNode> WHITE_SPACE_PREDICATE = new IPredicate<PstNode>() 
    {
        /** {@inheritDoc} */
        @Override
        public boolean evaluate(PstNode node) {
            return (TokenTypes.WHITE_SPACE == node.getElementType())
                || (  PRAGMA_CONDITIONAL_STATEMENT_SET.contains(node.getElementType())
                   && (node.getParent() != null)
                   && (ModulaElementTypes.INACTIVE_CODE != node.getParent().getElementType())
                   );    
        }
    };  
    
    private static Set<IElementType> PRAGMA_CONDITIONAL_STATEMENT_SET = new HashSet<IElementType>(Arrays.asList(
            ModulaElementTypes.PRAGMA_IF_STATEMENT,
            ModulaElementTypes.PRAGMA_ELSIF_STATEMENT,
            ModulaElementTypes.PRAGMA_ELSE_STATEMENT,
            ModulaElementTypes.PRAGMA_END_STATEMENT
    ));

}
