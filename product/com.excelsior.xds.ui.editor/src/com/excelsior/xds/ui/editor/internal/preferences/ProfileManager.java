package com.excelsior.xds.ui.editor.internal.preferences;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.ui.commons.utils.SWTFactory;
import com.excelsior.xds.ui.editor.internal.nls.Messages;

public class ProfileManager {
    private static final String ID_PROFILES_COUNT = ".PROFILES_COUNT"; //$NON-NLS-1$
    private static final String ID_ACTIVE_PROFILE_NAME = ".ACTIVE_PROFILE_NAME"; //$NON-NLS-1$
    private static final String ID_PROFILE = ".PROFILE_"; //$NON-NLS-1$
    private static final String XML_FORMATTER_PROFILE_SECTION = "FormatterProfileSection_"; //$NON-NLS-1$
    private static String importExportPath = null;

    private String activeProfileName;
    private List<IProfile> profiles;
    private final IProfile profileFactory;
    private final String idInStore;
    
    
    public ProfileManager(List<IProfile> initialProfiles, IProfile profileFactory, String idInStore) {
        this.profileFactory = profileFactory;
        this.idInStore = idInStore;
        if (initialProfiles != null) { 
            profiles = initialProfiles;
        } else {
            profiles = new ArrayList<IProfile>();
        }
        setActiveProfileName(""); //$NON-NLS-1$
    }
    
    public String getActiveProfileName() {
        if (activeProfileName.isEmpty() && profiles.size()>0) {
            return profiles.get(0).getName();
        }
        return activeProfileName;
    }

    public void setActiveProfileName(String activeProfileName) {
        this.activeProfileName = activeProfileName;
        IProfile ip = getActiveProfile();
        if (ip != null) {
            ip.reActivate();
        }
    }

    public IProfile getActiveProfile() {
        return getProfile(activeProfileName);
    }

    public IProfile getProfile(String name) {
        for (IProfile ip : profiles) {
            if (ip.getName().equals(name)) {
                return ip;
            }
        }
        return null;
    }

    public IProfile get(int idx) {
        return profiles.get(idx);
    }
    
    public int size() {
        return profiles.size();
    }

    public void remove(int idx) {
        profiles.remove(idx);
    }
    
    public void add(IProfile ip) {
        profiles.add(ip);
    }
    
    public List<IProfile> getProfiles() {
        return Collections.unmodifiableList(profiles);
    }
    
    public List<String> getProfileNames() {
        ArrayList<String> lst = new ArrayList<String>();
        for (IProfile ip : profiles) {
            lst.add(ip.getName());
        }
        return lst;
    }
    
    public void saveToStore(IPreferenceStore ips) {
        int oldCount = ips.getInt(idInStore + ID_PROFILES_COUNT);
        
        ips.setValue(idInStore + ID_ACTIVE_PROFILE_NAME, activeProfileName);
        int prfCount = 0;
        for (IProfile ip : profiles) {
            if (!ip.isDefaultProfile()) {
                XMLMemento memento = XMLMemento.createWriteRoot("tagFormatterProfile"); //$NON-NLS-1$
                ip.toMemento(memento);
                StringWriter sw = new StringWriter();
                try {
                    memento.save(sw);
                } catch(Exception e) {
                    LogHelper.logError(e);
                }
                ips.setValue(idInStore + ID_PROFILE + prfCount++, sw.toString());
            }
        }
        ips.setValue(idInStore + ID_PROFILES_COUNT, prfCount);
        while (prfCount < oldCount) {
            ips.setDefault(idInStore + ID_PROFILE + prfCount++, ""); //$NON-NLS-1$
        }
        
        IProfile ip = getActiveProfile();
        if (ip != null) {
            ip.reActivate();
        }
    }
    
    /**
     * Read all profiles from the given store to this profile manager
     * @param ips
     */
    public void readFromStore(IPreferenceStore ips) {
        int allCount = ips.getInt(idInStore + ID_PROFILES_COUNT);
        for (int cnt=0; cnt < allCount; ++cnt) {
            try {
                String xml = ips.getString(idInStore + ID_PROFILE + cnt);
                StringReader reader = new StringReader(xml);
                XMLMemento memento = XMLMemento.createReadRoot(reader);
                IProfile ip = profileFactory.createFromMemento(memento);
                if (ip != null) {
                    String name = ip.getName();
                    IProfile ipOld = getProfile(name);
                    if (ipOld == null) {
                        profiles.add(ip);
                    } else {
                        ipOld.copyFrom(ip);
                    }
                }
            } catch (Exception e) {
                LogHelper.logError(e);
            }
        }
        setActiveProfileName(ips.getString(idInStore + ID_ACTIVE_PROFILE_NAME));
    }

