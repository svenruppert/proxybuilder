package org.rapidpm.proxybuilder.core.metrics;

import com.codahale.metrics.MetricRegistry;

/**
 * Created by sven on 28.04.15.
 */
public class RapidPMMetricsRegistry {
  private static RapidPMMetricsRegistry ourInstance = new RapidPMMetricsRegistry();
  private final MetricRegistry metrics = new MetricRegistry();

  private RapidPMMetricsRegistry() {
  }

  public static RapidPMMetricsRegistry getInstance() {
    return ourInstance;
  }

  public MetricRegistry getMetrics() {
    return metrics;
  }
}
