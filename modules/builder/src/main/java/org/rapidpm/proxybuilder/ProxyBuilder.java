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
package org.rapidpm.proxybuilder;

import org.rapidpm.proxybuilder.proxy.dymamic.DynamicProxyBuilder;
import org.rapidpm.proxybuilder.proxy.dymamic.virtual.CreationStrategy;
import org.rapidpm.proxybuilder.proxy.dymamic.virtual.VirtualDynamicProxyInvocationHandler.ServiceFactory;

public class ProxyBuilder {


  private ProxyBuilder() {
  }


  public static <I, T extends I> DynamicProxyBuilder<I, T> newDynamicProxyBuilder(Class<I> clazz, T original) {
    return DynamicProxyBuilder.createBuilder(clazz, original);
  }


  public static <I, T extends I> DynamicProxyBuilder<I, T> newDynamicVirtualProxyBuilder(Class<I> clazz, Class<T> original, CreationStrategy creationStrategy) {
    return DynamicProxyBuilder.createBuilder(clazz, original, creationStrategy);
  }

  public static <I> DynamicProxyBuilder<I, I> newDynamicVirtualProxyBuilder(Class<I> clazz,
                                                                            CreationStrategy creationStrategy,
                                                                            ServiceFactory<I> serviceFactory) {
    return DynamicProxyBuilder.createBuilder(clazz, creationStrategy, serviceFactory);
  }


}
