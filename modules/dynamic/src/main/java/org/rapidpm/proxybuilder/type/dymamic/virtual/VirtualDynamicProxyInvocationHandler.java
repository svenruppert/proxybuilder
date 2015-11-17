/*
 * Copyright [2014] [www.rapidpm.org / Sven Ruppert (sven.ruppert@rapidpm.org)]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.rapidpm.proxybuilder.type.dymamic.virtual;


import org.rapidpm.proxybuilder.type.dymamic.DynamicProxyBuilder;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by Sven Ruppert on 14.01.14.
 */
public class VirtualDynamicProxyInvocationHandler<I, C extends I> implements InvocationHandler {

  private ServiceFactory<C> serviceFactory;
  private ServiceStrategyFactory<C> serviceStrategyFactory;

  private VirtualDynamicProxyInvocationHandler() {
  }

  private VirtualDynamicProxyInvocationHandler(final Builder<I, C> builder) {
    serviceFactory = builder.serviceFactory;
    serviceStrategyFactory = builder.serviceStrategyFactory;
  }


  public static <I, C extends I> Builder<I, C> newBuilder() {
    return new Builder<>();
  }


  public Object invoke(Object proxy, @Nonnull Method method, Object[] args) throws Throwable {
    final C obj = serviceStrategyFactory.realSubject(serviceFactory);
    return DynamicProxyBuilder.invoke(obj, method, args);
//    invoke(obj, method, args);
  }

  /**
   * Strategy of creating: ThreadSave, NotThreadSave, ...
   */
  public interface ServiceStrategyFactory<C> {
    C realSubject(ServiceFactory<C> factory);
  }

  public interface ServiceFactory<C> {
    C createInstance();
  }

  public static final class Builder<I, C extends I> {
    private ServiceFactory<C> serviceFactory;
    private ServiceStrategyFactory<C> serviceStrategyFactory;

    private Builder() {
    }

    @Nonnull
    public Builder<I, C> withServiceFactory(@Nonnull final ServiceFactory<C> serviceFactory) {
      this.serviceFactory = serviceFactory;
      return this;
    }

    @Nonnull
    public Builder<I, C> withServiceStrategyFactory(@Nonnull final ServiceStrategyFactory<C> serviceStrategyFactory) {
      this.serviceStrategyFactory = serviceStrategyFactory;
      return this;
    }

    @Nonnull
    public VirtualDynamicProxyInvocationHandler<I, C> build() {
      return new VirtualDynamicProxyInvocationHandler<>(this);
    }
  }
}
