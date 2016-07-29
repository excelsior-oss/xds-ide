package com.excelsior.xds.ui.console.preferences;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.TextConsole;

import com.excelsior.xds.core.console.ColorStreamType;
import com.excelsior.xds.core.console.XdsConsoleSettings;
import com.excelsior.xds.core.help.IXdsHelpContextIds;
import com.excelsior.xds.core.preferences.WorkspacePreferencesManager;
import com.excelsior.xds.core.sdk.SdkManager;
import com.excelsior.xds.ui.commons.utils.HelpUtils;
import com.excelsior.xds.ui.commons.utils.SWTFactory;
import com.excelsior.xds.ui.internal.nls.Messages;

/**
 * The XDS build console preference page.
 */
public class XdsConsolePreferencePage extends PreferencePage implements
        IWorkbenchPreferencePage {
    /**
     * ID for the page
     */
    public static final String ID = "com.excelsior.xds.ui.preferences.XdsConsolePreferencePage"; //$NON-NLS-1$
    
    private Button cbShowWhenBuild;
    private Button cbClearBeforeBuild;
    private ColorSelector csText;
    private ColorSelector csInfo;
    private ColorSelector csError;
    private ColorSelector csCompError;
    private ColorSelector csCompWarning;
    private ColorSelector csUserInput;
    private ColorSelector csBackground;


    /**
     * @wbp.parser.constructor
     */
    public XdsConsolePreferencePage() {
    }

    public XdsConsolePreferencePage(String title) {
        super(title);
    }

    public XdsConsolePreferencePage(String title, ImageDescriptor image) {
        super(title, image);
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    protected Control createContents(Composite parent) {
        final int nColumns = 2;
        initializeDialogUnits(parent);
        
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setLayout(new GridLayout(nColumns, false));
        
        SWTFactory.createLabel(composite, Messages.XdsConsolePreferencePage_GeneralConsoleSettings+':', nColumns);
        SWTFactory.createVerticalSpacer(composite, 0.3);
        
        cbShowWhenBuild = SWTFactory.createCheckbox(composite, Messages.XdsConsolePreferencePage_ShowConsoleWhenBuilding, nColumns);
        cbClearBeforeBuild = SWTFactory.createCheckbox(composite, Messages.XdsConsolePreferencePage_AlwaysClearBeforeBuilding, nColumns);

        SWTFactory.createVerticalSpacer(composite, 0.3);
        
        // -- Console text color ---------------------------
        final int color_nColumns = 2;
        Group color_group = SWTFactory.createGroup(composite, Messages.XdsConsolePreferencePage_ConsoleTextColor, color_nColumns, nColumns, GridData.FILL_HORIZONTAL);
        
        SWTFactory.createLabel(color_group, Messages.XdsConsolePreferencePage_ErrorMessage+':', 1);
        csCompError = SWTFactory.createColorSelector(color_group, 1);

        SWTFactory.createLabel(color_group, Messages.XdsConsolePreferencePage_WarningMessage+':', 1);
        csCompWarning = SWTFactory.createColorSelector(color_group, 1);

        SWTFactory.createVerticalSpacer(color_group, 0.2);

        SWTFactory.createLabel(color_group, Messages.XdsConsolePreferencePage_ErrorNotification+':', 1);
        csError = SWTFactory.createColorSelector(color_group, 1);

        SWTFactory.createLabel(color_group, Messages.XdsConsolePreferencePage_InfoMessage+':', 1);
        csInfo = SWTFactory.createColorSelector(color_group, 1);

        SWTFactory.createVerticalSpacer(color_group, 0.2);

        SWTFactory.createLabel(color_group, Messages.XdsConsolePreferencePage_OutputText+':', 1);
        csText = SWTFactory.createColorSelector(color_group, 1);

        SWTFactory.createLabel(color_group, Messages.XdsConsolePreferencePage_InputText+':', 1);
        csUserInput = SWTFactory.createColorSelector(color_group, 1);

        SWTFactory.createLabel(color_group, Messages.XdsConsolePreferencePage_BackgroundColor+':', 1);
        csBackground = SWTFactory.createColorSelector(color_group, 1);
        
        initControls(false);

        return composite;
    }

    @Override
    public void createControl(Composite parent) {
        HelpUtils.setHelp(parent, IXdsHelpContextIds.MODULA2_CONSOLE_PREFERENCE_PAGE);

        super.createControl(parent);
        getApplyButton().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                doApply();
            }
        });
    }

    @Override
    public boolean performOk() {
        doApply();
        return super.performOk();
    }

    @Override
    public boolean performCancel() {
        SdkManager.getInstance().unloadSdkRegistry();
        return super.performCancel();
    }
    
    @Override
    public void performDefaults() {
        initControls(true);
    }
    
    private void initControls(boolean setDefVlaues) {
        cbShowWhenBuild.setSelection(XdsConsoleSettings.getShowOnBuild());
        cbClearBeforeBuild.setSelection(XdsConsoleSettings.getClearBeforeBuild());
        csText.setColorValue(ColorStreamType.NORMAL.getRgb(setDefVlaues));
        csInfo.setColorValue(ColorStreamType.SYSTEM.getRgb(setDefVlaues));
        csError.setColorValue(ColorStreamType.ERROR.getRgb(setDefVlaues));
        csCompError.setColorValue(ColorStreamType.XDS_LOG_ERROR.getRgb(setDefVlaues));
        csCompWarning.setColorValue(ColorStreamType.XDS_LOG_WARNING.getRgb(setDefVlaues));
        csUserInput.setColorValue(ColorStreamType.USER_INPUT.getRgb(setDefVlaues));
        csBackground.setColorValue(ColorStreamType.BACKGROUND.getRgb(setDefVlaues));
    }
    
    private void doApply() {
    	// TODO : these settings should be stored under com.excelsior.xds.ui preference store.
    	
        XdsConsoleSettings.setShowOnBuild(cbShowWhenBuild.getSelection());
        XdsConsoleSettings.setClearBeforeBuild(cbClearBeforeBuild.getSelection());
        ColorStreamType.NORMAL.setRgb(csText.getColorValue());
        ColorStreamType.SYSTEM.setRgb(csInfo.getColorValue());
        ColorStreamType.ERROR.setRgb(csError.getColorValue());
        ColorStreamType.XDS_LOG_ERROR.setRgb(csCompError.getColorValue());
        ColorStreamType.XDS_LOG_WARNING.setRgb(csCompWarning.getColorValue());
        ColorStreamType.USER_INPUT.setRgb(csUserInput.getColorValue());
        ColorStreamType.BACKGROUND.setRgb(csBackground.getColorValue());
        // Force background change:
        for (IConsole con : ConsolePlugin.getDefault().getConsoleManager().getConsoles()) {
            if (con instanceof TextConsole) {
                ((TextConsole) con).setBackground(new Color(Display.getDefault(), csBackground.getColorValue()));
            }
        }
        
        WorkspacePreferencesManager.getInstance().flush();
    }
}
