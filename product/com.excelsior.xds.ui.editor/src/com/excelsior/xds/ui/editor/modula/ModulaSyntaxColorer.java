package com.excelsior.xds.ui.editor.modula;

import java.io.IOException;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.parser.commons.ast.TokenType;
import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenSets;
import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenTypes;
import com.excelsior.xds.parser.modula.ast.tokens.PragmaKeywordType;
import com.excelsior.xds.parser.modula.ast.tokens.PragmaTokenType;
import com.excelsior.xds.parser.modula.ast.tokens.PragmaTokenTypes;
import com.excelsior.xds.parser.modula.scanner.jflex._XdsFlexScanner;
import com.excelsior.xds.ui.commons.swt.resources.ResourceRegistry;
import com.excelsior.xds.ui.commons.utils.XStyledString;
import com.excelsior.xds.ui.editor.commons.PersistentTokenDescriptor;
import com.excelsior.xds.ui.editor.commons.RgbStyle;
import com.excelsior.xds.ui.tools.colorers.IModulaSyntaxColorer;

public class ModulaSyntaxColorer implements IModulaSyntaxColorer {
	private final ResourceRegistry resourceRegistry;
	
    public ModulaSyntaxColorer(ResourceRegistry resourceRegistry) {
		this.resourceRegistry = resourceRegistry;
	}
    
    public ModulaSyntaxColorer() {
    	resourceRegistry = new ResourceRegistry(false);
    }
    
    public XStyledString color(String m2text, Map<PersistentTokenDescriptor, RgbStyle> colorConvertationMap) {
        XStyledString ssRes = new XStyledString(resourceRegistry);
        
        _XdsFlexScanner input = new _XdsFlexScanner(); 
        input.reset(m2text);
        
        boolean wasTimes = false;
        boolean inPragma = false;

        while (true) {
            TokenType token;
			try {
				token = input.nextToken();
				ModulaTokens cs = ModulaTokens.Default;
	            ModulaTokens csSetForAll = null;
	            int tokOffs = input.getTokenOffset();
	            int tokLen  = input.yylength();

	            if (ModulaTokenTypes.EOF == token) {
	                break;
	            } else if (token.getClass().equals(PragmaKeywordType.class)) {
	                cs = ModulaTokens.PragmaKeyword;
	            } else if (inPragma) {
	                cs = ModulaTokens.Pragma;
	                if (PragmaTokenTypes.PRAGMA_END == token) {
	                    inPragma = false;
	                }
	            } else if (token.getClass().equals(PragmaTokenType.class)) {
	                cs = ModulaTokens.Pragma;
	                if (PragmaTokenTypes.PRAGMA_BEGIN == token) {
	                    inPragma = true;
	                }
	            } else if (ModulaTokenTypes.RPARENTH == token && wasTimes) {
	                csSetForAll = ModulaTokens.BlockComment; // unexpected "*)" - it all was block comment
	            } else if (ModulaTokenTypes.GTR == token && wasTimes) {
	                csSetForAll = ModulaTokens.Pragma; // unexpected "*>" - it all was pragma
	            } else if (ModulaTokenSets.KEYWORD_SET.contains(token)) {
	                cs = ModulaTokens.Keyword;
	            } else if (ModulaTokenTypes.BLOCK_COMMENT == token) {
	                cs = ModulaTokens.BlockComment;
	            } else if (ModulaTokenTypes.END_OF_LINE_COMMENT == token) {
	                cs = ModulaTokens.EndOfLineComment;
	            } else if (ModulaTokenSets.STRING_LITERAL_SET.contains(token)) {
	                cs = ModulaTokens.String;
	            } else if (ModulaTokenSets.LITERAL_SET.contains(token)) {
	                cs = ModulaTokens.Number;
	            } else if (ModulaTokenSets.BRACKETS_SET.contains(token)) {
	                cs = ModulaTokens.Bracket; 
	            } else if (ModulaTokenTypes.IDENTIFIER == token) {
	                String tokText = m2text.substring(tokOffs, tokOffs + tokLen);
	                if (IModulaKeywords.PERVASIVE_CONSTANTS.contains(tokText)) {
	                    cs = ModulaTokens.BuiltinConstant; // assume that it is keyword too
	                } else if (IModulaKeywords.PERVASIVE_IDENTIFIERS.contains(tokText)) {
	                    cs = ModulaTokens.Keyword; // assume that it is keyword too
	                } else if (IModulaKeywords.SYSTEM_IDENTIFIERS.contains(tokText)) {
		                cs = ModulaTokens.SystemModuleKeyword;
		            }
	            }
	                
	            if (csSetForAll != null) {
	                ssRes = new XStyledString(resourceRegistry);
	                append(ssRes, m2text.substring(0, tokOffs + tokLen), csSetForAll.getToken(), colorConvertationMap);
	            } else {
	                append(ssRes, m2text.substring(tokOffs, tokOffs + tokLen), cs.getToken(), colorConvertationMap);
	            }
	            
	            wasTimes = ModulaTokenTypes.TIMES == token; // '*'
			} catch (IOException e) {
				LogHelper.logError(e);
			}
        }
        return ssRes;
    }
    
    private void append(XStyledString ss, String text, PersistentTokenDescriptor pt, Map<PersistentTokenDescriptor, RgbStyle> colorConvertationMap) {
        if (pt == null) {
            ss.append(text);
        } else {
            if (pt.isDisabled()) {
                pt = ModulaTokens.Default.getToken();
            }

            RGB rgb = pt.getRgbWhenEnabled();
            int style = pt.getStyleWhenEnabled();

            if (colorConvertationMap != null) {
                RgbStyle rs = colorConvertationMap.get(pt);
                if (rs != null) {
                    rgb = rs.rgb;
                    style = rs.style;
                }
            }
            
            Color clr = resourceRegistry.createColor(rgb);
            ss.append(text, clr, style);
        }
    }
    
    
    //--- IFastSyntaxColorer:

    @Override
    public XStyledString color(String m2text) throws Exception {
        return color(m2text, null);
    }
}
