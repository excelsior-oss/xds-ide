package com.excelsior.xds.ui.editor.modula.spellcheck.internal;

// see org.eclipse.jdt.internal.ui.preferences.SpellingConfigurationBlock

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ide.dialogs.EncodingFieldEditor;
import org.eclipse.ui.texteditor.spelling.IPreferenceStatusMonitor;
import org.eclipse.ui.texteditor.spelling.ISpellingPreferenceBlock;

import com.excelsior.xds.core.preferences.PreferenceConstants;
import com.excelsior.xds.core.preferences.PreferenceKey;
import com.excelsior.xds.core.preferences.PreferenceKeys;
import com.excelsior.xds.ui.commons.utils.SWTFactory;
import com.excelsior.xds.ui.commons.utils.SwtUtils;
import com.excelsior.xds.ui.editor.XdsEditorsPlugin;
import com.excelsior.xds.ui.editor.commons.SourceCodeTextEditor;
import com.excelsior.xds.ui.editor.modula.ModulaEditor;
import com.excelsior.xds.ui.editor.modula.spellcheck.internal.nls.Messages;

public class SpellingPreferenceBlock implements ISpellingPreferenceBlock {

    private HashSet<PreferenceCheckbox> hsPrefCBoxes; 
    private HashSet<Control> hsAllControls;
    private HashSet<Control> hsEnabledControls;
    
    private ArrayList<String> locValues;
    private ArrayList<String> locLabels; 

    
    private Text fDictionaryPath;
    private Text fTextMaxPerFile;
    private Text fTextMaxProposals;
    private Composite fEncodingEditorParent;
    private EncodingFieldEditor fEncodingEditor;
    private Combo fCmbLocales;
    
    private IPreferenceStatusMonitor statusMonitor;
    
    /** The status for the workspace dictionary file */
    private IStatus fFileStatus = new MyStatus (IStatus.OK, XdsEditorsPlugin.PLUGIN_ID, ""); //$NON-NLS-1$
            
    /** The status for the proposal threshold */
    private IStatus fThresholdStatus = new MyStatus (IStatus.OK, XdsEditorsPlugin.PLUGIN_ID, ""); //$NON-NLS-1$

    /** The status for the encoding field editor */
    private IStatus fEncodingFieldEditorStatus = new MyStatus (IStatus.OK, XdsEditorsPlugin.PLUGIN_ID, ""); //$NON-NLS-1$


    public SpellingPreferenceBlock() {
    }

    
    
