/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the EUPL-1.2.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package demo.reflections;


import com.svenruppert.dependencies.core.logger.HasLogger;
import org.reflections8.Reflections;
import org.reflections8.scanners.*;
import org.reflections8.util.ClasspathHelper;
import org.reflections8.util.ConfigurationBuilder;
import org.reflections8.util.FilterBuilder;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

public class ReflectionsDemo implements HasLogger {

  private static final HasLogger LOGGER = new ReflectionsDemo();


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
    allTypes.forEach(type -> LOGGER.logger().info("type = {}", type));

    final Set<Field> fieldsAnnotatedWith = reflections.getFieldsAnnotatedWith(Inject.class);
    for (final Field field : fieldsAnnotatedWith) {
      final Class<?> type = field.getType();
      final Set<? extends Class<?>> subTypesOf = reflections.getSubTypesOf(type);
      if (subTypesOf.size() > 1) {

        LOGGER.logger().info("field = {}", field);
      } else {
      }
    }


  }


}