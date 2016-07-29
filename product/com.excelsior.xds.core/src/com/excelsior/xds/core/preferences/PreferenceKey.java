package com.excelsior.xds.core.preferences;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.IWorkingCopyManager;

public final class PreferenceKey {
    private static final IScopeContext defScope = InstanceScope.INSTANCE;
    private static final IWorkingCopyManager defManager = null;
    
	private final String fQualifier;
	private final String fKey;
	private final String defStringValue;
    private final boolean defBooleanValue;
    private final int defIntValue;

	public PreferenceKey(String qualifier, String key) {
	    this(qualifier, key, null, false, 0);
	}
	
    public PreferenceKey(String qualifier, String key, String defStringValue) {
        this(qualifier, key, defStringValue, false, 0);
    }
    
    public PreferenceKey(String qualifier, String key, boolean defBooleanValue) {
        this(qualifier, key, null, defBooleanValue, 0);
    }
	
    public PreferenceKey(String qualifier, String key, int defIntValue) {
        this(qualifier, key, null, false, defIntValue);
    }
    
    private PreferenceKey(String qualifier, String key, String defStringValue, boolean defBooleanValue, int defIntValue) {
        this.fQualifier= qualifier;
        this.fKey= key;
        this.defStringValue = defStringValue;
        this.defBooleanValue = defBooleanValue;
        this.defIntValue = defIntValue;
    }
    
    /**
     * Check whether the event passed indicate the change of 'this' preference.
     * @param e event possibly indicating change of 'this' preference
     * @return whether this preference actually changed
     */
    public boolean isChanged(PreferenceChangeEvent e) {
    	String thisPath = "/" +defScope.getName() + "/" + getName(); //$NON-NLS-1$ //$NON-NLS-2$
    	String thatPath = e.getNode().absolutePath() + "/" + e.getKey(); //$NON-NLS-1$
    	return thisPath.equals(thatPath);
    }

	/**
	 * Full name of the preference, excluding the scope (because actually one preference key can be used in different scopes).
	 * @return
	 */
	public String getName() {
		return fQualifier + "/" + fKey; //$NON-NLS-1$
	}

	public String getStoredValue(IScopeContext context, IWorkingCopyManager manager) {
		return getNode(context, manager).get(fKey, defStringValue);
	}
	
    public String getStoredValue() {
        return getStoredValue(defScope, defManager);
    }
	
	public String getStoredValue(IScopeContext[] lookupOrder, boolean ignoreTopScope, IWorkingCopyManager manager) {
		for (int i= ignoreTopScope ? 1 : 0; i < lookupOrder.length; i++) {
			String value= getStoredValue(lookupOrder[i], manager);
			if (value != null) {
				return value;
			}
		}
		return null;
	}
	
    public boolean getStoredBoolean(){
        return getStoredBoolean(defScope, defManager);
    }

    public boolean getStoredBoolean(IScopeContext context, IWorkingCopyManager manager){
		return getNode(context, manager).getBoolean(fKey, defBooleanValue);
	}

    public int getStoredInt(){
        return getStoredInt(defScope, defManager);
    }

    public int getStoredInt(IScopeContext context, IWorkingCopyManager manager){
        return getNode(context, manager).getInt(fKey, defIntValue);
    }

    
    public void setStoredValue(String value) {
        setStoredValue(defScope, value, defManager);
    }

    public void setStoredValue(IScopeContext context, String value, IWorkingCopyManager manager) {
		if (value != null) {
			getNode(context, manager).put(fKey, value);
		} else {
			getNode(context, manager).remove(fKey);
		}
	}
	
    public void setStoredBoolean(boolean value) {
        setStoredBoolean(defScope, value, defManager);
    }
    
	public void setStoredBoolean(IScopeContext context, Boolean value, IWorkingCopyManager manager) {
		if (value != null) {
			getNode(context, manager).putBoolean(fKey, value);
		} else {
			getNode(context, manager).remove(fKey);
		}
	}

    public void setStoredInt(int value) {
        setStoredInt(defScope, value, defManager);
    }
    
    public void setStoredInt(IScopeContext context, Integer value, IWorkingCopyManager manager) {
        if (value != null) {
            getNode(context, manager).putInt(fKey, value);
        } else {
            getNode(context, manager).remove(fKey);
        }
    }
    
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return fQualifier + "/" + fKey; //$NON-NLS-1$
	}

	public String getKey() {
		return fKey;
	}
	
    public String getQualifier() {
        return fQualifier;
    }
    
	public String getDefStringValue() {
	    return defStringValue;
	}
	
	public boolean getDefBooleanValue() {
	    return defBooleanValue;
	}

    public int getDefIntValue() {
        return defIntValue;
    }

    public void addChangeListener(IPreferenceChangeListener listener) {
    	addChangeListener(getQualifier(), listener);
    }
    
    
    public static void addChangeListener(String qualifier, IPreferenceChangeListener listener) 
    {
        addChangeListener(defScope, defManager, qualifier, listener);
    }

    public static void addChangeListener(IScopeContext context, IWorkingCopyManager manager, 
                                         String qualifier, IPreferenceChangeListener listener) 
    {
        IEclipsePreferences node= context.getNode(qualifier);
        if (manager != null) {
            node =  manager.getWorkingCopy(node);
        }
        node.addPreferenceChangeListener(listener);
    }

    public static void removeChangeListener(String qualifier, IPreferenceChangeListener listener) 
    {
        removeChangeListener(defScope, defManager, qualifier, listener);
    }

    public static void removeChangeListener(IScopeContext context, IWorkingCopyManager manager, 
                                            String qualifier, IPreferenceChangeListener listener) 
    {
        IEclipsePreferences node= context.getNode(qualifier);
        if (manager != null) {
            node =  manager.getWorkingCopy(node);
        }
        node.removePreferenceChangeListener(listener);
    }
    
 
    private IEclipsePreferences getNode(IScopeContext context, IWorkingCopyManager manager) {
        IEclipsePreferences node= context.getNode(fQualifier);
        if (manager != null) {
            return manager.getWorkingCopy(node);
        }
        return node;
    }


}