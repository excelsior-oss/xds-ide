package com.excelsior.xds.ui.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.excelsior.xds.core.help.IXdsHelpContextIds;
import com.excelsior.xds.ui.commons.utils.HelpUtils;
import com.excelsior.xds.ui.internal.nls.Messages;

/*
 * The page for setting general XDS plug-in preferences.
 */
public class XdsBasePreferencePage extends    PreferencePage 
                                   implements IWorkbenchPreferencePage 
{

	/**
	 * ID for the page
	 */
	public static final String ID = "com.excelsior.xds.ui.preferences.XdsBasePreferencePage"; //$NON-NLS-1$
	
	public XdsBasePreferencePage() {
	}

	public XdsBasePreferencePage(String title) {
		super(title);
	}

	public XdsBasePreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected Control createContents(Composite parent) {
		parent.setLayout(new GridLayout());
		Label lbl = new Label(parent, SWT.NONE);
		lbl.setText(Messages.XdsBasePreferencePage_Description);
		lbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		return parent;
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		HelpUtils.setHelp(getControl(), IXdsHelpContextIds.MODULA2_PREFERENCE_PAGE);
		getApplyButton().setVisible(false);
		getDefaultsButton().setVisible(false);
	}
}
