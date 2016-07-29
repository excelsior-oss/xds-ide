package com.excelsior.xds.launching.commons.internal.delegate;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;

import com.excelsior.xds.builder.listener.IBuilderListener;
import com.excelsior.xds.core.builders.XdsBuildResult;

public class BuildListener implements IBuilderListener {
    
    private static Set<IBuilderListener> listeners = new HashSet<IBuilderListener>();
    
    public static void addListener(IBuilderListener l) {
        listeners.add(l);
    }

    public static void removeListener(IBuilderListener l) {
        listeners.remove(l);
    }

	public BuildListener() {
	}

	@Override
	public void onBuildStarted(IProject p) {
	    for (IBuilderListener l : listeners) {
	        l.onBuildStarted(p);
	    }
	}

	@Override
	public void onBuildFinished(IProject p, XdsBuildResult buildRes) {
        for (IBuilderListener l : listeners) {
            l.onBuildFinished(p, buildRes);
        }
	}
}