package com.excelsior.xds.xbookmarks;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.excelsior.xds.xbookmarks.internal.nls.Messages;

public class XPreferencePage extends PreferencePage implements IWorkbenchPreferencePage 
{
	
	public static final String TEXT_MODE_KEY= "com.excelsior.xds.xbookmarks.TEXT_MODE_KEY"; //$NON-NLS-1$
    public static final String HIDE_NUMBERS_KEY= "com.excelsior.xds.xbookmarks.HIDE_NUMBERS_KEY"; //$NON-NLS-1$
	
    private Button cbShowNumbers;
	private Button cbTextMode;


	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected Control createContents(final Composite parent) {
		parent.setLayout(new GridLayout());
		
        Link link = new Link(parent, SWT.NONE);
        link.setText(Messages.XPreferencePage_LinkTextWithHref);
        link.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                PreferencesUtil.createPreferenceDialogOn(parent.getShell(), "org.eclipse.ui.editors.preferencePages.Annotations", null, null); //$NON-NLS-1$
            }
        });
        
        (new Label(parent, SWT.NONE)).setText(""); //$NON-NLS-1$
		
		cbShowNumbers= new Button(parent, SWT.CHECK);
		cbShowNumbers.setText(Messages.XPreferencePage_ShowBMNumbers);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.grabExcessHorizontalSpace = false;
        cbShowNumbers.setLayoutData(gd);
        cbShowNumbers.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                cbTextMode.setEnabled(cbShowNumbers.getSelection());
            }
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
            
        });
        
		cbTextMode = new Button(parent, SWT.CHECK);
		cbTextMode.setText(Messages.XPreferencePage_ShowNumbersInTextMode);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = false;
		cbTextMode.setLayoutData(gd);
		
        Label fillBottom = new Label(parent, SWT.NONE);
        fillBottom.setText(""); //$NON-NLS-1$
        gd = new GridData(GridData.FILL_VERTICAL | GridData.FILL_HORIZONTAL);
        fillBottom.setLayoutData(gd);

    	
		cbShowNumbers.setSelection(!getHideNumbers());
		cbTextMode.setSelection(getTextMode());
        cbTextMode.setEnabled(cbShowNumbers.getSelection());
    	
		return parent;
	}
	
    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
    }
    
    @Override
    public boolean performOk() {
    	setTextMode(cbTextMode.getSelection());
        setHideNumbers(!cbShowNumbers.getSelection());
    	return true;
    }
    
    protected void performDefaults() {
        setTextMode(false);
        setHideNumbers(false);
        cbShowNumbers.setSelection(!getHideNumbers());
        cbTextMode.setSelection(getTextMode());
    }


    
    public static boolean getTextMode() {
    	IPreferenceStore store = XBookmarksPlugin.getDefault().getPreferenceStore();
    	return store.getBoolean(TEXT_MODE_KEY);
    }

    private void setTextMode(boolean b) {
    	IPreferenceStore store = XBookmarksPlugin.getDefault().getPreferenceStore();
    	store.setValue(TEXT_MODE_KEY, b);
    }
    
    public static boolean getHideNumbers() {
        IPreferenceStore store = XBookmarksPlugin.getDefault().getPreferenceStore();
        return store.getBoolean(HIDE_NUMBERS_KEY);
    }

    private void setHideNumbers(boolean b) {
        IPreferenceStore store = XBookmarksPlugin.getDefault().getPreferenceStore();
        store.setValue(HIDE_NUMBERS_KEY, b);
    }
    
}
