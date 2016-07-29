package com.excelsior.xds.ui.editor.dbgscript.compare;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;

import com.excelsior.xds.ui.commons.syntaxcolor.TokenManager;
import com.excelsior.xds.ui.editor.dbgscript.DbgScriptDocumentSetupParticipant;
import com.excelsior.xds.ui.editor.dbgscript.DbgScriptEditor;
import com.excelsior.xds.ui.editor.dbgscript.DbgScriptSourceViewerConfiguration;
import com.excelsior.xds.ui.editor.dbgscript.IDbgScriptPartitions;

@SuppressWarnings("restriction")
public class PktMergeViewer extends TextMergeViewer{
    private final TokenManager tokenManager = new TokenManager(); 

	public PktMergeViewer(Composite parent, CompareConfiguration configuration) {
		super(parent, configuration);
	}

	@Override
	protected void configureTextViewer(TextViewer textViewer) {
		if (textViewer instanceof SourceViewer) {
			((SourceViewer)textViewer).configure(getSourceViewerConfiguration());
		}
	}
	
	@Override
	protected String getDocumentPartitioning() {
		return IDbgScriptPartitions.PKT_PARTITIONING;
	}

	private SourceViewerConfiguration getSourceViewerConfiguration() {
		return new DbgScriptSourceViewerConfiguration(tokenManager, EditorsPlugin.getDefault().getPreferenceStore(), DbgScriptEditor.eolCommentPrefix);
	}
	
	@Override
	protected IDocumentPartitioner getDocumentPartitioner() {
		return DbgScriptDocumentSetupParticipant.createDocumentPartitioner();
	}
}
