package com.excelsior.xds.ui.editor.internal.preferences.formatter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.MarginPainter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import com.excelsior.xds.core.builders.BuildSettings;
import com.excelsior.xds.core.builders.DefaultBuildSettingsHolder;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.parser.commons.ParserCriticalErrorReporter;
import com.excelsior.xds.parser.commons.ast.IElementType;
import com.excelsior.xds.parser.commons.pst.PstCompositeNode;
import com.excelsior.xds.parser.commons.pst.PstLeafNode;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.XdsParser;
import com.excelsior.xds.parser.modula.XdsSettings;
import com.excelsior.xds.parser.modula.XdsSourceType;
import com.excelsior.xds.parser.modula.ast.ModulaAst;
import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenTypes;
import com.excelsior.xds.parser.modula.symbol.binding.DefaultImportResolver;
import com.excelsior.xds.parser.modula.symbol.binding.IImportResolver;
import com.excelsior.xds.ui.editor.modula.ModulaTokens;
import com.excelsior.xds.ui.editor.modula.format.ModulaTextFormatter;

@SuppressWarnings("restriction")
public class FormatterPreview extends TextViewer {

    private static final String RESOURCE_FOLDER_LOCATION = "resources/previews/formatter/"; //$NON-NLS-1$

    private StyledText styledText;
    private MarginPainter fMarginPainter;
    private XdsSourceType xdsSourceType;
    private ArrayList<PstLeafNode> leafsNodes;
    private static HashMap<IElementType, ModulaTokens> pstToTokenMap;
    private Color defBackgroundColor;
    private String initialText;

