/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the EUPL-1.2.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package com.svenruppert.proxybuilder.proxy.dymamic.virtual.strategy;



import com.svenruppert.proxybuilder.proxy.dymamic.virtual.factory.ServiceFactory;

import java.util.concurrent.atomic.AtomicReference;

public class ServiceStrategyFactorySomeDuplicates<T> implements ServiceStrategyFactory<T> {
  private final AtomicReference<T> ref = new AtomicReference<>();

  @Override
  public T realSubject(ServiceFactory<T> factory) {

    T service = ref.get();
    if (service == null) {
      service = factory.createInstance();
      if (!ref.compareAndSet(null, service)) {
        service = ref.get();
      }
    }
    return service;
  }
}