/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the EUPL-1.2.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package junit.com.svenruppert.proxybuilder.model;


public class DemoClassA implements DemoInterface {
  public DemoClassB demoClassB;

  public DemoClassB getDemoClassB() {
    return demoClassB;
  }

  @Override
  public String doSomething() {
    return null;
  }
}