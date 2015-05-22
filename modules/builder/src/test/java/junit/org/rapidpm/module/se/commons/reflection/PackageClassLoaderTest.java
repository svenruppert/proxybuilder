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

package junit.org.rapidpm.module.se.commons.reflection;

import org.junit.Test;

import java.util.List;

/**
 * Created by Sven Ruppert on 07.12.2014.
 */
public class PackageClassLoaderTest {


  @Test
  public void testLoad001() throws Exception {
    final PackageClassLoader loader = new PackageClassLoader();
    final List<Class> classes = loader.getClasses("junit.org.rapidpm.module.se.commons.reflection.test001");
    for (final Class aClass : classes) {
      System.out.println("aClass = " + aClass);
    }

  }

  @Test
  public void testLoad002() throws Exception {
    final PackageClassLoader loader = new PackageClassLoader();
    final List<Class> classes = loader.getClasses("org.jboss.weld");
    for (final Class aClass : classes) {
      System.out.println("aClass = " + aClass);
    }

  }
}
