package com.excelsior.xds.ui.editor.modula.contentassist2;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import com.excelsior.xds.core.utils.JavaUtils;
import com.excelsior.xds.parser.commons.ast.TokenType;
import com.excelsior.xds.parser.commons.symbol.ISymbol;
import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenTypes;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbolScope;
import com.excelsior.xds.parser.modula.symbol.IModuleAliasSymbol;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IArrayTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IPointerTypeSymbol;

class SymbolResolver {
	static IModulaSymbol resolve(CompletionContext context, IDocument doc, String line, IModulaSymbolScope parentScope, List<Token> tokens, int tokenIdx) throws BadLocationException {
		if (tokens.isEmpty() || tokenIdx < 0) {
			return null;
		}
		
		boolean inIndex = false;
		boolean waitForIdentifier = false;
		@SuppressWarnings("unused")
		boolean wrong = false;
		
		Deque<TokenType> stack = new ArrayDeque<>();
		
		List<Function<ISymbol, ISymbol>> opers = new ArrayList<>();
		
		ISymbol currentSymbol = new SymbolScopeWrapper(parentScope);
		
		waitForIdentifier = context.isDotBeforeCursor();
		
		do{
			Token t = tokens.get(tokenIdx--);
			TokenType token = t.tokenType;
			if (ModulaTokenTypes.WHITE_SPACE == token) {
                continue;
            }
			
			if (ModulaTokenTypes.RBRACKET == token) {
				inIndex = true;
				stack.push(ModulaTokenTypes.RBRACKET);
			}
			else if (ModulaTokenTypes.LBRACKET == token) {
				if (inIndex && !stack.isEmpty()) {
					if (stack.peek() == ModulaTokenTypes.RBRACKET) {
						stack.pop();
						if (stack.isEmpty()) {
							inIndex = false;
							opers.add(ArrayIndex.INSTANCE);
						}
					}
					else {
						wrong = true;
						break;
					}
				}
			}
			else if (ModulaTokenTypes.RPARENTH == token) {
				if (inIndex) {
					stack.push(ModulaTokenTypes.RPARENTH);
				}
				else {
					wrong = true;
					break;
				}
			}
			else if (ModulaTokenTypes.LPARENTH == token) {
				if (inIndex && stack.peek() == ModulaTokenTypes.RPARENTH) {
					stack.pop();
				}
				else {
					wrong = true;
					break;
				}
				
			}
			else if (ModulaTokenTypes.COMMA == token) { // [xx , yy]
				if (inIndex) {
					opers.add(ArrayIndex.INSTANCE);
				}
				else {
					wrong = true;
					break;
				}
            }
		    else if (ModulaTokenTypes.BAR == token) { // '^'
		    	if (!inIndex) {
		    		opers.add(Dereference.INSTANCE);
		    	}
		    }
		    else if (ModulaTokenTypes.IDENTIFIER == token) {
		    	if (!inIndex) {
		    		if (waitForIdentifier) {
		    			waitForIdentifier = false;
		    			opers.add(new Resolve(ContentAssistUtils.region(line, t)));
		    		}
		    		else {
		    			wrong = true;
						break;
		    		}
		    	}
		    }
		    else if (ModulaTokenTypes.DOT == token) {
		    	if (!inIndex) {
		    		if (!waitForIdentifier) {
		    			waitForIdentifier = true;
		    		}
		    		else {
		    			wrong = true;
						break;
		    		}
		    	}
		    }
		    else if (!inIndex) {
		    	break;
		    }
		}
		while(tokenIdx > -1 );
		
//		if (wrong) {
//			return null;
//		}
		Collections.reverse(opers);
		
		for (Function<ISymbol, ISymbol> f : opers) {
			currentSymbol = f.apply(currentSymbol);
			if (currentSymbol == null) {
				break;
			}
		}
		
		return JavaUtils.as(IModulaSymbol.class, currentSymbol);
	}
	
	static class Resolve implements Function<ISymbol, ISymbol> {
		private String name;
		
		public Resolve(String name) {
			this.name = name;
		}

		@Override
		public ISymbol apply(ISymbol s) {
			IModulaSymbol result = null;
			IModulaSymbolScope scope = ModulaAssistProcessor2.getScope(s);
			if (scope != null) {
				result = scope.resolveName(name);
			}
			return getTargetSymbol(result);
		}

		@Override
		public String toString() {
			return "Resolve(\"" + name + "\")";
		}
	}
	
	static class ArrayIndex implements Function<ISymbol, ISymbol> {
		public static final ArrayIndex INSTANCE = new ArrayIndex();
		private ArrayIndex() {
		}
		
		@Override
		public IModulaSymbol apply(ISymbol s) {
			s = getTargetSymbol(s);
			IModulaSymbol result = null; 
			if (s instanceof IArrayTypeSymbol) {
				result = ((IArrayTypeSymbol)s).getElementTypeSymbol();
            }
			return result;
		}
	}
	
	static class Dereference implements Function<ISymbol, ISymbol> {
		public static final ArrayIndex INSTANCE = new ArrayIndex();
		private Dereference() {
		}
		
		@Override
		public IModulaSymbol apply(ISymbol s) {
			s = getTargetSymbol(s);
			IModulaSymbol result = null; 
			 if (s instanceof IPointerTypeSymbol) {
				 result = ((IPointerTypeSymbol)s).getBoundTypeSymbol();
             }
			return result;
		}
	}
	
	static IModulaSymbol getTargetSymbol(ISymbol s) {
		if (s instanceof IModuleAliasSymbol) {
			s = ((IModuleAliasSymbol)s).getReference();
        }
		if (s == null || s instanceof IModuleSymbol) {
			return (IModuleSymbol)s;
		}
		return ModulaAssistProcessor2.getTypeSymbol(s);
	}
	
	private static class SymbolScopeWrapper implements ISymbol, IModulaSymbolScope {
		private IModulaSymbolScope wrapped;
		
		public SymbolScopeWrapper(IModulaSymbolScope wrapped) {
			this.wrapped = wrapped;
		}

		@Override
		public IModulaSymbol resolveName(String symbolName) {
			return wrapped.resolveName(symbolName);
		}

		@Override
		public IModulaSymbol findSymbolInScope(String symbolName) {
			return wrapped.findSymbolInScope(symbolName);
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public IModulaSymbolScope getParentScope() {
			return wrapped.getParentScope();
		}

		@Override
		public Iterator<IModulaSymbol> iterator() {
			return wrapped.iterator();
		}

		@Override
		public IModulaSymbol findSymbolInScope(String symbolName,
				boolean isPublic) {
			return wrapped.findSymbolInScope(symbolName, isPublic);
		}
	}
}
