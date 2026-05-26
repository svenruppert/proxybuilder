/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the EUPL-1.2.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package com.svenruppert.proxybuilder;

import com.squareup.javapoet.TypeName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.List;

public record MethodIdentifier(String name, List<TypeName> parameters) {

  public MethodIdentifier {
    parameters = List.copyOf(parameters);
  }

  public static MethodIdentifier of(final ExecutableElement methodElement) {
    return new MethodIdentifier(
        methodElement.getSimpleName().toString(),
        methodElement.getParameters()
            .stream()
            .map(VariableElement::asType)
            .map(TypeName::get)
            .toList());
  }

  @Override
  public List<TypeName> parameters() {
    return List.copyOf(parameters);
  }
}