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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.texteditor.spelling.SpellingProblem;

import com.excelsior.xds.core.preferences.PreferenceKey;
import com.excelsior.xds.ui.commons.utils.WorkbenchUtils;
import com.excelsior.xds.ui.editor.modula.spellcheck.internal.engine.ISpellCheckEngine;
import com.excelsior.xds.ui.editor.modula.spellcheck.internal.engine.ISpellChecker;
import com.excelsior.xds.ui.editor.modula.spellcheck.internal.nls.Messages;
import com.excelsior.xds.ui.editor.spellcheck.internal.plugin.SpellcheckPlugin;
import com.excelsior.xds.ui.images.ImageUtils;

/**
 * Proposal to add the unknown word to the dictionaries.
 *
 * @since 3.0
 */
public class AddWordProposal implements ICompletionProposal {

	private static final PreferenceKey PREF_KEY_PREF_KEY_DO_NOT_ASK = 
	        new PreferenceKey(SpellcheckPlugin.PLUGIN_ID, "do_not_ask_to_install_user_dictionary", false); //$NON-NLS-1$

	/** The invocation context */
	private final IQuickAssistInvocationContext fContext;

	/** The word to add */
	private final String fWord;


	/**
	 * Creates a new add word proposal
	 *
	 * @param word
	 *                   The word to add
	 * @param context
	 *                   The invocation context
	 */
	public AddWordProposal(final String word, final IQuickAssistInvocationContext context) {
		fContext= context;
		fWord= word;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#apply(org.eclipse.jface.text.IDocument)
	 */
	public final void apply(final IDocument document) {

		final ISpellCheckEngine engine= SpellCheckEngine.getInstance();
		final ISpellChecker checker= engine.getSpellChecker();

		if (checker == null)
			return;

		if (!checker.acceptsWords()) {
			final Shell shell;
			if (fContext != null && fContext.getSourceViewer() != null)
				shell= fContext.getSourceViewer().getTextWidget().getShell();
			else
				shell= WorkbenchUtils.getWorkbenchWindowShell();

			if (!canAskToConfigure() || !askUserToConfigureUserDictionary(shell))
				return;

			String[] preferencePageIds= new String[] { "org.eclipse.ui.editors.preferencePages.Spelling" }; //$NON-NLS-1$
			PreferencesUtil.createPreferenceDialogOn(shell, preferencePageIds[0], preferencePageIds, null).open();
		}

		if (checker.acceptsWords()) {
			checker.addWord(fWord);
			if (fContext != null && fContext.getSourceViewer() != null)
				SpellingProblem.removeAll(fContext.getSourceViewer(), fWord);
		}
	}

	/**
	 * Asks the user whether he wants to configure a user dictionary.
	 * 
	 * @param shell the shell
	 * @return <code>true</code> if the user wants to configure the user dictionary
	 * @since 3.3
	 */
	private boolean askUserToConfigureUserDictionary(Shell shell) {
		MessageDialogWithToggle toggleDialog= MessageDialogWithToggle.openYesNoQuestion(
				shell,
				Messages.Spelling_add_askToConfigure_title,
				Messages.Spelling_add_askToConfigure_question,
				Messages.Spelling_add_askToConfigure_ignoreMessage,
				false,
				null,
				null);

		PREF_KEY_PREF_KEY_DO_NOT_ASK.setStoredBoolean(toggleDialog.getToggleState());
		
		return toggleDialog.getReturnCode() == IDialogConstants.YES_ID;
	}

	/**
	 * Tells whether this proposal can ask to
	 * configure a user dictionary.
	 *
	 * @return <code>true</code> if it can ask the user
	 */
	static boolean canAskToConfigure() {
	    return !PREF_KEY_PREF_KEY_DO_NOT_ASK.getStoredBoolean();
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getAdditionalProposalInfo()
	 */
	public String getAdditionalProposalInfo() {
		return MessageFormat.format(Messages.Spelling_add_info, new Object[] { WordCorrectionProposal.getHtmlRepresentation(fWord)}); 
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
		return MessageFormat.format(Messages.Spelling_addWordProposal, new Object[] { WordCorrectionProposal.getHtmlRepresentation(fWord)}); 
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getImage()
	 */
	public Image getImage() {
       return ImageUtils.getImage(ImageUtils.CORRECTION_ADD);
	}

	/*
	 * @see org.eclipse.jdt.ui.text.java.IJavaCompletionProposal#getRelevance()
	 */
	public int getRelevance() {
		return Integer.MIN_VALUE;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getSelection(org.eclipse.jface.text.IDocument)
	 */
	public final Point getSelection(final IDocument document) {
		return new Point(fContext.getOffset(), fContext.getLength());
	}
}
