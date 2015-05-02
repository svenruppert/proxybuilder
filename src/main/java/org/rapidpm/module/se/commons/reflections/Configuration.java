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

package org.rapidpm.module.se.commons.reflections;

import com.google.common.base.Predicate;
import org.rapidpm.module.se.commons.reflections.adapters.MetadataAdapter;
import org.rapidpm.module.se.commons.reflections.scanners.Scanner;
import org.rapidpm.module.se.commons.reflections.serializers.Serializer;

import java.net.URL;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * Configuration is used to create a configured instance of {@link Reflections}
 * <p>it is preferred to use {@link org.rapidpm.module.se.commons.reflections.util.ConfigurationBuilder}
 */
public interface Configuration {
  /**
   * the scanner instances used for scanning different metadata
   */
  Set<Scanner> getScanners();

  /**
   * the urls to be scanned
   */
  Set<URL> getUrls();

  /**
   * the metadata adapter used to fetch metadata from classes
   */
  @SuppressWarnings({"RawUseOfParameterizedType"})
  MetadataAdapter getMetadataAdapter();

  /**
   * get the fully qualified name filter used to filter types to be scanned
   */
  Predicate<String> getInputsFilter();

  /**
   * executor service used to scan files. if null, scanning is done in a simple for loop
   */
  ExecutorService getExecutorService();

  /**
   * the default serializer to use when saving Reflection
   */
  Serializer getSerializer();

  /**
   * get class loaders, might be used for resolving methods/fields
   */
  ClassLoader[] getClassLoaders();
}
