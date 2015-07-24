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

package org.rapidpm.proxybuilder.type.virtual;


import org.rapidpm.proxybuilder.generator.Generator;
import org.rapidpm.proxybuilder.type.virtual.dynamic.VirtualDynamicProxyInvocationHandler;
import org.rapidpm.proxybuilder.type.virtual.dynamic.VirtualDynamicProxyInvocationHandler.ServiceFactory;
import org.rapidpm.proxybuilder.type.virtual.dynamic.VirtualDynamicProxyInvocationHandler.ServiceStrategyFactory;

import javax.annotation.Nonnull;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by Sven Ruppert on 14.01.14.
 */
public class ProxyGenerator<I, C extends I> {

  private static final WeakHashMap CACHE = new WeakHashMap();

  private ProxyGenerator() {
  }

  private Class<I> subject;
  private Class<C> realClass;
  private Concurrency concurrency;
  private ProxyType type;
  private ServiceFactory<C> serviceFactory;
  private ServiceStrategyFactory<C> serviceStrategyFactory;

  private ProxyGenerator(final Builder builder) {
    subject = builder.subject;
    realClass = builder.realClass;
    concurrency = builder.concurrency;
    type = builder.type;
    serviceFactory = builder.serviceFactory;
    serviceStrategyFactory = builder.serviceStrategyFactory;
  }


  public I make() {
    Object proxy = null;
    ClassLoader loader = subject.getClassLoader();

    if (type == ProxyType.STATIC) {
      proxy = createStaticProxy(loader, subject, realClass, concurrency);
    } else if (type == ProxyType.DYNAMIC) {
      proxy = createDynamicProxy(loader, subject, concurrency, serviceFactory, serviceStrategyFactory);
    } else if (type == ProxyType.OnExistingObject) {
      //Hier den OnExistingObject Proxy erzeugen!
      proxy = createStaticProxy(loader, subject, realClass, Concurrency.OnExistingObject);
    }
    return subject.cast(proxy);
  }

//
//  public static <I, C extends I> I make(Class<I> subject, Class<C> realClass, Concurrency concurrency, ProxyType type) {
//    return make(subject.getClassLoader(), subject, realClass, concurrency, type);
//  }
//
//  public static <I, C extends I> I make(Class<I> subject, Class<C> realClass, Concurrency concurrency) {
//    return make(subject, realClass, concurrency, ProxyType.STATIC);
//  }
//
//  public static <I, C extends I> I make(Class<I> subject, Class<C> realClass) {
//    return make(subject, realClass, Concurrency.NONE, ProxyType.STATIC);
//  }
//
//  public static <I, C extends I> I make(ClassLoader loader,
//                                        Class<I> subject,
//                                        Class<C> realClass,
//                                        Concurrency concurrency,
//                                        ProxyType type) {
//    return make(loader, subject, realClass, concurrency, type, new DefaultServiceFactory<>(realClass));
//  }
//
//  public static <I, C extends I> I make(Class<I> subject,
//                                        Class<C> realClass,
//                                        Concurrency concurrency,
//                                        ProxyType type,
//                                        ServiceFactory<C> serviceFactory) {
//    return make(subject.getClassLoader(), subject, realClass, concurrency, type, serviceFactory, new ServiceStrategyFactoryNotThreadSafe<>());
//  }
//
//  public static <I, C extends I> I make(ClassLoader loader,
//                                        Class<I> subject,
//                                        Class<C> realClass,
//                                        Concurrency concurrency,
//                                        ProxyType type,
//                                        ServiceFactory<C> serviceFactory) {
//    return make(loader, subject, realClass, concurrency, type, serviceFactory, new ServiceStrategyFactoryNotThreadSafe<>());
//  }
//
//  public static <I, C extends I> I make(ClassLoader loader,
//                                        Class<I> subject,
//                                        Class<C> realClass,
//                                        Concurrency concurrency,
//                                        ProxyType type,
//                                        ServiceFactory<C> serviceFactory,
//                                        ServiceStrategyFactory<C> serviceStrategyFactory) {
//    Object proxy = null;
//    if (type == ProxyType.STATIC) {
//      proxy = createStaticProxy(loader, subject, realClass, concurrency);
//    } else if (type == ProxyType.DYNAMIC) {
//      proxy = createDynamicProxy(loader, subject, concurrency, serviceFactory, serviceStrategyFactory);
//    } else if (type == ProxyType.OnExistingObject) {
//      //Hier den OnExistingObject Proxy erzeugen!
//      proxy = createStaticProxy(loader, subject, realClass, Concurrency.OnExistingObject);
//    }
//    return subject.cast(proxy);
//  }

