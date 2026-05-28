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

class ProxyEnforcementTest {

  interface MemberDirectory {
    String find(String id);
  }

  static class MemberDirectoryImpl implements MemberDirectory {
    @Override
    public String find(final String id) {
      return "raw:" + id;
    }
  }

  static class MemberDirectorySecured extends MemberDirectoryImpl {
    @Override
    public String find(final String id) {
      return "secured:" + id;
    }
  }

  @WrappedBy(MemberDirectorySecured.class)
  static class AnnotatedMemberDirectory extends MemberDirectoryImpl {
  }

  static class AnnotatedAndWrapped extends MemberDirectorySecured {
  }

  @WrappedBy(MemberDirectorySecured.class)
  static class AnnotatedAndWrappedDirectly extends MemberDirectorySecured {
  }

  static class Unannotated {
  }

  @Test
  void requireWrappedAcceptsAnInstanceOfTheDeclaredWrapper() {
    final AnnotatedAndWrappedDirectly instance = new AnnotatedAndWrappedDirectly();
    Assertions.assertSame(instance, ProxyEnforcement.requireWrapped(instance));
  }

  @Test
  void requireWrappedRejectsTheBareOriginal() {
    final AnnotatedMemberDirectory bare = new AnnotatedMemberDirectory();
    final IllegalStateException ex = Assertions.assertThrows(
        IllegalStateException.class,
        () -> ProxyEnforcement.requireWrapped(bare));
    Assertions.assertTrue(ex.getMessage().contains(MemberDirectorySecured.class.getName()),
                          ex.getMessage());
  }

  @Test
  void requireWrappedPassesThroughForUnannotatedTypes() {
    final Unannotated instance = new Unannotated();
    Assertions.assertSame(instance, ProxyEnforcement.requireWrapped(instance));
  }
}
