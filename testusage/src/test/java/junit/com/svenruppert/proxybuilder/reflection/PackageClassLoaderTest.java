/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the EUPL-1.2.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package junit.com.svenruppert.proxybuilder.reflection;


import com.svenruppert.dependencies.core.logger.HasLogger;
import org.junit.jupiter.api.Test;

import java.util.List;

public class PackageClassLoaderTest implements HasLogger {


  @Test
  public void testLoad001() throws Exception {
    final PackageClassLoader loader = new PackageClassLoader();
    final List<Class> classes = loader.getClasses("junit.com.svenruppert.proxybuilder.reflection.test001");
    for (final Class aClass : classes) {
      logger().info("aClass = {}", aClass);
    }

  }

  @Test
  public void testLoad002() throws Exception {
    final PackageClassLoader loader = new PackageClassLoader();
    final List<Class> classes = loader.getClasses("org.jboss.weld");
    for (final Class aClass : classes) {
      logger().info("aClass = {}", aClass);
    }

  }
}