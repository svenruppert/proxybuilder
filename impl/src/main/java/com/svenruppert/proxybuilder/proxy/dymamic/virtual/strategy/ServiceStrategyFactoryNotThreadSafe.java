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

public class ServiceStrategyFactoryNotThreadSafe<T>
    implements ServiceStrategyFactory<T> {

  private T service; //nix lambda

  @Override
  public T realSubject(ServiceFactory<T> factory) {
    if (service == null) {
      service = factory.createInstance();
    }
    return service;
  }

}