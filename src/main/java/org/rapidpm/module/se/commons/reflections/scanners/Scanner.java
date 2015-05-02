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

import com.google.common.base.Predicate;
import com.google.common.collect.Multimap;
import org.rapidpm.module.se.commons.reflections.Configuration;
import org.rapidpm.module.se.commons.reflections.vfs.Vfs;


/**
 *
 */
public interface Scanner {

  void setConfiguration(Configuration configuration);

  Multimap<String, String> getStore();

  void setStore(Multimap<String, String> store);

  Scanner filterResultsBy(Predicate<String> filter);

  boolean acceptsInput(String file);

  Object scan(Vfs.File file, Object classObject);

  boolean acceptResult(String fqn);
}
