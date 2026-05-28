/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the EUPL-1.2.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package com.svenruppert.proxybuilder.annotations;

public final class ProxyEnforcement {

  private ProxyEnforcement() {
  }

  public static <T> T requireWrapped(T instance) {
    Class<?> c = instance.getClass();
    WrappedBy wrappedBy = c.getAnnotation(WrappedBy.class);
    if (wrappedBy != null && !wrappedBy.value().isInstance(instance)) {
      throw new IllegalStateException(
          c.getName() + " must be used through its @WrappedBy(" +
          wrappedBy.value().getName() + ") subclass");
    }
    return instance;
  }
}
