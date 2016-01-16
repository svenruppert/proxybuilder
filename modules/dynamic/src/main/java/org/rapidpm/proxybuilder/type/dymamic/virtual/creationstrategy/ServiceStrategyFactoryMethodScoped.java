package org.rapidpm.proxybuilder.type.dymamic.virtual.creationstrategy;


import org.rapidpm.proxybuilder.type.dymamic.virtual.VirtualDynamicProxyInvocationHandler.ServiceFactory;
import org.rapidpm.proxybuilder.type.dymamic.virtual.VirtualDynamicProxyInvocationHandler.ServiceStrategyFactory;

/**
 * Created by Sven Ruppert on 06.11.15.
 */
public class ServiceStrategyFactoryMethodScoped<T> implements ServiceStrategyFactory<T> {

  @Override
  public synchronized T realSubject(ServiceFactory<T> factory) {
    return factory.createInstance();
  }
}
