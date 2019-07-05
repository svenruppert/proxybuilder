/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
  Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package demo.reflections;

import org.reflections.Reflections;
import org.reflections.scanners.*;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

public class ReflectionsDemo {


  public static void main(String[] args) {
    final FilterBuilder TestModelFilter = new FilterBuilder().include("demo.*.model.*");

    final Reflections reflections = new Reflections(new ConfigurationBuilder()
        .setUrls(Collections.singletonList(ClasspathHelper.forClass(ReflectionsDemo.class)))
        .filterInputsBy(TestModelFilter)
        .setScanners(
            new SubTypesScanner(false),
            new TypeAnnotationsScanner(),
            new FieldAnnotationsScanner(),
            new MethodAnnotationsScanner(),
            new MethodParameterScanner(),
            new MethodParameterNamesScanner(),
            new MemberUsageScanner()));

    frageFindeAlleInjectsMitMultiplizitaeten(reflections);

  }

  private static void frageFindeAlleInjectsMitMultiplizitaeten(Reflections reflections) {

    final Set<String> allTypes = reflections.getAllTypes();
    allTypes.forEach(System.out::println);

    final Set<Field> fieldsAnnotatedWith = reflections.getFieldsAnnotatedWith(Inject.class);
    for (final Field field : fieldsAnnotatedWith) {
      final Class<?> type = field.getType();
      final Set<? extends Class<?>> subTypesOf = reflections.getSubTypesOf(type);
      if (subTypesOf.size() > 1) {

        System.out.println("field = " + field);
      } else {
      }
    }


  }


}
