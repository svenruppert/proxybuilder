package org.rapidpm.proxybuilder;

import org.rapidpm.proxybuilder.type.dymamic.DynamicProxyBuilder;
import org.rapidpm.proxybuilder.type.dymamic.virtual.CreationStrategy;
import org.rapidpm.proxybuilder.type.dymamic.virtual.VirtualDynamicProxyInvocationHandler;

/**
 * Created by svenruppert on 06.11.15.
 */
public class ProxyBuilder {


  private ProxyBuilder() {
  }


  public static <I, T extends I> DynamicProxyBuilder<I, T> newDynamicProxyBuilder(Class<I> clazz, T original) {
    return DynamicProxyBuilder.createBuilder(clazz, original);
  }


  public static <I, T extends I> DynamicProxyBuilder<I, T> newDynamicProxyBuilder(Class<I> clazz, Class<T> original, CreationStrategy creationStrategy) {
    return DynamicProxyBuilder.createBuilder(clazz, original, creationStrategy);
  }

  public static <I> DynamicProxyBuilder<I, I> newDynamicProxyBuilder(Class<I> clazz,
                                                                     CreationStrategy creationStrategy,
                                                                     VirtualDynamicProxyInvocationHandler.ServiceFactory<I> serviceFactory) {
    return DynamicProxyBuilder.createBuilder(clazz, creationStrategy, serviceFactory);
  }


}
