package com.excelsior.xds.ui.internal.update;

public interface IUpdateChecker {
	public static final String SERVICE_NAME = IUpdateChecker.class.getName();
	public static long ONE_TIME_CHECK = -1L;

	public abstract void addUpdateCheck(long delay, long poll, IUpdateListener listener);

	public abstract void removeUpdateCheck(IUpdateListener listener);
}