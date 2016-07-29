package com.excelsior.xds.ui.editor.internal.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.excelsior.xds.ui.editor.internal.nls.Messages;

public class CodeStylePreferencePage extends PreferencePage implements IWorkbenchPreferencePage           
{

    public CodeStylePreferencePage() {
    }

    public CodeStylePreferencePage(String title) {
        super(title);
    }

    public CodeStylePreferencePage(String title, ImageDescriptor image) {
        super(title, image);
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    protected Control createContents(final Composite parent) {
        Composite comp = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        comp.setLayout(layout);

        Label descLabel = new Label(comp, SWT.NONE);
        descLabel.setText(Messages.CodeStylePreferencePage_ExpandTreeToEdit);
        
        noDefaultAndApplyButton();

        return comp;
    }

}
