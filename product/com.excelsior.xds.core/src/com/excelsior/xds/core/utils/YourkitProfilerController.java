package com.excelsior.xds.core.utils;

/**
 *  Sample usage:
 *  YourkitProfilerController.startProfiler();
    YourkitProfilerController.startCpuProfiling(YourkitProfilerController.CPU_SAMPLING, YourkitProfilerController.DEFAULT_FILTERS);
    
    YourkitProfilerController.captureSnapshot(YourkitProfilerController.SNAPSHOT_WITHOUT_HEAP);
 */
public class YourkitProfilerController {
  private static Object profiler;
  
  public static long CPU_SAMPLING;
  public static long CPU_TRACING;
  public static long CPU_J2EE;
  public static long SNAPSHOT_WITHOUT_HEAP;
  public static long SNAPSHOT_WITH_HEAP;
  
  public static String DEFAULT_FILTERS;
  
  public static void startProfiler() {
    Class<?> controllerClazz;
    Class<?> profilingModesClazz;
    try {
      controllerClazz = Class.forName("com.yourkit.api.Controller");
      profilingModesClazz = Class.forName("com.yourkit.api.ProfilingModes");
      
      DEFAULT_FILTERS = (String)ReflectionUtils.getField(controllerClazz, "DEFAULT_FILTERS", null, true);
      DEFAULT_FILTERS += System.getProperty("line.separator")+"com.excelsior.asda.dashmap.core.utils.YourkitProfilerController";
      DEFAULT_FILTERS += System.getProperty("line.separator")+"com.excelsior.asda.dashmap.ui.gis.JMapPaneProfiler";
      System.out.println("DEFAULT_FILTERS:"+DEFAULT_FILTERS);
      
      CPU_SAMPLING = (Long)ReflectionUtils.getField(profilingModesClazz, "CPU_SAMPLING", null, true);
      CPU_TRACING = (Long)ReflectionUtils.getField(profilingModesClazz, "CPU_TRACING", null, true);
      CPU_J2EE = (Long)ReflectionUtils.getField(profilingModesClazz, "CPU_J2EE", null, true);
      SNAPSHOT_WITHOUT_HEAP = (Long)ReflectionUtils.getField(profilingModesClazz, "SNAPSHOT_WITHOUT_HEAP", null, true);
      SNAPSHOT_WITH_HEAP = (Long)ReflectionUtils.getField(profilingModesClazz, "SNAPSHOT_WITH_HEAP", null, true);
    } catch (Exception e) {
      System.out.println("Profiler not present:" + e);
      return;
    }
    
    try {
      profiler = controllerClazz.newInstance();
    } catch (Exception e) {
      System.out.println("startProfiler: Profiler was active, but failed due: " + e);
    }
  }

  public static void takeMemorySnapshot() {
    if (profiler != null) {
      try {
        profiler.getClass().getMethod("forceGC").invoke(profiler);
        profiler.getClass().getMethod("captureMemorySnapshot").invoke(profiler);
      } catch (Exception e) {
        System.out.println("takeMemorySnapshot: Profiler was active, but failed due: " + e);
      }
    }
    else{
      System.out.println("takeMemorySnapshot: Profiler not present");
    }
  }
  
  public static void startCpuProfiling(long mode, String filters) {
    if (profiler != null) {
      try {
        profiler.getClass().getMethod("startCPUProfiling", long.class, String.class).invoke(profiler, mode, filters);
      } catch (Exception e) {
        System.out.println("startCpuProfiling: Profiler was active, but failed due: " + e);
      }
    }
    else{
      System.out.println("startCpuProfiling: Profiler not present");
    }
  }
  
  public static void stopCPUProfiling() {
    if (profiler != null) {
      try {
        profiler.getClass().getMethod("stopCPUProfiling").invoke(profiler);
      } catch (Exception e) {
        System.out.println("stopCPUProfiling: Profiler was active, but failed due: " + e);
      }
    }
    else{
      System.out.println("stopCPUProfiling: Profiler not present");
    }
  }

  public static void captureSnapshot(long snapshotFlags) {
    if (profiler != null) {
      try {
        String path = (String)profiler.getClass().getMethod("captureSnapshot", long.class).invoke(profiler, snapshotFlags);
        System.out.println();
        System.out.println("Snapshot was captured to path:" + path);
      } catch (Exception e) {
        System.out.println("captureSnapshot: Profiler was active, but failed due: " + e);
      }
    }
    else{
      System.out.println("captureSnapshot: Profiler not present");
    }
  }
}
