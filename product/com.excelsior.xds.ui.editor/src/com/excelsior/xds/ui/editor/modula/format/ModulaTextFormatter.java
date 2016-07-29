package com.excelsior.xds.ui.editor.modula.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import com.excelsior.xds.builder.buildsettings.BuildSettingsCache;
import com.excelsior.xds.core.builders.BuildSettings;
import com.excelsior.xds.parser.commons.ParserCriticalErrorReporter;
import com.excelsior.xds.parser.commons.ast.IElementType;
import com.excelsior.xds.parser.commons.ast.TokenTypes;
import com.excelsior.xds.parser.commons.pst.PstCompositeNode;
import com.excelsior.xds.parser.commons.pst.PstLeafNode;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.XdsParser;
import com.excelsior.xds.parser.modula.XdsSettings;
import com.excelsior.xds.parser.modula.XdsSourceType;
import com.excelsior.xds.parser.modula.ast.ModulaAst;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;
import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenTypes;
import com.excelsior.xds.parser.modula.symbol.binding.DefaultImportResolver;
import com.excelsior.xds.parser.modula.symbol.binding.IImportResolver;
import com.excelsior.xds.parser.modula.utils.ModulaFileUtils;
import com.excelsior.xds.ui.commons.utils.WorkbenchUtils;
import com.excelsior.xds.ui.editor.internal.preferences.formatter.FormatterProfile;
import com.excelsior.xds.ui.editor.internal.preferences.formatter.FormatterProfile.NewlineSetting;
import com.excelsior.xds.ui.editor.internal.preferences.formatter.FormatterProfile.WhiteSpaceSetting;
import com.excelsior.xds.ui.editor.modula.utils.ModulaEditorSymbolUtils;

/**
 * Modula-2 formatter to format Modula-2 source code 
 */
public class ModulaTextFormatter 
{
    private FormatterProfile fp;
    private int tabSize = 2;
    private int indentSize = 2;
    private boolean useTabs   = false;
    private boolean mixedMode = false; // when useTabs == true

    private int indentDeclInMod;
    private int indentDeclInProc;
    private int indentDeclInVCT;       // in VAR/CONST/TYPE
    private int indentLocalProcs;
    private int indentLocalMods;
    private int indentStatements;
    private int indentDeclOfRecFields;
    private int indentInCase;
    private int indentInCaseAlt;

    
    ////////////
    // Indents:
    private static final HashSet<IElementType> hsVCTblockParents; // VAR/CONST/TYPE declaration block
    private static final HashSet<IElementType> hsVCTkeywords;     // VAR/CONST/TYPE keywords
    // Whitespaces
    private static final HashSet<String> hsDiTriGraphs;           //   .. := <= >= <> << >> ** ::= 
    // Split lines:
    private static final HashMap<IElementType, Integer> hmSplitAfter;
    private static final HashMap<IElementType, Integer> hmSplitBefore;


    private ArrayList<Chunk> chunks;
    private ArrayList<Chunk> removedChunks;
    private String crlf;
    
    private int getIndentSize(FormatterProfile fp, FormatterProfile.IndentSetting fs) {
        if (fs.isRange()) {
            return fp.getValue(fs);
        } else {
            return fp.getAsBoolean(fs) ? indentSize : 0;
        }
    }
    
    public ModulaTextFormatter() { 
        this(FormatterProfile.getActiveProfile());
    }

    public ModulaTextFormatter(FormatterProfile fp) {
        this.fp = fp;
        crlf = System.getProperty("line.separator"); //$NON-NLS-1$
        tabSize = fp.getValue(FormatterProfile.IndentSetting.TabSize);
        indentSize = fp.getValue(FormatterProfile.IndentSetting.IndentSize);
        
        int tabm = fp.getValue(FormatterProfile.IndentSetting.TabMode);
        useTabs   = tabm != FormatterProfile.TABMODE_SPACES;
        mixedMode = tabm == FormatterProfile.TABMODE_MIXED;
        
        indentDeclInMod  = getIndentSize(fp, FormatterProfile.IndentSetting.IndentDeclInModule);
        indentDeclInProc = getIndentSize(fp, FormatterProfile.IndentSetting.IndentDeclInProc);
        indentDeclInVCT  = getIndentSize(fp, FormatterProfile.IndentSetting.IndentDeclInVCT);
        indentLocalProcs = getIndentSize(fp, FormatterProfile.IndentSetting.IndentDeclOfLocalProc);
        indentLocalMods  = getIndentSize(fp, FormatterProfile.IndentSetting.IndentDeclOfLocalMods);
        indentStatements = getIndentSize(fp, FormatterProfile.IndentSetting.IndentStatements);
        indentDeclOfRecFields = getIndentSize(fp, FormatterProfile.IndentSetting.IndentDeclOfRecFields);
        indentInCase     = getIndentSize(fp, FormatterProfile.IndentSetting.IndentInCaseBody);
        indentInCaseAlt  = getIndentSize(fp, FormatterProfile.IndentSetting.IndentInCaseAlternative);
    }       
        
    /**
     * Formats Modula-2 source code in the active editor. 
     */
    public void doFormat() {
        doFormat(null, -1, -1, false);
    }

    /**
     * Indents Modula-2 source code in the active editor. 
     */
    public void doIndent() {
        doFormat(null, -1, -1, true);
    }

