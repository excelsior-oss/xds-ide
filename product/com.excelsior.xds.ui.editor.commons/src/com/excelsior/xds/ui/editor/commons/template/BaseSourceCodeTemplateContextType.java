package com.excelsior.xds.ui.editor.commons.template;

import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateVariable;

import com.excelsior.xds.ui.editor.commons.template.BaseTemplateCompletionProcessor;
import com.excelsior.xds.ui.editor.commons.template.SourceCodeTemplateContext;

/**
 * A context type defines a context within which source code templates are resolved.
 */
public abstract class BaseSourceCodeTemplateContextType extends TemplateContextType {
    public BaseSourceCodeTemplateContextType() {
        addResolver(new GlobalTemplateVariables.Date());
        addResolver(new GlobalTemplateVariables.Cursor());
        addResolver(new GlobalTemplateVariables.Dollar());
        addResolver(new GlobalTemplateVariables.LineSelection());
        addResolver(new GlobalTemplateVariables.User());
        addResolver(new GlobalTemplateVariables.WordSelection());
        addResolver(new GlobalTemplateVariables.Year());
        addResolver(new GlobalTemplateVariables.Time());
    }
    
    @Override
    public void resolve(TemplateVariable variable, TemplateContext context) {
        try { // check nothing, all exceptions causes standard resolve() call
            if (GlobalTemplateVariables.LineSelection.NAME.equals(variable.getType())) {
                SourceCodeTemplateContext scontext = (SourceCodeTemplateContext)context;
                String templateBody = scontext.getTemplateBody();
                StringBuilder sbInd = new StringBuilder();
                int voffs = variable.getOffsets()[0];
                for (int i=0; i<voffs; ++i) {
                    char ch = templateBody.charAt(i);
                    if (ch == '\n' || ch == '\r') {
                        sbInd.setLength(0);
                    } else if (ch == ' ' || ch == '\t') {
                        sbInd.append(ch);
                    }
                }
                
                // Indent inside template before 'line_selection' (or "" when smth wrong):
                String indent = sbInd.toString(); 

                // When template contains <line_selection>';' or <line_selection><cursor>';' and
                // 'line_selection' value ends with ';' this last ';' in the value will be suppressed: 
                boolean suppressDup = templateBody.substring(voffs).startsWith(GlobalTemplateVariables.LineSelection.NAME + ';')
                                   || templateBody.substring(voffs).startsWith(GlobalTemplateVariables.LineSelection.NAME + 
                                                                               GlobalTemplateVariables.Cursor.NAME + ';');

                // Usual 'line_selection' value:
                String selection= context.getVariable(GlobalTemplateVariables.SELECTION);
                
                // Suppress ';;' if need:
                if (suppressDup && selection.endsWith(";")) { //$NON-NLS-1$
                    selection = selection.substring(0, selection.length()-1);
                }

                boolean mlsel = (selection.contains("\r") || selection.contains("\n")); //$NON-NLS-1$ //$NON-NLS-2$
                
                // When 'indent' is not empty and selection is miltiline turn ON artificial intelligence:
                // Lines in selection consists of some <l_indent> + <l_body>. Replace 1st line with
                //   <INDENT_MAGIC> + <l_indent> + <l_body>
                // and each next line with 
                //   <INDENT_MAGIC> + <l_indent> + <indent> + <l_body>
                // (INDENT_MAGIC indicates that this line shouldn't be indented when template
                // will be inserted into the final document)
                if (!indent.isEmpty() && mlsel) {
                    StringBuilder sb = new StringBuilder();
                    int state = 0;
                    boolean line1 = true;
                    for (int i=0; i<selection.length(); ++i) {
                        char ch = selection.charAt(i);
                        if (ch == '\n' || ch == '\r') {
                            state = 0;
                            line1 = false;
                        } else if (state == 0) {
                            sb.append(BaseTemplateCompletionProcessor.INDENT_MAGIC);
                            state =1;
                        } 
                        if (ch != ' ' && ch != '\t' && state == 1) {
                            if (!line1) sb.append(indent);
                            state = 2;
                        }
                        sb.append(ch);
                    }
                    selection = sb.toString();
                } else if (!mlsel) {
                    int i=0;
                    for (; i<selection.length(); ++i) {
                        char ch = selection.charAt(i);
                        if (ch != ' ' && ch != '\t') break;
                    }
                    selection = selection.substring(i); // single-line selection: trim first spaces to hard indent it
                }
                
                variable.setValues(new String[]{selection});
                variable.setUnambiguous(true);
                variable.setResolved(true);
                return;
            }
        } catch (Exception e) {}
        super.resolve(variable, context);
    }

    
}
