/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.rapidpm.proxybuilder.type.dymamic.virtual.creationstrategy;


import org.rapidpm.proxybuilder.type.dymamic.virtual.VirtualDynamicProxyInvocationHandler.ServiceFactory;
import org.rapidpm.proxybuilder.type.dymamic.virtual.VirtualDynamicProxyInvocationHandler.ServiceStrategyFactory;

import java.util.concurrent.atomic.AtomicReference;

public class ServiceStrategyFactorySomeDuplicates<T> implements ServiceStrategyFactory<T> {
  private final AtomicReference<T> ref = new AtomicReference<>();

  @Override
  public T realSubject(ServiceFactory<T> factory) {

    T service = ref.get();
    if (service == null) {
      service = factory.createInstance();
      if (!ref.compareAndSet(null, service)) {
        service = ref.get();
      }
    }
    return service;
  }
}
