package com.excelsior.xds.parser.indents;

import java.util.ArrayList;
import java.util.List;

import com.excelsior.xds.core.utils.collections.IntStack;

public class IndentsParser {

    public static List<IndentGuideDescriptor> buildDescriptors(CharSequence chars, int tabSize, String eolPrefix) throws Exception {
        if (tabSize < 1) tabSize = 1;
       
        LineIterator lit = new LineIterator(chars);
        int lincnt = lit.lengths.size();

        /// Calculate indents:
        int lineIndents[] = new int[lincnt];
        {

            for (int i=0; i<lincnt; ++i) {
                lineIndents[i] = calcLineSpace(chars, lit.offsets.get(i), lit.lengths.get(i), tabSize, eolPrefix); // -1 for blank lines
            }
            int topIndent = 0;
            for (int line = 0; line < lincnt; line++) {
                if (lineIndents[line] >= 0) {
                    topIndent = lineIndents[line];
                } else {
                    int startLine = line;
                    while (line < lincnt && lineIndents[line] < 0) {
                        line++;
                    }
    
                    int bottomIndent = line < lineIndents.length ? lineIndents[line] : topIndent;
    
                    int indent = bottomIndent > topIndent ? 1000 : Math.min(topIndent, bottomIndent);
                    for (int blankLine = startLine; blankLine < line; blankLine++) {
                        lineIndents[blankLine] = indent;
                    }
    
                    //noinspection AssignmentToForLoopParameter
                    line--; // will be incremented back at the end of the loop;
                }
            }
        }
        

        List<IndentGuideDescriptor> descriptors = new ArrayList<IndentGuideDescriptor>();

        IntStack lines = new IntStack();
        IntStack indents = new IntStack();

        lines.push(0);
        indents.push(0);
        for (int line = 1; line < lincnt; line++) {
            int curIndent = lineIndents[line];

            while (!indents.empty() && curIndent <= indents.peek()) {
                final int level = indents.pop();
                int startLine = lines.pop();
                descriptors.add(createDescriptor(level, startLine, line, chars, lit));
            }

            int prevLine = line - 1;
            int prevIndent = lineIndents[prevLine];

            if (curIndent - prevIndent > 1) {
                lines.push(prevLine);
                indents.push(prevIndent);
            }
        }

        while (!indents.empty()) {
            final int level = indents.pop();
            if (level > 0) {
                int startLine = lines.pop();
                descriptors.add(createDescriptor(level, startLine, lincnt, chars, lit));
            }
        }
        return descriptors;
    }
    
    
    /**
     * Returns 1st non-space char indent (with opening tabs) or -1 when line contains nothing except spaces
     * Lines with EOL comments only treats as empty lines (returns -1)
     */
    private static int calcLineSpace(CharSequence chars, int lineOffset, int lineLen, int tabSize, String eolPrefix) throws Exception {
        int indent = 0;
        for (int i=0; i<lineLen; ++i) {
            char ch = chars.charAt(lineOffset + i);
            if (ch == ' ') {
                ++indent;
            } else if (ch == '\t') {
                indent = ( indent / tabSize) * tabSize + tabSize;
            } else {
                if (eolPrefix != null && eolPrefix.indexOf(ch) == 0 && lineLen-i >= eolPrefix.length()) {
                    if (eolPrefix.equals(chars.subSequence(lineOffset + i, lineOffset + i + eolPrefix.length()).toString())) {
                        return -1; 
                    }
                }
                return indent;
            }
        }
        return -1;
    }


    private static boolean isBlankLine(CharSequence chars, int line, LineIterator lit) throws Exception {
        int offs = lit.offsets.get(line);
        for (int i = lit.lengths.get(line)-1; i>=0; --i) {
            char ch = chars.charAt(offs + i);
            if (ch != ' ' && ch != '\t') {
                return false;
            }
        }
        return true;
    }

    private static IndentGuideDescriptor createDescriptor(int level, int startLine, int endLine, CharSequence chars, LineIterator lit) throws Exception {
        while (startLine > 0 && isBlankLine(chars, startLine, lit)) 
            startLine--;
        return new IndentGuideDescriptor(level, startLine, endLine);
    }


    private static class LineIterator {

        private static final char CR = 0x0D;
        private static final char LF = 0x0A;
        
        public ArrayList<Integer> offsets;
        public ArrayList<Integer> lengths;
        
        public LineIterator(CharSequence chars) {
            offsets = new ArrayList<Integer>();
            lengths = new ArrayList<Integer>();
            int top = chars.length();

            int nextOffset = 0;

            while (true) {
                int offset = nextOffset;

                if (offset >= top) {
                    break; // EOF
                }

                int length = 0;
                
                while(offset + length < top) {
                    char ch = chars.charAt(offset + length);
                    nextOffset = offset + length + 1;
                    if (ch == CR) {
                        if (nextOffset < top && chars.charAt(nextOffset) == LF) {
                            ++nextOffset;
                        }
                        break;
                        
                    } else if (ch == LF) {
                        break;
                    }
    
                    ++length;
                }
                
                offsets.add(offset);
                lengths.add(length);
            }
        }
    }

}

