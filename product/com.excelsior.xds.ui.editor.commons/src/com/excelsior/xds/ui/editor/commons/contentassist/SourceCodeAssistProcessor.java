package com.excelsior.xds.ui.editor.commons.contentassist;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

/**
 * Content assist processor to suggest source code completions.
 */
public abstract class SourceCodeAssistProcessor<T extends BaseCompletionContext> implements IContentAssistProcessor {

    protected final List<IContentAssistProcessor> contentAssistProcessors;
    protected final IContentAssistProcessor fastProcessor;
    
    /**
     * @param fastProcessor null or processor to call w/o Ast. If it returns some result(s), 
     *        the next processing is skipped
     * @param contentAssistProcessors list of processors to call when Ast is ready (and was no
     *        results from fastProcessor)
     */
    public SourceCodeAssistProcessor(IContentAssistProcessor fastProcessor, IContentAssistProcessor... contentAssistProcessors) {
        this.fastProcessor = fastProcessor;
        this.contentAssistProcessors = Arrays.asList(contentAssistProcessors);
    }
    
    @SuppressWarnings("unchecked")
	protected void doComputeFastCompletionProposals(T context, List<ICompletionProposal> completionProposals) {
        if (fastProcessor != null) {
            if (fastProcessor instanceof ICompletionContextUser) {
                // 'context' here may be based on bad Ast
                ((ICompletionContextUser<T>)fastProcessor).setCompletionContext(context);
            }
            ICompletionProposal[] cp = fastProcessor.computeCompletionProposals(context.getViewer(), context.getOffset());
            CollectionUtils.addAll(completionProposals, cp);
        }
    }
    
    @SuppressWarnings("unchecked")
	protected void doComputeCompletionProposals(T context, List<ICompletionProposal> completionProposals) {
    	for (IContentAssistProcessor contentAssistProcessor : contentAssistProcessors) {
    		if (contentAssistProcessor instanceof ICompletionContextUser) {
    			((ICompletionContextUser<T>)contentAssistProcessor).setCompletionContext(context);
    		}
    		ICompletionProposal[] cp = contentAssistProcessor.computeCompletionProposals(context.getViewer(), context.getOffset());
    		CollectionUtils.addAll(completionProposals, cp);
    	}
    }
    
    /**
     * {@inheritDoc}
     */
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
            return null;
    }

    /**
     * {@inheritDoc}
     */
    public char[] getCompletionProposalAutoActivationCharacters() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public char[] getContextInformationAutoActivationCharacters() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IContextInformationValidator getContextInformationValidator() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getErrorMessage() {
        return null;
    }
}
