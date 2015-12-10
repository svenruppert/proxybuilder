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

package org.rapidpm.proxybuilder.type.staticruntime.virtual;



import org.rapidpm.proxybuilder.type.staticruntime.generator.Generator;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by Sven Ruppert on 14.01.14.
 */
public class StaticProxyGenerator {

  private StaticProxyGenerator() {
  }

  private static final WeakHashMap CACHE = new WeakHashMap();

  public static <T> T make(Class<T> subject, Class<? extends T> realClass, CreationStrategy creationStrategy) {
    final T make = make(subject.getClassLoader(), subject, realClass, creationStrategy);
    //hier ref auf realSubject einfuegen ??
    return make;
  }

  public static <T> T make(ClassLoader loader,
                           Class<T> subject,
                           Class<? extends T> realClass,
                           CreationStrategy creationStrategy) {
    final Object proxy = createStaticProxy(loader, subject, realClass, creationStrategy);
//    if (type == ProxyType.STATIC) {
//    } else if (type == ProxyType.OnExistingObject) {
//      proxy = createStaticProxy(loader, subject, realClass, CreationStrategy.OnExistingObject);
//    }
    return subject.cast(proxy);
  }

  private static Object createStaticProxy(ClassLoader loader, Class subject, Class realClass, CreationStrategy creationStrategy) {
    Map clcache;
    synchronized (CACHE) {
      clcache = (Map) CACHE.get(loader);
      if (clcache == null) {
        CACHE.put(loader, clcache = new HashMap());
      }
    }
    try {
      Class clazz;
      CacheKey key = new CacheKey(subject, creationStrategy);
      synchronized (clcache) {
        clazz = (Class) clcache.get(key);
        if (clazz == null) {
          VirtualProxySourceGenerator vpsg = create(subject, realClass, creationStrategy);
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

  private static VirtualProxySourceGenerator create(Class subject, Class realClass, CreationStrategy creationStrategy) {
    switch (creationStrategy) {
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
            "Unsupported Concurrency: " + creationStrategy);
    }
  }


  private static class CacheKey {
    private final Class subject;
    private final CreationStrategy creationStrategy;

    private CacheKey(Class subject, CreationStrategy creationStrategy) {
      this.subject = subject;
      this.creationStrategy = creationStrategy;
    }

    public int hashCode() {
      return 31 * subject.hashCode() + creationStrategy.hashCode();
    }

    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      CacheKey that = (CacheKey) o;
      if (creationStrategy != that.creationStrategy) return false;
      return subject.equals(that.subject);
    }
  }
}