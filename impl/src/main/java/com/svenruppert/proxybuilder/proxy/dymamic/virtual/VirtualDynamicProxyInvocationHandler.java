/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the EUPL-1.2.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package com.svenruppert.proxybuilder.proxy.dymamic.virtual;



import com.svenruppert.proxybuilder.proxy.dymamic.virtual.factory.ServiceFactory;
import com.svenruppert.proxybuilder.proxy.dymamic.virtual.strategy.ServiceStrategyFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class VirtualDynamicProxyInvocationHandler<I, C extends I> implements InvocationHandler {

  private ServiceFactory<C>         serviceFactory;
  private ServiceStrategyFactory<C> serviceStrategyFactory;

  public static <I, C extends I> Builder<I, C> newBuilder() {
    return new Builder<>();
  }

  private VirtualDynamicProxyInvocationHandler(final Builder<I, C> builder) {
    serviceFactory = builder.serviceFactory;
    serviceStrategyFactory = builder.serviceStrategyFactory;
  }

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    final C obj = serviceStrategyFactory.realSubject(serviceFactory);
    try {
      return method.invoke(obj, args);
    } catch (InvocationTargetException ex) {
      throw ex.getCause();
    }
    //    invoke(obj, method, args);
  }

  public static final class Builder<I, C extends I> {
    private ServiceFactory<C> serviceFactory;
    private ServiceStrategyFactory<C> serviceStrategyFactory;

    private Builder() {
    }

    public Builder<I, C> withServiceFactory(final ServiceFactory<C> serviceFactory) {
      this.serviceFactory = serviceFactory;
      return this;
    }

    public Builder<I, C> withServiceStrategyFactory(final ServiceStrategyFactory<C> serviceStrategyFactory) {
      this.serviceStrategyFactory = serviceStrategyFactory;
      return this;
    }

    public VirtualDynamicProxyInvocationHandler<I, C> build() {
      return new VirtualDynamicProxyInvocationHandler<>(this);
    }
  }
}