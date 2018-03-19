/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.rapidpm.proxybuilder.core.metrics;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;

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