    /**
     * 
     * @param editor - editor of NULL to get active editor
     * @param begPos begin offset of the formatted area (-1 to examine current selection)
     * @param endPos end offset of the formatted area (<=doc.getLength(), not used when begPos == -1)
     */
    public void doFormat(ITextEditor editor, int begPos, int endPos, boolean indentOnly) {
        try {
            IDocument doc = null;
            ModulaAst ast = null;
            if (editor == null) {
                editor = (ITextEditor) WorkbenchUtils.getActiveEditor(false);
            }
            if (editor != null && editor instanceof ITextEditor) {
                IEditorInput input = editor.getEditorInput();
                IDocumentProvider provider = ((ITextEditor) editor).getDocumentProvider();
                doc = provider.getDocument(input);
                ast = ModulaEditorSymbolUtils.getModulaAst(input);
            }
            if (ast == null) { 
                return;  // Nothing to do
            }
            
            if (begPos < 0) {
                // Determine text range to format:
                ISelection sel = editor.getSelectionProvider().getSelection();
                if (!(sel instanceof TextSelection)) {
                    return; // Nothing to format
                }
                TextSelection textSelection = (TextSelection)sel;
                int tmp = textSelection.getStartLine();
                begPos = doc.getLineOffset(tmp);
                tmp = textSelection.getEndLine();
                endPos = doc.getLineOffset(tmp) + doc.getLineLength(tmp);
            }
            
            if (begPos < 0 || endPos <= begPos) {
                return; // hz what to do
            }
            
            doFormat(doc, ast, begPos, endPos, indentOnly);
            
        } catch (Exception e) {
            System.out.println("Format exeption: "); //$NON-NLS-1$
            e.printStackTrace();
        }
    }
    
    /**
     * Calculates max() indent level for the given lines
     */
    public int calcMaxIndentLevel(ITextEditor editor, List<Integer> lineNums) {
        try {
            IDocument doc = null;
            ModulaAst ast = null;
            IDocumentProvider provider = ((ITextEditor) editor).getDocumentProvider();
            IEditorInput input = editor.getEditorInput();
            if ((provider != null) && (input instanceof IFileEditorInput)) {
                IFile ifile = ((IFileEditorInput)input).getFile();
                doc = provider.getDocument(input);
                XdsSourceType sourceType = ModulaFileUtils.getSourceType(input.getName());
                BuildSettings buildSettings = BuildSettingsCache.createBuildSettings(ifile);
                ParserCriticalErrorReporter errorReporter = ParserCriticalErrorReporter.getInstance();
				IImportResolver defaultImportResolver = new DefaultImportResolver(buildSettings, errorReporter, null);
                XdsParser parser = new XdsParser(null, doc.get(), new XdsSettings(buildSettings, sourceType), defaultImportResolver, errorReporter);
                ast = parser.parseModule();
            }
            if (ast == null) { 
                return -1;  // Nothing to do
            }

            buildChunksModel(doc, ast, doc.getLength());
            if (chunks.size() == 0) {
                return -1; // Nothing to do
            }
            
            int max = -1;
            for (int lnum : lineNums) {
                int chunkIdx = chunkIdxAtPos(doc.getLineOffset(lnum));
                int il = calcIndentLevel(chunkIdx);
                if (il > max) max = il;
            }
            return max;
            
        } catch (Exception e) {}

        return -1;
    }

    
    /**
     * 
     * @param document
     * @param ast
     * @param begPos begin offset of the formatted area (0 to format all)
     * @param endPos end offset of the formatted area (doc.getLength() to format all)
     */
    public void doFormat(IDocument doc, ModulaAst ast, int begPos, int endPos, boolean indentOnly) throws Exception {
        buildChunksModel(doc, ast, doc.getLength());
        if (chunks.size() == 0) {
            return; // Nothing to do
        }
        
        int firstChunkIdx = chunkIdxAtPos(begPos);
        int lastChunkIdx  = chunkIdxAtPos(endPos-1);

        { // Fix selection margins to include whole chunks:
            begPos = chunks.get(firstChunkIdx).getOffsetInDoc();
            Chunk c = chunks.get(lastChunkIdx);
            endPos = c.getOffsetInDoc() + c.getLengthInDoc();
        }


        if (lastChunkIdx == chunks.size()-1) {
            // chunks[lastChunkIdx+1] should always be some chunk with offset,
            // it will be required to build format edits from chunk model
            chunks.add(Chunk.createNoPlnChunkFromDoc(doc, doc.getLength(), 0));
        }
        
        // lastChunkIdx may be changed with model so use terminalChunk:
        Chunk terminalChunk = chunks.get(lastChunkIdx+1);  
       
        if (!indentOnly) {
            processNewLines(doc, firstChunkIdx, terminalChunk);
        }

        processWhitespaces(doc, firstChunkIdx, terminalChunk, indentOnly);

        if (!indentOnly) {
            processWrapLines(doc, firstChunkIdx, terminalChunk);
        }

        ArrayList<ReplaceEdit> edits = buildEditsFromModel(doc, firstChunkIdx, terminalChunk);
        
        DocumentRewriteSession drws = null;
        try {
            if (doc instanceof IDocumentExtension4) {
                drws = ((IDocumentExtension4)doc).startRewriteSession(DocumentRewriteSessionType.UNRESTRICTED);
            }
            MultiTextEdit edit= new MultiTextEdit(0, doc.getLength());
            edit.addChildren((TextEdit[]) edits.toArray(new TextEdit[edits.size()]));
            edit.apply(doc, TextEdit.CREATE_UNDO);
        }
        finally {
            if (doc instanceof IDocumentExtension4 && drws != null) {
                ((IDocumentExtension4)doc).stopRewriteSession(drws);
            }
        }
    }
    
    
    public int getTabSize() {
        return tabSize;
    }

