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

import org.rapidpm.module.se.commons.reflections.adapters.MetadataAdapter;

import java.util.List;

@SuppressWarnings({"unchecked"})
/** scans for method's annotations */
public class MethodAnnotationsScanner extends AbstractScanner {
  public void scan(final Object cls) {
    final MetadataAdapter metadataAdapter = getMetadataAdapter();
    final List methods = metadataAdapter.getMethods(cls);
    for (Object method : methods) {
      ((List<String>) metadataAdapter.getMethodAnnotationNames(method))
          .stream()
          .filter(this::acceptResult)
          .forEach(methodAnnotation -> getStore().put(methodAnnotation, metadataAdapter.getMethodFullKey(cls, method)));
    }
  }
}
