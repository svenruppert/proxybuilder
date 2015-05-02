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

package org.rapidpm.module.se.commons.reflections.util;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;
import com.google.common.collect.Sets;
import org.rapidpm.module.se.commons.reflections.Configuration;
import org.rapidpm.module.se.commons.reflections.Reflections;
import org.rapidpm.module.se.commons.reflections.ReflectionsException;
import org.rapidpm.module.se.commons.reflections.adapters.JavaReflectionAdapter;
import org.rapidpm.module.se.commons.reflections.adapters.JavassistAdapter;
import org.rapidpm.module.se.commons.reflections.adapters.MetadataAdapter;
import org.rapidpm.module.se.commons.reflections.scanners.Scanner;
import org.rapidpm.module.se.commons.reflections.scanners.SubTypesScanner;
import org.rapidpm.module.se.commons.reflections.scanners.TypeAnnotationsScanner;
import org.rapidpm.module.se.commons.reflections.serializers.Serializer;
import org.rapidpm.module.se.commons.reflections.serializers.XmlSerializer;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * a fluent builder for {@link org.rapidpm.module.se.commons.reflections.Configuration}, to be used for constructing a {@link org.rapidpm.module.se.commons.reflections.Reflections} instance
 * <p>usage:
 * <pre>
 *      new Reflections(
 *          new ConfigurationBuilder()
 *              .filterInputsBy(new FilterBuilder().include("your project's common package prefix here..."))
 *              .setUrls(ClasspathHelper.forClassLoader())
 *              .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner().filterResultsBy(myClassAnnotationsFilter)));
 * </pre>
 * <br>{@link #executorService} is used optionally used for parallel scanning. if value is null then scanning is done in a simple for loop
 * <p>defaults: accept all for {@link #inputsFilter},
 * {@link #executorService} is null,
 * {@link #serializer} is {@link org.rapidpm.module.se.commons.reflections.serializers.XmlSerializer}
 */
public class ConfigurationBuilder implements Configuration {
  /*lazy*/ protected MetadataAdapter metadataAdapter;
  private Set<Scanner> scanners;
  private Set<URL> urls;
  private Predicate<String> inputsFilter;
  /*lazy*/ private Serializer serializer;
  private ExecutorService executorService;
  private ClassLoader[] classLoaders;

  public ConfigurationBuilder() {
    scanners = Sets.<Scanner>newHashSet(new TypeAnnotationsScanner(), new SubTypesScanner());
    urls = Sets.newHashSet();
  }

  /**
   * constructs a {@link ConfigurationBuilder} using the given parameters, in a non statically typed way. that is, each element in {@code params} is
   * guessed by it's type and populated into the configuration.
   * <ul>
   * <li>{@link String} - add urls using {@link ClasspathHelper#forPackage(String, ClassLoader...)} ()}</li>
   * <li>{@link Class} - add urls using {@link ClasspathHelper#forClass(Class, ClassLoader...)} </li>
   * <li>{@link ClassLoader} - use these classloaders in order to find urls in ClasspathHelper.forPackage(), ClasspathHelper.forClass() and for resolving types</li>
   * <li>{@link org.rapidpm.module.se.commons.reflections.scanners.Scanner} - use given scanner, overriding the default scanners</li>
   * <li>{@link java.net.URL} - add the given url for scanning</li>
   * <li>{@code Object[]} - flatten and use each element as above</li>
   * </ul>
   * <p>
   * use any parameter type in any order. this constructor uses instanceof on each param and instantiate a {@link ConfigurationBuilder} appropriately.
   */
  @SuppressWarnings("unchecked")
  public static ConfigurationBuilder build(final Object... params) {
    ConfigurationBuilder builder = new ConfigurationBuilder();

    //flatten
    List<Object> parameters = Lists.newArrayList();
    if (params != null) {
      for (Object param : params) {
        if (param != null) {
          if (param.getClass().isArray()) {
            for (Object p : (Object[]) param) if (p != null) parameters.add(p);
          } else if (param instanceof Iterable) {
            for (Object p : (Iterable) param) if (p != null) parameters.add(p);
          } else parameters.add(param);
        }
      }
    }

    List<ClassLoader> loaders = Lists.newArrayList();
    for (Object param : parameters) if (param instanceof ClassLoader) loaders.add((ClassLoader) param);

    ClassLoader[] classLoaders = loaders.isEmpty() ? null : loaders.toArray(new ClassLoader[loaders.size()]);
    FilterBuilder filter = new FilterBuilder();
    List<Scanner> scanners = Lists.newArrayList();

    for (Object param : parameters) {
      if (param instanceof String) {
        builder.addUrls(ClasspathHelper.forPackage((String) param, classLoaders));
        filter.includePackage((String) param);
      } else if (param instanceof Class) {
        if (Scanner.class.isAssignableFrom((Class) param)) {
          try {
            builder.addScanners(((Scanner) ((Class) param).newInstance()));
          } catch (Exception e) { /*fallback*/ }
        }
        builder.addUrls(ClasspathHelper.forClass((Class) param, classLoaders));
        filter.includePackage(((Class) param));
      } else if (param instanceof Scanner) {
        scanners.add((Scanner) param);
      } else if (param instanceof URL) {
        builder.addUrls((URL) param);
      } else if (param instanceof ClassLoader) { /* already taken care */ } else if (param instanceof Predicate) {
        filter.add((Predicate<String>) param);
      } else if (param instanceof ExecutorService) {
        builder.setExecutorService((ExecutorService) param);
      } else if (Reflections.log != null) {
        throw new ReflectionsException("could not use param " + param);
      }
    }

    if (builder.getUrls().isEmpty()) {
      if (classLoaders != null) {
        builder.addUrls(ClasspathHelper.forClassLoader(classLoaders)); //default urls getResources("")
      } else {
        builder.addUrls(ClasspathHelper.forClassLoader()); //default urls getResources("")
      }
    }

    builder.filterInputsBy(filter);
    if (!scanners.isEmpty()) {
      builder.setScanners(scanners.toArray(new Scanner[scanners.size()]));
    }
    if (!loaders.isEmpty()) {
      builder.addClassLoaders(loaders);
    }

    return builder;
  }

  /**
   * add urls to be scanned
   * <p>use {@link org.rapidpm.module.se.commons.reflections.util.ClasspathHelper} convenient methods to get the relevant urls
   */
  public ConfigurationBuilder addUrls(final Collection<URL> urls) {
    this.urls.addAll(urls);
    return this;
  }

  /**
   * set the scanners instances for scanning different metadata
   */
  public ConfigurationBuilder addScanners(final Scanner... scanners) {
    this.scanners.addAll(Sets.newHashSet(scanners));
    return this;
  }

  /**
   * add urls to be scanned
   * <p>use {@link org.rapidpm.module.se.commons.reflections.util.ClasspathHelper} convenient methods to get the relevant urls
   */
  public ConfigurationBuilder addUrls(final URL... urls) {
    this.urls.addAll(Sets.newHashSet(urls));
    return this;
  }

  /**
   * sets the input filter for all resources to be scanned.
   * <p> supply a {@link com.google.common.base.Predicate} or use the {@link FilterBuilder}
   */
  public ConfigurationBuilder filterInputsBy(Predicate<String> inputsFilter) {
    this.inputsFilter = inputsFilter;
    return this;
  }

  /**
   * add class loader, might be used for resolving methods/fields
   */
  public ConfigurationBuilder addClassLoaders(Collection<ClassLoader> classLoaders) {
    return addClassLoaders(classLoaders.toArray(new ClassLoader[classLoaders.size()]));
  }

  /**
   * add class loader, might be used for resolving methods/fields
   */
  public ConfigurationBuilder addClassLoaders(ClassLoader... classLoaders) {
    this.classLoaders = this.classLoaders == null ? classLoaders : ObjectArrays.concat(this.classLoaders, classLoaders, ClassLoader.class);
    return this;
  }

  public ConfigurationBuilder forPackages(String... packages) {
    for (String pkg : packages) {
      addUrls(ClasspathHelper.forPackage(pkg));
    }
    return this;
  }

  public Set<Scanner> getScanners() {
    return scanners;
  }

  /**
   * set the scanners instances for scanning different metadata
   */
  public ConfigurationBuilder setScanners( final Scanner... scanners) {
    this.scanners.clear();
    return addScanners(scanners);
  }

  public Set<URL> getUrls() {
    return urls;
  }

  /**
   * set the urls to be scanned
   * <p>use {@link org.rapidpm.module.se.commons.reflections.util.ClasspathHelper} convenient methods to get the relevant urls
   */
  public ConfigurationBuilder setUrls(final URL... urls) {
    this.urls = Sets.newHashSet(urls);
    return this;
  }

  /**
   * returns the metadata adapter.
   * if javassist library exists in the classpath, this method returns {@link org.rapidpm.module.se.commons.reflections.adapters.JavassistAdapter} otherwise defaults to {@link org.rapidpm.module.se.commons.reflections.adapters.JavaReflectionAdapter}.
   * <p>the {@link org.rapidpm.module.se.commons.reflections.adapters.JavassistAdapter} is preferred in terms of performance and class loading.
   */
  public MetadataAdapter getMetadataAdapter() {
    if (metadataAdapter != null) return metadataAdapter;
    else {
      try {
        return (metadataAdapter = new JavassistAdapter());
      } catch (Throwable e) {
        if (Reflections.log != null)
          Reflections.log.warn("could not create JavassistAdapter, using JavaReflectionAdapter", e);
        return (metadataAdapter = new JavaReflectionAdapter());
      }
    }
  }

  /**
   * sets the metadata adapter used to fetch metadata from classes
   */
  public ConfigurationBuilder setMetadataAdapter(final MetadataAdapter metadataAdapter) {
    this.metadataAdapter = metadataAdapter;
    return this;
  }

  public Predicate<String> getInputsFilter() {
    return inputsFilter;
  }

  /**
   * sets the input filter for all resources to be scanned.
   * <p> supply a {@link com.google.common.base.Predicate} or use the {@link FilterBuilder}
   */
  public void setInputsFilter(Predicate<String> inputsFilter) {
    this.inputsFilter = inputsFilter;
  }

  public ExecutorService getExecutorService() {
    return executorService;
  }

  /**
   * sets the executor service used for scanning.
   */
  public ConfigurationBuilder setExecutorService( ExecutorService executorService) {
    this.executorService = executorService;
    return this;
  }

  public Serializer getSerializer() {
    return serializer != null ? serializer : (serializer = new XmlSerializer()); //lazily defaults to XmlSerializer
  }

  /**
   * sets the serializer used when issuing {@link org.rapidpm.module.se.commons.reflections.Reflections#save}
   */
  public ConfigurationBuilder setSerializer(Serializer serializer) {
    this.serializer = serializer;
    return this;
  }

  /**
   * get class loader, might be used for scanning or resolving methods/fields
   */
  public ClassLoader[] getClassLoaders() {
    return classLoaders;
  }

  /**
   * set class loader, might be used for resolving methods/fields
   */
  public void setClassLoaders(ClassLoader[] classLoaders) {
    this.classLoaders = classLoaders;
  }

  /**
   * set the urls to be scanned
   * <p>use {@link org.rapidpm.module.se.commons.reflections.util.ClasspathHelper} convenient methods to get the relevant urls
   */
  public ConfigurationBuilder setUrls( final Collection<URL> urls) {
    this.urls = Sets.newHashSet(urls);
    return this;
  }

  /**
   * sets the executor service used for scanning to ThreadPoolExecutor with core size as {@link Runtime#availableProcessors()}
   * <p>default is ThreadPoolExecutor with a single core
   */
  public ConfigurationBuilder useParallelExecutor() {
    return useParallelExecutor(Runtime.getRuntime().availableProcessors());
  }

  /**
   * sets the executor service used for scanning to ThreadPoolExecutor with core size as the given availableProcessors parameter
   * <p>default is ThreadPoolExecutor with a single core
   */
  public ConfigurationBuilder useParallelExecutor(final int availableProcessors) {
    setExecutorService(Executors.newFixedThreadPool(availableProcessors));
    return this;
  }

  /**
   * add class loader, might be used for resolving methods/fields
   */
  public ConfigurationBuilder addClassLoader(ClassLoader classLoader) {
    return addClassLoaders(classLoader);
  }
}
