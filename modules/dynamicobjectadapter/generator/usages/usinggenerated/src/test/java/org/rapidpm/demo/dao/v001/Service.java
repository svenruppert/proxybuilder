package org.rapidpm.demo.dao.v001;

import org.rapidpm.proxybuilder.dynamicobjectadapter.DynamicObjectAdapterBuilder;

/**
 * Created by svenruppert on 07.08.15.
 */
@DynamicObjectAdapterBuilder
public interface Service {
  String doWorkA();
  String doWorkB();
}
