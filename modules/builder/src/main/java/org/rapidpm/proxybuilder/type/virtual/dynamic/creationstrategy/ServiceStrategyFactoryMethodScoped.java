package org.rapidpm.proxybuilder.type.virtual.dynamic.creationstrategy;

import org.rapidpm.proxybuilder.type.virtual.dynamic.VirtualDynamicProxyInvocationHandler;

/**
 * Created by svenruppert on 06.11.15.
 */
public class ServiceStrategyFactoryMethodScoped<T> implements VirtualDynamicProxyInvocationHandler.ServiceStrategyFactory<T> {

  @Override
  public synchronized T realSubject(VirtualDynamicProxyInvocationHandler.ServiceFactory<T> factory) {
    return factory.createInstance();
  }
}
