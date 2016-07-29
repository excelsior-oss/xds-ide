package com.excelsior.xds.ui.editor.modula.text;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;

import com.excelsior.xds.ui.editor.commons.text.PairedBracketsMatcher;
import com.excelsior.xds.ui.editor.modula.IModulaPartitions;
import com.excelsior.xds.ui.editor.modula.ModulaTokens;


/**
 * Helper class for Modula-2 specific bracket matching.
 * 
 * @author fsa
 */
public final class ModulaPairedBracketsMatcher extends PairedBracketsMatcher {
    
    public ModulaPairedBracketsMatcher() {
        super(IModulaPartitions.M2_PARTITIONING, IModulaPartitions.M2_CONTENT_TYPE_DEFAULT);
    }
    
    
    /*
     * Performs the actual work of matching for #match(IDocument, int).
     */
    @Override
    protected IRegion performMatch(IDocument document, int offs) throws BadLocationException {
        final int doclen = document.getLength();
        fMatchFlags = 0; 
        if (document == null || doclen < 1 || offs < 0 || offs > doclen) {
            return null;
        }
        char begCh = 0;
        int idx = -1;
        if (offs > 0) {
        // Try char on the left of caret:
            begCh = document.getChar(offs - 1);
            idx = BRACE_PAIRS.indexOf(begCh);
        }
        if (idx < 0) {
            // No bracket on the left. Try the caret position (move offs+=1 and try it):
            ++offs;
            if (offs <= doclen) {
                begCh = document.getChar(offs - 1);
                idx = BRACE_PAIRS.indexOf(begCh);
            }
        }
        if (idx<0 && offs >= 3) {
            // May be it is (*_ ? Try char at left.left from the caret:
            offs -= 2;
            if(document.getChar(offs) == '*') {
                begCh = document.getChar(offs-1);
                if (begCh == '(' || begCh=='<') {
                    idx = BRACE_PAIRS.indexOf(begCh);
                }
            }
        }

        if (idx < 0) {
            return null;
        }
        int pos = offs-1;
            
        // Bracket 'ch' found at [pos]
        int pos0 = pos;
        int step = ((idx & 0x1) == 0) ? 1 : -1; // open/close bracket
        
        ITypedRegion partition = getPartition(document, pos);

        final String partType = partition.getType(); 
        boolean digraph = false; // is it 2-char bracket? (like "(*" )
        boolean diPart  = true;  // is it bracket inside (* *) or <* *> partition? 
        if (partType.equals(IModulaPartitions.M2_CONTENT_TYPE_BLOCK_COMMENT)) {
            if (begCh == '(' && getch(document, pos+1, 0, doclen-1) == '*') {
                fTokenWithColors = ModulaTokens.BlockComment.getToken();
                fMatchFlags = 0;
                digraph = true;
                ++pos;
            } else if (begCh == ')' && getch(document, pos-1, 0, doclen-1) == '*'  
                                    && getch(document, pos-2, 0, doclen-1) != '(') {
                fTokenWithColors = ModulaTokens.BlockComment.getToken();
                digraph = true;
                --pos;
            }
        } else if (partType.equals(IModulaPartitions.M2_CONTENT_TYPE_PRAGMA)) {
            if (begCh == '<' && getch(document, pos+1, 0, doclen-1) == '*') {
                fTokenWithColors = ModulaTokens.Pragma.getToken();
                digraph = true;
                ++pos;
            } else if (begCh == '>' && getch(document, pos-1, 0, doclen-1) == '*'  
                    && getch(document, pos-2, 0, doclen-1) != '<') {
                fTokenWithColors = ModulaTokens.Pragma.getToken();
                digraph = true;
                --pos;
            }
        } else {
            diPart = false;
        }
        
        if (!digraph) {
            fTokenWithColors = ModulaTokens.Bracket.getToken();
        }
        
        // Search direction depends on step. Search area is current partition if the partition type
        // is not 'M2_CONTENT_TYPE_DEFAULT' or all document in other case
        int minPos = 0;
        int maxPos = doclen-1;
        if (partType != IModulaPartitions.M2_CONTENT_TYPE_DEFAULT) {
            minPos = Math.max(0,  partition.getOffset());
            maxPos = Math.min(maxPos, partition.getOffset() + partition.getLength() - 1);
        }

        char pairCh = BRACE_PAIRS.charAt(idx ^ 0x1);
        char diBeg = 0;
        char diEnd = 0;
        if ("()<>".indexOf(begCh) >= 0) { //$NON-NLS-1$
            diBeg = BRACE_PAIRS.charAt(idx & ~0x1);
            diEnd = BRACE_PAIRS.charAt(idx | 0x1);
        }

        fMatchFlags |= digraph ? MATCH_FLAG_DIGRAPH : 0; 
        fAnchor = step > 0 ? LEFT : RIGHT;
        
        int  deep = 1;
        while(true) {
            pos += step;
            if (pos < minPos || pos>maxPos) {
                // unmatched
                fMatchFlags |= MATCH_FLAG_NO_MATCH; 
                return new Region(pos0, 1); // painter will paint 2-chars here for digraph 
            }
            if (!getPartition(document, pos).getType().equals(partType)) {
                // skip other partitions (required in default partition)
                continue;
            }
            char c = document.getChar(pos);
            if (c != begCh && c != pairCh) {
                continue;
            }
            char cR1 = getch(document, pos+1, minPos, maxPos);
            char cL1 = getch(document, pos-1, minPos, maxPos);
            char cL2 = getch(document, pos-2, minPos, maxPos);
            if (!digraph) {
                // simple () [] ...
                if (diPart) {
                    if ((  c == diBeg && cR1 == '*') ||
                        (cL2 != diBeg && cL1 == '*' && c == diEnd ))
                    { // Don't search "(" and ")" in "(*" and "*)" in comments (and the same in pragmas) 
                        continue;
                    }
                }
                if (c == begCh) {
                    ++deep;
                } else if (c == pairCh) {
                    if (--deep == 0) {
                        break;
                    }
                }
            } else {
                // (* *) <* *> :
                if (c == begCh) {
                    if (step > 0 && cR1 == '*') {
                        ++deep;
                        ++pos;
                    } else if (step < 0 && cL1 == '*') { 
                        if (cL2 == pairCh) { 
                            // "(*)" scanned from Right to Left: it is NOT "*)", it is "(*" at pos-2:
                            pos -= 2;
                            if (--deep == 0) {
                                break;
                            }
                        } else {
                            ++deep;
                            --pos;
                        }
                    }
                } else if (c == pairCh) {
                    if (step<0 && cR1 == '*') {
                        if (--deep == 0) {
                            break;
                        }
                    } else if (step>0 && cL1 == '*') {
                        if (cL2 != begCh) {
                            if (--deep == 0) {
                                break;
                            }
                        }
                    }
                }
            }
        } // while
        
        // While was breaked => found:
        if (step > 0) {
            fAnchor = LEFT;
            return new Region(pos0, pos-pos0+1);
        } else {
            fAnchor = RIGHT;
            return new Region(pos, pos0-pos+1);
        }
    }


    private char getch(IDocument doc, int pos, int minPos, int maxPos)  throws BadLocationException {
        if (pos >= minPos && pos <= maxPos) {
            return doc.getChar(pos);
        } 
        return ' ';
    }

}