    public int getIndentSize() {
        return indentSize;
    }
    
    public int getIndentDeclInVCT() {
        return indentDeclInVCT;
    }

    public int getIndentInCase() {
        return indentInCase;
    }
    
    public int getIndentStatements() {
        return indentStatements;
    }
    
    public int getIndentDeclOfRecFields() {
        return indentDeclOfRecFields;
    }

    private int chunkIdxAtPos(int pos) {
        for (int i=0; i<chunks.size(); ++i) {
            Chunk c = chunks.get(i);
            int offs = c.getOffsetInDoc();
            if (offs <= pos && pos < offs+c.getLengthInDoc()) {
                return i;
            }
        }
        return -1;
    }

    
    private void collectChunks(IDocument doc, PstNode pn) throws Exception {
        if (pn instanceof PstCompositeNode) {
            for (PstNode n : ((PstCompositeNode)pn).getChildren()) {
                collectChunks(doc, n);
            }
        } else if (pn instanceof PstLeafNode) {
            chunks.add(Chunk.createPlnChunkFromDoc(doc, (PstLeafNode)pn));
        }
    }

    
    private void buildChunksModel(IDocument doc, ModulaAst ast, int docLen) throws Exception {
        //TODO create shorter chunks array to cover reformatted lines only?
        chunks = new ArrayList<Chunk>();
        removedChunks = new ArrayList<Chunk>();
        collectChunks(doc, ast);
        Collections.sort(chunks, new Comparator<Chunk>() {
            public int compare(Chunk a, Chunk b) {
                return a.getOffsetInDoc() - b.getOffsetInDoc();
            }
        });
        
        // Fix holes and check for overlaps if any
        if (chunks.size() > 0) {
            int holesCounter = 0;
            int pos = 0;
            for (int i=0; i<chunks.size(); ++i) {
                Chunk c = chunks.get(i);
                int offs = c.getOffsetInDoc();
                if (offs < pos) {
                    throw new Exception ("Formatter: overlapped leafNodes, can't format"); //$NON-NLS-1$
                } else if (offs > pos) {
                    ++holesCounter;
                    chunks.add(i, Chunk.createNoPlnChunkFromDoc(doc, pos, offs-pos));
                    pos = offs;
                    System.out.println("Hole offs=" + pos + " text = '" + chunks.get(i).getText() + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                } else {
                    pos = offs + c.getLengthInDoc();
                }
            }
            if (holesCounter > 0) {
                System.out.println("Formatter varning: " + holesCounter + " holes inside PstLeafNodes found."); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (pos < docLen) {
                chunks.add(Chunk.createNoPlnChunkFromDoc(doc, pos, docLen-pos));
                System.out.println("Formatter varning: no PstLeafNode covers the end of text (length = " + (docLen - pos) + ")"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            
            Chunk last = chunks.get(chunks.size()-1);
            if (last.getOffsetInDoc() + last.getLengthInDoc() != docLen) {
                throw new Exception ("Formatter: last Chunk exceeds the document size"); //$NON-NLS-1$
            }
            
            //Split 'raw' whitespaces to whitespace and newline chunks:
            int eolPos = -1;
            int eolLen = 0;
            for (int i=0; i<chunks.size(); ++i) {
                Chunk c = chunks.get(i);
                if (c.isWhitespace()) {
                    if (eolPos < c.getOffsetInDoc()) {
                        int lnum = doc.getLineOfOffset(c.getOffsetInDoc());
                        IRegion li = doc.getLineInformation(lnum);
                        String crlf = doc.getLineDelimiter(lnum);
                        eolPos = li.getOffset() + li.getLength();
                        eolLen = crlf!=null ? crlf.length() : 0;
                    }
                    if (eolPos >= c.getOffsetInDoc() + c.getLengthInDoc()) {
                        continue; // next CRLF is outside this chunk
                    }
                    //--- Split it:
                    Chunk cNewLine = Chunk.createNewlineChunkFromDoc(doc, eolPos, eolLen); 
                    int endC = c.getOffsetInDoc() + c.getLengthInDoc();
                    int endEol = eolPos + eolLen; 
                    Chunk cWhiteTail = (endC > endEol) ? Chunk.createWhitespaceChunkFromDoc(doc, endEol, endC-endEol) : null;
                    if (c.getOffsetInDoc() < eolPos) {
                        // trim c:
                        c.trimChunkFromDoc(eolPos - c.getOffsetInDoc());
                        // insert Newline chunk after c:
                        chunks.add(++i, cNewLine);
                    } else {
                        // c starts at EOL so replace c with cNewLine:
                        chunks.set(i, cNewLine);
                    }
                    // insert tail of initial whitespace after NewLine if any:
                    if (cWhiteTail != null) {
                        chunks.add(i+1, cWhiteTail); // don't ++ i to continue from this chunk
                    }
                } 
            }
        }
    }
    
    private ArrayList<ReplaceEdit> buildEditsFromModel(IDocument doc, int firstChunkIdx, Chunk terminalChunk) throws BadLocationException {
        ArrayList<ReplaceEdit> res = new ArrayList<ReplaceEdit>();
        
        for (Chunk c : removedChunks) {
            if (c.getOffsetInDoc() >= 0) {
                res.add(new ReplaceEdit(c.getOffsetInDoc(), c.getLengthInDoc(), "")); //$NON-NLS-1$
            }
        }
        
        StringBuilder sb = new StringBuilder();
        
        for (int cidx = firstChunkIdx; true; cidx++) {
            Chunk c = chunks.get(cidx);
            
            int docOffs = c.getOffsetInDoc();
            
            if (docOffs >= 0) { // last will be terminaChunk. It allways has document offset so last 'sb' will be purged if any
                if (sb.length() > 0) {
                    res.add(new ReplaceEdit(docOffs, 0, sb.toString()));
                    sb.setLength(0);
                }
                
                if (c.isTextChanged()) {
                    String oldTxt = doc.get(c.getOffsetInDoc(), c.getLengthInDoc());
                    if (!oldTxt.equals(c.getText())) { // really changed?
                        res.add(new ReplaceEdit(c.getOffsetInDoc(), c.getLengthInDoc(), c.getText()));
                    }
                }
            } else {
                sb.append(c.getText());
            }
            
            if (c == terminalChunk) {
                break;
            }
        }
        
        
        return res;
    }
    


    private void processWhitespaces(IDocument doc, int firstChunkIdx, Chunk terminalChunk, boolean indentOnly) throws Exception {
        for (int cidx = firstChunkIdx; true; cidx++) {
            Chunk c = chunks.get(cidx);
            if (c == terminalChunk) {
                break;
            }
            
            Chunk cPrev = (cidx > 0) ? chunks.get(cidx-1) : null; 
            WhiteSpaceSetting wssPrev = null; 
            if (cPrev != null) {
                PstLeafNode pln = cPrev.getPstLeafNode();
                if (pln != null) {
                    wssPrev = fp.searchWhiteSpaceSetting(pln);
                }
            }
            
            Chunk cNext = chunks.get(cidx+1);
            WhiteSpaceSetting wssNext = null; 
            if (cNext != null) {
                PstLeafNode pln = cNext.getPstLeafNode();
                if (pln != null) {
                    wssNext = fp.searchWhiteSpaceSetting(pln);
                }
            }
            
            if (c.isNewLine()) {
                // nothing for now
            } else if (cPrev==null || cPrev.isNewLine()) { // 1st chunk in the line
                
                int lev = calcIndentLevel(cidx);
                if (lev >= 0) {
                    // Create indent:
                    String indent = constructIndent(lev, c.isWhitespace() ? c.getText() : null);
                    
                    if (c.isWhitespace()) {
                        if (!indent.equals(c.getText())) {
                            // replace 'c' with 'indent': insert 'indent' _after_ 'c' and remove 'c' - it 
                            // will not move possible markers at 0 position
                            c.setNewText(indent);
                        }
                    } else if (!indent.isEmpty()) {
                        // insert 'indent' before 'c'
                        chunks.add(cidx, Chunk.createWhitespaceChunk(indent));
                    }
                }
            } else if (!indentOnly){ // chunk inside line, not 1st:
                
                if (c.isWhitespace()) {
                    boolean preserve = true;
                    if (wssPrev != null || wssNext != null) {
                        preserve = !((wssPrev==null || !fp.isInsSpaceAfter(wssPrev)) &&
                                     (wssNext==null || !fp.isInsSpaceBefore(wssNext)));
                    }
                    if (!preserve) {
                        removedChunks.add(c);
                        chunks.remove(cidx);
                        --cidx;
                    } else {
                        // all preserved whitespaces inside line replace with single " "
                        if (!" ".equals(c.getText())) { //$NON-NLS-1$
                            c.setNewText(" "); //$NON-NLS-1$
                        }
                    }
                } else {
                    if (wssPrev != null && fp.isInsSpaceAfter(wssPrev)) {
                        // insert space after cPrev  
                        chunks.add(cidx, Chunk.createWhitespaceChunk(" ")); //$NON-NLS-1$
                    } else {
                        if (cPrev == null || cPrev.isWhitespace() || cPrev.isNewLine()) {
                            // chunk after newline or whitespace - never insert space before
                        } else {
                            PstLeafNode pln = c.getPstLeafNode();
                            if (pln != null) {
                                WhiteSpaceSetting wssC = fp.searchWhiteSpaceSetting(pln);
                                if (wssC != null && fp.isInsSpaceBefore(wssC))
                                {
                                    // insert space before c
                                    chunks.add(cidx, Chunk.createWhitespaceChunk(" ")); //$NON-NLS-1$
                                }
                            }
                        }
                    }
                }
            }
            
        } // for
    }
    
    
    
    private void processNewLines(IDocument doc, int firstChunkIdx, Chunk terminalChunk) throws Exception {
        int spaceBeg   = firstChunkIdx; // idx of 1st chunk after previous non-whitespace and non-newline chunk (cPrev)
        int spaceCRLFs = 0; // number of newline chunks inside [spaceBeg .. cidx[
        
        int delayedNewlines = -1;  // getInsNewLinesAfter() for [spaceBeg-1]

        for (int cidx = firstChunkIdx; true; cidx++) {
            Chunk c = chunks.get(cidx);
            if (c == terminalChunk) {
                break;
            }
            
            if (c.isWhitespace()) {
                continue;
            } 
            
            if (c.isNewLine()) {
                ++spaceCRLFs;
                continue;
            }
            
            int cBeforeNL = -1;
            int cAfterNL = -1;
            PstLeafNode pln = c.getPstLeafNode();
            if (pln != null) {
                NewlineSetting ss = fp.searchStmtSetting(pln);
                if (ss != null) {
                    cBeforeNL = fp.getInsNewLinesBefore(ss);
                    cAfterNL  = fp.getInsNewLinesAfter(ss);
                }
            }
            
            // prefNL - preferred number of newlines for 'space' (or -1 when hz) 
            int prefNL = Math.max(cBeforeNL, delayedNewlines);
            //if (prefNL < 0) prefNL = 0;

            if (prefNL >= 0 && spaceBeg > firstChunkIdx) {
                // adjust number of newlines inside 'space' 
                // indents will be formatted on the next 'whitespace' walk
                if (prefNL > spaceCRLFs) {
                    while (spaceCRLFs++ < prefNL) {
                        chunks.add(cidx, Chunk.createNewlineChunk(crlf));
                        ++cidx; 
                    }
                } else if (prefNL < spaceCRLFs) {
                    // Turned off ability to decrease newlines number to preferred. We never removes newlines now.                    
                    //    while (spaceCRLFs-- > prefNL) {
                    //        Chunk ccc;
                    //        while ((ccc = chunks.get(spaceBeg)).isWhitespace()) {
                    //            removedChunks.add(ccc);
                    //            chunks.remove(spaceBeg);
                    //            --cidx;
                    //        }
                    //        if (!ccc.isNewLine()) {
                    //            System.out.println("Formatter int. error at processNewLines()"); //$NON-NLS-1$
                    //            throw new Exception("formatter int. error"); //$NON-NLS-1$
                    //        }
                    //        removedChunks.add(ccc);
                    //        chunks.remove(spaceBeg);
                    //        --cidx;
                    //    }
                }
            }
            
            // c is non-whitespace and non-newline:
            delayedNewlines = cAfterNL;
            spaceBeg = cidx+1;
            spaceCRLFs = 0;
        }
        
    }

    private class SplitPos { 
        SplitPos(Chunk chunk, boolean before, int weight, int posInLine) {
            this.chunk = chunk;
            this.before = before;
            this.weight = weight;
            this.posInLine = posInLine;
        }
        Chunk chunk;
        boolean before;
        int weight;
        int posInLine;
    }
    
    private void processWrapLines(IDocument doc, int firstChunkIdx, Chunk terminalChunk) throws Exception {
        int lineLimit = fp.getWrappingWidth();
        int posInLine = 0;
        ArrayList<SplitPos> splitPosList = new ArrayList<ModulaTextFormatter.SplitPos>();
        int    cntInLine = 0;
        
        for (int cidx = firstChunkIdx; true; ++cidx) {
            Chunk c2     = chunks.get(cidx);
            if (c2 == terminalChunk) {
                break;
            }

            ++cntInLine;
            
            PstLeafNode pln = c2.getPstLeafNode();
            IElementType et = pln == null ? null : pln.getElementType();

            int cLen = c2.getText().length();
            if (c2.isNewLine()) {
                posInLine = 0;
                cntInLine = 0;
                splitPosList.clear();
                continue;
            }

            //
            // Want to split the line?
            //
            if (posInLine + cLen > lineLimit) {
                if (pln != null && (ModulaTokenTypes.BLOCK_COMMENT.equals(et) || ModulaTokenTypes.END_OF_LINE_COMMENT.equals(et))) {
                    //////// ignore comments for now 
                } else {
                    // Heuristic search for best split position:
                    // summary weight is (position in the string) * (split weight)
                    int sw = -1;
                    SplitPos splitPos = null;
                    for (SplitPos sp : splitPosList) {
                        int x = sp.weight * sp.posInLine;
                        if (x >= sw ) {
                            sw = x;
                            splitPos = sp;
                        }
                    }
                    if (splitPos != null) {
                        // Do split here:
                        int splitChunkIdx = chunks.indexOf(splitPos.chunk);
                        int crlfIdx = splitChunkIdx;
                        if (!splitPos.before || splitPos.chunk.isWhitespace()) {
                            ++crlfIdx;
                        }

                        // prepare indent:
                        String indent = ""; //$NON-NLS-1$
                        PstLeafNode plnNext = null;
                        for (int i = crlfIdx; i < chunks.size() && plnNext == null; ++i) {
                            plnNext = chunks.get(i).getPstLeafNode();
                        }
                        if (plnNext != null) {
                            int lev = calcIndentLevel(plnNext);
                            indent = constructIndent(lev, ""); //$NON-NLS-1$
                        }
                        
                        // -- Do split:
                        // cut EOL whitespace if any:
                        if (crlfIdx > 0 && chunks.get(crlfIdx-1).isWhitespace()) {
                            chunks.remove(crlfIdx);
                            --crlfIdx;
                            --cidx;
                        }
                        chunks.add(crlfIdx,  Chunk.createNewlineChunk(crlf));
                        ++cidx;
                        if (!indent.isEmpty()) {
                            chunks.add(crlfIdx+1, Chunk.createWhitespaceChunk(indent));
                            ++cidx;
                        }
                        
                        // begin of new line: :
                        splitPosList.clear();
                        // re-calculate posInLine and cntInLine for chunks[cidx]:
                        posInLine = 0;
                        cntInLine = 0;
                        for (int i = crlfIdx + 1; i < cidx; ++i) {
                            posInLine += chunks.get(i).getText().length();
                            ++cntInLine;
                        }
                    }
                }
            }
            
            // Collect candidates to split positions:
            Integer integer;
            if (cntInLine > 1 && (integer = hmSplitAfter.get(et)) != null) {
                splitPosList.add(new SplitPos(c2, false, integer.intValue(), posInLine + cLen));
            } 

            if (cntInLine > 2 && (integer = hmSplitBefore.get(et)) != null) {
                splitPosList.add(new SplitPos(c2, true, integer.intValue(), posInLine));
            } 
            
            posInLine += cLen;
            
        }
    }


//XXX может оставить эту проверялку и применять всегда? на всякий случай....
//    
//    private boolean isRequiredWhitespace(Chunk cPrev, Chunk cNext, IDocument doc) {
//        if (cPrev == null || cNext == null) {
//            return false; // out of formatted area, area is whole strings 
//        }
//        try {
//            String sP = cPrev.getText(doc);
//            String sN = cNext.getText(doc);
//            char chP = Character.toUpperCase(sP.charAt(sP.length()-1));
//            boolean b1 = (chP>='A' && chP <='Z') || (chP>='0' && chP<='9') || chP=='_';
//            char chN = Character.toUpperCase(sN.charAt(0));
//            boolean b2 = (chN >= 'A' && chN <= 'Z') || (chN>='0' && chN<='9') || chN == '_';
//            if (b1 && b2 || (chP == '.' && chN == '.')) {
//                return true; // whitespace between two identifiers/operators/num.constants/"3. .3"
//            }
//            
//            if (hsDiTriGraphs.contains(sP + sN)) {
//                return true;
//            }
//            
//            return false; // No need to preserve space
//        } catch (Exception e) {}
//        
//        return true;
//    }
    
    

    /**
     * 
     * @param lev - indent level (in characters on the screen)
     * @param oldIndent - current indent of this line (for mixed mode) or null
     * @return string with tabs and spaces
     */
    public String constructIndent(int lev, String oldIndent) {
        String indent = ""; //$NON-NLS-1$
        // Create indent:
        if (lev > 0) {
            char seq[] = new char[lev];
            if (!useTabs) {
                for (int i = 0; i < lev; ++i) seq[i] = ' ';
            } else {
                if (!mixedMode || oldIndent == null) {
                    for (int i = 0; i < lev; ++i) seq[i] = '\t';
                } else {
                    // preserve tabs/spaces sequence from beffining of the line as far as it is possible
                    for (int i=0; i<lev; ++i) {
                        if (i < oldIndent.length()) {
                            seq[i] = oldIndent.charAt(i);
                        } else {
                            seq[i] = '\t';
                        }
                    }
                }
            }
            int pos = 0;
            for (int useSeq = 0; true; ++useSeq) {
                int cx = 1;
                char ch = ' ';
                if (seq[useSeq] == '\t') {
                    cx = ((pos + tabSize) / tabSize) * tabSize - pos;
                    ch = '\t';
                }
                if (pos + cx > lev) {
                    break;
                }
                indent += ch;
                pos += cx;
                if (pos == lev) {
                    break;
                }
            }
            while(pos++ < lev) {
                indent += ' ';
            }
        }
        return indent;
    }


    private int calcIndentLevel(int cidx) {
        if (cidx>0 && !chunks.get(cidx-1).isNewLine()) {
            return -1; // hz. start of line expected
        }
        
        // cidx - start of some string
        // here may be whitespace or smth else with pstnode. skip to 1st normal chunk
        PstLeafNode pln = null;
        for (; cidx < chunks.size(); ++cidx) {
            Chunk c = chunks.get(cidx);
            if (c.isNewLine()) {
                return -1; 
            }
            pln = c.getPstLeafNode();
            if (pln != null) {
                break;
            }
        }
        if (pln == null) return -1; // no normal chunks in this line
        
        return calcIndentLevel(pln);
    }
        
    private int calcIndentLevel(PstLeafNode pln) {
        int lev = 0;
        int inModuleDecl = 0;
        int inProcDecl = 0;
        int localProcLev = -1;
        int localModsLev = 0;
        int inStmtLst = 0;
        int inRec = 0;
        int inCase = 0;
        boolean isInExpression = false;
        boolean isInParams = false;
        boolean isCaseAlt = false;
        for (PstNode pn = pln.getParent(); pn != null ; pn = pn.getParent()) {
            IElementType et = pn.getElementType();
            
            // Procedure/module declarations, local procedures:
            if (et.equals(ModulaElementTypes.DECLARATIONS)) {
                IElementType pt = pn.getParent().getElementType();
                if (pt.equals(ModulaElementTypes.PROGRAM_MODULE) ||
                    pt.equals(ModulaElementTypes.LOCAL_MODULE) ||
                    pt.equals(ModulaElementTypes.DEFINITION_MODULE) )
                {
                    ++inModuleDecl;
                } else if (pt.equals(ModulaElementTypes.PROCEDURE_DECLARATION) ||
                           pt.equals(ModulaElementTypes.OBERON_METHOD_DECLARATION)) 
                {
                    ++inProcDecl;
                }
            } else if (et.equals(ModulaElementTypes.PROCEDURE_DECLARATION)) {
                ++localProcLev;
                if (localProcLev > 0) --inProcDecl;
            } else if (et.equals(ModulaElementTypes.LOCAL_MODULE)) {
                ++localModsLev;
            // Records:
            } else if (et.equals(ModulaElementTypes.RECORD_FIELD_BLOCK_LIST) && ModulaElementTypes.RECORD_TYPE_DEFINITION.equals(pn.getParent().getElementType())) {
                ++inRec;
            // CASE in variant records:
            } else if (et.equals(ModulaElementTypes.RECORD_VARIANT_LIST)) { // whole case body
                ++inCase;
                if (pln.getElementType().equals(ModulaTokenTypes.SEP)) {                  // | 1..2: ('|' before 1st alternative)
                    isCaseAlt = true;
                }
            } else if (et.equals(ModulaElementTypes.RECORD_VARIANT_LABEL_LIST)) {         // 1..2:
                isCaseAlt = true;
            } else if (et.equals(ModulaElementTypes.RECORD_VARIANT)) {       
                if (pln.getElementType().equals(ModulaTokenTypes.SEP)) {                  // | 1..2:
                    isCaseAlt = true;
                }
            } else if (et.equals(ModulaElementTypes.RECORD_VARIANT_ELSE_PART)) {
                if (pln.getElementType().equals(ModulaTokenTypes.ELSE_KEYWORD)) {         // ELSE
                    isCaseAlt = true;
                }
            // CASE operator:
            } else if (et.equals(ModulaElementTypes.CASE_VARIANT_LIST)) {
                ++ inCase;
                if (pln.getElementType().equals(ModulaTokenTypes.SEP)) {
                    isCaseAlt = true;
                }
            } else if (et.equals(ModulaElementTypes.CASE_LABEL_LIST)) {
                isCaseAlt = true;
            } else if (et.equals(ModulaElementTypes.CASE_ELSE_PART)) {
                if (pln.getElementType().equals(ModulaTokenTypes.ELSE_KEYWORD)) {
                    isCaseAlt = true;
                }
            // Statement list (usual and in CASE):
            } else if (et.equals(ModulaElementTypes.STATEMENT_LIST)) {
                IElementType pet = pn.getParent().getElementType();
                if (pet.equals(ModulaElementTypes.CASE_VARIANT) || pet.equals(ModulaElementTypes.CASE_ELSE_PART)) {
                  // statement list of CASE variant:
                } else {              
                    // usual statement list:
                    ++ inStmtLst;
                }
            } else if (et.equals(ModulaElementTypes.EXPRESSION)) {
                // expression splited to the next string:
                isInExpression = true;
            } else if (et.equals(ModulaElementTypes.FORMAL_PARAMETER_LIST)) {
                isInParams = true;
            }

            // System.out.print(et.toString() + "   ");
            
            if (indentDeclInVCT > 0) {
                if (hsVCTblockParents.contains(et)) {
                    lev += indentSize;
                }
            }
        } // for

        if (indentDeclInVCT > 0 && hsVCTkeywords.contains(pln.getElementType()) && lev > 0) {
            lev -= indentSize;
        }
        
        lev += inModuleDecl * indentDeclInMod;
        lev += inStmtLst * indentStatements;
        lev += inRec * indentDeclOfRecFields; 
        lev += inCase * (indentInCase + indentInCaseAlt);
        if (inProcDecl > 0)   lev += inProcDecl * indentDeclInProc;
        if (localProcLev > 0) lev += localProcLev * indentLocalProcs;
        if (localModsLev > 0) lev += localModsLev * indentLocalMods;
        if (isCaseAlt && (inCase > 0)) {
            lev -= indentInCaseAlt;
        }

        // Need to tune separately? Use indentStatements for now:
        if (isInExpression)   lev += indentStatements;
        if (isInParams)   lev += indentStatements;

        // System.out.println("");
        return lev;
    }
    
    
    static {
        hsVCTblockParents = new HashSet<IElementType>();
        hsVCTblockParents.add(ModulaElementTypes.CONSTANT_DECLARATION_BLOCK);
        hsVCTblockParents.add(ModulaElementTypes.TYPE_DECLARATION_BLOCK);
        hsVCTblockParents.add(ModulaElementTypes.VARIABLE_DECLARATION_BLOCK);
        
        hsVCTkeywords = new HashSet<IElementType>();
        hsVCTkeywords.add(ModulaTokenTypes.CONST_KEYWORD);
        hsVCTkeywords.add(ModulaTokenTypes.TYPE_KEYWORD);
        hsVCTkeywords.add(ModulaTokenTypes.VAR_KEYWORD);

        hsDiTriGraphs = new HashSet<String>();
        for (String s : new String[]{"..", ":=", "<=", ">=", "<>", "<<", ">>", "**", "::="}) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
            hsDiTriGraphs.add(s);
        }
        
        hmSplitAfter = new HashMap<IElementType, Integer>(); 
        hmSplitAfter.put(ModulaTokenTypes.COMMA, 80);        // ,
        hmSplitAfter.put(ModulaTokenTypes.SEMICOLON, 90);    // ; 
        hmSplitAfter.put(ModulaTokenTypes.BECOMES, 20);      // :=

        hmSplitBefore = new HashMap<IElementType, Integer>(); 
        hmSplitBefore.put(ModulaTokenTypes.NOT, 60);         // ~
        hmSplitBefore.put(ModulaTokenTypes.LPARENTH, 50);    // (
        hmSplitBefore.put(ModulaTokenTypes.LBRACKET, 50);    // [
        hmSplitBefore.put(ModulaTokenTypes.PLUS, 55);        // +
        hmSplitBefore.put(ModulaTokenTypes.MINUS, 55);       // -
        hmSplitBefore.put(ModulaTokenTypes.TIMES, 55);       // *
        hmSplitBefore.put(ModulaTokenTypes.SLASH, 55);       // /
        hmSplitBefore.put(ModulaTokenTypes.SEP, 60);         // |
        hmSplitBefore.put(ModulaTokenTypes.AND, 60);         // &
        hmSplitBefore.put(ModulaTokenTypes.EQU, 70);         // =
        hmSplitBefore.put(ModulaTokenTypes.NEQ, 70);         // # <>
        hmSplitBefore.put(ModulaTokenTypes.LSS, 70);         // <
        hmSplitBefore.put(ModulaTokenTypes.GTR, 70);         // >
        hmSplitBefore.put(ModulaTokenTypes.LTEQ, 70);        // <=
        hmSplitBefore.put(ModulaTokenTypes.GTEQ, 70);        // >=
        hmSplitBefore.put(ModulaTokenTypes.LEFT_SHIFT, 70);  // <<
        hmSplitBefore.put(ModulaTokenTypes.RIGHT_SHIFT, 70); // >>
        hmSplitBefore.put(ModulaTokenTypes.EXPONENT, 70);    // **
        hmSplitBefore.put(ModulaTokenTypes.WHITE_SPACE, 10);  // ' ' - particular case of splitBefore
        
    }
    
    /////////////////////////////////////////////////////////////////////////////////

    /**
     * Chunk may be: 
     * - normal chunk refers to its (not-whitespace) PstLeafNode
     * - WhiteSpace chunk - sequence of blanks and tabs (w/o CRLF inside)
     * - NewLine chunk - line delimeter
     * 
     * @author fsa
     *
     */
    private static class Chunk {
        private static final int CT_PLN_OR_NULL = 0;
        private static final int CT_WHITE_SPACE = 1;
        private static final int CT_NEW_LINE = 2;
        
        private PstLeafNode pln;
        protected int offsInDoc;
        private int lenInDoc;
        private int ctype;
        private String text;
        private boolean isTextChanged;

        /**
         * Creates 'primary' chunk with offsetInDoc/lenInDoc.
         * 
         * @param doc
         * @param pln
         * @return
         * @throws BadLocationException
         */
        public static Chunk createPlnChunkFromDoc(IDocument doc, PstLeafNode pln) throws BadLocationException {
            Chunk chunk = new Chunk(doc, pln.getOffset(), pln.getLength(), CT_PLN_OR_NULL);
            chunk.offsInDoc = pln.getOffset();
            chunk.lenInDoc = pln.getLength();
            if (pln.getToken() == TokenTypes.WHITE_SPACE) {
                // Note: Initial 'raw' whitespaces will be splitted to CT_WHITE_SPACE & CT_NEW_LINE
                // It not requires leafNode (and splitter don't know what to do with this node)
                chunk.ctype = CT_WHITE_SPACE;
            } else {
                chunk.pln = pln;
            }
            return chunk;
        }

        /**
         * Creates 'primary' chunk with offsetInDoc/lenInDoc and without leaf node for 'bad' uncovered ranges in source.
         * This chunk may be created when bugging parser was not covered all document with leaf nodes.
         * 
         * @param doc
         * @param offsInDoc
         * @param lenInDoc
         * @return
         * @throws BadLocationException
         */
        public static Chunk createNoPlnChunkFromDoc(IDocument doc, int offsInDoc, int lenInDoc) throws BadLocationException {
            return new Chunk(doc, offsInDoc, lenInDoc, CT_PLN_OR_NULL);
        }
        
        /**
         * Creates 'primary' chunk
         */
        public static Chunk createWhitespaceChunkFromDoc(IDocument doc, int offsInDoc, int lenInDoc) throws BadLocationException {
            return new Chunk(doc, offsInDoc, lenInDoc, CT_WHITE_SPACE);
        }

        /**
         * Creates primary chunk
         */
        public static Chunk createNewlineChunkFromDoc(IDocument doc, int offsInDoc, int lenInDoc) throws BadLocationException {
            return new Chunk(doc, offsInDoc, lenInDoc, CT_NEW_LINE);
        }

        /**
         * Creates 'new' chunk with offsetInDoc/lenInDoc == -1/-1
         */
        public static Chunk createWhitespaceChunk(String text) {
            return new Chunk(text, CT_WHITE_SPACE);
        }
        
        /**
         * Creates 'new' chunk with offsetInDoc/lenInDoc == -1/-1
         */
        public static Chunk createNewlineChunk(String text) {
            return new Chunk(text, CT_NEW_LINE);
        }
        
        
        
        private Chunk(IDocument doc, int offsInDoc, int lenInDoc, int ctype) throws BadLocationException {
            this(doc.get(offsInDoc, lenInDoc), ctype);
            this.offsInDoc = offsInDoc;
            this.lenInDoc = lenInDoc;
            this.pln = null;
        }

        
        private Chunk(String text, int ctype) {
            this.text = text;
            this.ctype = ctype;
            isTextChanged = false;
            offsInDoc = -1;
            lenInDoc = -1;
        }


        public void trimChunkFromDoc(int newLenInDoc) {
            this.lenInDoc = newLenInDoc;
            text = text.substring(0, newLenInDoc);
         }

        /**
         * @return offset of chunk prototype in document or -1 when it is new chunk
         */
        public int getOffsetInDoc() {
            return offsInDoc;
        }

        /**
         * @return length of chunk prototype in document or -1 when it is new chunk
         */
        public int getLengthInDoc() {
            return lenInDoc;
        }
        
        public String getText() {
            return text; 
        }
        
        public void setNewText(String s) {
            text = s;
            isTextChanged = true;
        }
        
        public PstLeafNode getPstLeafNode() {
            return pln;
        }
        
        public boolean isWhitespace() {
            return ctype == CT_WHITE_SPACE;
        }

        public boolean isNewLine() {
            return ctype == CT_NEW_LINE;
        }
        
        public boolean isTextChanged() {
            return isTextChanged;
        }
    }

  
}
