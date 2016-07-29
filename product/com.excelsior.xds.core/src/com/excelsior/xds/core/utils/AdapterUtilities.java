package com.excelsior.xds.core.utils;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;

public class AdapterUtilities {
  @SuppressWarnings("unchecked")
  public static <T> T getAdapter(Object sourceObject, Class<T> adapterType) {
    Assert.isNotNull(adapterType);
    if (sourceObject == null) {
      return null;
    }
    if (adapterType.isInstance(sourceObject)) {
      return (T) sourceObject;
    }

    if (sourceObject instanceof IAdaptable) {
      IAdaptable adaptable = (IAdaptable) sourceObject;

      Object result = adaptable.getAdapter(adapterType);
      if (result != null) {
        // Sanity-check
        Assert.isTrue(adapterType.isInstance(result));
        return (T) result;
      }
    }

    if (!(sourceObject instanceof PlatformObject)) {
      Object result = Platform.getAdapterManager().getAdapter(sourceObject, adapterType);
      if (result != null) {
        return (T) result;
      }
    }

    return null;
  }
}