    public FormatterPreview(Composite parent, String resFileName, XdsSourceType xdsSourceType) {
        super(parent, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
        this.xdsSourceType = xdsSourceType;
        this.styledText = this.getTextWidget();
        
        String resPath = RESOURCE_FOLDER_LOCATION + resFileName; 
        try(InputStream resourceStream = ResourceUtils.getPluginResourceAsStream(ResourceUtils.getXdsResourcesPluginBundle(), resPath)) {
        	this.initialText = IOUtils.toString(resourceStream);
        } 
        catch (Exception e) {
            this.initialText = "** Internal error: can't read " + resPath + "\n** Preview not available"; //$NON-NLS-1$ //$NON-NLS-2$
        }

        Font font= JFaceResources.getTextFont();
        styledText.setFont(font);
        defBackgroundColor = getEditorBackgroundColor(EditorsPlugin.getDefault().getPreferenceStore());
        styledText.setBackground(defBackgroundColor);
        
        styledText.setText(initialText);
        colorIt();
    }
    
    public void turnOnMarginPainter() {
        fMarginPainter= new MarginPainter(this);
        fMarginPainter.setMarginRulerStyle(SWT.LINE_DOT);
        fMarginPainter.setMarginRulerColor(new Color(Display.getDefault(), 0,0,0));
        this.addPainter(fMarginPainter);
    }
    
    public void setProfile(FormatterProfile fp) {
        try {
            int top = styledText.getTopPixel();
            int hor = styledText.getHorizontalPixel();
            ModulaTextFormatter m2f = new ModulaTextFormatter(fp);
            Document doc = new Document(initialText);
            BuildSettings buildSettings = DefaultBuildSettingsHolder.DefaultBuildSettings;
            ParserCriticalErrorReporter errorReporter = ParserCriticalErrorReporter.getInstance();
			IImportResolver defaultImportResolver = new DefaultImportResolver(buildSettings, errorReporter, null);
            XdsParser parser = new XdsParser(null, doc.get(), new XdsSettings(buildSettings, xdsSourceType), defaultImportResolver, errorReporter);
            ModulaAst ast = parser.parseModule();
            m2f.doFormat(doc, ast, 0, doc.getLength(), false);
            styledText.setText(doc.get());
            styledText.setHorizontalPixel(hor);
            styledText.setTopPixel(top);
            if (fMarginPainter != null) {
                fMarginPainter.setMarginRulerColumn(fp.getWrappingWidth());
            }
        } catch (Exception e) {e.printStackTrace();}
        colorIt();
    }
    
    private void colorIt() {
        try {
            if (pstToTokenMap == null) {
                initTokenMap();
            }
            // Parse text:
            Document doc = new Document(styledText.getText());
            BuildSettings buildSettings = DefaultBuildSettingsHolder.DefaultBuildSettings;
            ParserCriticalErrorReporter errorReporter = ParserCriticalErrorReporter.getInstance();
			IImportResolver defaultImportResolver = new DefaultImportResolver(buildSettings, errorReporter, null);
            XdsParser parser = new XdsParser(null, doc.get(), new XdsSettings(buildSettings, xdsSourceType), defaultImportResolver, errorReporter);
            ModulaAst ast = parser.parseModule();
            leafsNodes = new ArrayList<PstLeafNode>();
            // Fill Leaf array:
            collectLeafs(ast);
            Collections.sort(leafsNodes, new Comparator<PstLeafNode>() {
                public int compare(PstLeafNode a, PstLeafNode b) {
                    return a.getOffset() - b.getOffset();
                }
            });
            // Search matched M2Token for elements in the leaf array and collect style ranges for them:
            ArrayList<StyleRange> asr = new ArrayList<StyleRange>(); 
            for (PstLeafNode pln : leafsNodes) {
                ModulaTokens mt = pstToTokenMap.get(pln.getElementType());
                if (mt == null) {
                    mt = ModulaTokens.Default; 
                }
                int style = mt.getToken().getStyleWhenEnabled();
                StyleRange sr = new StyleRange();
                sr.fontStyle = (style & (SWT.BOLD | SWT.ITALIC));
                sr.underline = (style & TextAttribute.UNDERLINE) != 0;
                sr.strikeout = (style & TextAttribute.STRIKETHROUGH) != 0;
                sr.foreground = new Color(Display.getDefault(), mt.getToken().getRgbWhenEnabled());
                sr.background = defBackgroundColor;
                sr.start = pln.getOffset();
                sr.length = pln.getLength();
                asr.add(sr);
            }
            if (asr.size() > 0) {
                styledText.setStyleRanges(asr.toArray(new StyleRange[asr.size()]));
            }
        } catch (Exception e) {}
    }

    
    private void collectLeafs(PstNode pn) {
        if (pn instanceof PstCompositeNode) {
            for (PstNode n : ((PstCompositeNode)pn).getChildren()) {
                collectLeafs(n);
            }
        } else if (pn instanceof PstLeafNode) {
            leafsNodes.add((PstLeafNode)pn);
        }
    }

    
    private Color getEditorBackgroundColor(IPreferenceStore preferenceStore) {
        if (preferenceStore.getBoolean(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT)) {
            return null;
        } else {
            RGB rgb = PreferenceConverter.getColor(preferenceStore, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND);
            return new Color(Display.getDefault(), rgb);
        }
    }

    
    /**
     * pstToTokenMap translates 'IElementType' (token type in Ats terms) 
     *               to 'ModulaTokens' (token type in terms of editor colorer)
     */
    private void initTokenMap() {
        pstToTokenMap = new HashMap<IElementType ,ModulaTokens>();
//TODO:
//        Pragma             (Messages.ModulaTokens_Pragmas,         "Pragmas",         new RGB(0x80, 0, 0x80), SWT.ITALIC),          //$NON-NLS-1$
//        PragmaKeyword      (Messages.ModulaTokens_PragmaKeywords,  "PragmaKeywords",  new RGB(0x80, 0, 0x80), SWT.ITALIC+SWT.BOLD), //$NON-NLS-1$
//        SystemModuleKeyword(Messages.ModulaTokens_SystemModuleKeywords, "SystemModuleKeywords", new RGB(0,    0,    0x80), SWT.BOLD),   //$NON-NLS-1$

        pstToTokenMap.put(ModulaTokenTypes.BLOCK_COMMENT, ModulaTokens.BlockComment);
        pstToTokenMap.put(ModulaTokenTypes.CPP_BLOCK_COMMENT, ModulaTokens.BlockComment);

        pstToTokenMap.put(ModulaTokenTypes.END_OF_LINE_COMMENT     , ModulaTokens.EndOfLineComment);
        pstToTokenMap.put(ModulaTokenTypes.CPP_END_OF_LINE_COMMENT , ModulaTokens.EndOfLineComment);
        
        pstToTokenMap.put(ModulaTokenTypes.DEC_INTEGER_LITERAL  , ModulaTokens.Number);
        pstToTokenMap.put(ModulaTokenTypes.OCT_INTEGER_LITERAL  , ModulaTokens.Number);
        pstToTokenMap.put(ModulaTokenTypes.HEX_INTEGER_LITERAL  , ModulaTokens.Number);
        pstToTokenMap.put(ModulaTokenTypes.REAL_LITERAL         , ModulaTokens.Number);
        pstToTokenMap.put(ModulaTokenTypes.LONG_REAL_LITERAL    , ModulaTokens.Number);
        pstToTokenMap.put(ModulaTokenTypes.COMPLEX_LITERAL      , ModulaTokens.Number);
        pstToTokenMap.put(ModulaTokenTypes.LONG_COMPLEX_LITERAL , ModulaTokens.Number);
        pstToTokenMap.put(ModulaTokenTypes.CHAR_HEX_LITERAL     , ModulaTokens.Number);
        pstToTokenMap.put(ModulaTokenTypes.CHAR_OCT_LITERAL     , ModulaTokens.Number);

        pstToTokenMap.put(ModulaTokenTypes.STRING_LITERAL, ModulaTokens.String);

        pstToTokenMap.put(ModulaTokenTypes.LBRACKET , ModulaTokens.Bracket);
        pstToTokenMap.put(ModulaTokenTypes.RBRACKET , ModulaTokens.Bracket);
        pstToTokenMap.put(ModulaTokenTypes.LPARENTH , ModulaTokens.Bracket);
        pstToTokenMap.put(ModulaTokenTypes.RPARENTH , ModulaTokens.Bracket);
        pstToTokenMap.put(ModulaTokenTypes.LBRACE   , ModulaTokens.Bracket);
        pstToTokenMap.put(ModulaTokenTypes.RBRACE   , ModulaTokens.Bracket);
        
        pstToTokenMap.put(ModulaTokenTypes.AND                , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.AND_KEYWORD        , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.ASM_KEYWORD        , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.ARRAY_KEYWORD      , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.BEGIN_KEYWORD      , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.BY_KEYWORD         , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.CASE_KEYWORD       , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.CONST_KEYWORD      , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.DEFINITION_KEYWORD , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.DIV_KEYWORD        , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.DO_KEYWORD         , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.ELSE_KEYWORD       , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.ELSIF_KEYWORD      , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.END_KEYWORD        , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.EXCEPT_KEYWORD     , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.EXIT_KEYWORD       , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.EXPORT_KEYWORD     , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.FINALLY_KEYWORD    , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.FOR_KEYWORD        , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.FORWARD_KEYWORD    , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.FROM_KEYWORD       , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.IF_KEYWORD         , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.IMPLEMENTATION_KEYWORD, ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.IMPORT_KEYWORD     , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.IN_KEYWORD         , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.IS_KEYWORD         , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.LOOP_KEYWORD       , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.MOD_KEYWORD        , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.MODULE_KEYWORD     , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.NOT_KEYWORD        , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.OF_KEYWORD         , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.OR_KEYWORD         , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.PACKEDSET_KEYWORD  , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.POINTER_KEYWORD    , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.PROCEDURE_KEYWORD  , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.QUALIFIED_KEYWORD  , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.RECORD_KEYWORD     , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.REM_KEYWORD        , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.REPEAT_KEYWORD     , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.RETRY_KEYWORD      , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.RETURN_KEYWORD     , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.SEQ_KEYWORD        , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.SET_KEYWORD        , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.THEN_KEYWORD       , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.TO_KEYWORD         , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.TYPE_KEYWORD       , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.UNTIL_KEYWORD      , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.VAR_KEYWORD        , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.WHILE_KEYWORD      , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.WITH_KEYWORD       , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.LABEL_KEYWORD      , ModulaTokens.Keyword);
        pstToTokenMap.put(ModulaTokenTypes.GOTO_KEYWORD       , ModulaTokens.Keyword);
    }
    
}
