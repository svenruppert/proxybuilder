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

class ProxyBuilderVersionTest {

  @Test
  void versionConstantIsExposed() {
    Assertions.assertNotNull(ProxyBuilderVersion.VERSION);
    Assertions.assertFalse(ProxyBuilderVersion.VERSION.isBlank());
  }

  @Test
  void versionConstantUsesProjectFormat() {
    Assertions.assertTrue(ProxyBuilderVersion.VERSION.matches("\\d{2}\\.\\d{2}\\.\\d{2}"),
                          "expected XX.YY.ZZ, got " + ProxyBuilderVersion.VERSION);
  }
}
