package com.excelsior.xds.ui.editor.modula.template;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.Template;

import com.excelsior.xds.ui.editor.XdsEditorsPlugin;
import com.excelsior.xds.ui.editor.commons.template.BaseTemplateCompletionProcessor;

/**
 * Completion processor for source code templates. 
 */
public abstract class SourceCodeTemplateCompletionProcessor extends BaseTemplateCompletionProcessor
{
	/**
	 * Create the instance of completion processor that computes template proposals
	 * for given template context type.
	 * @param contentAssistant 
	 * 
	 * @param contextTypeId the id of the template context type
	 */
	public SourceCodeTemplateCompletionProcessor(ContentAssistant contentAssistant) {
		super(contentAssistant);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.templates.TemplateCompletionProcessor#getTemplates(java.lang.String)
	 */
	@Override
	protected Template[] getTemplates(String contextTypeId) {
		List<Template> templatesList = new ArrayList<Template>();
		for (Template t : XdsEditorsPlugin.getDefault().getTemplateStore().getTemplates()) {
			if (t.getContextTypeId().equals(contextTypeId)) {
				templatesList.add(t);
			}
		}
		return templatesList.toArray(new Template[0]);
	}

	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
	    if (isAutoActivated) {
	        return new ICompletionProposal[0];
	    }

	    if (!isApplicable()) {
	    	return new ICompletionProposal[0];
	    }
	    
	    List<ICompletionProposal> proposals = new ArrayList<>();
	    doComputeCompletionProposals(viewer, offset, proposals);
	    return proposals.toArray(new ICompletionProposal[0]);
    }
	
	protected abstract boolean isApplicable();
	
	@Override
	protected boolean isEnabled(ITextViewer viewer, int offset,
			Template template) {
		return true;
	}
}
