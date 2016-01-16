package org.rapidpm.proxybuilder.type.dymamic.virtual.creationstrategy;


import org.rapidpm.proxybuilder.type.dymamic.virtual.VirtualDynamicProxyInvocationHandler.ServiceFactory;
import org.rapidpm.proxybuilder.type.dymamic.virtual.VirtualDynamicProxyInvocationHandler.ServiceStrategyFactory;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Sven Ruppert on 06.11.15.
 */
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
