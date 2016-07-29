package com.excelsior.xds.ui.editor.commons.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionListenerExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.ui.images.ImageUtils;

/**
 * Completion processor for source code templates. 
 */
public abstract class BaseTemplateCompletionProcessor extends TemplateCompletionProcessor
{
	public static final String INDENT_MAGIC = "INDENT_MAGIC_327459045190"; //$NON-NLS-1$
	
    private final int RELEVANCE_HIGH = 90;
    private final int RELEVANCE_LOW  = 0;

	private Image templateImage = ImageUtils.getImage(ImageUtils.TEMPLATE_IMAGE_NAME);
	protected ContentAssistant contentAssistant;
	
	public boolean isAutoActivated;
	
	/**
	 * Create the instance of completion processor that computes template proposals
	 * for given template context type.
	 * @param contentAssistant 
	 * 
	 * @param contextTypeId the id of the template context type
	 */
	public BaseTemplateCompletionProcessor(ContentAssistant contentAssistant) {
	    this.contentAssistant = contentAssistant;
	    this.contentAssistant.addCompletionListener(new CompletionListener());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.templates.TemplateCompletionProcessor#getImage(org.eclipse.jface.text.templates.Template)
	 */
	@Override
	protected Image getImage(Template template) {
		return templateImage;
	}
	
	@Override
	protected int getRelevance(Template template, String prefix) {
        if (template.getName().toLowerCase().startsWith(prefix.toLowerCase())) {
            return RELEVANCE_HIGH;
        }
        return RELEVANCE_LOW;
    }

	protected void doComputeCompletionProposals(ITextViewer viewer, int offset, List<ICompletionProposal> proposals) {
	    boolean usePrefix;
	    int selOffset;
	    int selLength;

        // For multiline selection: selection will be extended to whole lines but inserted template should be indented to
        // selection start position. 'selShift' is the string to be inserted before selection lines to indent them: it
        // requires to preserve tabs/spaces sequence at the start of the string:
        String selShift = "";
        int docSize     = 0;
        boolean multilineSel = false;
        String indents[] = {"",""};
        boolean cutTemplateCRLF = false;

        String selText;
        {
            ITextSelection selection= (ITextSelection) viewer.getSelectionProvider().getSelection();

            // When typed prefix is used (== selection is empty) only matched to this prefix proposals
            // should be returned.
            // (the prefix will be deleted when template will be applied, and non-matched prefix too):
            usePrefix = (selection.getLength() == 0);
            
            selOffset = selection.getOffset();
            selLength = selection.getLength();
            selText   = selection.getText();
            
            try {
                IDocument document= viewer.getDocument();
                docSize = document.getLength();
                int selStartLine = selection.getStartLine();
                int selEndLine = selection.getEndLine();
                if (selEndLine > selStartLine) {
                    // for multilite selection extend selection to WHOLE lines
                    multilineSel = true;
                    StringBuilder sb = new StringBuilder();
                    int selOffs = 0;
                    int selLen = 0;
                    for (int i=selStartLine; i<=selEndLine; ++i) {
                        IRegion r = document.getLineInformation(i);
                        String ld = document.getLineDelimiter(i);
                        String docstr = document.get(r.getOffset(), r.getLength());
                        if (i == selStartLine) {
                            indents = getIndentations(docstr, selection.getOffset() - r.getOffset());
                        }
                        sb.append(docstr);
                        if (ld != null) {
                            sb.append(ld);  
                        }
                        if (i == selStartLine) {
                            selOffs = r.getOffset();
                            for (int p=selOffs; p<selOffset; ++p) {
                                char ch = docstr.length()>(p-selOffs) ? docstr.charAt(p-selOffs) : ' ';
                                if (ch != ' ' && ch !='\t') { // selection was started after text begins - indent anyway using spaces
                                    ch = ' ';
                                }
                                selShift += ch;
                            }
                        }
                        if (i == selEndLine) {
                            selLen = r.getOffset() + r.getLength() - selOffs;
                            if (ld != null) {
                                selLen += ld.length();
                            }
                        }
                    } // for
                    selText = sb.toString();
                    selOffset = selOffs;
                    selLength = selLen;
                } else {
                    IRegion r = document.getLineInformation(selEndLine);
                    String docstr = document.get(r.getOffset(), r.getLength());

                    indents = getIndentations(docstr, selection.getOffset() - r.getOffset());

                    // When template ended with CRLF is inserted into single line
                    // and this line has nothing but CRLF after insetrion region then
                    // it will produce udly empty line after insertion. 
                    // Set 'cutTemplateCRLF' in this case:
                    int e = selOffset + selLength - r.getOffset(); // selection end pos in the line
                    cutTemplateCRLF = true;
                    if (e > r.getLength()) {
                        cutTemplateCRLF = false; // selection was up to selEndLine+1:pos=0, its CRLF will be deleted 
                    } else if (e > 0 && e < r.getLength()) {
                        for (; e<r.getLength(); ++e) {
                            char ch = docstr.charAt(e);
                            if (ch != ' ' && ch != '\t') {
                                cutTemplateCRLF = false;
                                break;
                            }
                        }
                    }
                }
                
                // cut last CRLF from selText if any (for single-line selections too): 
                String ld = document.getLineDelimiter(selEndLine);
                if (ld != null && selText.endsWith(ld)) {
                    selText = selText.substring(0, selText.length() - ld.length());
                }

                
            } catch (Exception e) {
                LogHelper.logError(e);
                return;
            }
            
            //viewer.getSelectionProvider().setSelection(new TextSelection(viewer.getDocument(), selOffset, 0)); // deselect all
            //viewer.getTextWidget().setCaretOffset(selOffset);

            // Now: use as selection document from 'selOffset' to 'selOffset + selLength', 'selText' contains this text fragment  
        }

        String prefix= "";
        if (usePrefix) {
            prefix= extractPrefix(viewer, offset);
        }
        Region region= new Region(offset - prefix.length(), prefix.length());
        
        TemplateContext context = usePrefix ?
                new SourceCodeTemplateContext(getContextType(viewer, region), viewer.getDocument(), region.getOffset(), region.getLength(), "",       docSize, multilineSel, indents, cutTemplateCRLF) :
                new SourceCodeTemplateContext(getContextType(viewer, region), viewer.getDocument(), selOffset,          selLength,          selShift, docSize, multilineSel, indents, cutTemplateCRLF);

        context.setVariable(GlobalTemplateVariables.SELECTION, selText); // name of the selection variables {line, word}_selection //$NON-NLS-1$

        Template[] templates= getTemplates(context.getContextType().getId());

        List<ICompletionProposal> matches= new ArrayList<ICompletionProposal>();
        for (int i= 0; i < templates.length; i++) {
            Template template= templates[i];
            if (!isEnabled(viewer, offset, template)) {
            	continue;
            }
            try {
                context.getContextType().validate(template.getPattern());
            } catch (TemplateException e) {
                continue;
            }
            if (template.matches(prefix, context.getContextType().getId())) {
                ICompletionProposal t = createProposal(template, context, (IRegion) region, getRelevance(template, prefix));
                if (!usePrefix || ((TemplateProposal)t).getRelevance() == RELEVANCE_HIGH) {
                    matches.add(t);
                }
            }
        }

        Collections.sort(matches,  new Comparator<ICompletionProposal>() {
            public int compare(ICompletionProposal t1, ICompletionProposal t2) {
                    return ((TemplateProposal)t2).getRelevance() - ((TemplateProposal)t1).getRelevance();
                }
            });

        proposals.addAll(matches);
    }
	
	/**
	 * Subclasses override to decide, whether the given template is enabled at offset.
	 * @param viewer
	 * @param offset
	 * @param template
	 * @return
	 */
	protected abstract boolean isEnabled(ITextViewer viewer, int offset, Template template);

    /**
     * Returns the indentation strings (withtabs and spaces).
     * [0] - It is tabs and spaces from start of 'str' up to 1st non-blank char
     * [1] - It is tabs and spaces from 'pos' to 1st non-blank char (or "") - it is 
     *       used to indent 1st insertion string in case of single-line selection
     * 
     * @return  the indentation strings
     */
    private String[] getIndentations(String str, int pos) {
        if (pos < 0) pos = 0;
        if (pos > str.length()) pos = str.length();
        int len = str.length();
        
        String res[] = {"", ""};
        for (int i=0; i<len; ++i) {
            char ch = str.charAt(i);
            if (ch == ' ' || ch == '\t') {
                res[0] += ch;
                if (i >= pos) {
                    res[1] += ch;
                }
            } else {
                break;
            }
        }
        return res;
    }


	protected ICompletionProposal createProposal(Template template, TemplateContext context, IRegion region, int relevance) {
        return new TemplateProposalExtension(template, context, region, getImage(template), relevance);
    }
	
	class TemplateProposalExtension extends TemplateProposal implements ICompletionProposalExtension2, ICompletionProposalExtension4 {
	    
        //XXX uncomment it to disable template animation near templat eselection dialog
//	    public String getAdditionalProposalInfo() { 
//            return null;
//        }

	    public TemplateProposalExtension(Template template,
                TemplateContext context, IRegion region, Image image,
                int relevance) {
            super(template, context, region, image, relevance);
        }

        public TemplateProposalExtension(Template template,
                TemplateContext context, IRegion region, Image image) {
            super(template, context, region, image);
        }

        @Override
        public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
            super.apply(viewer, trigger, stateMask, offset);
        }

        protected ITextSelection getTextSelection() {
            ITextSelection textSelection = null;
            IEditorPart editorPart = PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow().getActivePage().getActiveEditor();
            if (editorPart instanceof ITextEditor) {
                ITextEditor textEditor = (ITextEditor) editorPart;
                ISelection selection = textEditor.getSelectionProvider()
                        .getSelection();
                if (selection instanceof ITextSelection) {
                    textSelection = (ITextSelection) selection;
                }
            }
            return textSelection;
        }
        
        public boolean validate(IDocument document, int offset, DocumentEvent event) {
            try {
                int replaceOffset= getReplaceOffset();
                if (offset >= replaceOffset) {
                    String content= document.get(replaceOffset, offset - replaceOffset);
                    int blankSpace = -1;
                    while(++blankSpace < content.length()) {
                        char c = content.charAt(blankSpace);
                        if (c != ' ' && c != '\t') {
                            break;
                        }
                    }
                    content = content.substring(blankSpace);
                    return false;
//                    return this.getTemplate().getName().toLowerCase().startsWith(content.toLowerCase());
                }
            } catch (BadLocationException e) {
                // concurrent modification - ignore
            }
            return false;
        }

        @Override
        public boolean isAutoInsertable() {
            return false;
        }

	}
	
	private final class CompletionListener implements ICompletionListener, ICompletionListenerExtension  {
		@Override
		public void selectionChanged(ICompletionProposal proposal,
				boolean smartToggle) {
		}

		@Override
		public void assistSessionStarted(ContentAssistEvent event) {
			isAutoActivated = event.isAutoActivated;
		}

		@Override
		public void assistSessionEnded(ContentAssistEvent event) {
		}

		@Override
		public void assistSessionRestarted(ContentAssistEvent event) {
		}
	}
}
