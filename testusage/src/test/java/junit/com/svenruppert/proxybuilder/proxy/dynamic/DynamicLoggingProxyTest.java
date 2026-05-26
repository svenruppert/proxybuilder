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
import org.junit.jupiter.api.Test;
import com.svenruppert.proxybuilder.proxy.dymamic.DynamicProxyBuilder;
import com.svenruppert.proxybuilder.proxy.dymamic.virtual.strategy.CreationStrategy;

public class DynamicLoggingProxyTest implements HasLogger {


  @Test
  public void test001() throws Exception {
    final InnerDemoInterface proxy = DynamicProxyBuilder
        .createBuilder(
            InnerDemoInterface.class,
            InnerDemoClass.class,
            CreationStrategy.NONE)
        .addLogging()
        .build();
    logger().info("System.nanoTime() = {}", System.nanoTime());
    final String s = proxy.doWork();
    logger().info("System.nanoTime() = {}", System.nanoTime());
  }

  @Test
  public void test002() throws Exception {
    final DemoInterface proxy = DynamicProxyBuilder
        .createBuilder(
            DemoInterface.class,
            Demo.class,
            CreationStrategy.NONE)
        .addLogging()
        .build();
    logger().info("System.nanoTime() = {}", System.nanoTime());
    final String s = proxy.doWork("cc");
    logger().info("System.nanoTime() = {}", System.nanoTime());
  }


  public interface DemoInterface {
    String doWork(String txt);
  }

  public static class Demo implements DemoInterface {
    public String doWork(String txt) {
      return "XX" + txt;
    }
  }


}