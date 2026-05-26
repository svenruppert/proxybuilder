/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the EUPL-1.2.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package com.svenruppert.proxybuilder.objectadapter.testusage.v001;


import com.svenruppert.proxybuilder.objectadapter.generated.StaticObjectAdapter;

@StaticObjectAdapter
public class ServiceImpl implements Service {
  @Override
  public String doWork(final String txt) {
    return txt + " - ServiceImpl";
  }

  @Override
  public String doMoreWorkA(final String txt) {
    return txt + " - ServiceImpl.doMoreWorkA";
  }

  @Override
  public String doMoreWorkB(final String txt) {
    return txt + " - ServiceImpl.doMoreWorkB";
  }

  @Override
  public String doMoreWorkC(final String txt) {
    return txt + " - ServiceImpl.doMoreWorkC";
  }

  @Override
  public String doMoreWorkD(final String txt) {
    return txt + " - ServiceImpl.doMoreWorkD";
  }
}