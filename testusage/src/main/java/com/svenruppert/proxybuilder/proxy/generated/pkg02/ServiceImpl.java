/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the EUPL-1.2.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package com.svenruppert.proxybuilder.proxy.generated.pkg02;


import com.svenruppert.proxybuilder.proxy.generated.annotations.StaticMetricsProxy;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@StaticMetricsProxy
public class ServiceImpl {

  public String doWork(String txt) {
    return txt + LocalDateTime.now();
  }


  public void doNothing() {
  }

  public List<String> createList() throws NegativeArraySizeException {
    return Collections.emptyList();
  }

  public List<String> createListA() throws Exception {
    return Collections.emptyList();
  }


}