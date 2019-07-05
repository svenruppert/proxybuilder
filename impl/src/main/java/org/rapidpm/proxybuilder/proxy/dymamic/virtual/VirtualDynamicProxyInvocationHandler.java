/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
  Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package org.rapidpm.proxybuilder.proxy.dymamic.virtual;


import org.rapidpm.proxybuilder.proxy.dymamic.virtual.factory.ServiceFactory;
import org.rapidpm.proxybuilder.proxy.dymamic.virtual.strategy.ServiceStrategyFactory;

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
