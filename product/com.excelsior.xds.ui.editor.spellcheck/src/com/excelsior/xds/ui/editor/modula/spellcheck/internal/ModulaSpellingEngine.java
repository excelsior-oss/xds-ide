/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.excelsior.xds.ui.editor.modula.spellcheck.internal;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector;

import com.excelsior.xds.core.preferences.PreferenceKeys;
import com.excelsior.xds.ui.editor.modula.IModulaPartitions;
import com.excelsior.xds.ui.editor.modula.spellcheck.internal.engine.ISpellChecker;


/**
 * Java spelling engine
 *
 * @since 3.1
 */
public class ModulaSpellingEngine extends SpellingEngine {


	/*
	 * @see org.eclipse.jdt.internal.ui.text.spelling.SpellingEngine#check(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.IRegion[], org.eclipse.jdt.internal.ui.text.spelling.engine.ISpellChecker, org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void check(IDocument document, IRegion[] regions, ISpellChecker checker, ISpellingProblemCollector collector, IProgressMonitor monitor) {
		SpellEventListener listener= new SpellEventListener(collector, document);
		boolean isIgnoringModulaStrings= PreferenceKeys.PKEY_SPELLING_IGNORE_MODULA_STRINGS.getStoredBoolean(); 
		try {
			for (int i= 0; i < regions.length; i++) {
				IRegion region= regions[i];
				ITypedRegion[] partitions= TextUtilities.computePartitioning(document, IModulaPartitions.M2_PARTITIONING, region.getOffset(), region.getLength(), false);
				for (int index= 0; index < partitions.length; index++) {
					if (monitor != null && monitor.isCanceled())
						return;

					if (listener.isProblemsThresholdReached())
						return;

					ITypedRegion partition= partitions[index];
					final String type= partition.getType();

					if (isIgnoringModulaStrings && (type.equals(IModulaPartitions.M2_CONTENT_TYPE_SINGLE_QUOTE_STRING) || type.equals(IModulaPartitions.M2_CONTENT_TYPE_DOUBLE_QUOTE_STRING)))
						continue;

                    if (type.equals(IModulaPartitions.M2_CONTENT_TYPE_PRAGMA))
                        continue;

					if (!type.equals(IDocument.DEFAULT_CONTENT_TYPE))
						checker.execute(listener, new SpellCheckIterator(document, partition, checker.getLocale()));
				}
			}
		} catch (BadLocationException x) {
			// ignore: the document has been changed in another thread and will be checked again
		} catch (AssertionFailedException x) {
			// ignore: the document has been changed in another thread and will be checked again
		}
	}
}
