package org.rapidpm.proxybuilder.type.dymamic;


import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import org.rapidpm.proxybuilder.core.metrics.RapidPMMetricsRegistry;
import org.rapidpm.proxybuilder.type.dymamic.virtual.CreationStrategy;
import org.rapidpm.proxybuilder.type.dymamic.virtual.DefaultConstructorServiceFactory;
import org.rapidpm.proxybuilder.type.dymamic.virtual.DynamicProxyGenerator;
import org.rapidpm.proxybuilder.type.dymamic.virtual.VirtualDynamicProxyInvocationHandler;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by sven on 28.04.15.
 */
public class DynamicProxyBuilder<I, T extends I> {

  private T original;
  private Class<I> clazz;
  private List<SecurityRule> securityRules = new ArrayList<>();

  private DynamicProxyBuilder() {
  }

  public static <I, T extends I> DynamicProxyBuilder<I, T> createBuilder(Class<I> clazz, T original) {
    final DynamicProxyBuilder<I, T> dynamicProxyBuilder = new DynamicProxyBuilder<>();
    dynamicProxyBuilder.original = original;
    dynamicProxyBuilder.clazz = clazz;
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
    final I proxy = DynamicProxyGenerator.<I, T>newBuilder()
        .withSubject(clazz)
        .withCreationStrategy(creationStrategy)
        .withServiceFactory(new DefaultConstructorServiceFactory<>(original))
        .build()
        .make();

    dynamicProxyBuilder.original = (T) proxy;
    dynamicProxyBuilder.clazz = clazz;
    return dynamicProxyBuilder;
  }

  public static <I> DynamicProxyBuilder<I, I> createBuilder(Class<I> clazz,
                                                            CreationStrategy creationStrategy,
                                                            VirtualDynamicProxyInvocationHandler.ServiceFactory<I> serviceFactory) {
    final DynamicProxyBuilder<I, I> dynamicProxyBuilder = new DynamicProxyBuilder<>();
    final I proxy = DynamicProxyGenerator.<I, I>newBuilder()
        .withSubject(clazz)
        .withCreationStrategy(creationStrategy)
        .withServiceFactory(serviceFactory)
        .build()
        .make();

    dynamicProxyBuilder.original = proxy;
    dynamicProxyBuilder.clazz = clazz;
    return dynamicProxyBuilder;
  }


  public DynamicProxyBuilder<I, T> addSecurityRule(SecurityRule rule) {
    securityRules.add(rule);
    return this;
  }

  //die originalReihenfolge behalten in der die Methoden aufgerufen worden sind.
  public I build() {
    Collections.reverse(securityRules);
    securityRules.forEach(this::buildAddSecurityRule);
    return this.original;
  }

  private DynamicProxyBuilder<I, T> buildAddSecurityRule(SecurityRule rule) {
    final InvocationHandler invocationHandler = new InvocationHandler() {
      private T original = DynamicProxyBuilder.this.original;

      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final boolean checkRule = rule.checkRule();
        if (checkRule) {
//          return method.invoke(original, args);
          return DynamicProxyBuilder.invoke(original, method, args);
        } else {
          return null;
        }
      }
    };
    createProxy(invocationHandler);
    return this;
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

  //wo die Metriken ablegen ?
  public DynamicProxyBuilder<I, T> addMetrics() {
    final MetricRegistry metrics = RapidPMMetricsRegistry.getInstance().getMetrics();
    final InvocationHandler invocationHandler = new InvocationHandler() {
      private final T original = DynamicProxyBuilder.this.original;

      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final long start = System.nanoTime();
        final Object invoke = DynamicProxyBuilder.invoke(original, method, args);
        final long stop = System.nanoTime();
        Histogram methodCalls = metrics.histogram(clazz.getSimpleName() + "." + method.getName());
        methodCalls.update((stop - start));
        return invoke;
      }
    };
    createProxy(invocationHandler);
    return this;
  }

  public DynamicProxyBuilder<I, T> addIPreAction(PreAction<I> preAction) {
    final InvocationHandler invocationHandler = new InvocationHandler() {
      private final T original = DynamicProxyBuilder.this.original;

      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        preAction.execute(original, method, args);
        final Object invoke = method.invoke(original, args);
        return invoke;
      }
    };
    createProxy(invocationHandler);
    return this;
  }

  public DynamicProxyBuilder<I, T> addIPostAction(PreAction<I> postAction) {
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
    return this;
  }

  public interface PreAction<T> {
    void execute(T original, Method method, Object[] args) throws Throwable;
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

  public interface PostAction<T> {
    void execute(T original, Method method, Object[] args) throws Throwable;
  }

}
