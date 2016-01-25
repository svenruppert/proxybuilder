package org.rapidpm.proxybuilder.core.metrics;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;

import java.util.concurrent.TimeUnit;

/**
 * Created by Sven Ruppert on 28.04.15.
 */
public class RapidPMMetricsRegistry {


  public static final TimeUnit DURATION_UNIT = TimeUnit.MILLISECONDS;
  public static final TimeUnit RATE_UNIT = TimeUnit.MILLISECONDS;
  private static final Object LOCK = new Object();
  private static final RapidPMMetricsRegistry RAPID_PM_METRICS_REGISTRY = new RapidPMMetricsRegistry();
  private final MetricRegistry metrics = new MetricRegistry();

  private JmxReporter jmxReporter;
  private ConsoleReporter consoleReporter;

  private RapidPMMetricsRegistry() {
  }

  public static RapidPMMetricsRegistry getInstance() {
    return RAPID_PM_METRICS_REGISTRY;
  }

  public MetricRegistry getMetrics() {
    return metrics;
  }

  public void startJmxReporter() {

    synchronized (LOCK) {
      if (jmxReporter == null) {
        jmxReporter = JmxReporter.forRegistry(metrics)
            .convertDurationsTo(DURATION_UNIT)
            .convertRatesTo(RATE_UNIT)
            .build();
      }
      jmxReporter.start();
    }

  }

  public void stopJmxReporter() {
    synchronized (LOCK) {
      if (jmxReporter != null) {
        jmxReporter.stop();
        jmxReporter.close();
        jmxReporter = null;
      }
    }
  }

  public void startConsoleReporter() {
    synchronized (LOCK) {
      if (consoleReporter == null) {
        consoleReporter = ConsoleReporter.forRegistry(metrics)
            .convertDurationsTo(DURATION_UNIT)
            .convertRatesTo(RATE_UNIT)
            .build();
        consoleReporter.start(5, TimeUnit.SECONDS);
      }
    }
  }

  public void stopConsoleReporter() {
    synchronized (LOCK) {
      if (consoleReporter != null) {
        consoleReporter.stop();
        consoleReporter.close();
        consoleReporter = null;
      }
    }
  }

}
