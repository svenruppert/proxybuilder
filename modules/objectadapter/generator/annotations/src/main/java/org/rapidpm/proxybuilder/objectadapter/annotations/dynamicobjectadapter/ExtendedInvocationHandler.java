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
package org.rapidpm.proxybuilder.objectadapter.annotations.dynamicobjectadapter;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public abstract class ExtendedInvocationHandler<T> implements InvocationHandler {

  private final Map<MethodIdentifier, Method> adaptedMethods = new HashMap<>();
  private final Map<MethodIdentifier, Object> adapters = new HashMap<>();
  private T original;

  public void setOriginal(T original) {
    this.original = original;
  }

  public void addAdapter(Object adapter) {
    final Class<?> adapterClass = adapter.getClass();
    Method[] methods = adapterClass.getDeclaredMethods();
    for (Method m : methods) {
      final MethodIdentifier key = new MethodIdentifier(m);
      adaptedMethods.put(key, m);
      adapters.put(key, adapter);
    }
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      final MethodIdentifier key = new MethodIdentifier(method);
      Method other = adaptedMethods.get(key);
      if (other != null) {
        other.setAccessible(true); //Lambdas...
        final Object result = other.invoke(adapters.get(key), args);
        other.setAccessible(false);
        return result;
      } else {
        return method.invoke(original, args);
      }
    } catch (InvocationTargetException e) {
      throw e.getTargetException();
    }
  }

}
