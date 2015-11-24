package org.rapidpm.proxybuilder.type.dymamic.virtual.creationstrategy;


import org.rapidpm.proxybuilder.type.dymamic.virtual.VirtualDynamicProxyInvocationHandler;

/**
 * Created by svenruppert on 21.07.15.
 */
public class ServiceStrategyFactoryNotThreadSafe<T> implements VirtualDynamicProxyInvocationHandler.ServiceStrategyFactory<T> {

  private T service = null; //nix lambda

  @Override
  public T realSubject(VirtualDynamicProxyInvocationHandler.ServiceFactory<T> factory) {
    if (service == null) {
      service = factory.createInstance();
    }
    return service;
  }

}
