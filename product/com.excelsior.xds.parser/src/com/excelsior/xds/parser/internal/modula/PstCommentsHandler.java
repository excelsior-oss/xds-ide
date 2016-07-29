package com.excelsior.xds.parser.internal.modula;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.excelsior.xds.core.utils.IPredicate;
import com.excelsior.xds.parser.commons.ast.IElementType;
import com.excelsior.xds.parser.commons.ast.TokenTypes;
import com.excelsior.xds.parser.commons.pst.PstCompositeNode;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.AstBlock;
import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenSets;

/**
 * Handler to arrange leading and trailing whitespace and comments in the PST tree.  
 * @author lion
 */
public class PstCommentsHandler 
{
    public static void arrangeComments( CharSequence chars, PstCompositeNode root
                                      , IPredicate<PstNode> isTrailingWhiteSpaceAllowed ) 
    {
        int i = 0; 
        List<PstNode> children = root.getChildren();
        while (i < children.size()) {
            PstNode pstNode = children.get(i);
            if (pstNode instanceof AstBlock) {
                AstBlock compositeNode = (AstBlock) pstNode;
                arrangeTrailingComments(chars, compositeNode, i, isTrailingWhiteSpaceAllowed);
                i -= arrangeLeadingComments(chars, children, compositeNode, i);
                arrangeComments(chars, compositeNode, isTrailingWhiteSpaceAllowed);
            } 
            if (pstNode instanceof PstCompositeNode) {
                PstCompositeNode compositeNode = (PstCompositeNode) pstNode;
                arrangeComments(chars, compositeNode, isTrailingWhiteSpaceAllowed);
                arrangeTrailingComments(chars, compositeNode, i, isTrailingWhiteSpaceAllowed);
                i -= arrangeLeadingComments(chars, children, compositeNode, i);
            }
            i++;
        }
    }
    
    /**
     * All comments which precede the given node with 0 or 1 newline delimiters 
     * are removed from the parent node and inserted to the beginning given one. 
     * 
     * @param chars input char sequence
     * @param siblingNodes
     * @param node node to be processed
     * @param nodeIndex index of the node to be processed in <code>siblingNodes</code> 
     * 
     * @return number of nodes removed from the <code>siblingNodes</code> 
     */
    private static int arrangeLeadingComments( CharSequence chars
                                             , List<PstNode> siblingNodes
                                             , PstCompositeNode node, int nodeIndex ) 
    {
        int removedsibling = 0;
        int siblingIndex = -1;
        for (int i = nodeIndex - 1; i >= 0; --i) {
            PstNode siblingNode = siblingNodes.get(i);
            IElementType siblingElementType = siblingNode.getElementType();
            if (ModulaTokenSets.WHITE_SPACE == siblingElementType) {
                int newLineCount = newLineCount(siblingNode, chars);
                if (newLineCount > 1) {
                    break;
                }
                else if (newLineCount == 0) {
                    // comment is located in the same line as the other element 
                    siblingIndex = -1;
                    break;
                }
            }
            else if (ModulaTokenSets.COMMENT_SET.contains(siblingElementType)) {
                siblingIndex = i;
            }
            else {
                if (i + i == siblingIndex) {
                    // comment is located in the same line as the other element 
                    siblingIndex = -1;
                }
                break;
            }
        }
        
        if (siblingIndex > -1) {
            for (int i = nodeIndex - 1; i >= siblingIndex; i--) {
                PstNode siblingNode = siblingNodes.remove(i);
                node.insertChild(0, siblingNode);
            }
            removedsibling = nodeIndex - siblingIndex; 
        }
        return removedsibling;
    }


    /**
     * All trailing comments are removed from the node to the parent if they 
     * separated from last non-comment node by a newline delimiter(s). 
     * 
     * @param chars input char sequence
     * @param node node to be processed
     * @param nodeIndex index of the node to be processed in the parent's children 
     */
    private static void arrangeTrailingComments( CharSequence chars, PstCompositeNode node
                                               , int nodeIndex
                                               , IPredicate<PstNode> isTrailingWhiteSpaceAllowed ) 
    {
        if (isTrailingWhiteSpaceAllowed.evaluate(node)) {
            return;
        }
        
        List<PstNode> children = node.getChildren();
        int childIndex = -1;

        for (int i = children.size() - 1; i > -1; --i) {
            PstNode childNode = children.get(i);
            if (!ModulaTokenSets.WHITE_SPACE_AND_COMMENT_SET.contains(childNode.getElementType())) {
                break;
            }
            childIndex = i;
        }

        if (childIndex > -1) {
            PstNode childNode = children.get(childIndex);
            if ( (TokenTypes.WHITE_SPACE != childNode.getElementType()) 
              || (newLineCount(childNode, chars) == 0) )
            {
                while (childIndex < children.size()) {
                    childNode  = children.get(childIndex);
                    if ( (TokenTypes.WHITE_SPACE == childNode.getElementType()) 
                      && (newLineCount(childNode, chars) > 1) )
                    {
                        break;
                    }
                    childIndex++;
                }
            }

            if (childIndex < children.size()) {
                PstCompositeNode root = (PstCompositeNode)node.getParent();
                for (int i = children.size() - 1; i >= childIndex; --i) {
                    PstNode removedNode = node.removeLastChild();
                    root.insertChild(nodeIndex + 1, removedNode);
                }
            }
        }
    }
    

    private static int newLineCount(PstNode node, CharSequence chars) {
        int count = 0;
        
        if (node.getElementType() == TokenTypes.WHITE_SPACE) {
            Matcher matcher = NEWLINE.matcher(chars.subSequence(node.getOffset(), node.getOffset() + node.getLength()));
            while (matcher.find()) {
                ++count;
            }
        }
        
        return count;
    }
    
    private static Pattern NEWLINE = Pattern.compile("\\n+");     //$NON-NLS-1$
    
}
