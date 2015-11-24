package org.rapidpm.proxybuilder.type.dymamic.virtual.creationstrategy;


import org.rapidpm.proxybuilder.type.dymamic.virtual.VirtualDynamicProxyInvocationHandler;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by svenruppert on 06.11.15.
 */
public class ServiceStrategyFactorySomeDuplicates<T> implements VirtualDynamicProxyInvocationHandler.ServiceStrategyFactory<T> {
  private final AtomicReference<T> ref = new AtomicReference<>();

  @Override
  public T realSubject(VirtualDynamicProxyInvocationHandler.ServiceFactory<T> factory) {

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
