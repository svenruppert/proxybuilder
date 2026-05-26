/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the EUPL-1.2.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package com.svenruppert.proxybuilder.objectadapter.dynamic;


import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by RapidPM - Team
 */
public record MethodIdentifier(String name, List<Class<?>> parameters) {

  public MethodIdentifier {
    parameters = List.copyOf(parameters);
  }

  public MethodIdentifier(Method m) {
    this(m.getName(), List.of(m.getParameterTypes()));
  }

  @Override
  public List<Class<?>> parameters() {
    return List.copyOf(parameters);
  }
}