/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the EUPL-1.2.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package com.svenruppert.proxybuilder.annotations;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

class AnnotationContractTest {

  @Test
  void generatedByProxyBuilderIsRuntimeOnType() {
    assertContract(GeneratedByProxyBuilder.class, RetentionPolicy.RUNTIME, ElementType.TYPE);
  }

  @Test
  void skipProxyIsSourceOnMethodOrConstructor() {
    assertContract(SkipProxy.class, RetentionPolicy.SOURCE,
                   ElementType.METHOD, ElementType.CONSTRUCTOR);
  }

  @Test
  void proxyBuilderOptionsIsSourceOnType() {
    assertContract(ProxyBuilderOptions.class, RetentionPolicy.SOURCE, ElementType.TYPE);
  }

  @Test
  void delegatesToIsRuntimeOnMethod() {
    assertContract(DelegatesTo.class, RetentionPolicy.RUNTIME, ElementType.METHOD);
  }

  @Test
  void wrappedByIsRuntimeOnType() {
    assertContract(WrappedBy.class, RetentionPolicy.RUNTIME, ElementType.TYPE);
  }

  @Test
  void internalIsClassOnEverythingThatCouldLeak() {
    assertContract(Internal.class, RetentionPolicy.CLASS,
                   ElementType.TYPE, ElementType.METHOD,
                   ElementType.CONSTRUCTOR, ElementType.FIELD);
  }

  @Test
  void proxyEntryIsSourceOnMethod() {
    assertContract(ProxyEntry.class, RetentionPolicy.SOURCE, ElementType.METHOD);
  }

  @Test
  void generatedSourceIsSourceOnMembers() {
    assertContract(GeneratedSource.class, RetentionPolicy.SOURCE,
                   ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD);
  }

  @Test
  void proxyNameIsSourceOnType() {
    assertContract(ProxyName.class, RetentionPolicy.SOURCE, ElementType.TYPE);
  }

  @Test
  void failOnStaticEnumHasThreeStates() {
    Assertions.assertEquals(3, ProxyBuilderOptions.FailOnStatic.values().length);
    Assertions.assertNotNull(ProxyBuilderOptions.FailOnStatic.valueOf("DEFAULT"));
    Assertions.assertNotNull(ProxyBuilderOptions.FailOnStatic.valueOf("TRUE"));
    Assertions.assertNotNull(ProxyBuilderOptions.FailOnStatic.valueOf("FALSE"));
  }

  private static void assertContract(final Class<? extends Annotation> annotationType,
                                     final RetentionPolicy expectedRetention,
                                     final ElementType... expectedTargets) {
    final Retention retention = annotationType.getAnnotation(Retention.class);
    Assertions.assertNotNull(retention, annotationType.getName() + " must declare @Retention");
    Assertions.assertEquals(expectedRetention, retention.value(),
                            annotationType.getName() + " retention");

    final Target target = annotationType.getAnnotation(Target.class);
    Assertions.assertNotNull(target, annotationType.getName() + " must declare @Target");
    Assertions.assertEquals(Set.of(expectedTargets), Set.of(target.value()),
                            annotationType.getName() + " targets");
  }
}
