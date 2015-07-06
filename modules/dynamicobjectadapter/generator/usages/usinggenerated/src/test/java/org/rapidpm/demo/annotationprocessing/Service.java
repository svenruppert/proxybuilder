package org.rapidpm.demo.annotationprocessing;

import org.rapidpm.proxybuilder.dynamicobjectadapter.DynamicObjectAdapterBuilder;

/**
 * Created by sven on 13.05.15.
 */
@DynamicObjectAdapterBuilder
public interface Service {
  String doWorkA(String txt);
  String doWorkB(String txt);
}
