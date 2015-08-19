package junit.org.rapidpm.proxybuilder;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import junit.org.rapidpm.proxybuilder.model.DemoInterface;
import junit.org.rapidpm.proxybuilder.model.DemoLogic;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.rapidpm.proxybuilder.VirtualProxyBuilder;
import org.rapidpm.proxybuilder.type.metrics.MetricsRegistry;
import org.rapidpm.proxybuilder.type.virtual.Concurrency;

import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * ProxyBuilder Tester.
 *
 * @author <Sven Rupperte>
 * @version 1.0
 * @since <pre>Apr 28, 2015</pre>
 */
public class VirtualProxyBuilderTest {

  String s1;

  @Before
  public void before() throws Exception {
  }

  @After
  public void after() throws Exception {
  }

  /**
   * Method: createBuilder(T original)
   */
  @Test
  public void testCreateBuilder01() throws Exception {
//TODO: Test goes here...
  }

  @Test
  public void testCreateBuilder02() throws Exception {
    final VirtualProxyBuilder<InnerDemoInterface, InnerDemoClass> builder = VirtualProxyBuilder.createBuilder(
        InnerDemoInterface.class,
        InnerDemoClass.class,
        Concurrency.NONE);
    final InnerDemoInterface demoLogic = builder.build();
    Assert.assertNotNull(demoLogic);
    final InnerDemoClass original = new InnerDemoClass();
    Assert.assertEquals(demoLogic.doWork(), original.doWork());
  }

  @Test
  public void testCreateBuilder03() throws Exception {
    final VirtualProxyBuilder<InnerDemoInterface, InnerDemoClass> builder = VirtualProxyBuilder.createBuilder(
        InnerDemoInterface.class,
        InnerDemoClass.class,
        Concurrency.NONE);

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
    final DemoInterface demoLogic = VirtualProxyBuilder
        .createBuilder(DemoInterface.class, original)
        .addSecurityRule(() -> false)
        .build();
    Assert.assertNotNull(demoLogic);
    demoLogic.doSomething();
  }

  @Test
  public void testAddSecurityRule002() throws Exception {
    final DemoLogic original = new DemoLogic();
    final DemoInterface demoLogic = VirtualProxyBuilder
        .createBuilder(DemoInterface.class, original)
        .addSecurityRule(() -> true)
        .build();
    Assert.assertNotNull(demoLogic);
    demoLogic.doSomething();
  }

  @Test
  public void testAddSecurityRule003() throws Exception {
    final InnerDemoClass original = new InnerDemoClass();
    final InnerDemoInterface demoLogic = VirtualProxyBuilder
        .createBuilder(InnerDemoInterface.class, original)
        .addSecurityRule(() -> false)
        .build();
    Assert.assertNotNull(demoLogic);
    Assert.assertEquals(demoLogic.doWork(), null);
  }

  @Test
  public void testAddSecurityRule004() throws Exception {
    final InnerDemoClass original = new InnerDemoClass();
    final InnerDemoInterface demoLogic = VirtualProxyBuilder
        .createBuilder(InnerDemoInterface.class, original)
        .addSecurityRule(() -> true)
        .build();
    Assert.assertNotNull(demoLogic);
    Assert.assertEquals(demoLogic.doWork(), "InnerDemoClass.doWork()");
  }

  @Test
  public void testAddSecurityRule005() throws Exception {
    final InnerDemoClass original = new InnerDemoClass();
    final InnerDemoInterface demoLogic = VirtualProxyBuilder
        .createBuilder(InnerDemoInterface.class, original)
        .addSecurityRule(() -> true)
        .addSecurityRule(() -> true)
        .addSecurityRule(() -> false)
        .build();
    Assert.assertNotNull(demoLogic);
    Assert.assertNotEquals(demoLogic.doWork(), original.doWork());
  }

  /**
   * Method: addMetrics()
   */
  @Test
  public void testAddMetrics() throws Exception {

    final InnerDemoClass original = new InnerDemoClass();
    final InnerDemoInterface demoLogic = VirtualProxyBuilder
        .createBuilder(InnerDemoInterface.class, original)
        .addSecurityRule(() -> true)
        .addSecurityRule(() -> true)
        .addMetrics()
        .build();
    Assert.assertNotNull(demoLogic);
    Assert.assertEquals(demoLogic.doWork(), original.doWork());

    final MetricRegistry metrics = MetricsRegistry.getInstance().getMetrics();
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
    Assert.assertTrue(histograms.containsKey(InnerDemoInterface.class.getSimpleName() + ".doWork"));

    final Histogram histogram = histograms.get(InnerDemoInterface.class.getSimpleName() + ".doWork");
    Assert.assertNotNull(histogram);
    Assert.assertNotNull(histogram.getSnapshot());

    reporter.close();
  }

  private void workingHole(String s) {
    s1 = s;
  }


  /**
   * Method: addVirtualProxy()
   */
  @Test
  public void testAddVirtualProxy() throws Exception {


  }

  /**
   * Method: addLogging()
   */
  @Test
  public void testAddLogging() throws Exception {
//TODO: Test goes here... 
  }

  /**
   * Method: checkRule()
   */
  @Test
  public void testCheckRule() throws Exception {
//TODO: Test goes here... 
  }


} 
