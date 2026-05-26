/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the EUPL-1.2.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package junit.com.svenruppert.proxybuilder.proxy.dynamic;


import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.svenruppert.dependencies.core.logger.HasLogger;
import junit.com.svenruppert.proxybuilder.model.DemoInterface;
import junit.com.svenruppert.proxybuilder.model.DemoLogic;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.svenruppert.proxybuilder.RapidPMMetricsRegistry;
import com.svenruppert.proxybuilder.proxy.dymamic.DynamicProxyBuilder;
import com.svenruppert.proxybuilder.proxy.dymamic.virtual.strategy.CreationStrategy;

import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * ProxyBuilder Tester.
 *
 * @author Sven Rupperte
 * @version 1.0
 * @since <pre>Apr 28, 2015</pre>
 */
public class DynamicProxyBuilderTest implements HasLogger {

  String s1;

  @BeforeEach
  public void before() throws Exception {
  }

  @AfterEach
  public void after() throws Exception {
  }


  @Test
  public void testCreateBuilder01()  {
//TODO: Test goes here...
  }

  @Test
  public void testCreateBuilder02() throws Exception {
    final DynamicProxyBuilder<InnerDemoInterface, InnerDemoClass> builder = DynamicProxyBuilder.createBuilder(
        InnerDemoInterface.class,
        InnerDemoClass.class,
        CreationStrategy.NONE);
    final InnerDemoInterface demoLogic = builder.build();
    Assertions.assertNotNull(demoLogic);
    final InnerDemoClass original = new InnerDemoClass();
    Assertions.assertEquals(demoLogic.doWork(), original.doWork());
  }

  @Test
  public void testCreateBuilder03() throws Exception {
    final DynamicProxyBuilder<InnerDemoInterface, InnerDemoClass> builder = DynamicProxyBuilder.createBuilder(
        InnerDemoInterface.class,
        InnerDemoClass.class,
        CreationStrategy.NONE);

    builder.addSecurityRule(() -> true);
    builder.addMetrics();

    final InnerDemoInterface demoLogic = builder.build();
    Assertions.assertNotNull(demoLogic);
    final InnerDemoClass original = new InnerDemoClass();
    Assertions.assertEquals(demoLogic.doWork(), original.doWork());
  }

  @Test
  public void testAddSecurityRule001() throws Exception {
    final DemoLogic original = new DemoLogic();
    final DemoInterface demoLogic = DynamicProxyBuilder
        .createBuilder(DemoInterface.class, original)
        .addSecurityRule(() -> false)
        .build();
    Assertions.assertNotNull(demoLogic);
    demoLogic.doSomething();
    Assertions.assertNull(demoLogic.doSomething());
  }

  @Test
  public void testAddSecurityRule002() throws Exception {
    final DemoLogic original = new DemoLogic();
    final DemoInterface demoLogic = DynamicProxyBuilder
        .createBuilder(DemoInterface.class, original)
        .addSecurityRule(() -> true)
        .build();
    Assertions.assertNotNull(demoLogic);
    Assertions.assertEquals("doSomething-> DemoLogic", demoLogic.doSomething());
  }

  @Test
  public void testAddSecurityRule003() throws Exception {
    final InnerDemoClass original = new InnerDemoClass();
    final InnerDemoInterface demoLogic = DynamicProxyBuilder
        .createBuilder(InnerDemoInterface.class, original)
        .addSecurityRule(() -> false)
        .build();
    Assertions.assertNotNull(demoLogic);
    Assertions.assertNull(demoLogic.doWork());
  }

  @Test
  public void testAddSecurityRule004() throws Exception {
    final InnerDemoClass original = new InnerDemoClass();
    final InnerDemoInterface demoLogic = DynamicProxyBuilder
        .createBuilder(InnerDemoInterface.class, original)
        .addSecurityRule(() -> true)
        .build();
    Assertions.assertNotNull(demoLogic);
    Assertions.assertEquals("InnerDemoClass.doWork()", demoLogic.doWork());
  }

  @Test
  public void testAddSecurityRule005() throws Exception {
    final InnerDemoClass original = new InnerDemoClass();
    final InnerDemoInterface demoLogic = DynamicProxyBuilder
        .createBuilder(InnerDemoInterface.class, original)
        .addSecurityRule(() -> true)
        .addSecurityRule(() -> true)
        .addSecurityRule(() -> false)
        .build();
    Assertions.assertNotNull(demoLogic);
    Assertions.assertNotEquals(demoLogic.doWork(), original.doWork());
  }

  @Test
  public void testAddMetrics() throws Exception {

    final InnerDemoClass original = new InnerDemoClass();
    final InnerDemoInterface demoLogic = DynamicProxyBuilder
        .createBuilder(InnerDemoInterface.class, original)
        .addSecurityRule(() -> true)
        .addSecurityRule(() -> true)
        .addMetrics()
        .build();
    Assertions.assertNotNull(demoLogic);
    Assertions.assertEquals(demoLogic.doWork(), original.doWork());

    final MetricRegistry metrics = RapidPMMetricsRegistry.getInstance().getMetrics();
    final ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
        .convertRatesTo(TimeUnit.NANOSECONDS)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .build();
    reporter.start(1, TimeUnit.SECONDS);

    IntStream.range(0, 10_000_000).forEach(i -> {
      final String s = demoLogic.doWork();
      workingHole(s.toUpperCase());
    });
    logger().info("s1 = {}", s1);


    final SortedMap<String, Histogram> histograms = metrics.getHistograms();
    Assertions.assertNotNull(histograms);
    Assertions.assertFalse(histograms.isEmpty());
    Assertions.assertTrue(histograms.containsKey(InnerDemoInterface.class.getName() + ".doWork"));

    final Histogram histogram = histograms.get(InnerDemoInterface.class.getName() + ".doWork");
    Assertions.assertNotNull(histogram);
    Assertions.assertNotNull(histogram.getSnapshot());

    reporter.close();
  }

  private void workingHole(String s) {
    s1 = s;
  }


  @Test
  public void testAddVirtualProxy() throws Exception {


  }


  @Test
  public void testAddLogging() throws Exception {
//TODO: Test goes here...
  }


  @Test
  public void testCheckRule() throws Exception {
//TODO: Test goes here...
  }


}