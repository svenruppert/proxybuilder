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

package org.rapidpm.module.se.commons.reflections.scanners;

import org.rapidpm.module.se.commons.reflections.vfs.Vfs;

/**
 * collects all resources that are not classes in a collection
 * <p>key: value - {web.xml: WEB-INF/web.xml}
 */
public class ResourcesScanner extends AbstractScanner {
  public boolean acceptsInput(String file) {
    return !file.endsWith(".class"); //not a class
  }

  @Override
  public Object scan(Vfs.File file, Object classObject) {
    getStore().put(file.getName(), file.getRelativePath());
    return classObject;
  }

  public void scan(Object cls) {
    throw new UnsupportedOperationException(); //shouldn't get here
  }
}
