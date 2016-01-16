package org.rapidpm.proxybuilder.objectadapter.annotations.dynamicobjectadapter;

import java.lang.reflect.Proxy;

/**
 * Created by Sven Ruppert on 12.05.15.
 */
public abstract class AdapterBuilder<T> {

  public T buildForTarget(Class<T> target) {
    return (T) Proxy.newProxyInstance(
        target.getClassLoader(),
        new Class[]{target},
        getInvocationHandler()
    );
  }

  protected abstract <I extends ExtendedInvocationHandler<T>> I getInvocationHandler();
}
