package org.rapidpm.proxybuilder.type.dymamic.virtual;


import org.rapidpm.proxybuilder.type.dymamic.virtual.VirtualDynamicProxyInvocationHandler.ServiceFactory;

/**
 * Created by Sven Ruppert on 22.07.15.
 */
public class DefaultConstructorServiceFactory<C> implements ServiceFactory<C> {


  private final Class<C> realClass;

  public DefaultConstructorServiceFactory(final Class<C> realClass) {
    this.realClass = realClass;
  }

  @Override
  public C createInstance() {
    C newInstance = null;
    try {
      newInstance = realClass.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      e.printStackTrace();
    }
    return newInstance;
  }
}
