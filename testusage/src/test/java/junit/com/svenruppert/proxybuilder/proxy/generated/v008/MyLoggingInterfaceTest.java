/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the EUPL-1.2.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package junit.com.svenruppert.proxybuilder.proxy.generated.v008;


import org.junit.jupiter.api.Test;
import com.svenruppert.proxybuilder.proxy.generated.v008.LoggerExample;
import com.svenruppert.proxybuilder.proxy.generated.v008.MyLoggingInterface;
import com.svenruppert.proxybuilder.proxy.generated.v008.MyLoggingInterfaceStaticLoggingProxy;

import java.util.List;

import static java.util.Arrays.asList;

class MyLoggingInterfaceTest {


  @Test
  void test001() {
    final MyLoggingInterface demo = new MyLoggingInterfaceStaticLoggingProxy()
        .withDelegator(new LoggerExample());

    final List<Integer> list = demo
        .unwrapList(asList(1, 2, 3, 4), "AEAEA");
//
  }
}