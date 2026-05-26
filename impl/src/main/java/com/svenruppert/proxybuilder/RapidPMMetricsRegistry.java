/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the EUPL-1.2.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package com.svenruppert.proxybuilder;


import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jmx.JmxReporter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.concurrent.TimeUnit;

public class RapidPMMetricsRegistry {


  public static final TimeUnit DURATION_UNIT = TimeUnit.MILLISECONDS;
  public static final TimeUnit RATE_UNIT = TimeUnit.MILLISECONDS;
  private static final Object LOCK = new Object();
  private static final RapidPMMetricsRegistry RAPID_PM_METRICS_REGISTRY = new RapidPMMetricsRegistry();
  private final MetricRegistry metrics = new MetricRegistry();

  private JmxReporter     jmxReporter;
  private ConsoleReporter consoleReporter;

  private RapidPMMetricsRegistry() {
  }

  public static RapidPMMetricsRegistry getInstance() {
    return RAPID_PM_METRICS_REGISTRY;
  }

  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP",
      justification = "The shared MetricRegistry is intentionally exposed so proxy builders and callers can register and inspect metrics.")
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