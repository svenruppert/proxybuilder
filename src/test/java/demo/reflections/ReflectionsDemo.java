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

package demo.reflections;

import org.rapidpm.module.se.commons.reflections.Reflections;
import org.rapidpm.module.se.commons.reflections.scanners.*;
import org.rapidpm.module.se.commons.reflections.util.ClasspathHelper;
import org.rapidpm.module.se.commons.reflections.util.ConfigurationBuilder;
import org.rapidpm.module.se.commons.reflections.util.FilterBuilder;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;

/**
 * Created by Sven Ruppert on 07.12.2014.
 */
public class ReflectionsDemo {


  public static void main(String[] args) {
    final FilterBuilder TestModelFilter = new FilterBuilder().include("demo.*.model.*");

    final Reflections reflections = new Reflections(new ConfigurationBuilder()
          .setUrls(Arrays.asList(ClasspathHelper.forClass(ReflectionsDemo.class)))
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
      if(subTypesOf.size() > 1){

        System.out.println("field = " + field);
      } else{
      }
    }





  }


}