  private static Object createStaticProxy(ClassLoader loader, Class subject, Class realClass, Concurrency concurrency) {
    Map clcache;
    synchronized (CACHE) {
      clcache = (Map) CACHE.get(loader);
      if (clcache == null) {
        CACHE.put(loader, clcache = new HashMap());
      }
    }
    try {
      Class clazz;
      CacheKey key = new CacheKey(subject, concurrency);
      synchronized (clcache) {
        clazz = (Class) clcache.get(key);
        if (clazz == null) {
          VirtualProxySourceGenerator vpsg = create(subject, realClass, concurrency);
          clazz = Generator.make(loader, vpsg.getProxyName(), vpsg.getCharSequence());
          clcache.put(key, clazz);
        }
      }
      return clazz.newInstance(); //proxy erzeugt
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }

  private static VirtualProxySourceGenerator create(Class subject, Class realClass, Concurrency concurrency) {
    switch (concurrency) {
      case NONE:
        return new VirtualProxySourceGeneratorNotThreadsafe(subject, realClass);
      case SOME_DUPLICATES:
        return new VirtualProxySourceGeneratorSomeDuplicates(subject, realClass);
      case NO_DUPLICATES:
        return new VirtualProxySourceGeneratorNoDuplicates(subject, realClass);
      case OnExistingObject:
        return new VirtualProxySourceGeneratorOnExistingObject(subject, realClass);
      default:
        throw new IllegalArgumentException(
            "Unsupported Concurrency: " + concurrency);
    }
  }

  private static <I, C extends I> I createDynamicProxy(ClassLoader loader,
                                                       Class<I> subject,
                                                       Concurrency concurrency,
                                                       ServiceFactory<C> serviceFactory,
                                                       ServiceStrategyFactory<C> serviceStrategyFactory) {
    if (concurrency != Concurrency.NONE) {
      throw new IllegalArgumentException("Unsupported Concurrency: " + concurrency);
    }


    final VirtualDynamicProxyInvocationHandler<I, C> dynamicProxy;
    if (Concurrency.NONE.equals(concurrency)) {
      dynamicProxy = VirtualDynamicProxyInvocationHandler.<I, C>newBuilder()
          .withServiceStrategyFactory(serviceStrategyFactory)
          .withServiceFactory(serviceFactory)
          .build();

    } else {
      dynamicProxy = null;
    }

    return (I) Proxy.newProxyInstance(
        loader,
        new Class<?>[]{subject},
        dynamicProxy);
  }

  public static <I, C extends I> Builder<I, C> newBuilder() {
    return new Builder<>();
  }

  private static class CacheKey {
    private final Class subject;
    private final Concurrency concurrency;

    private CacheKey(Class subject, Concurrency concurrency) {
      this.subject = subject;
      this.concurrency = concurrency;
    }

    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      CacheKey that = (CacheKey) o;
      if (concurrency != that.concurrency) return false;
      return subject.equals(that.subject);
    }

    public int hashCode() {
      return 31 * subject.hashCode() + concurrency.hashCode();
    }
  }

  public static final class Builder<I, C extends I> {
    private Class<I> subject;
    private Class<C> realClass;

    private Concurrency concurrency = Concurrency.NONE;
    private ProxyType type = ProxyType.DYNAMIC;

    private ServiceFactory<C> serviceFactory;
    private ServiceStrategyFactory<C> serviceStrategyFactory;

    private Builder() {
    }

    @Nonnull
    public Builder<I, C> withSubject(@Nonnull final Class<I> subject) {
      this.subject = subject;
      return this;
    }

    @Nonnull
    public Builder<I, C> withRealClass(@Nonnull final Class<C> realClass) {
      this.realClass = realClass;
      return this;
    }

    @Nonnull
    public Builder<I, C> withConcurrency(@Nonnull final Concurrency concurrency) {
      this.concurrency = concurrency;
      return this;
    }

    @Nonnull
    public Builder<I, C> withType(@Nonnull final ProxyType type) {
      this.type = type;
      return this;
    }

    @Nonnull
    public Builder<I, C> withServiceFactory(@Nonnull final ServiceFactory<C> serviceFactory) {
      this.serviceFactory = serviceFactory;
      return this;
    }

    @Nonnull
    public Builder<I, C> withServiceStrategyFactory(@Nonnull final ServiceStrategyFactory<C> serviceStrategyFactory) {
      this.serviceStrategyFactory = serviceStrategyFactory;
      return this;
    }

    @Nonnull
    public ProxyGenerator<I, C> build() {
      return new ProxyGenerator<>(this);
    }
  }
}
