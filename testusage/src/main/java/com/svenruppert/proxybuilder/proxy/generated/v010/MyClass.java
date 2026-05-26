/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the EUPL-1.2.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package com.svenruppert.proxybuilder.proxy.generated.v010;


import com.svenruppert.dependencies.core.logger.HasLogger;
import com.svenruppert.proxybuilder.proxy.generated.annotations.StaticLoggingProxy;

@StaticLoggingProxy
public interface MyClass {

  default void doSomething() {
    HasLogger.staticLogger().info("I did something");
  }

}