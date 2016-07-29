package com.excelsior.xds.ui.editor.modula;

import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * Tokens returned by Modula-2 partition scanner.
 * 
 * @author lion
 */
public interface ModulaPartitionTokens {

    static final IToken BLOCK_COMMENT_TOKEN       = new Token(IModulaPartitions.M2_CONTENT_TYPE_BLOCK_COMMENT);

    static final IToken END_OF_LINE_COMMENT_TOKEN = new Token(IModulaPartitions.M2_CONTENT_TYPE_END_OF_LINE_COMMENT);
    
    static final IToken SINGLE_QUOTE_STRING_TOKEN = new Token(IModulaPartitions.M2_CONTENT_TYPE_SINGLE_QUOTE_STRING);
    
    static final IToken DOUBLE_QUOTE_STRING_TOKEN = new Token(IModulaPartitions.M2_CONTENT_TYPE_DOUBLE_QUOTE_STRING);
    
    static final IToken PRAGMA_TOKEN              = new Token(IModulaPartitions.M2_CONTENT_TYPE_PRAGMA);
    
    static final IToken DISABLED_CODE		      = new Token(IModulaPartitions.M2_CONTENT_TYPE_DISABLED_CODE);
    
    static final IToken DEFAULT_TOKEN             = new Token(IModulaPartitions.M2_CONTENT_TYPE_DEFAULT);

}
