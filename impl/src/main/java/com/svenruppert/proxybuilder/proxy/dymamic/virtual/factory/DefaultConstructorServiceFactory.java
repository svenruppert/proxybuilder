/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the EUPL-1.2.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package com.svenruppert.proxybuilder.proxy.dymamic.virtual.factory;



import com.svenruppert.dependencies.core.logger.HasLogger;


public class DefaultConstructorServiceFactory<C> implements ServiceFactory<C>, HasLogger {


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
      logger().warn("Could not create instance of {}", realClass, e);
    }
    return newInstance;
  }
}