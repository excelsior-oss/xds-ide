package com.excelsior.xds.ui.editor.modula;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;

import com.excelsior.xds.parser.commons.ast.TokenType;
import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenTypes;
import com.excelsior.xds.parser.modula.scanner.jflex._XdsFlexScanner;
import com.excelsior.xds.ui.editor.modula.format.ModulaTextFormatter;

public class ModulaAutoEditStrategy extends DefaultIndentLineAutoEditStrategy {
    private static enum ProcessingResult{NONE, INDENT_ON_ENTER, INDENT_OF_INSERTION, PAIR_OPERATOR_INSERTED, PAIR_OPERATOR_REINDENTED};
    private static final HashMap<String, HashSet<TokenType>> hmEndPairs;
    private static final HashSet<TokenType> hsEnd;
    private static final HashMap<TokenType, Integer> hsAddIndentOperators;

    private  ModulaTextFormatter m2f;
    
    @Override
    public void customizeDocumentCommand(IDocument doc, DocumentCommand cmd) {
        ProcessingResult r1 = ProcessingResult.NONE;
        ProcessingResult r2 = ProcessingResult.NONE;
        try {
            boolean isEnterPressed = (TextUtilities.equals(doc.getLegalLineDelimiters(), cmd.text) != -1); 
            r1 = insertIndentIfNeed(doc, cmd, isEnterPressed);
            r2 = checkTypingAfterLine(doc, cmd, isEnterPressed, r1);
        } catch (Exception e) {}
        
        m2f = null;
        if (r1 == ProcessingResult.NONE && r2 == ProcessingResult.NONE) {
            super.customizeDocumentCommand(doc, cmd);
        }
    }
    
    private ModulaTextFormatter getM2Formatter() {
        if (m2f == null) {
            m2f = new ModulaTextFormatter();
        }
        return m2f;
    }
    
    private int calcIndentLen(String indent) {
        int tabSize = getM2Formatter().getTabSize(); 
        int res = 0;
        for (int i=0; i<indent.length(); ++i) {
            if (indent.charAt(i) == '\t') {
                res = ((res + tabSize) / tabSize) * tabSize;
            } else {
                ++res;
            }
        }
        return res;
    }


    private String indentFromStr(String s) {
        for (int i = 0; i<s.length(); ++i) {
            char ch = s.charAt(i);
            if (ch != ' ' && ch != '\t') {
                s = s.substring(0, i);
                break;
            }
        }
        return s;
    }


    //-------------------------------------------------------------------------------------------------------------------------------------
    // Indent insertion if need
    //-------------------------------------------------------------------------------------------------------------------------------------

