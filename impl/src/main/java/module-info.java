/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the EUPL-1.2.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
module com.svenruppert.proxybuilder {
  requires transitive java.compiler;
  requires transitive com.squareup.javapoet;
  requires transitive com.svenruppert.proxybuilder.annotations;
  requires transitive core;
  requires org.slf4j;
  requires transitive com.codahale.metrics;
  requires com.codahale.metrics.jmx;
  requires static com.github.spotbugs.annotations;

  exports com.svenruppert.proxybuilder;
  exports com.svenruppert.proxybuilder.objectadapter;
  exports com.svenruppert.proxybuilder.objectadapter.dynamic;
  exports com.svenruppert.proxybuilder.objectadapter.generated;
  exports com.svenruppert.proxybuilder.proxy.dymamic;
  exports com.svenruppert.proxybuilder.proxy.dymamic.virtual;
  exports com.svenruppert.proxybuilder.proxy.dymamic.virtual.strategy;
  exports com.svenruppert.proxybuilder.proxy.generated;
  exports com.svenruppert.proxybuilder.proxy.generated.annotations;
}
