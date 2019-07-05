/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
  Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package junit.org.rapidpm.proxybuilder.type.dynamic;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import junit.org.rapidpm.proxybuilder.model.DemoInterface;
import junit.org.rapidpm.proxybuilder.model.DemoLogic;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.rapidpm.proxybuilder.core.metrics.RapidPMMetricsRegistry;
import org.rapidpm.proxybuilder.proxy.dymamic.DynamicProxyBuilder;
import org.rapidpm.proxybuilder.proxy.dymamic.virtual.CreationStrategy;

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
public class DynamicProxyBuilderTest {

  String s1;

  @Before
  public void before() throws Exception {
  }

  @After
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
    Assert.assertNotNull(demoLogic);
    final InnerDemoClass original = new InnerDemoClass();
    Assert.assertEquals(demoLogic.doWork(), original.doWork());
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
    Assert.assertNotNull(demoLogic);
    final InnerDemoClass original = new InnerDemoClass();
    Assert.assertEquals(demoLogic.doWork(), original.doWork());
  }

  @Test
  public void testAddSecurityRule001() throws Exception {
    final DemoLogic original = new DemoLogic();
    final DemoInterface demoLogic = DynamicProxyBuilder
        .createBuilder(DemoInterface.class, original)
        .addSecurityRule(() -> false)
        .build();
    Assert.assertNotNull(demoLogic);
    demoLogic.doSomething();
    Assert.assertNull(demoLogic.doSomething());
  }

  @Test
  public void testAddSecurityRule002() throws Exception {
    final DemoLogic original = new DemoLogic();
    final DemoInterface demoLogic = DynamicProxyBuilder
        .createBuilder(DemoInterface.class, original)
        .addSecurityRule(() -> true)
        .build();
    Assert.assertNotNull(demoLogic);
    Assert.assertEquals("doSomething-> DemoLogic", demoLogic.doSomething());
  }

  @Test
  public void testAddSecurityRule003() throws Exception {
    final InnerDemoClass original = new InnerDemoClass();
    final InnerDemoInterface demoLogic = DynamicProxyBuilder
        .createBuilder(InnerDemoInterface.class, original)
        .addSecurityRule(() -> false)
        .build();
    Assert.assertNotNull(demoLogic);
    Assert.assertNull(demoLogic.doWork());
  }

  @Test
  public void testAddSecurityRule004() throws Exception {
    final InnerDemoClass original = new InnerDemoClass();
    final InnerDemoInterface demoLogic = DynamicProxyBuilder
        .createBuilder(InnerDemoInterface.class, original)
        .addSecurityRule(() -> true)
        .build();
    Assert.assertNotNull(demoLogic);
    Assert.assertEquals("InnerDemoClass.doWork()", demoLogic.doWork());
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
    Assert.assertNotNull(demoLogic);
    Assert.assertNotEquals(demoLogic.doWork(), original.doWork());
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
    Assert.assertNotNull(demoLogic);
    Assert.assertEquals(demoLogic.doWork(), original.doWork());

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
    System.out.println("s1 = " + s1);


    final SortedMap<String, Histogram> histograms = metrics.getHistograms();
    Assert.assertNotNull(histograms);
    Assert.assertFalse(histograms.isEmpty());
    Assert.assertTrue(histograms.containsKey(InnerDemoInterface.class.getName() + ".doWork"));

    final Histogram histogram = histograms.get(InnerDemoInterface.class.getName() + ".doWork");
    Assert.assertNotNull(histogram);
    Assert.assertNotNull(histogram.getSnapshot());

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
