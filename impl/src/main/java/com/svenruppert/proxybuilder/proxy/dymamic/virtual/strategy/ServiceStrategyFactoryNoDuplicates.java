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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServiceStrategyFactoryNoDuplicates<T> implements ServiceStrategyFactory<T> {

  private final Lock initializationLock = new ReentrantLock();
  private volatile T realSubject;

  @Override
  public T realSubject(ServiceFactory<T> factory) {
    T result = realSubject;
    if (result == null) {
      initializationLock.lock();
      try {
        result = realSubject;
        if (result == null) {
          result = realSubject = factory.createInstance();
        }
      } finally {
        initializationLock.unlock();
      }
    }
    return result;
  }
}