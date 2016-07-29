package com.excelsior.xds.core.builders;

import com.excelsior.xds.core.sdk.XdsOptions;

public class DefaultBuildSettingsHolder {
    public static final BuildSettings DefaultBuildSettings;
    public static final BuildSettingsKey DefaultBuildSettingsKey;

    static {
        DefaultBuildSettings = new BuildSettings();

        DefaultBuildSettings.addOption(XdsOptions.O2ISOPRAGMA, true);
        DefaultBuildSettings.addOption(XdsOptions.M2EXTENSIONS, false);
        DefaultBuildSettings.addOption(XdsOptions.CPPCOMMENTS, false);

        DefaultBuildSettings.addOption(XdsOptions.ASSERT, true);
        DefaultBuildSettings.addOption(XdsOptions.CHECKDINDEX, true);
        DefaultBuildSettings.addOption(XdsOptions.CHECKDIV, true);
        DefaultBuildSettings.addOption(XdsOptions.CHECKINDEX, true);
        DefaultBuildSettings.addOption(XdsOptions.CHECKNIL, true);
        DefaultBuildSettings.addOption(XdsOptions.CHECKPROC, true);
        DefaultBuildSettings.addOption(XdsOptions.CHECKRANGE, true);
        DefaultBuildSettings.addOption(XdsOptions.CHECKSET, true);
        DefaultBuildSettings.addOption(XdsOptions.CHECKTYPE, true);
        DefaultBuildSettings.addOption(XdsOptions.COVERFLOW, true);
        DefaultBuildSettings.addOption(XdsOptions.IOVERFLOW, true);
        DefaultBuildSettings.addOption(XdsOptions.FOVERFLOW, true);

        DefaultBuildSettings.addOption(XdsOptions.STORAGE, false);
        DefaultBuildSettings.addOption(XdsOptions.M2ADDTYPES, false);
        DefaultBuildSettings.addOption(XdsOptions.M2BASE16, false);
        DefaultBuildSettings.addOption(XdsOptions.M2CMPSYM, false);
        DefaultBuildSettings.addOption(XdsOptions.M2EXTENSIONS, false);
        DefaultBuildSettings.addOption(XdsOptions.O2EXTENSIONS, false);
        DefaultBuildSettings.addOption(XdsOptions.O2ISOPRAGMA, false);
        DefaultBuildSettings.addOption(XdsOptions.O2NUMEXT, false);
        DefaultBuildSettings.addOption(XdsOptions.O2ADDKWD, false);

        DefaultBuildSettings.addOption(XdsOptions.TOPSPEED, false);

        DefaultBuildSettings.addOption(XdsOptions.MAKEDEF, false);
        DefaultBuildSettings.addOption(XdsOptions.BSCLOSURE, false);
        DefaultBuildSettings.addOption(XdsOptions.BSREDEFINE, false);
        DefaultBuildSettings.addOption(XdsOptions.BSALPHA, false);

        DefaultBuildSettings.addOption(XdsOptions.CHANGESYM, false);
        DefaultBuildSettings.addOption(XdsOptions.MAIN, false);
        DefaultBuildSettings.addOption(XdsOptions.XCOMMENTS, false);

        DefaultBuildSettings.addOption(XdsOptions.CHECKDZ, true);
        
        DefaultBuildSettingsKey = DefaultBuildSettings.createKey();
    }
}