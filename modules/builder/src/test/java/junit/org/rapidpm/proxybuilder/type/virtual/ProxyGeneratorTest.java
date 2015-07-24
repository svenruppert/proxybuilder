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

package junit.org.rapidpm.proxybuilder.type.virtual;

import junit.org.rapidpm.proxybuilder.model.DemoClassA;
import junit.org.rapidpm.proxybuilder.model.DemoClassB;
import junit.org.rapidpm.proxybuilder.model.DemoClassC;
import junit.org.rapidpm.proxybuilder.model.DemoInterface;
import junit.org.rapidpm.proxybuilder.model.DemoLogic;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.rapidpm.proxybuilder.type.virtual.Concurrency;
import org.rapidpm.proxybuilder.type.virtual.ProxyGenerator;
import org.rapidpm.proxybuilder.type.virtual.ProxyType;
import org.rapidpm.proxybuilder.type.virtual.dynamic.DefaultServiceFactory;
import org.rapidpm.proxybuilder.type.virtual.dynamic.ServiceStrategyFactoryNotThreadSafe;

/**
 * Created by Sven Ruppert on 19.02.14.
 */
public class ProxyGeneratorTest {


  @Test
  public void testGenerator001() throws Exception {

    final DemoInterface demoInterface = createBuilder4DemoLogic()
        .withConcurrency(Concurrency.NO_DUPLICATES)
        .withType(ProxyType.STATIC)
        .build()
        .make();
//    final DemoInterface demoInterface = ProxyGenerator.make(DemoInterface.class, DemoLogic.class, Concurrency.NO_DUPLICATES);
    Assert.assertNotNull(demoInterface);
    demoInterface.doSomething();
  }

  @NotNull
  private ProxyGenerator.Builder<DemoInterface, DemoLogic> createBuilder4DemoLogic() {
    return ProxyGenerator.<DemoInterface, DemoLogic>newBuilder()
          .withSubject(DemoInterface.class)
          .withRealClass(DemoLogic.class)
          .withServiceFactory(new DefaultServiceFactory<>(DemoLogic.class))
          .withServiceStrategyFactory(new ServiceStrategyFactoryNotThreadSafe<>());
  }

  @Test @Ignore
  public void testGenerator002() throws Exception {
//    final DemoLogic demoInterface = ProxyGenerator.make(DemoLogic.class, DemoLogic.class, Concurrency.NO_DUPLICATES);
    final DemoLogic demoInterface = (DemoLogic) createBuilder4DemoLogic()
        .withConcurrency(Concurrency.NO_DUPLICATES)
        .withType(ProxyType.STATIC)
        .build()
        .make();

    Assert.assertNotNull(demoInterface);
    demoInterface.doSomething();
    Assert.assertEquals("nooop", demoInterface.getSomething());
  }

  @Test @Ignore
  public void testGenerator003() throws Exception {
//    final DemoLogic demoInterface = ProxyGenerator.make(aClass, aClass, Concurrency.NO_DUPLICATES);
    final DemoLogic demoInterface = ( DemoLogic) createBuilder4DemoLogic()
        .withConcurrency(Concurrency.NO_DUPLICATES)
        .withType(ProxyType.STATIC)
        .build()
        .make();


    demoInterface.doSomething();
    Assert.assertEquals("nooop", demoInterface.getSomething());

    final String value = demoInterface.getValue();
    Assert.assertNull(value);
  }

  @Test @Ignore
  public void testGenerator004() throws Exception {
    DemoClassA demoClassA = new DemoClassA();
//        demoClassA.demoClassB = new DemoClassB();
    demoClassA.demoClassB = null;
//        demoClassA.demoClassB.value = " HoppelPoppel";
//        demoClassA.demoClassB.demoClassC = new DemoClassC();
//        demoClassA.demoClassB.demoClassC.setValue("DumDiDum");

    final DemoClassA demo = (DemoClassA) proxy(demoClassA);


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

  private DemoInterface proxy(DemoClassA demoClassA) {
    final Class<DemoClassA> aClass = (Class<DemoClassA>) demoClassA.getClass();
//    final DemoClassA demo = ProxyGenerator.make(aClass, aClass, Concurrency.OnExistingObject);

    final ProxyGenerator.Builder<DemoInterface, DemoClassA> proxyBuilder = ProxyGenerator.<DemoInterface, DemoClassA>newBuilder()
        .withSubject(DemoInterface.class)
        .withRealClass(aClass)
        .withConcurrency(Concurrency.OnExistingObject)
        .withType(ProxyType.OnExistingObject)
        .withServiceFactory(new DefaultServiceFactory<>(DemoClassA.class))
        .withServiceStrategyFactory(new ServiceStrategyFactoryNotThreadSafe<>());

    final DemoInterface demo = proxyBuilder.build().make();

    final Class<? extends DemoInterface> aClassProxy = demo.getClass();
    try {
      aClassProxy.getDeclaredField("realSubject").set(demo, demoClassA);
    } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
      e.printStackTrace();
    }
    return demo;
  }


  @Test @Ignore
  public void testGenerator00X() throws Exception {
    DemoClassA demoClassA = new DemoClassA();
    demoClassA.demoClassB = null;
    final String value = ((DemoClassA)proxy(demoClassA)).getDemoClassB().getDemoClassC().getValue();
    Assert.assertNull(value);

  }


}
