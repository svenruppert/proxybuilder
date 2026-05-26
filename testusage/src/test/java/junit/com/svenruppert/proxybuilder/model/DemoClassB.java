/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the EUPL-1.2.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package junit.com.svenruppert.proxybuilder.model;


public class DemoClassB {
  public String value;

  public DemoClassC demoClassC;

  public DemoClassC getDemoClassC() {
    return demoClassC;
  }

  public void setDemoClassC(DemoClassC demoClassC) {
    this.demoClassC = demoClassC;
  }

  public String getValue() {
    return value;
  }
}