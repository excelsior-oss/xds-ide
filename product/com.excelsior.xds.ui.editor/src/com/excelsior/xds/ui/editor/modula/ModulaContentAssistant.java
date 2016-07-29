package com.excelsior.xds.ui.editor.modula;

import java.lang.reflect.Method;
import java.util.Objects;

import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.excelsior.xds.core.utils.ReflectionUtils;

public class ModulaContentAssistant extends ContentAssistant {
	private ICompletionProposal lastSelectedProposal;
	private boolean isAssistSessionRestarted;
	private final ISourceViewer sourceViewer;

	public ModulaContentAssistant(ISourceViewer sourceViewer) {
		this.sourceViewer = sourceViewer;
		addCompletionListener(new CompletionListener());
	}
	
	public ISourceViewer getSourceViewer() {
		return sourceViewer;
	}

	@Override
	public boolean isProposalPopupActive() {
		return super.isProposalPopupActive();
	}
	
	public boolean isAssistSessionRestarted() {
		return isAssistSessionRestarted;
	}
	
	public String showPossibleCompletions(boolean isRestarted) {
		isAssistSessionRestarted = isRestarted;
		
		ICompletionProposal targetProposal = lastSelectedProposal;
        String result = super.showPossibleCompletions();
        if (isRestarted) {
        	selectProposal(targetProposal);
        }
        isAssistSessionRestarted = false;
		return result;
	}

	private void selectProposal(ICompletionProposal proposal) {
		try{
			Object fProposalPopup = ReflectionUtils.getField(this.getClass(), "fProposalPopup", this, true);
			Object fProposalTable = ReflectionUtils.getField(fProposalPopup.getClass(), "fProposalTable", fProposalPopup, true);
			if (fProposalTable instanceof Table) {
				Table table = (Table) fProposalTable;
				int i = 0; 
				for (;i < table.getItemCount(); i++) {
					TableItem item = table.getItem(i);
					if (Objects.equals(item.getData(), proposal)) {
						break;
					}
				}
				if (i < table.getItemCount()) {
					Method selectMethod = ReflectionUtils.findMethod(fProposalPopup.getClass(), "selectProposal", int.class, boolean.class);
					ReflectionUtils.invokeMethod(selectMethod, fProposalPopup, i, false);
				}
			}
		}
		catch(AssertionError e) {
		}
	}
    
	private final class CompletionListener implements
			ICompletionListener {
		@Override
		public void selectionChanged(ICompletionProposal proposal,
				boolean smartToggle) {
			lastSelectedProposal = proposal;
		}
	
		@Override
		public void assistSessionStarted(ContentAssistEvent event) {
		}
	
		@Override
		public void assistSessionEnded(ContentAssistEvent event) {
			lastSelectedProposal = null;
		}
	}
}
