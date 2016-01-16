package org.rapidpm.proxybuilder.type.dymamic.virtual.creationstrategy;


import org.rapidpm.proxybuilder.type.dymamic.virtual.VirtualDynamicProxyInvocationHandler.ServiceFactory;
import org.rapidpm.proxybuilder.type.dymamic.virtual.VirtualDynamicProxyInvocationHandler.ServiceStrategyFactory;

/**
 * Created by Sven Ruppert on 05.11.15.
 */
public class ServiceStrategyFactorySynchronized<T> implements ServiceStrategyFactory<T> {

  private T service; //nix lambda

  @Override
  public synchronized T realSubject(ServiceFactory<T> factory) {
    if (service == null) {
      service = factory.createInstance();
    }
    return service;
  }
}
