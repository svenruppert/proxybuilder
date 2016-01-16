package org.rapidpm.proxybuilder.type.dymamic.virtual.creationstrategy;


import org.rapidpm.proxybuilder.type.dymamic.virtual.VirtualDynamicProxyInvocationHandler.ServiceFactory;
import org.rapidpm.proxybuilder.type.dymamic.virtual.VirtualDynamicProxyInvocationHandler.ServiceStrategyFactory;

/**
 * Created by Sven Ruppert on 21.07.15.
 */
public class ServiceStrategyFactoryNotThreadSafe<T> implements ServiceStrategyFactory<T> {

  private T service; //nix lambda

  @Override
  public T realSubject(ServiceFactory<T> factory) {
    if (service == null) {
      service = factory.createInstance();
    }
    return service;
  }

}
