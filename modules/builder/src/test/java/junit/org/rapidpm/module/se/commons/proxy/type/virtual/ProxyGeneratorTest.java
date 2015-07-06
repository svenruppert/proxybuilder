/*
 * Copyright [2014] [www.rapidpm.org / Sven Ruppert (sven.ruppert@rapidpm.org)]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package junit.org.rapidpm.module.se.commons.proxy.type.virtual;

import junit.org.rapidpm.module.se.commons.DemoClassA;
import junit.org.rapidpm.module.se.commons.DemoClassB;
import junit.org.rapidpm.module.se.commons.DemoClassC;
import junit.org.rapidpm.module.se.commons.proxy.DemoInterface;
import junit.org.rapidpm.module.se.commons.proxy.DemoLogic;
import org.junit.Assert;
import org.junit.Test;
import org.rapidpm.proxybuilder.type.virtual.Concurrency;
import org.rapidpm.proxybuilder.type.virtual.ProxyGenerator;

/**
 * Created by Sven Ruppert on 19.02.14.
 */
public class ProxyGeneratorTest {


  @Test
  public void testGenerator001() throws Exception {
    final DemoInterface demoInterface = ProxyGenerator.make(DemoInterface.class, DemoLogic.class, Concurrency.NO_DUPLICATES);
    Assert.assertNotNull(demoInterface);
    demoInterface.doSomething();
  }

  @Test
  public void testGenerator002() throws Exception {
    final DemoLogic demoInterface = ProxyGenerator.make(DemoLogic.class, DemoLogic.class, Concurrency.NO_DUPLICATES);
    Assert.assertNotNull(demoInterface);
    demoInterface.doSomething();
    Assert.assertEquals("nooop", demoInterface.getSomething());
  }

  @Test
  public void testGenerator003() throws Exception {
    DemoLogic logic = new DemoLogic();

    final Class<DemoLogic> aClass = (Class<DemoLogic>) logic.getClass();
    final DemoLogic demoInterface = ProxyGenerator.make(aClass, aClass, Concurrency.NO_DUPLICATES);
    demoInterface.doSomething();
    Assert.assertEquals("nooop", demoInterface.getSomething());

    final String value = demoInterface.getValue();
    Assert.assertNull(value);
  }

  @Test
  public void testGenerator004() throws Exception {
    DemoClassA demoClassA = new DemoClassA();
//        demoClassA.demoClassB = new DemoClassB();
    demoClassA.demoClassB = null;
//        demoClassA.demoClassB.value = " HoppelPoppel";
//        demoClassA.demoClassB.demoClassC = new DemoClassC();
//        demoClassA.demoClassB.demoClassC.setValue("DumDiDum");

    final DemoClassA demo = proxy(demoClassA);


    final DemoClassB demoClassB = demo.getDemoClassB();
//    System.out.println("demoClassB = " + demoClassB);
    Assert.assertTrue(demoClassB.toString().startsWith("NullObjectHolder"));
    final DemoClassC demoClassC = demoClassB.getDemoClassC();
//    System.out.println("demoClassC = " + demoClassC);
    Assert.assertTrue(demoClassC.toString().startsWith("NullObjectHolder"));
    final String value = demoClassB.getValue();
    Assert.assertNull(value);
    final String value1 = demoClassC.getValue();
    Assert.assertNull(value1);
  }

  private DemoClassA proxy(DemoClassA demoClassA) {
    final Class<DemoClassA> aClass = (Class<DemoClassA>) demoClassA.getClass();
    final DemoClassA demo = ProxyGenerator.make(aClass, aClass, Concurrency.OnExistingObject);

    final Class<? extends DemoClassA> aClassProxy = demo.getClass();
    try {
      aClassProxy.getDeclaredField("realSubject").set(demo, demoClassA);
    } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
      e.printStackTrace();
    }
    return demo;
  }


  @Test
  public void testGenerator00X() throws Exception {
    DemoClassA demoClassA = new DemoClassA();
    demoClassA.demoClassB = null;
    final String value = proxy(demoClassA).getDemoClassB().getDemoClassC().getValue();
    Assert.assertNull(value);

  }

  @Test
  public void testMake001() throws Exception {
    //ProxyType.DYNAMIC

  }

  @Test
  public void testMake002() throws Exception {

  }

  @Test
  public void testMake003() throws Exception {

  }
}