	@Override
	public Control createControl(Composite parent) {
	    hsPrefCBoxes = new HashSet<PreferenceCheckbox>(); 
	    hsAllControls = new HashSet<Control>();
	    locValues = new ArrayList<String>();
	    locLabels = new ArrayList<String>(); 

	    Composite composite= new Composite(parent, SWT.NONE);
	    composite.setLayout(new GridLayout());

	    Group user= new Group(composite, SWT.NONE);
	    user.setText(Messages.SpellingPreferenceBlock_Options);
        user.setLayout(new GridLayout());
        user.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        hsAllControls.add(user);
        
        new PreferenceCheckbox(user, Messages.SpellingPreferenceBlock_IgnoreWordsWithDigits, PreferenceKeys.PKEY_SPELLING_IGNORE_DIGITS);
        new PreferenceCheckbox(user, Messages.SpellingPreferenceBlock_IgnoreMixedCaseWords, PreferenceKeys.PKEY_SPELLING_IGNORE_MIXED);
        new PreferenceCheckbox(user, Messages.SpellingPreferenceBlock_IgnoreCapitalization, PreferenceKeys.PKEY_SPELLING_IGNORE_SENTENCE);
        new PreferenceCheckbox(user, Messages.SpellingPreferenceBlock_IgnoreUpperCaseWords, PreferenceKeys.PKEY_SPELLING_IGNORE_UPPER);
        new PreferenceCheckbox(user, Messages.SpellingPreferenceBlock_IgnoreURLs, PreferenceKeys.PKEY_SPELLING_IGNORE_URLS);
        new PreferenceCheckbox(user, Messages.SpellingPreferenceBlock_IgnoreNonLettersBoundaries, PreferenceKeys.PKEY_SPELLING_IGNORE_NON_LETTERS);
        new PreferenceCheckbox(user, Messages.SpellingPreferenceBlock_IgnoreSingleLetters, PreferenceKeys.PKEY_SPELLING_IGNORE_SINGLE_LETTERS);
        new PreferenceCheckbox(user, Messages.SpellingPreferenceBlock_IgnoreM2Strings, PreferenceKeys.PKEY_SPELLING_IGNORE_MODULA_STRINGS);
        
        final Set<Locale> locales= SpellCheckEngine.getLocalesWithInstalledDictionaries();
        boolean hasPlaformDictionaries= locales.size() > 0;

        final Group engine= new Group(composite, SWT.NONE);
        if (hasPlaformDictionaries)
            engine.setText(Messages.SpellingPreferenceBlock_Dictionaries);
        else
            engine.setText(Messages.SpellingPreferenceBlock_Dictionary);
        engine.setLayout(new GridLayout(4, false));
        engine.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        hsAllControls.add(engine);

        if (hasPlaformDictionaries) {
            for (final Iterator<Locale> iterator= locales.iterator(); iterator.hasNext();) {
                Locale locale= iterator.next();
                locLabels.add(locale.getDisplayName());
                locValues.add(locale.toString());
            }
            locLabels.add(Messages.SpellingPreferenceBlock_none);
            locValues.add(PreferenceConstants.PREF_VALUE_NO_LOCALE);
            
            hsAllControls.add(SWTFactory.createLabel(engine,  Messages.SpellingPreferenceBlock_PlatformDictionary + ':', 1));

            fCmbLocales = SWTFactory.createCombo(engine, 2, SWT.READ_ONLY);
            fCmbLocales.setItems(locLabels.toArray(new String[0]));
            new Label(engine, SWT.NONE); // placeholder
            hsAllControls.add(fCmbLocales);
        }

        hsAllControls.add(SWTFactory.createLabel(engine,  Messages.SpellingPreferenceBlock_UserDefDictionary + ':', 1));
        fDictionaryPath = SWTFactory.createSingleText(engine, 2);
        fDictionaryPath.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                validateAbsoluteFilePath();
            }
        });

        Composite buttons=new Composite(engine, SWT.NONE);
        buttons.setLayout(new GridLayout(2,true));
        buttons.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

        Button button= SWTFactory.createPushButton(buttons, Messages.SpellingPreferenceBlock_Browse, null);
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                handleBrowseButtonSelected();
            }
        });
        hsAllControls.add(button);

        button= SWTFactory.createPushButton(buttons, Messages.SpellingPreferenceBlock_Variables, null);
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleVariablesButtonSelected();
            }
        });
        hsAllControls.add(button);

        // Description for user dictionary
        new Label(engine, SWT.NONE); // filler
        hsAllControls.add(SWTFactory.createLabel(engine, Messages.SpellingPreferenceBlock_UserDictDesc, 3));

        createEncodingFieldEditor(engine);

        Group advanced= new Group(composite, SWT.NONE);
        advanced.setText(Messages.SpellingPreferenceBlock_Advanced);
        advanced.setLayout(new GridLayout(3, false));
        advanced.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        hsAllControls.add(advanced);

        fTextMaxPerFile   = createNumEFWithLabel(advanced, Messages.SpellingPreferenceBlock_MaxProblems + ':', 4);
        fTextMaxPerFile.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                validateTresholds();
            }
        });
        
        fTextMaxProposals = createNumEFWithLabel(advanced, Messages.SpellingPreferenceBlock_MaxProposals + ':', 4);
        fTextMaxProposals.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                validateTresholds();
            }
        });

        setAllFromStore();
		return composite;
	}

	private Text createNumEFWithLabel(Composite parent, String label, int digits) {
	    hsAllControls.add(SWTFactory.createLabel(parent, label, 1));
	    
	    Text txt = SWTFactory.createSingleText(parent, 2, SWT.BORDER | SWT.SINGLE);
	    hsAllControls.add(txt);

	    GridData data= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
	    int wh = SwtUtils.getTextWidth(txt, "0") * (digits+1); //$NON-NLS-1$
        data.widthHint= wh;
        data.horizontalSpan= 2;
        txt.setLayoutData(data);

        return txt;
	}

	private void createEncodingFieldEditor(Composite composite) {
	    Label filler= new Label(composite, SWT.NONE);
	    GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
	    gd.horizontalSpan= 4;
	    filler.setLayoutData(gd);

	    Label label= new Label(composite, SWT.NONE);
	    label.setText(Messages.SpellingPreferenceBlock_Encoding + ':');
	    label.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
	    hsAllControls.add(label);

	    fEncodingEditorParent= new Composite(composite, SWT.NONE);
	    GridLayout layout= new GridLayout(2, false);
	    layout.marginWidth= 0;
	    layout.marginHeight= 0;
	    fEncodingEditorParent.setLayout(layout);
	    gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
	    gd.horizontalSpan= 3;
	    fEncodingEditorParent.setLayoutData(gd);

	    fEncodingEditor= new EncodingFieldEditor(PreferenceConstants.SPELLING_USER_DICTIONARY_ENCODING, "", null, fEncodingEditorParent); //$NON-NLS-1$

	    PreferenceStore tmpStore= new PreferenceStore();
	    String defaultEncoding= ResourcesPlugin.getEncoding();
	    tmpStore.setDefault(PreferenceConstants.SPELLING_USER_DICTIONARY_ENCODING, defaultEncoding);
	    String encoding= PreferenceKeys.PKEY_SPELLING_USER_DICTIONARY_ENCODING.getStoredValue();
	    if (encoding != null && encoding.length() > 0) {
	        tmpStore.setValue(PreferenceConstants.SPELLING_USER_DICTIONARY_ENCODING, encoding);
	    }
	    fEncodingEditor.setPreferenceStore(tmpStore);

	    // Redirect status messages from the field editor to the status change listener
	    DialogPage fakePage= new DialogPage() {
	        public void createControl(Composite c) {
	        }
	        @Override
	        public void setErrorMessage(String newMessage) {
	            if (newMessage != null && !newMessage.isEmpty()) {
	                fEncodingFieldEditorStatus = new MyStatus(IStatus.ERROR, XdsEditorsPlugin.PLUGIN_ID, newMessage);
	            } else {
                    fEncodingFieldEditorStatus = new MyStatus (IStatus.OK, XdsEditorsPlugin.PLUGIN_ID, null);
	            }
	            updateStatus();
	        }
	    };
	    fEncodingEditor.setPage(fakePage);

	    fEncodingEditor.load();

	    if (encoding == null || encoding.equals(defaultEncoding) || encoding.length() == 0)
	        fEncodingEditor.loadDefault();

	}


	@Override
	public void initialize(IPreferenceStatusMonitor statusMonitor) {
	    this.statusMonitor = statusMonitor;
	    validateAll();
	}

	@Override
    public void setEnabled(boolean enabled) {
        fEncodingEditor.setEnabled(enabled, fEncodingEditorParent);

        if (enabled && hsEnabledControls != null) {
            for (Control ctr : hsEnabledControls)
                ctr.setEnabled(true);
            hsEnabledControls= null;
        }
        if (!enabled && hsEnabledControls == null) {
            hsEnabledControls = new HashSet<Control>();
            for (Control ctr : hsAllControls) {
                if (ctr.getEnabled()) {
                    hsEnabledControls.add(ctr);
                    ctr.setEnabled(false);
                }
            }
        }
    }
	
    protected void handleVariablesButtonSelected() {
        StringVariableSelectionDialog dialog= new StringVariableSelectionDialog(fDictionaryPath.getShell());
        if (dialog.open() == Window.OK)
            fDictionaryPath.setText(fDictionaryPath.getText() + dialog.getVariableExpression());
    }

    protected void handleBrowseButtonSelected() {
	    final FileDialog dialog= new FileDialog(fDictionaryPath.getShell(), SWT.OPEN);
	    dialog.setText(Messages.SpellingPreferenceBlock_SelectUserDictionary);
	    dialog.setFilterPath(fDictionaryPath.getText());

	    final String path= dialog.open();
	    if (path != null)
	        fDictionaryPath.setText(path);
	}


	@Override
	public boolean canPerformOk() {
		return true;
	}

	@Override
	public void performOk() {
	    for (PreferenceCheckbox pcb : hsPrefCBoxes) {
	        pcb.prefKey.setStoredBoolean(pcb.cbox.getSelection());
	    }
	    
	    PreferenceKeys.PKEY_SPELLING_USER_DICTIONARY.setStoredValue(fDictionaryPath.getText().trim());
	    
        fEncodingEditor.store();
        if (fEncodingEditor.presentsDefaultValue()) {
            PreferenceKeys.PKEY_SPELLING_USER_DICTIONARY_ENCODING.setStoredValue(""); //$NON-NLS-1$
        } else {
            PreferenceKeys.PKEY_SPELLING_USER_DICTIONARY_ENCODING.setStoredValue(
                    fEncodingEditor.getPreferenceStore().getString(PreferenceConstants.SPELLING_USER_DICTIONARY_ENCODING)); 
        }

        if (fCmbLocales != null) {
            int idx = fCmbLocales.getSelectionIndex();
            PreferenceKeys.PKEY_SPELLING_LOCALE.setStoredValue(
                    idx >= 0 ? locValues.get(idx) : PreferenceConstants.PREF_VALUE_NO_LOCALE);
        }
        
        PreferenceKeys.PKEY_SPELLING_PROBLEMS_THRESHOLD.setStoredInt(getIntFrom(fTextMaxPerFile));
        PreferenceKeys.PKEY_SPELLING_PROPOSAL_THRESHOLD.setStoredInt(getIntFrom(fTextMaxProposals));
        
        SourceCodeTextEditor.refreshEditorsConfiguration(ModulaEditor.class);
	}
	
	private int getIntFrom(Text text) {
	    String s = text.getText().trim();
	    int res = -1;
	    for (int i=0; i < s.length(); ++i) {
	        char ch = s.charAt(i);
	        if (ch >= '0' && ch <= '9') {
	            if (res < 0) {
	                res = ch - '0';
	            } else {
	                res = res * 10 + ch - '0';
	            }
	        } else {
	            res = -1;
	            break;
	        }
	    }
	    return res;
	}
	
	   
    private void selLocale(String locValue) {
        if (fCmbLocales != null) {
            
            Locale locale= SpellCheckEngine.convertToLocale(locValue);
            locale= SpellCheckEngine.findClosestLocale(locale);
            if (locale != null) {
                locValue= locale.toString();
            } else {
                locValue= PreferenceConstants.PREF_VALUE_NO_LOCALE;
            }
            
            int idx = 0;
            for (String v : locValues) {
                if (v.equals(locValue)) {
                    fCmbLocales.select(idx);
                    return;
                }
                ++idx;
            }
            fCmbLocales.select(fCmbLocales.getItemCount() - 1); // err? select 'none' (it is the last)
        }
    }


	@Override
	public void performDefaults() {
        for (PreferenceCheckbox pcb : hsPrefCBoxes) {
            pcb.cbox.setSelection(pcb.prefKey.getDefBooleanValue());
        }
        
        fDictionaryPath.setText(PreferenceKeys.PKEY_SPELLING_USER_DICTIONARY.getDefStringValue());

        String enc = PreferenceKeys.PKEY_SPELLING_USER_DICTIONARY_ENCODING.getDefStringValue();
        fEncodingEditor.getPreferenceStore().setValue(fEncodingEditor.getPreferenceName(), enc);
        if (enc.isEmpty()) {
            fEncodingEditor.loadDefault();
        } else {
            fEncodingEditor.load();
        }

        selLocale(PreferenceConstants.PREF_VALUE_NO_LOCALE);

        fTextMaxPerFile.setText("" + PreferenceKeys.PKEY_SPELLING_PROBLEMS_THRESHOLD.getDefIntValue()); // $//$NON-NLS-1$
        fTextMaxProposals.setText("" + PreferenceKeys.PKEY_SPELLING_PROPOSAL_THRESHOLD.getDefIntValue()); // $//$NON-NLS-1$
        
        validateAll();
	}

	@Override
	public void performRevert() {
		setAllFromStore();
	}

	@Override
	public void dispose() {
	}
	
	private void setAllFromStore() {
        for (PreferenceCheckbox pcb : hsPrefCBoxes) {
            pcb.cbox.setSelection(pcb.prefKey.getStoredBoolean());
        }

        fDictionaryPath.setText(PreferenceKeys.PKEY_SPELLING_USER_DICTIONARY.getStoredValue());

        String enc = PreferenceKeys.PKEY_SPELLING_USER_DICTIONARY_ENCODING.getStoredValue();
        fEncodingEditor.getPreferenceStore().setValue(fEncodingEditor.getPreferenceName(), enc);
        if (enc.isEmpty()) {
            fEncodingEditor.loadDefault();
        } else {
            fEncodingEditor.load();
        }
        
        selLocale(PreferenceKeys.PKEY_SPELLING_LOCALE.getStoredValue());
        
        fTextMaxPerFile.setText("" + PreferenceKeys.PKEY_SPELLING_PROBLEMS_THRESHOLD.getStoredInt()); // $//$NON-NLS-1$
        fTextMaxProposals.setText("" + PreferenceKeys.PKEY_SPELLING_PROPOSAL_THRESHOLD.getStoredInt()); // $//$NON-NLS-1$
        
        validateAll();
	}

	
    private void validateAll() {
        validateAbsoluteFilePath();
        validateTresholds();
        updateStatus();
    }


    private void validateAbsoluteFilePath() {
	    MyStatus ms = new MyStatus(IStatus.OK, XdsEditorsPlugin.PLUGIN_ID, null);
	    String path = fDictionaryPath == null ? "" : fDictionaryPath.getText().trim(); //$NON-NLS-1$
	    if (!path.isEmpty()) {
    	    IStringVariableManager variableManager= VariablesPlugin.getDefault().getStringVariableManager();
    	    try {
    	        path= variableManager.performStringSubstitution(path);
    	        if (path.length() > 0) {
    	            final File file= new File(path);
    	            if (!file.exists() || !file.isFile() || !file.isAbsolute() || !file.canRead()) {
                        ms = new MyStatus(IStatus.ERROR, XdsEditorsPlugin.PLUGIN_ID, Messages.SpellingPreferenceBlock_BadDictFile);
    	            } else if (!file.getParentFile().canWrite() || !file.canWrite()) {
    	                ms = new MyStatus(IStatus.ERROR, XdsEditorsPlugin.PLUGIN_ID, Messages.SpellingPreferenceBlock_RWAccessRequired);
    	            }
    	        }
    	    } catch (CoreException e) {
    	        ms = new MyStatus(IStatus.ERROR, XdsEditorsPlugin.PLUGIN_ID, e.getLocalizedMessage());
    	    }
	    }
	    
	    if (ms.matches(IStatus.ERROR) || fFileStatus.matches(IStatus.ERROR)) {
	        fFileStatus = ms;
	        updateStatus();
	    }
	}
	
    private void validateTresholds() {
        MyStatus ms = new MyStatus(IStatus.OK, XdsEditorsPlugin.PLUGIN_ID, null);
        for (Text text : new Text[]{fTextMaxPerFile, fTextMaxProposals}) {
            if (text != null) {
                int v = getIntFrom(text);
                if (v < 0) {
                    String s = text.getText().trim();
                    ms = new MyStatus(IStatus.ERROR, XdsEditorsPlugin.PLUGIN_ID, 
                            s.isEmpty() ? Messages.SpellingPreferenceBlock_NumberRequired :
                            String.format(Messages.SpellingPreferenceBlock_InvalidInt, s));
                    break;
                }
            }
            
        }
        if (ms.matches(IStatus.ERROR) || fThresholdStatus.matches(IStatus.ERROR)) {
            fThresholdStatus = ms;
            updateStatus();
        }
    }

	private void updateStatus() {
	    if (statusMonitor != null) {
    	    IStatus status[] = new IStatus[] { fThresholdStatus, fFileStatus, fEncodingFieldEditorStatus };
    	    IStatus max= null;
    	    for (int i= 0; i < status.length; i++) {
    	        IStatus curr= status[i];
    	        if (curr.matches(IStatus.ERROR)) {
    	            max = curr;
    	            break;
    	        }
    	        if (max == null || curr.getSeverity() > max.getSeverity()) {
    	            max= curr;
    	        }
    	    }
    
    	    statusMonitor.statusChanged(max);
	    }
	}

	private class MyStatus extends Status {

        public MyStatus(int severity, String pluginId, String message) {
            super(severity, pluginId, message);
            messageWithNull = "".equals(message) ? null : message; //$NON-NLS-1$
        }
        
        private String messageWithNull;

        @Override
        protected void setMessage(String message) {
            messageWithNull = message;
            super.setMessage(message);
        }

        @Override
        public String getMessage() {
            return messageWithNull;
        }

	    
	}

    private class PreferenceCheckbox {
        private Button cbox;
        private PreferenceKey prefKey;
        
        public PreferenceCheckbox(Composite parent, String label, PreferenceKey prefKey) {
            this.prefKey = prefKey;
            cbox = SWTFactory.createCheckbox(parent, label, 1);
            hsAllControls.add(cbox);
            hsPrefCBoxes.add(this);
        }
    }

}
