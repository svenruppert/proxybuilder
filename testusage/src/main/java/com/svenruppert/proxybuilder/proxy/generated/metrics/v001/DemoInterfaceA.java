/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the EUPL-1.2.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package com.svenruppert.proxybuilder.proxy.generated.metrics.v001;


import com.svenruppert.proxybuilder.proxy.generated.annotations.StaticLoggingProxy;
import com.svenruppert.proxybuilder.proxy.generated.annotations.StaticMetricsProxy;

@StaticMetricsProxy
@StaticLoggingProxy
public class DemoInterfaceA {
  public String doWork(String txt) {
    return "XX" + txt;
  }
}