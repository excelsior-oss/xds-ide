package com.excelsior.xds.ui.editor.internal.preferences;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;

import com.excelsior.xds.core.help.IXdsHelpContextIds;
import com.excelsior.xds.ui.commons.utils.HelpUtils;
import com.excelsior.xds.ui.editor.XdsEditorsPlugin;

/**
 * Preference page for Modula-2 code templates. 
 */
public class Modula2TemplatesPreferencePage extends TemplatePreferencePage
		                                    implements IWorkbenchPreferencePage 
{
    public Modula2TemplatesPreferencePage() {
		super();
	}
    
    @Override
    protected Control createContents(Composite ancestor) {
        HelpUtils.setHelp(ancestor, IXdsHelpContextIds.MODULA2_TEMPLATES_PREFERENCE_PAGE);
        return super.createContents(ancestor);
    }
        

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
		super.init(workbench);
		setPreferenceStore(XdsEditorsPlugin.getDefault().getPreferenceStore());
		setTemplateStore(XdsEditorsPlugin.getDefault().getTemplateStore());
		setContextTypeRegistry(XdsEditorsPlugin.getDefault().getContextTypeRegistry());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@SuppressWarnings("deprecation")
	@Override
	public boolean performOk() {
		boolean ok = super.performOk();
		XdsEditorsPlugin.getDefault().savePluginPreferences();
		return ok;
	}
}
