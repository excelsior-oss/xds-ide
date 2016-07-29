package com.excelsior.xds.ui.editor.modula.contentassist2;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;

import com.excelsior.xds.ui.editor.XdsEditorsPlugin;
import com.excelsior.xds.ui.editor.commons.contentassist.ICompletionContextUser;
import com.excelsior.xds.ui.editor.modula.IModulaPartitions;
import com.excelsior.xds.ui.editor.modula.template.SourceCodeTemplateCompletionProcessor;
import com.excelsior.xds.ui.editor.modula.template.SourceCodeTemplateContextType;

class ModulaTemplateCompletionProcessor extends SourceCodeTemplateCompletionProcessor implements ICompletionContextUser<CompletionContext> {
	private CompletionContext completionContext;
	
	private static final Set<RegionType> ignoreRegions = new HashSet<RegionType>();
	static{
		ignoreRegions.addAll(Arrays.asList(RegionType.IMPORT_STATEMENT));
	}

	public ModulaTemplateCompletionProcessor(
			ContentAssistant contentAssistant) {
		super(contentAssistant);
	}

	@Override
	protected boolean isApplicable() {
		if (completionContext != null) {
			if (!IModulaPartitions.M2_CONTENT_TYPE_DEFAULT
					.equals(completionContext.getContentType())
					|| completionContext.isDotBeforeCursor()
					|| ignoreRegions.contains(completionContext
							.getRegionType())) {
				return false;
			}
		}
		return completionContext != null;
	}

	@Override
	public void setCompletionContext(CompletionContext context) {
		this.completionContext = context;
	}

	@Override
	protected boolean isEnabled(ITextViewer viewer, int offset,
			Template template) {
		return ModulaTemplateRegistry.isEnabled(template, completionContext.getRegionType());
	}

	@Override
	protected TemplateContextType getContextType(ITextViewer viewer,
			IRegion region) {
		ContextTypeRegistry registry = XdsEditorsPlugin.getDefault().getContextTypeRegistry();
		return registry.getContextType(SourceCodeTemplateContextType.MODULA_CONTEXTTYPE);
	}
}