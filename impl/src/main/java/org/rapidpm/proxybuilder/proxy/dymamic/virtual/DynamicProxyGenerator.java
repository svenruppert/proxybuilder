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


import org.rapidpm.proxybuilder.proxy.dymamic.virtual.strategy.*;
import org.rapidpm.proxybuilder.proxy.dymamic.virtual.factory.ServiceFactory;

import java.lang.reflect.Proxy;

import static org.rapidpm.proxybuilder.proxy.dymamic.virtual.strategy.CreationStrategy.NONE;

public class DynamicProxyGenerator<I, C extends I> {

  private Class<I>          subject; // Interface
  private CreationStrategy  creationStrategy; // StrategyFactory Selector
  private ServiceFactory<C> serviceFactory;

  private DynamicProxyGenerator() {
  }

  private DynamicProxyGenerator(final Builder builder) {
    subject = builder.subject;
    creationStrategy = builder.creationStrategy;
    serviceFactory = builder.serviceFactory;
  }

  public static <I, C extends I> Builder<I, C> newBuilder() {
    return new Builder<>();
  }

  public I make() {

    VirtualDynamicProxyInvocationHandler<I, C> dynamicProxy = VirtualDynamicProxyInvocationHandler
        .<I, C>newBuilder()
        .withServiceStrategyFactory(createStrategyFactory())
        .withServiceFactory(serviceFactory)
        .build();

    final Object newProxyInstance = Proxy
        .newProxyInstance(
            subject.getClassLoader(),
            new Class<?>[]{subject},
            dynamicProxy);
    return subject.cast(newProxyInstance);
  }

  private ServiceStrategyFactory<C> createStrategyFactory() {
    ServiceStrategyFactory<C> serviceStrategyFactory;
    switch (creationStrategy) {
      case NONE:
        serviceStrategyFactory = new ServiceStrategyFactoryNotThreadSafe<>();
        break;
      case SOME_DUPLICATES:
        serviceStrategyFactory = new ServiceStrategyFactorySomeDuplicates<>(); //missing
        break;
      case SYNCHRONIZED:
        serviceStrategyFactory = new ServiceStrategyFactorySynchronized<>();
        break;
      case NO_DUPLICATES:
        serviceStrategyFactory = new ServiceStrategyFactoryNoDuplicates<>(); //missing
        break;
      case METHOD_SCOPED:
        serviceStrategyFactory = new ServiceStrategyFactoryMethodScoped<>();
        break;
      default:
        serviceStrategyFactory = new ServiceStrategyFactoryNotThreadSafe<>();
    }
    return serviceStrategyFactory;
  }

  public static final class Builder<I, C extends I> {
    private Class<I> subject;
    private CreationStrategy creationStrategy = NONE;
    private ServiceFactory<C> serviceFactory;

    private Builder() {
    }

    public Builder<I, C> withSubject(final Class<I> subject) {
      this.subject = subject;
      return this;
    }

    public Builder<I, C> withCreationStrategy(final CreationStrategy creationStrategy) {
      this.creationStrategy = creationStrategy;
      return this;
    }

    public Builder<I, C> withServiceFactory(final ServiceFactory<C> serviceFactory) {
      this.serviceFactory = serviceFactory;
      return this;
    }

    public DynamicProxyGenerator<I, C> build() {
      return new DynamicProxyGenerator<>(this);
    }
  }
}
