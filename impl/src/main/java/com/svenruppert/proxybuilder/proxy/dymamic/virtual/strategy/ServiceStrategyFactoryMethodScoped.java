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

public class ServiceStrategyFactoryMethodScoped<T> implements ServiceStrategyFactory<T> {

  @Override
  public synchronized T realSubject(ServiceFactory<T> factory) {
    return factory.createInstance();
  }
}