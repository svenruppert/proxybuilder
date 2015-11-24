package org.rapidpm.proxybuilder.type.dymamic.virtual.creationstrategy;


import org.rapidpm.proxybuilder.type.dymamic.virtual.VirtualDynamicProxyInvocationHandler;

/**
 * Created by svenruppert on 06.11.15.
 */
public class ServiceStrategyFactoryMethodScoped<T> implements VirtualDynamicProxyInvocationHandler.ServiceStrategyFactory<T> {

  @Override
  public synchronized T realSubject(VirtualDynamicProxyInvocationHandler.ServiceFactory<T> factory) {
    return factory.createInstance();
  }
}
