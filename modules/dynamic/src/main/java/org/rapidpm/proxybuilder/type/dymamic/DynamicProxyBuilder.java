/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.rapidpm.proxybuilder.type.dymamic;


import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Joiner;
import org.rapidpm.proxybuilder.core.metrics.RapidPMMetricsRegistry;
import org.rapidpm.proxybuilder.type.dymamic.virtual.CreationStrategy;
import org.rapidpm.proxybuilder.type.dymamic.virtual.DefaultConstructorServiceFactory;
import org.rapidpm.proxybuilder.type.dymamic.virtual.DynamicProxyGenerator;
import org.rapidpm.proxybuilder.type.dymamic.virtual.VirtualDynamicProxyInvocationHandler.ServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DynamicProxyBuilder<I, T extends I> {

  private final List<SecurityRule> securityRules = new ArrayList<>();
  private final List<PreAction> preActionList = new ArrayList<>();
  private final List<PostAction> postActionList = new ArrayList<>();


  private Class<I> clazz;
  private Class<I> clazzOrigin;
  private CreationStrategy creationStrategy;
  private ServiceFactory serviceFactory;
  private T original;
  private boolean metrics;
  private boolean logging;


  private DynamicProxyBuilder() {
  }

  public static <I, T extends I> DynamicProxyBuilder<I, T> createBuilder(Class<I> clazz, T original) {
    final DynamicProxyBuilder<I, T> dynamicProxyBuilder = new DynamicProxyBuilder<>();
    dynamicProxyBuilder.clazz = clazz;
    dynamicProxyBuilder.original = original;
    return dynamicProxyBuilder;
  }

  /**
   * @param clazz
   * @param original
   * @param creationStrategy
   * @param <I>
   * @param <T>
   *
   * @return
   */

  public static <I, T extends I> DynamicProxyBuilder<I, T> createBuilder(Class<I> clazz, Class<T> original, CreationStrategy creationStrategy) {
    final DynamicProxyBuilder<I, T> dynamicProxyBuilder = new DynamicProxyBuilder<>();
    dynamicProxyBuilder.clazz = clazz;
    dynamicProxyBuilder.clazzOrigin = (Class<I>) original;
    dynamicProxyBuilder.creationStrategy = creationStrategy;
    return dynamicProxyBuilder;
  }

  public static <I> DynamicProxyBuilder<I, I> createBuilder(Class<I> clazz,
                                                            CreationStrategy creationStrategy,
                                                            ServiceFactory<I> serviceFactory) {
    final DynamicProxyBuilder<I, I> dynamicProxyBuilder = new DynamicProxyBuilder<>();
    dynamicProxyBuilder.clazz = clazz;
    dynamicProxyBuilder.serviceFactory = serviceFactory;
    dynamicProxyBuilder.creationStrategy = creationStrategy;
    return dynamicProxyBuilder;
  }


  public DynamicProxyBuilder<I, T> addSecurityRule(SecurityRule rule) {
    securityRules.add(rule);
    return this;
  }

  //die originalReihenfolge behalten in der die Methoden aufgerufen worden sind.
  public I build() {

    if (original == null) {
      //virtual
      this.original = (T) DynamicProxyGenerator.<I, I>newBuilder()
          .withSubject(clazz)
          .withCreationStrategy((creationStrategy != null) ? creationStrategy : CreationStrategy.NONE)
          .withServiceFactory((serviceFactory != null) ? serviceFactory : new DefaultConstructorServiceFactory<>(clazzOrigin))
//          .withPostActions(postActionList)
          .build()
          .make();
    }

    //post
    postActionList.forEach(this::buildPostActionProxy);

    //pre
    Collections.reverse(preActionList);
    preActionList.forEach(this::buildPreActionProxy);

    Collections.reverse(securityRules);
    securityRules.forEach(this::buildAddSecurityRule);

    if (logging) buildLoggingProxy();

    if (metrics) buildMetricsProxy();

    return this.original;
  }

  private void buildMetricsProxy() {
    final MetricRegistry metrics = RapidPMMetricsRegistry.getInstance().getMetrics();
    final InvocationHandler invocationHandler = new InvocationHandler() {
      private final T original = DynamicProxyBuilder.this.original;

      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final long start = System.nanoTime();
        final Object invoke = DynamicProxyBuilder.invoke(original, method, args);
        final long stop = System.nanoTime();
        Histogram methodCalls = metrics.histogram(clazz.getName() + "." + method.getName());
        methodCalls.update((stop - start));
        return invoke;
      }
    };
    createProxy(invocationHandler);
  }

  private void buildLoggingProxy() {
    final InvocationHandler invocationHandler = new InvocationHandler() {
      private final T original = DynamicProxyBuilder.this.original;
      private final Logger logger = LoggerFactory.getLogger(clazzOrigin);;

      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final int length = (args == null) ? 0 : args.length;
        if (logger.isInfoEnabled()) {
          logger.info(method.getName() + " (" + ((length == 0) ? ")" :
              ""
                  + Joiner
                  .on(" +  -  + ")
                  .join(args)
                  + ")"));
        }
        return method.invoke(original, args);
      }
    };
    createProxy(invocationHandler);
  }


  public static Object invoke(Object proxy, @Nonnull Method method, Object[] args) throws Throwable {
    try {
      return method.invoke(proxy, args);
    } catch (InvocationTargetException ex) {
      throw ex.getCause();
    }
  }

  private void createProxy(InvocationHandler invocationHandler) {
    final ClassLoader classLoader = original.getClass().getClassLoader();
    final Class<?>[] interfaces = {clazz};
    final Object nextProxy = Proxy.newProxyInstance(
        classLoader,
        interfaces,
        invocationHandler);
    original = (T) clazz.cast(nextProxy);
  }

  private DynamicProxyBuilder<I, T> buildAddSecurityRule(SecurityRule rule) {
    final InvocationHandler invocationHandler = new InvocationHandler() {
      private final T original = DynamicProxyBuilder.this.original;

      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final boolean checkRule = rule.checkRule();
        if (checkRule) {
          return DynamicProxyBuilder.invoke(original, method, args);
        } else {
          return null;
        }
      }
    };
    createProxy(invocationHandler);
    return this;
  }

  //wo die Metriken ablegen ?
  public DynamicProxyBuilder<I, T> addMetrics() {
    this.metrics = true;
    return this;
  }

  public DynamicProxyBuilder<I, T> addLogging() {
    this.logging = true;
    return this;
  }

  public DynamicProxyBuilder<I, T> addIPreAction(PreAction<I> preAction) {
    preActionList.add(preAction);
    //buildPreActionProxy(preAction);
    return this;
  }

  private void buildPreActionProxy(final PreAction<I> preAction) {
    final InvocationHandler invocationHandler = new InvocationHandler() {
      private final T original = DynamicProxyBuilder.this.original;

      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        preAction.execute(original, method, args);
        return method.invoke(original, args);
      }
    };
    createProxy(invocationHandler);
  }


  public DynamicProxyBuilder<I, T> addIPostAction(PostAction<I> postAction) {
    postActionList.add(postAction);
    //buildPostActionProxy(postAction);
    return this;
  }

  private void buildPostActionProxy(final PostAction<I> postAction) {
    final InvocationHandler invocationHandler = new InvocationHandler() {
      private final T original = DynamicProxyBuilder.this.original;

      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final Object invoke = method.invoke(original, args);
        postAction.execute(original, method, args);
        return invoke;
      }
    };
    createProxy(invocationHandler);
  }

  public interface PreAction<T> {
    void execute(T original, Method method, Object[] args) throws Throwable;
  }

  public interface PostAction<T> {
    void execute(T original, Method method, Object[] args) throws Throwable;
  }

}
