package org.rapidpm.proxybuilder.type.metrics;

import com.codahale.metrics.MetricRegistry;

/**
 * Created by sven on 28.04.15.
 */
public class MetricsRegistry {
  private static MetricsRegistry ourInstance = new MetricsRegistry();
  private final MetricRegistry metrics = new MetricRegistry();

  private MetricsRegistry() {
  }

  public static MetricsRegistry getInstance() {
    return ourInstance;
  }

  public MetricRegistry getMetrics() {
    return metrics;
  }
}