    private ProcessingResult insertIndentIfNeed(IDocument doc, DocumentCommand cmd, boolean isEnterPressed) throws Exception {
        if (isEnterPressed) {
            // Here we can append indent to cmd.text if need ("\r\n" --> "\r\n    ")
            int incIndent = calcIncIndentOnEnter(doc, cmd.offset, true);
            if (incIndent > 0) {
                IRegion region       = doc.getLineInformation(doc.getLineOfOffset(cmd.offset));
                String  oldIndent    = indentFromStr(doc.get(region.getOffset(), region.getLength()));
                int     oldIndentLen = calcIndentLen(oldIndent);
                String  indent       = getM2Formatter().constructIndent(oldIndentLen + incIndent, oldIndent);
                cmd.text = cmd.text + indent;
                return ProcessingResult.INDENT_ON_ENTER;
            }
        } else if (cmd.text.length() > 1) {
            // Paste command. Re-indent all pasted lines to insertion point
            int insLen = cmd.text.length();
            int insLinesCnt = 1;
            boolean insHasNoText = true;
            for (int i=0; i<insLen; ++i) {
                char ch = cmd.text.charAt(i);
                if (ch == '\n') {
                    ++insLinesCnt;
                } else if (ch != '\r' && ch!=' ' && ch!='\t') {
                    insHasNoText = false;
                }
            }
            if (!insHasNoText) { // Yes. Insertion contains some text to indent it.
                int     docStartLineNum    = doc.getLineOfOffset(cmd.offset);
                IRegion region             = doc.getLineInformation(docStartLineNum);
                String  docStartLineText   = doc.get(region.getOffset(), region.getLength());
                int     offsInDocStartLine = cmd.offset - region.getOffset();

                boolean isDefinitionBlock = isDefinitionBlock(cmd.text); 
                
                boolean skipStartLineIndent = false; // Has docStartLineText some text before insertion point?
                for (int i=0; i < offsInDocStartLine; ++i) {
                    char ch = docStartLineText.charAt(i);
                    if (ch != ' ' && ch != '\t') {
                        skipStartLineIndent = true;
                        break; 
                    }
                }
                
                if (!skipStartLineIndent || insLinesCnt > 1) {
                    if (!isDefinitionBlock) {
                        
                        int dec1stIndent = 0;
                        if (!skipStartLineIndent && offsInDocStartLine>0) {
                            // insertion point is after some whitespace
                            // dec1stIndent := width of this space, need to recalc indent of 1st inserted line
                            String extraIndent = docStartLineText.substring(0, offsInDocStartLine);
                            dec1stIndent = calcIndentLen(indentFromStr(extraIndent));
                        }
    
                        int lev;
                        { // look into line before and calculate preferref indent level under it:
                            int lineToIndentUnderNum = skipStartLineIndent ? docStartLineNum : Math.max(docStartLineNum-1, 0);
                            IRegion reg;
                            String lineText = "";
                            ;
                            for (int lineToIndentUnderMin = Math.max(0, lineToIndentUnderNum - 10); 
                                 lineToIndentUnderNum >= lineToIndentUnderMin; 
                                 --lineToIndentUnderNum) 
                            {
                                // skip some empty lines (if any) to find line to indent under
                                reg = doc.getLineInformation(lineToIndentUnderNum);
                                lineText = doc.get(reg.getOffset(), reg.getLength());
                                if (!lineText.trim().isEmpty()) {
                                    break;
                                } else {
                                    lineText = "";
                                }
                            }
                            
                            String  oldIndent = indentFromStr(lineText);
                            int oldIndentLen = calcIndentLen(oldIndent);
                            int incIndent = calcIncIndentOnEnter(doc, doc.getLineOffset(lineToIndentUnderNum), false);
                            lev = oldIndentLen + Math.max(incIndent, 0);
                        }
    
                        int doc1stLineNumToIndent = docStartLineNum + (skipStartLineIndent ? 1 : 0);
                        doc1stLineNumToIndent = Math.min(doc1stLineNumToIndent, doc.getNumberOfLines() - 1);
                        region = doc.getLineInformation(doc1stLineNumToIndent);
                        String regionIndent = indentFromStr(doc.get(region.getOffset(), region.getLength()));
                        
                        // commonIndent - indent to use. Mix of tabs and spaces
                        String commonIndent = getM2Formatter().constructIndent(lev, regionIndent);
                        String commonIndent1st = getM2Formatter().constructIndent(lev - dec1stIndent, regionIndent);
                        
                        StringBuilder sbNewText = new StringBuilder();
                        int insertionIndentLen = -1; // Internal indent in the insertion (calc from its 1st line)
                        boolean is1stLine = true;
                        for (String insLine : linesFrom(cmd.text)) {
                            if (skipStartLineIndent && is1stLine) {
                                sbNewText.append(insLine);                                     // add insertion line as is
                            } else {
                                String ilIndent = indentFromStr(insLine);
                                int ilIndentLen = calcIndentLen(ilIndent);
                                
                                if (insertionIndentLen < 0) {
                                    insertionIndentLen = ilIndentLen; // 1st line to indent - assume that it is insertion internal
                                                                      // indent to be cut off from all inserted lines
                                }
                                
                                sbNewText.append(is1stLine ? commonIndent1st : commonIndent);  // add common indent
                                for (int i = 0; i < ilIndentLen - insertionIndentLen; ++i) {
                                    sbNewText.append(' ');                                     // add intermediate indent
                                }
                                sbNewText.append(insLine.substring(ilIndent.length()));        // add insertion line w/o indent
                            }
                            is1stLine = false;
                        }
                        cmd.text = sbNewText.toString(); // replace insertion text with re-indented one
                        
                    } else { // if (!isDefinitionBlock)
                        
                        StringBuilder sbNewText = new StringBuilder();
                        boolean is1stLine = true;
                        int indentLev = -1;
                        String indent = "";
                        for (String insLine : linesFrom(cmd.text)) {
                            if (skipStartLineIndent && is1stLine) {
                                sbNewText.append(insLine);                                // add insertion line as is
                            } else {
                                String ilIndent = indentFromStr(insLine);
                                int ilIndentLen = calcIndentLen(ilIndent);
                                
                                if (indentLev != ilIndentLen) {
                                    indentLev = ilIndentLen;
                                    indent = getM2Formatter().constructIndent(ilIndentLen, "");
                                }
                                
                                sbNewText.append(indent);                                 // add indent converted into our tab/space style
                                sbNewText.append(insLine.substring(ilIndent.length()));   // add insertion line w/o indent
                            }
                            is1stLine = false;
                        }
                        cmd.text = sbNewText.toString(); // replace insertion text with re-indented one
                        
                    }
                    return ProcessingResult.INDENT_OF_INSERTION;
                }
            }
            
            
        }
        return ProcessingResult.NONE;
    }
    
    
    /**
     * 
     * @param text
     * @return TRUE when it is starts like whole construction (ex: "(* comment *) \n\rVAR a : TT...") 
     *         which shouldn't be aligned under lines before
     */
    private boolean isDefinitionBlock(String text) {
        try {
            _XdsFlexScanner input = new _XdsFlexScanner(); 
            input.reset(text);
            
            while (true) {
                TokenType token = input.nextToken();
                if (ModulaTokenTypes.WHITE_SPACE == token ||
                    ModulaTokenTypes.BLOCK_COMMENT == token ||
                    ModulaTokenTypes.END_OF_LINE_COMMENT == token ) 
                {
                    continue;
                } else if (ModulaTokenTypes.TYPE_KEYWORD == token ||
                           ModulaTokenTypes.VAR_KEYWORD == token ||
                           ModulaTokenTypes.CONST_KEYWORD == token ||
                           ModulaTokenTypes.PROCEDURE_KEYWORD == token ||
                           ModulaTokenTypes.MODULE_KEYWORD == token ||
                           ModulaTokenTypes.BEGIN_KEYWORD == token ||
                           ModulaTokenTypes.END_KEYWORD == token)
                {
                    return true;
                }
                break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private int calcIncIndentOnEnter(IDocument doc, int start, boolean isEnterPressed) {
        try {
            IRegion region  = doc.getLineInformation(doc.getLineOfOffset(start));
            String partType = TextUtilities.getPartition(doc, IModulaPartitions.M2_PARTITIONING, region.getOffset(), false).getType();
            if (!IModulaPartitions.M2_CONTENT_TYPE_DEFAULT.equals(partType)) {
                return -1;
            }

            String line = doc.get(region.getOffset(), region.getLength());
            int cursorPos = start - region.getOffset();
            
            // Get non-whitespace tokens from this line
            _XdsFlexScanner input = new _XdsFlexScanner(); 
            input.reset(line);
            ArrayList<TokenWithPos> alTokens = new ArrayList<TokenWithPos>();
            try {
                while (true) {
                    TokenType token = input.nextToken();
                    if (token != ModulaTokenTypes.WHITE_SPACE) {
                        alTokens.add(new TokenWithPos(token, input.getTokenOffset()));
                    }
                    if (ModulaTokenTypes.EOF == token) {
                        break;
                    }
                }
            } catch (IOException e) {
            }
            //////
            if (alTokens.size() > 0) {
                int res = -1;
                TokenType token0 = alTokens.get(0).token;
                int minPos = alTokens.get(0).pos; // min. cursor position to indent _under_ this construction 
                int maxPos = -1; // position before 'END' in this string if any
                if (hsAddIndentOperators.containsKey(token0)) {
                    res = getM2Formatter().getIndentStatements();
                    minPos += hsAddIndentOperators.get(token0);
                } 
                else if (ModulaTokenTypes.CASE_KEYWORD.equals(token0)) {
                    res = getM2Formatter().getIndentInCase();
                    minPos += 4;
                } 
                else if (ModulaTokenTypes.VAR_KEYWORD.equals(token0)) {
                    res = getM2Formatter().getIndentDeclInVCT();
                    minPos += 3;
                } 
                else if (ModulaTokenTypes.CONST_KEYWORD.equals(token0)) {
                    res = getM2Formatter().getIndentDeclInVCT();
                    minPos += 5;
                } 
                else if (ModulaTokenTypes.TYPE_KEYWORD.equals(token0)) { 
                    res = getM2Formatter().getIndentDeclInVCT();
                    minPos += 4;
                }
                else if (alTokens.size() >= 3 &&
                         token0.equals(ModulaTokenTypes.IDENTIFIER) &&
                         alTokens.get(1).token.equals(ModulaTokenTypes.EQU) &&
                         alTokens.get(2).token.equals(ModulaTokenTypes.RECORD_KEYWORD)) 
                {   // <name> = RECORD
                    res = getM2Formatter().getIndentDeclOfRecFields();
                    minPos = alTokens.get(2).pos + 6;
                }
                
                if (res != -1) {
                    // check that there is no 'END' in this line:
                    for (TokenWithPos t : alTokens) {
                        if (ModulaTokenTypes.END_KEYWORD.equals(t.token)) {
                            maxPos = t.pos-1;
                            break; 
                        } else if (ModulaTokenTypes.END_OF_LINE_COMMENT.equals(t.token)) {
                            break;
                        }
                    }
                    
                    if (isEnterPressed && (cursorPos < minPos || (maxPos >= 0 && cursorPos > maxPos))) {
                        res = -1; // line is splitted in bad place, no new indentation required
                    }
                }
                return res;
            }
        } catch (Exception e) {}
        return -1;
    }
    
    private ArrayList<String> linesFrom(String text) {
        StringBuilder sb = new StringBuilder();
        ArrayList<String> res = new ArrayList<String>();
        boolean crlf = false;
        for (int i=0; i<text.length(); ++i) {
            char ch = text.charAt(i);
            if (ch == '\r' || ch == '\n') {
                crlf = true;
            } else {
                if (crlf) {
                    res.add(sb.toString());
                    sb.setLength(0);
                }
                crlf = false;
            }
            sb.append(ch);
        }
        if (sb.length() > 0) res.add(sb.toString());
        return res;
    }


    
    //-------------------------------------------------------------------------------------------------------------------------------------
    // Complete pair operators
    //-------------------------------------------------------------------------------------------------------------------------------------

    private ProcessingResult checkTypingAfterLine(IDocument doc, 
            DocumentCommand cmd, 
            boolean isEnterPressed,
            ProcessingResult onEnterResult) throws Exception 
    {
        if (cmd.text.length() == 0 || cmd.length != 0) { 
            return ProcessingResult.NONE; // no normal typing/pasting
        }

        IRegion reg = doc.getLineInformationOfOffset(cmd.offset);
        
        if (cmd.offset != reg.getOffset() + reg.getLength()) {
            return ProcessingResult.NONE; // no at the end of the line
        }

        String curLine = doc.get(reg.getOffset(), reg.getLength());
        String curLineIndent = indentFromStr(curLine);

        if (isEnterPressed) { 
            // Process <Enter> after line ended with THEN, LOOP, DO, OF: insert pair "  END;"

            String crlf = cmd.text;
            String nextLineIndent = ""; 
            
            if (onEnterResult == ProcessingResult.INDENT_ON_ENTER) {
                // cmd.text is now <crlf><correct indent for the next line>
                // get this parts from it:
                for (int i = cmd.text.length()-1; i>=0; --i) {
                    char ch = cmd.text.charAt(i);
                    if (ch != ' ' && ch != '\t') {
                        crlf = cmd.text.substring(0,i+1);
                        nextLineIndent = cmd.text.substring(i+1);
                        break;
                    }
                }
            }
            
            String lineT = curLine.trim();
            if (endsWithWord(lineT, "THEN") || endsWithWord(lineT, "LOOP") || endsWithWord(lineT, "DO") || endsWithWord(lineT, "OF") ) {
                boolean skip = false;
                int curLineIndentLen = calcIndentLen(curLineIndent);
                int nextLineNum = doc.getLineOfOffset(cmd.offset) + 1;
                int lnumTo = Math.min(doc.getNumberOfLines()-1, nextLineNum+10); 
                for (int lnum = nextLineNum; lnum <= lnumTo; ++lnum) {
                    IRegion lreg = doc.getLineInformation(lnum);
                    String str = doc.get(lreg.getOffset(), lreg.getLength());
                    String ind = indentFromStr(str);
                    if (ind.length() != str.length()) {
                        int indLen = calcIndentLen(ind);
                        if (indLen > curLineIndentLen) {
                            skip = true;
                            break;
                        } else if (indLen < curLineIndentLen) {
                            break; 
                        } else {
                            str = str.trim();
                            if (str.startsWith("END")) {
                                if (str.length() == 3 || !isIdentifierChar(str.charAt(3))) {
                                    skip = true;
                                }
                            } else if (str.startsWith("ELSE")) {
                                if (str.length() == 4 || !isIdentifierChar(str.charAt(4))) {
                                    skip = true;
                                }
                            } else if (str.startsWith("|") || str.contains(":")) {
                                skip = true;
                            }
                            break;
                        }
                    }
                }
                
                // skip when the next string starts from "END" or is indented more then current
                if (skip) {
                    return ProcessingResult.NONE; 
                } else {
                    cmd.text = crlf + nextLineIndent + crlf + curLineIndent + "END;";
                    cmd.caretOffset= cmd.offset + crlf.length() + nextLineIndent.length();
                    cmd.shiftsCaret= false;
                    return ProcessingResult.PAIR_OPERATOR_INSERTED;
                }
            }
        }
        
        
        if (!isIdentifierChar(cmd.text.charAt(0))) {
            //Process typing after "    END", "   ELSE" etc. -- re-indent this END etc. under its opening command
            
            String endWord =  curLine.substring(curLineIndent.length());
            HashSet<TokenType> hsOpen = hmEndPairs.get(endWord);
            if (hsOpen != null) {
                // The current line ~= '<curLineIndent>END' (..ELSE etc.) Some text is going to be inserted after this 'END'
                // So: try to indent END, ELSE etc. under its beginning token from hsOpen 
                String prevIndent = getIndentOfOpenLine(hsOpen, doc, cmd.offset);
                if (prevIndent != null) {
                    String nextIndent = "";
                    if (TextUtilities.equals(doc.getLegalLineDelimiters(), cmd.text) != -1) {
                        //XXX
                        // "   END" + <Enter> -- not indented in SmartTypingProcessor(?? - see Todo above) => next string should be indented too
                        nextIndent = prevIndent;
                    }
                    cmd.offset = reg.getOffset();
                    cmd.length = curLine.length();
                    cmd.text = prevIndent + endWord + cmd.text + nextIndent;
                    return ProcessingResult.PAIR_OPERATOR_REINDENTED;
                }
            }
        }
        
        return ProcessingResult.NONE;
    }

    
    private String getIndentOfOpenLine(HashSet<TokenType> hsOpen, IDocument doc, int offset) {
        try {
            boolean isItEnd = (hsOpen == hsEnd);
            int endLineNum = doc.getLineOfOffset(offset);
            IRegion region = doc.getLineInformation(endLineNum);

            int endIndentLen = calcIndentLen(indentFromStr(doc.get(region.getOffset(), region.getLength())));
                    
            int skipPair = 0;
            for (int lineNum = endLineNum-1; lineNum >= 0 && lineNum >= endLineNum-200; --lineNum) {
                region = doc.getLineInformation(lineNum);
                String partType = TextUtilities.getPartition(doc, IModulaPartitions.M2_PARTITIONING, region.getOffset(), false).getType();
                if (!IModulaPartitions.M2_CONTENT_TYPE_DEFAULT.equals(partType)) {
                    continue;
                }
    
                String line = doc.get(region.getOffset(), region.getLength());
                String indent = indentFromStr(line);
                int indentLen = calcIndentLen(indent);
                line = line.trim();
                
                if (isItEnd && line.startsWith("END") && (line.length()==3 || !isIdentifierChar(line.charAt(3)))) {
                    ++skipPair;
                }
                
                if (indentLen > endIndentLen) {
                    continue;
                }
                
                // Get all non-whitespace token from this line
                _XdsFlexScanner input = new _XdsFlexScanner(); 
                input.reset(line);
                ArrayList<TokenType> alTokens = new ArrayList<TokenType>();
                try {
                    while (true) {
                        TokenType token = input.nextToken();
                        if (token != ModulaTokenTypes.WHITE_SPACE) {
                            alTokens.add(token);
                        }
                        if (ModulaTokenTypes.EOF == token) {
                            break;
                        }
                    }
                } catch (IOException e) {
                }
                
                if (!alTokens.isEmpty()) {
                    if (hsOpen.contains(alTokens.get(0))) {
                        if (skipPair-- == 0) {
                            return indent;
                        }
                    } else if (isItEnd && alTokens.size() >= 3) {
                        // <name> = RECORD / ... / END   --- secial processing
                        if (alTokens.get(0).equals(ModulaTokenTypes.IDENTIFIER) &&
                            alTokens.get(1).equals(ModulaTokenTypes.EQU) &&
                            alTokens.get(2).equals(ModulaTokenTypes.RECORD_KEYWORD)) 
                        {
                            if (skipPair-- == 0) {
                                return indent;
                            }
                        }
                    } else if (ModulaTokenTypes.PROCEDURE_KEYWORD.equals(alTokens.get(0))) {
                        break; // Don't cross PROCEDURE bound
                    }
                }
            } // for
            
        } catch (Exception e) {}
        return null;
    }

    
    private boolean isIdentifierChar(char ch) {
        ch = Character.toUpperCase(ch);
        return ((ch>='A' && ch<='Z') || (ch>='0' && ch<='9') || ch == '_');
    }
    
    
    private boolean endsWithWord(String line, String s) {
        if (!line.endsWith(s)) {
            return false;
        }
        if ((line.length() > s.length()) && isIdentifierChar(line.charAt(line.length() - s.length() - 1))) {
            return false;
        }
        return true;
    }
    

    static {
        hsAddIndentOperators = new HashMap<TokenType, Integer>();
        // <token>, <its length>
        hsAddIndentOperators.put(ModulaTokenTypes.IF_KEYWORD, 2);
        hsAddIndentOperators.put(ModulaTokenTypes.THEN_KEYWORD, 4);
        hsAddIndentOperators.put(ModulaTokenTypes.ELSIF_KEYWORD, 5);
        hsAddIndentOperators.put(ModulaTokenTypes.ELSE_KEYWORD, 4);
        hsAddIndentOperators.put(ModulaTokenTypes.FOR_KEYWORD, 3);
        hsAddIndentOperators.put(ModulaTokenTypes.WHILE_KEYWORD, 5);
        hsAddIndentOperators.put(ModulaTokenTypes.LOOP_KEYWORD, 4);
        hsAddIndentOperators.put(ModulaTokenTypes.REPEAT_KEYWORD, 6);
        hsAddIndentOperators.put(ModulaTokenTypes.BEGIN_KEYWORD, 5);
        hsAddIndentOperators.put(ModulaTokenTypes.WITH_KEYWORD, 4);
        
        
        hmEndPairs = new HashMap<String, HashSet<TokenType>>();
        HashSet<TokenType> hs;
        
        hs = new HashSet<TokenType>();
        hs.add(ModulaTokenTypes.IF_KEYWORD);
        hs.add(ModulaTokenTypes.THEN_KEYWORD);
        hs.add(ModulaTokenTypes.ELSIF_KEYWORD);
        hs.add(ModulaTokenTypes.CASE_KEYWORD);
        hmEndPairs.put("ELSE", hs);
        
        hs = new HashSet<TokenType>();
        hs.add(ModulaTokenTypes.IF_KEYWORD);
        hs.add(ModulaTokenTypes.THEN_KEYWORD);
        hs.add(ModulaTokenTypes.ELSIF_KEYWORD);
        hmEndPairs.put("ELSIF", hs);
        
        hs = new HashSet<TokenType>();
        hs.add(ModulaTokenTypes.REPEAT_KEYWORD);
        hmEndPairs.put("UNTIL", hs);

        hs = new HashSet<TokenType>();
        hs.add(ModulaTokenTypes.PROCEDURE_KEYWORD);
        hs.add(ModulaTokenTypes.MODULE_KEYWORD);
        hmEndPairs.put("BEGIN", hs);

        hs = new HashSet<TokenType>();
        hs.add(ModulaTokenTypes.IF_KEYWORD);
        hs.add(ModulaTokenTypes.THEN_KEYWORD);
        hs.add(ModulaTokenTypes.ELSIF_KEYWORD);
        hs.add(ModulaTokenTypes.ELSE_KEYWORD);
        hs.add(ModulaTokenTypes.FOR_KEYWORD);
        hs.add(ModulaTokenTypes.WHILE_KEYWORD);
        hs.add(ModulaTokenTypes.LOOP_KEYWORD);
        hs.add(ModulaTokenTypes.BEGIN_KEYWORD);
        hs.add(ModulaTokenTypes.WITH_KEYWORD);
        hs.add(ModulaTokenTypes.CASE_KEYWORD);
        hmEndPairs.put("END", hs);
        hsEnd = hs;
    }
    
    private static class TokenWithPos {
        public TokenWithPos(TokenType token, int pos) {
            this.token = token;
            this.pos = pos;
        }
        public TokenType token;
        public int pos;
        
    }
}
