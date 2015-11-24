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

package org.rapidpm.proxybuilder.objectadapter.annotations.dynamicobjectadapter;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created by Sven Ruppert
 */
public class MethodIdentifier {
  private final String name;
  private final Class[] parameters;

  public MethodIdentifier(Method m) {
    name = m.getName();
    parameters = m.getParameterTypes();
  }

  public int hashCode() {
    return name.hashCode();
  }

  // we can save time by assuming that we only compare against
  // other MethodIdentifier objects
  public boolean equals(Object o) {
    MethodIdentifier mid = (MethodIdentifier) o;
    return name.equals(mid.name) &&
        Arrays.equals(parameters, mid.parameters);
  }
}
