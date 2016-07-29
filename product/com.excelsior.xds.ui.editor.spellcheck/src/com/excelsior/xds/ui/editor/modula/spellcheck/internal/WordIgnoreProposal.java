/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.excelsior.xds.ui.editor.modula.spellcheck.internal;

import java.text.MessageFormat;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.texteditor.spelling.SpellingProblem;

import com.excelsior.xds.ui.editor.modula.spellcheck.internal.engine.ISpellCheckEngine;
import com.excelsior.xds.ui.editor.modula.spellcheck.internal.engine.ISpellChecker;
import com.excelsior.xds.ui.editor.modula.spellcheck.internal.nls.Messages;
import com.excelsior.xds.ui.images.ImageUtils;

/**
 * Proposal to ignore the word during the current editing session.
 *
 * @since 3.0
 */
public class WordIgnoreProposal implements ICompletionProposal {

	/** The invocation context */
	private IQuickAssistInvocationContext fContext;

	/** The word to ignore */
	private String fWord;

	/**
	 * Creates a new spell ignore proposal.
	 *
	 * @param word
	 *                   The word to ignore
	 * @param context
	 *                   The invocation context
	 */
	public WordIgnoreProposal(final String word, final IQuickAssistInvocationContext context) {
		fWord= word;
		fContext= context;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#apply(org.eclipse.jface.text.IDocument)
	 */
	public final void apply(final IDocument document) {

		final ISpellCheckEngine engine= SpellCheckEngine.getInstance();
		final ISpellChecker checker= engine.getSpellChecker();

		if (checker != null) {
			checker.ignoreWord(fWord);
			ISourceViewer sourceViewer= fContext.getSourceViewer();
			if (sourceViewer != null)
				SpellingProblem.removeAll(sourceViewer, fWord);
		}
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getAdditionalProposalInfo()
	 */
	public String getAdditionalProposalInfo() {
		return MessageFormat.format(Messages.Spelling_ignore_info, new Object[] { WordCorrectionProposal.getHtmlRepresentation(fWord)});
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getContextInformation()
	 */
	public final IContextInformation getContextInformation() {
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getDisplayString()
	 */
	public String getDisplayString() {
		return MessageFormat.format(Messages.Spelling_ignore_label, new Object[] { fWord });
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getImage()
	 */
	public Image getImage() {
		return ImageUtils.getImage(ImageUtils.CORRECTION_TURN_OFF);
	}
	/*
	 * @see org.eclipse.jdt.ui.text.java.IJavaCompletionProposal#getRelevance()
	 */
	public final int getRelevance() {
		return Integer.MIN_VALUE + 1;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getSelection(org.eclipse.jface.text.IDocument)
	 */
	public final Point getSelection(final IDocument document) {
		return new Point(fContext.getOffset(), fContext.getLength());
	}
}
