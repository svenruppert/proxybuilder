package org.rapidpm.proxybuilder;


import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import org.jetbrains.annotations.NotNull;
import org.rapidpm.proxybuilder.type.metrics.MetricsRegistry;
import org.rapidpm.proxybuilder.type.virtual.Concurrency;
import org.rapidpm.proxybuilder.type.virtual.ProxyGenerator;
import org.rapidpm.proxybuilder.type.virtual.ProxyType;
import org.rapidpm.proxybuilder.type.virtual.dynamic.DefaultServiceFactory;
import org.rapidpm.proxybuilder.type.virtual.dynamic.ServiceStrategyFactoryNotThreadSafe;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by sven on 28.04.15.
 */
public class VirtualProxyBuilder<I, T extends I> {

  private T original;
  private Class<I> clazz;
  private List<SecurityRule> securityRules = new ArrayList<>();

  private VirtualProxyBuilder() {
  }

  public static <I, T extends I> VirtualProxyBuilder<I, T> createBuilder(Class<I> clazz, T original) {
    final VirtualProxyBuilder<I, T> virtualProxyBuilder = new VirtualProxyBuilder<>();
    virtualProxyBuilder.original = original;
    virtualProxyBuilder.clazz = clazz;
    return virtualProxyBuilder;
  }

  /**
   * Hier noch die Kombinationen abfangen zwischen Dynamic und Concurrent
   *
   * @param clazz
   * @param original
   * @param concurrency
   * @param <I>
   * @param <T>
   *
   * @return
   */

  public static <I, T extends I> VirtualProxyBuilder<I, T> createBuilder(Class<I> clazz, Class<T> original, Concurrency concurrency) {
    final VirtualProxyBuilder<I, T> virtualProxyBuilder = new VirtualProxyBuilder<>();
    final I proxy = ProxyGenerator.<I, T>newBuilder()
        .withSubject(clazz)
        .withRealClass(original)
        .withConcurrency(concurrency)
        .withServiceFactory(new DefaultServiceFactory<>(original))
        .withServiceStrategyFactory(new ServiceStrategyFactoryNotThreadSafe<T>())
        .withType(ProxyType.DYNAMIC)
        .build()
        .make();

    virtualProxyBuilder.original = (T) proxy;
    virtualProxyBuilder.clazz = clazz;
    return virtualProxyBuilder;
  }


  public VirtualProxyBuilder<I, T> addSecurityRule(SecurityRule rule) {
    securityRules.add(rule);
    return this;
  }

  //die originalReihenfolge behalten in der die Methoden aufgerufen worden sind.
  public I build() {
    Collections.reverse(securityRules);
    securityRules.forEach(this::buildAddSecurityRule);
    return this.original;
  }

  private VirtualProxyBuilder<I, T> buildAddSecurityRule(SecurityRule rule) {
    final InvocationHandler invocationHandler = new InvocationHandler() {
      private T original = VirtualProxyBuilder.this.original;

      @Override
      public Object invoke(Object proxy, @NotNull Method method, Object[] args) throws Throwable {
        final boolean checkRule = rule.checkRule();
        if (checkRule) {
          return method.invoke(original, args);
        } else {
          return null;
        }
      }
    };
    createProxy(invocationHandler);
    return this;
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

  //wo die Metriken ablegen ?
  public VirtualProxyBuilder<I, T> addMetrics() {
    final MetricRegistry metrics = MetricsRegistry.getInstance().getMetrics();
    final InvocationHandler invocationHandler = new InvocationHandler() {
      private final Histogram methodCalls = metrics.histogram(clazz.getSimpleName());
      private final T original = VirtualProxyBuilder.this.original;

      @Override
      public Object invoke(Object proxy, @NotNull Method method, Object[] args) throws Throwable {
        final long start = System.nanoTime();
        final Object invoke = method.invoke(original, args);
        final long stop = System.nanoTime();
        methodCalls.update((stop - start));
        return invoke;
      }
    };
    createProxy(invocationHandler);
    return this;
  }


//  public ProxyBuilder<I, T> addLogging() {
//
//    final InvocationHandler invocationHandler = new InvocationHandler() {
//
//      private final T original = ProxyBuilder.this.original;
//
//      @Override
//      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//        final long start = System.nanoTime();
//        final Object invoke = method.invoke(original, args);
//        final long stop = System.nanoTime();
////        methodCalls.update((stop - start));
//        return invoke;
//      }
//    };
//
//
//    createProxy(invocationHandler);
//    return this;
//  }


}
