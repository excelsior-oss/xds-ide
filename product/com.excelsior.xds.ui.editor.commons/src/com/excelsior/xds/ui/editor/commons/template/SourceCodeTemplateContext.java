/*
 * Modification date: $date$
 *
 * Copyright (C) José David Moreno Juárez
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.excelsior.xds.ui.editor.commons.template;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateTranslator;
import org.eclipse.jface.text.templates.TemplateVariable;

import com.excelsior.xds.core.log.LogHelper;

/**
 * Context for source code templates.
 * 
 * @see org.eclipse.jface.text.templates.TemplateContext
 */
public class SourceCodeTemplateContext extends DocumentTemplateContext {
    
    private String templateBody;
    private String selShift;
    private int    endBackOffset; // end of the affected area == (document_length - position_after_affected_area)
    private boolean multilineSel;
    private String[] indents;
    private boolean cutTemplateCRLF;
	
	/**
	 * Creates a new <code>Modula2TemplateContext</code>.
	 * 
	 * @param type             template context type
	 * @param document         document of the context
	 * @param offset	       offset of the context (1st affected position)
	 * @param length	       length of the context
     * @param selShift         string to use for indentation when there was multiline selection ( {' '|'\t'}* )
     * @param docSize          initial document size 
     * @param multilineSel     true when selection is multiline
     * @param indents[]        [0] - indent, [1] indent for 1st line when selection is singleline
     * @param cutTemplateCRLF  cut last CRLF if any
     * 
	 */
	public SourceCodeTemplateContext(TemplateContextType type, IDocument document, int offset, int length, String selShift, int docSize, 
	        boolean multilineSel, String indents[], boolean cutTemplateCRLF) {
		super(type, document, offset, length);
		this.selShift = selShift;
		this.endBackOffset = docSize - (offset + length);
		this.multilineSel = multilineSel;
		this.indents = indents;
		this.cutTemplateCRLF = cutTemplateCRLF;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.templates.DocumentTemplateContext#evaluate(org.eclipse.jface.text.templates.Template)
	 */
	@Override
	public TemplateBuffer evaluate(Template template) throws BadLocationException, TemplateException {

	    TemplateTranslator translator= new TemplateTranslator();
        TemplateBuffer buffer= translator.translate(template);
        
        templateBody = buffer.getString(); // required to process multiline 'line_selection' 

        getContextType().resolve(buffer, this);

		getIndentation();

		///* Indents the variables */
		//TemplateVariable[] variables = indentVariableOffsets(buffer, indentation.length());
		TemplateVariable[] variables = buffer.getVariables();
		
		/* Indents the template */
		String formattedTemplate = doIndent(buffer.getString(), variables, indents); 
		
		if (cutTemplateCRLF) {
		    if (formattedTemplate.endsWith("\r\n")) {
		        formattedTemplate = formattedTemplate.substring(0, formattedTemplate.length()-2);
		    } else if (formattedTemplate.endsWith("\n")) {
                formattedTemplate = formattedTemplate.substring(0, formattedTemplate.length()-1);
		    }
		}
		
		buffer.setContent(formattedTemplate, variables);
		
		return buffer;
	}

	/**
	 * Returns the indentation string (tabs and spaces) from the insertion point line.
	 * 
	 * @return	the indentation string
	 */
	private String getIndentation() {
		StringBuilder res = new StringBuilder();
		IDocument document= getDocument();
		try {
			IRegion region= document.getLineInformationOfOffset(getStart());
			String lineContent= document.get(region.getOffset(), region.getLength());
			for (int i=0; i<lineContent.length(); ++i) {
			    char ch = lineContent.charAt(i);
			    if (ch == ' ' || ch == '\t') {
			        res.append(ch);
			    } else {
			        break;
			    }
			}
		} catch (BadLocationException e) {
			LogHelper.logError(e);
		}
		return res.toString();
	}

	/**
	 * Inserts indentation chars to the string according to the current
	 * indentation level.
	 * 
	 * @param string           string to indent
	 * @param indentation      current indentation string (with tabs and spaces)
	 * @return	the indented string
	 */
	private String doIndent(String string, TemplateVariable[] variables, String[] indentations) {
		//int stringLength = string.length();
		//final char lastEolChar = System.getProperty("line.separator").charAt(System.getProperty("line.separator").length()-1); //NON-NLS-1 //$NON-NLS-2$
		StringBuilder sb = new StringBuilder();
		String line;

		//dumpVars4dbg(string, variables);

		int resPos = 0;
		boolean line1 = true; // true for intial empty lenes and for 1st non-empty line 
		for (int pos=0; !(line = nextLine(string, pos)).isEmpty(); ) {
		    int cx;
		    int p = line.indexOf(BaseTemplateCompletionProcessor.INDENT_MAGIC);
		    if (p < 0) {
		        if (multilineSel || !line1) {
		            sb.append(indentations[0]);
	                cx = indentations[0].length();
		        } else {
                    sb.append(indentations[1]);
                    cx = indentations[1].length();
		        }
		        sb.append(line);
		    } else {
		        if (p>0) {
		            // it is indent from template before 'line_selection':
		            sb.append(line.substring(0,p));
		            line = line.substring(p);
		            pos +=p;
		            resPos += p;
		            p = 0;
		        }
		        // drop INDENT_MAGIC:
		        cx = -BaseTemplateCompletionProcessor.INDENT_MAGIC.length();
		        sb.append(line.substring(-cx));
		    }
		    // move affected variables offsets:
            //System.out.println(String.format("** move: %d at resPos %d (pos %d)", cx, resPos, pos));
		    for (TemplateVariable v : variables) {
	            int[] offsets = v.getOffsets();
	            for (int k = 0; k < offsets.length; k++) {
	                if (cx > 0) {
                        if (offsets[k] >= resPos) {
                            //System.out.println(String.format("++ %s[%d] += %d", v.getName(), offsets[k], cx));
                            offsets[k] += cx;
                        }
	                } else {
                        if (offsets[k] >= resPos - cx) {
                            //System.out.println(String.format("-- %s[%d] += %d", v.getName(), offsets[k], cx));
                            offsets[k] += cx;
                        }
	                }
	            }
	            v.setOffsets(offsets);
		    }
		    pos    += line.length();
		    resPos += line.length()+cx;
		    for (char ch : line.toCharArray()) {
		        if (ch != ' ' && ch != '\t') {
		            line1=false;
		            break;
		        }
		    }
		}
        //dumpVars4dbg(sb.toString(), variables);
		return sb.toString();
	}
	
//	private static void dumpVars4dbg(String s, TemplateVariable[] variables) {
//	    System.out.println("----------");
//        for (TemplateVariable v : variables) {
//            System.out.println("Name='" + v.getName() + "'");
//            int[] offsets = v.getOffsets();
//            int len = v.getLength();
//            for (int k = 0; k < offsets.length; k++) {
//                if (offsets[k] > s.length()) {
//                    System.out.println(" Offs out of str - " + offsets[k]);
//                } else if (offsets[k] + len > s.length()) {
//                    System.out.println(" Offs+len out of str - " + offsets[k] + " " + len);
//                    if (offsets[k] < s.length()) {
//                        System.out.println(" Val[..eol] - '" + s.substring(offsets[k]) + "'");
//                    }
//                } else {
//                    System.out.println(" Offs/len - " + offsets[k] + "/" + len);
//                    System.out.println(" Val - '" + s.substring(offsets[k], offsets[k]+len) + "'");
//                }
//            }
//        }
//	}
	
	private static String nextLine(String str, int pos) {
	    StringBuilder sb = new StringBuilder();
	    boolean wait0xA = false;
	    while(pos < str.length()) {
	        char ch = str.charAt(pos++);
	        if (wait0xA && ch!=0xA) {
	            break; // single 0xD is a line delimiter
	        }
	        sb.append(ch);
            if (ch == 0xD) {
                wait0xA = true;
            } else if (ch == 0xA) {
                break;
            }
	    }
	    return sb.toString();
	}
	
    /**
     * Shifts the variable offsets according to the current indentation level.
     * 
     * @param buffer                template buffer
     * @param currentIndentation    current indentation level
     * @return  the variable offsets according to the current indentation level
     */
    @SuppressWarnings("unused")
	private static TemplateVariable[] indentVariableOffsets(TemplateBuffer buffer, int currentIndentation) {
        String content = buffer.getString();
        TemplateVariable[] variables = buffer.getVariables();
        List<Integer> lineBreaks     = getLinebreaksList(content);
        int line;
        for (int j = 0; j < variables.length; j++) {
            int[] offsets = variables[j].getOffsets();

            for (int k = 0; k < offsets.length; k++) {

                line = getLineOfOffset(offsets[k], lineBreaks);
                
                offsets[k] = offsets[k] + (line * currentIndentation);
            }
            variables[j].setOffsets(offsets);
        }
        
        return variables;
    }
    
    /**
     * Returns a list with the positions where the lines ends (with total string length as a last element).
     * (i.e.: for "ABC\nDEF" it returns [3, 7], for "\nA\nB\n" it returns [0, 2, 4, 5])
     * @param string    string to search for line offsets
     * @return  a list with the positions where the lines of the string start
     */
    private static List<Integer> getLinebreaksList(String string) {
        final char lastEolChar = System.getProperty("line.separator").charAt(System.getProperty("line.separator").length()-1); //$NON-NLS-1$ //$NON-NLS-2$

        List<Integer> lineOffsetsList = new ArrayList<Integer>();
        int len = string.length();
        for (int i=0; i<len; ++i) {
            if (string.charAt(i) == lastEolChar) {
                lineOffsetsList.add(i);
            }
        }
        lineOffsetsList.add(len);
        return lineOffsetsList;
    }
	/**
	 * Returns the line number where the offset is.
	 * 
	 * @param offset		offset
	 * @param lineBreaks	offsets of linebreaks
	 * @return	the line number (one-based) where the offset is
	 */
	private static int getLineOfOffset(int offset, List<Integer> lineBreaks) {
		int len = lineBreaks.size();
		for (int i=0; i<len; ++i) {
		    if (offset < lineBreaks.get(i)) {
		        return i;
		    }
		}
		return len-1; // int. error?
	}
	
	@Override
    public int getStart() {
	    if (getCompletionLength() == 0) { // initial selection len. if present - don't use prefix
            try {
                IDocument document= getDocument();
    
                int start= getCompletionOffset();
                int end= getCompletionOffset() + getCompletionLength();
    
                while (start != 0 && Character.isUnicodeIdentifierPart(document.getChar(start - 1)))
                    start--;
    
                while (start != end && Character.isWhitespace(document.getChar(start)))
                    start++;
    
                if (start == end)
                    start= getCompletionOffset();
    
                    return start;
    
            } catch (BadLocationException e) {
            }
	    }
	    int start = super.getStart(); 
        //TplDbg.println("super.getStart() = " + start);
        return start;
    }
	
	public String getTemplateBody() {
	    return templateBody;
	}

	public String getSelShift() {
        return selShift;
    }
	
	public int getEndBackOffset() {
	    // end of the affected area == (document_length - position_after_affected_area)
	    return endBackOffset;
	}
	
    public int getEnd() {
        // Document may de changed inside affected area but we can determine end of affected area: 
        return getDocument().getLength() - endBackOffset;
    }
    
    public void setVariable(String name, String value) {
        super.setVariable(name, value);
        if (value.isEmpty()) {
            return;
        }
    }

    public String getVariable(String name) {
        String v = super.getVariable(name);
        return v;
    }
}
