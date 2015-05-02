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

import com.google.common.base.Joiner;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import org.rapidpm.module.se.commons.reflections.adapters.MetadataAdapter;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * scans methods/constructors and indexes parameter names
 */
@SuppressWarnings("unchecked")
public class MethodParameterNamesScanner extends AbstractScanner {

  @Override
  public void scan(Object cls) {
    final MetadataAdapter md = getMetadataAdapter();

    for (Object method : md.getMethods(cls)) {
      String key = md.getMethodFullKey(cls, method);
      if (acceptResult(key)) {
        LocalVariableAttribute table = (LocalVariableAttribute) ((MethodInfo) method).getCodeAttribute().getAttribute(LocalVariableAttribute.tag);
        int length = table.tableLength();
        int i = Modifier.isStatic(((MethodInfo) method).getAccessFlags()) ? 0 : 1; //skip this
        if (i < length) {
          List<String> names = new ArrayList<String>(length - i);
          while (i < length) names.add(((MethodInfo) method).getConstPool().getUtf8Info(table.nameIndex(i++)));
          getStore().put(key, Joiner.on(", ").join(names));
        }
      }
    }
  }
}
