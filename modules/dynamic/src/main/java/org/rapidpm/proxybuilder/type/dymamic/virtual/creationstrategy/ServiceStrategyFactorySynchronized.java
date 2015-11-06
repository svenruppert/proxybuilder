package org.rapidpm.proxybuilder.type.dymamic.virtual.creationstrategy;


import org.rapidpm.proxybuilder.type.dymamic.virtual.VirtualDynamicProxyInvocationHandler;

/**
 * Created by svenruppert on 05.11.15.
 */
public class ServiceStrategyFactorySynchronized<T> implements VirtualDynamicProxyInvocationHandler.ServiceStrategyFactory<T> {

  private T service = null; //nix lambda

  @Override
  public synchronized T realSubject(VirtualDynamicProxyInvocationHandler.ServiceFactory<T> factory) {
    if (service == null) {
      service = factory.createInstance();
    }
    return service;
  }
}
