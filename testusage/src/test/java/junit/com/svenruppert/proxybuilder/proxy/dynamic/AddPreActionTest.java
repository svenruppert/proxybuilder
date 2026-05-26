/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the EUPL-1.2.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package junit.com.svenruppert.proxybuilder.proxy.dynamic;


import com.svenruppert.dependencies.core.logger.HasLogger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.svenruppert.proxybuilder.proxy.dymamic.DynamicProxyBuilder;
import com.svenruppert.proxybuilder.proxy.dymamic.virtual.strategy.CreationStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddPreActionTest implements HasLogger {


  @Test
  public void test001() throws Exception {
    final DemoService build = DynamicProxyBuilder
        .createBuilder(DemoService.class, DemoServiceImplementation.class, CreationStrategy.NONE)
        .addIPreAction((original, method, args) -> {
          logger().info("original = {}", original);
        })
        .build();
    Assertions.assertEquals("hhimpl", build.doWork("hh"));
  }

  @Test
  public void test002() throws Exception {
    final List<Boolean> done = new ArrayList<>();

    final Map build = DynamicProxyBuilder
        .createBuilder(Map.class, HashMap.class, CreationStrategy.NONE)
        .addIPreAction((original, method, args) -> {
          logger().info("original = {}", original);
          done.add(true);
        })
        .build();
    Assertions.assertTrue(done.isEmpty());
    final int size = build.size();
    Assertions.assertFalse(done.isEmpty());
  }

  public interface DemoService {
    String doWork(String txt);
  }

  public static class DemoServiceImplementation implements DemoService {
    @Override
    public String doWork(final String txt) {
      return txt + "impl";
    }
  }


}