    public static String readActiveProfileNameFromStore(IPreferenceStore ips, String id) {
        return ips.getString(id + ID_ACTIVE_PROFILE_NAME);
    }
    
    public static IProfile readProfileFromStore(String profName, IPreferenceStore ips, String id, IProfile profileFactory) {
        int allCount = ips.getInt(id + ID_PROFILES_COUNT);
        for (int cnt=0; cnt < allCount; ++cnt) {
            try {
                String xml = ips.getString(id + ID_PROFILE + cnt);
                StringReader reader = new StringReader(xml);
                XMLMemento memento = XMLMemento.createReadRoot(reader);
                IProfile ip = profileFactory.createFromMemento(memento);
                if (ip != null && profName.equals(ip.getName())) {
                    return ip;
                }
            } catch (Exception e) {
                LogHelper.logError(e);
            }
        }
        return null;
    }

    //////// Export/import dialogs:
    
    /**
     * Play export dialog and do the export 
     * 
     * @param prof - null to export all or the given profile
     * @param pm   - profile manager to get profiles from (when 'prof' == null)
     */
    public static void exportProfiles(Shell shell, IProfile prof, ProfileManager pm) {
        String title = prof==null ? Messages.ProfileManager_ExportProfile : Messages.ProfileManager_ExportProfiles;
        String s = SWTFactory.browseFile(shell, true, title, new String[]{"*.xml"}, importExportPath); //$NON-NLS-1$
        if (s != null) {
            File f = new File(s);
            if (f.exists()) {
                if (!SWTFactory.YesNoQuestion(shell, title, 
                                              String.format(Messages.ProfileManager_ReplaceQuestion, s))) 
                {
                    return;
                }
            }

            importExportPath = f.getParentFile().getAbsolutePath();
            XMLMemento memento = XMLMemento.createWriteRoot("tagExportedFormatterProfiles"); //$NON-NLS-1$
            int memNum = 0;
            int total = prof == null ? pm.size() : 1;
            for (int num=0; num < total; ++num) {
                IProfile ip = prof == null ? pm.get(num) : prof;
                IMemento childMem = memento.createChild(XML_FORMATTER_PROFILE_SECTION + memNum++);
                ip.toMemento(childMem);
            }
            try {
                f.delete();
                FileWriter fw = new FileWriter(f);
                memento.save(fw);
                fw.close();
            } catch(Exception e) {
                LogHelper.logError(e);
            }
        }
    }

    
        
    
    /**
     * Play import dialog and do the import 
     * 
     */
    public void importProfiles(Shell shell) {
        String title = Messages.ProfileManager_ImportProfiles;
        String s = SWTFactory.browseFile(shell, false, title, new String[]{"*.xml"}, importExportPath); //$NON-NLS-1$
        if (s != null) {
            ArrayList<IProfile> addedProfs = new ArrayList<ProfileManager.IProfile>();
            try {
                File f = new File(s);
                importExportPath = f.getParentFile().getAbsolutePath();
                FileReader fr = new FileReader(f);
                XMLMemento memento = XMLMemento.createReadRoot(fr);
                for (int memNum=0; true; ++memNum) {
                    IMemento childMem = memento.getChild(XML_FORMATTER_PROFILE_SECTION + memNum);
                    if (childMem == null) {
                        break;
                    }
                    IProfile ip = profileFactory.createFromMemento(childMem);
                    if (ip != null && !ip.isDefaultProfile()) {
                        IProfile ipOld = getProfile(ip.getName());
                        if (ipOld == null) {
                            add(ip);
                            addedProfs.add(ip);
                        } else if (!ipOld.isDefaultProfile()) {
                            ipOld.copyFrom(ip);
                            addedProfs.add(ip);
                        }
                    }
                }
            } catch (Exception e) {
                LogHelper.logError(e);
            }
            
            String desc = String.format(Messages.ProfileManager_NoProfilesFound, s);
            if (!addedProfs.isEmpty()) {
                desc = String.format(Messages.ProfileManager_ImportedProfiles, addedProfs.size());
                for (IProfile ip : addedProfs) {
                    desc += ip.getName(); 
                    desc += "\n"; //$NON-NLS-1$
                }
            }
            SWTFactory.OkMessageBox(shell, title, desc); 
        }
    }


    public interface IProfile {
        public String getName();
        public boolean isDefaultProfile(); // not saved to store, should be passed to ProfileManager constructor
        public void reActivate();          // profile becomes active or it is active and its settings was changed
        public void copyFrom(IProfile ip);
        public void toMemento(IMemento memento);

        //---  all IProfile may work as its class factory:
        
        /**
         * 
         * @param xml
         * @param subRoot - null to create profile from the xml root or xml section name to get profile from it
         * @return
         */
        public IProfile createFromMemento(IMemento memento);
        
        public IProfile createFromProfile(IProfile from, String newName); 
    }
    
}
