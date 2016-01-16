package org.rapidpm.proxybuilder.type.dymamic.virtual.creationstrategy;


import org.rapidpm.proxybuilder.type.dymamic.virtual.VirtualDynamicProxyInvocationHandler.ServiceFactory;
import org.rapidpm.proxybuilder.type.dymamic.virtual.VirtualDynamicProxyInvocationHandler.ServiceStrategyFactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Sven Ruppert on 06.11.15.
 */
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
