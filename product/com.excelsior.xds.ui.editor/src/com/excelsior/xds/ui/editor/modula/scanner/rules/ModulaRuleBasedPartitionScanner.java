package com.excelsior.xds.ui.editor.modula.scanner.rules;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;

import com.excelsior.xds.parser.modula.ast.ModulaAst;
import com.excelsior.xds.ui.editor.modula.IModulaPartitions;
import com.excelsior.xds.ui.editor.modula.ModulaPartitionTokens;

/**
 * This scanner checks for comment and string partitions. Everything else
 * is considered part of the default partition.
 */
public class ModulaRuleBasedPartitionScanner extends RuleBasedPartitionScanner
                                             implements ModulaPartitionTokens
{
	private InactiveCodeRule inactiveCodeRule;

	public ModulaRuleBasedPartitionScanner() {
		super();
		
        // Create the list of rules that produce tokens
		inactiveCodeRule = new InactiveCodeRule();
		IPredicateRule[] rules = new IPredicateRule[] {
			inactiveCodeRule,
		    new EndOfLineRule("--", END_OF_LINE_COMMENT_TOKEN), //$NON-NLS-1$
		    new ModulaCommentRule(BLOCK_COMMENT_TOKEN),
                             
            new SingleLineRule("\"", "\"", DOUBLE_QUOTE_STRING_TOKEN), //$NON-NLS-1$ //$NON-NLS-2$
            new SingleLineRule("'", "'",   SINGLE_QUOTE_STRING_TOKEN), //$NON-NLS-1$ //$NON-NLS-2$

		    new MultiLineRule("<*", "*>", PRAGMA_TOKEN, (char)0, true) //$NON-NLS-1$ //$NON-NLS-2$
		};

		setPredicateRules(rules);
        setDefaultReturnToken(DEFAULT_TOKEN);
	}
	
    /*
     * Region with multi-line comments should be always parsed from its beginning. 
     * @see RuleBasedPartitionScanner#setPartialRange(IDocument, int, int, String, int)
     */
    public void setPartialRange( IDocument document, int offset, int length
                               , String contentType, int partitionOffset ) 
    {
        if (IModulaPartitions.M2_CONTENT_TYPE_BLOCK_COMMENT.equals(contentType)) {
            if (offset > partitionOffset) {
                length += offset - partitionOffset;
                offset  = partitionOffset;
                partitionOffset = -1;
            }
        }
        super.setPartialRange(document, offset, length, contentType, partitionOffset);
    }

	public void setModulaAst(ModulaAst modulaAst) {
		inactiveCodeRule.setModulaAst(modulaAst);
	}

	public void setShowInactiveCode(boolean isShowInactiveCode) {
		inactiveCodeRule.setShowInactiveCode(isShowInactiveCode);
	}
}
