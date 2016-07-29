package com.excelsior.xds.ui.editor.modula.contentassist2;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.filesystem.IFileStore;

import com.excelsior.xds.core.builders.BuildSettings;
import com.excelsior.xds.parser.commons.IParserEventListener;
import com.excelsior.xds.parser.modula.IXdsParserMonitor;
import com.excelsior.xds.parser.modula.XdsExpressionParser;
import com.excelsior.xds.parser.modula.XdsSettings;
import com.excelsior.xds.parser.modula.XdsSourceType;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.binding.IImportResolver;
import com.excelsior.xds.parser.modula.utils.ModulaFileUtils;

class FromImportStatementParser extends XdsExpressionParser {
	FromImportStatementParser(IFileStore sourceFile, CharSequence chars,
			XdsSettings xdsSettings, IImportResolver importResolver,
			XdsSourceType sourceType, IParserEventListener reporter,
			IXdsParserMonitor monitor) {
		super(sourceFile, chars, xdsSettings, importResolver, reporter,
				monitor);
	}

	static class Statement {
		String moduleName;
		Set<String> symbolNames = new HashSet<>();
	}
	
	static FromImportStatementParser createParser(BuildSettings buildSettings, IModuleSymbol moduleSymbol, CharSequence text) {
		IFileStore sourceFile = moduleSymbol.getSourceFile();
		XdsSourceType sourceType = ModulaFileUtils.getSourceType(sourceFile.getName());
		
		XdsSettings settings = new XdsSettings(buildSettings, sourceType);
		settings.setBuildAst(false);
		FromImportStatementParser parser = new FromImportStatementParser(
				sourceFile, text, settings, null,
				sourceType, null, null);
		
		return parser;
	}
	
	Statement parse() {
		Statement statement = new Statement();
		
		nextToken();
		nextToken();
		
		if (token == IDENTIFIER) {
			statement.moduleName = getTokenText();
			nextToken();
		}
		
		parseToken(IMPORT_KEYWORD);
		
		while (true) {
            if (token == IDENTIFIER) {
            	statement.symbolNames.add(getTokenText());
                nextToken();
            }
            else {
                recoverOnUnexpectedToken(IDENTIFIER);
            }
            
            if (COMMA == token) {
                nextToken();
            } 
            else if (IDENTIFIER == token) {
                errorExpectedSymbol(COMMA);
            } 
            else {
                break;
            }
        }
		
		return statement;
	}
}
