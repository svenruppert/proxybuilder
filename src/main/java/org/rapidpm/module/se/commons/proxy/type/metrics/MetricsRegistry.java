package org.rapidpm.module.se.commons.proxy.type.metrics;

import com.codahale.metrics.MetricRegistry;

/**
 * Created by sven on 28.04.15.
 */
public class MetricsRegistry {
  private static MetricsRegistry ourInstance = new MetricsRegistry();

  public static MetricsRegistry getInstance() {
    return ourInstance;
  }

  private MetricsRegistry() {
  }

  private final MetricRegistry metrics = new MetricRegistry();

  public MetricRegistry getMetrics() {
    return metrics;
  }
}
