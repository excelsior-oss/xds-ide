package com.excelsior.xds.ui.preferences.sdk;

import java.util.List;

import org.eclipse.jface.wizard.Wizard;

import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.core.sdk.SdkTool;
import com.excelsior.xds.ui.internal.nls.Messages;

public class EditSdkToolDialog extends Wizard {
	private SdkTool editedTool;
	private Sdk sdk;
    private List<String> menuCategories;


	public EditSdkToolDialog(SdkTool editedTool, Sdk sdk, List<String> menuCategories) {
		setWindowTitle(Messages.EditSdkToolDialog_Title);
		this.editedTool = editedTool;
		this.sdk = sdk;
        this.menuCategories = menuCategories;
	}

	@Override
	public void addPages() {
		addPage(new EditSdkToolPage(editedTool, sdk, menuCategories));
	}

	@Override
	public boolean performFinish() {
		return true;
	}

}
