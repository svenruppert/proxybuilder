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
import junit.org.rapidpm.proxybuilder.model.DemoInterface;
import junit.org.rapidpm.proxybuilder.model.DemoLogic;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.rapidpm.proxybuilder.type.virtual.CreationStrategy;
import org.rapidpm.proxybuilder.type.virtual.ProxyGenerator;
import org.rapidpm.proxybuilder.type.virtual.ProxyType;
import org.rapidpm.proxybuilder.type.virtual.dynamic.DefaultConstructorServiceFactory;
import org.rapidpm.proxybuilder.type.virtual.dynamic.creationstrategy.ServiceStrategyFactoryNotThreadSafe;

import java.lang.reflect.Proxy;

/**
 * Created by Sven Ruppert on 19.02.14.
 */
public class ProxyGeneratorTest {


  @Test
  public void testGenerator001() throws Exception {

    final DemoInterface demoInterface = createBuilder4DemoLogic()
        .withCreationStrategy(CreationStrategy.NO_DUPLICATES)
        .withType(ProxyType.STATIC)
        .build()
        .make();
//    final DemoInterface demoInterface = ProxyGenerator.make(DemoInterface.class, DemoLogic.class, Concurrency.NO_DUPLICATES);
    Assert.assertNotNull(demoInterface);
    Assert.assertEquals("doSomething-> DemoLogic", demoInterface.doSomething());
    Assert.assertTrue(demoInterface.getClass().getSimpleName().contains("_NO_DUPLICATES"));
    Assert.assertFalse(Proxy.isProxyClass(demoInterface.getClass()));
  }

  private ProxyGenerator.Builder<DemoInterface, DemoLogic> createBuilder4DemoLogic() {
    return ProxyGenerator.<DemoInterface, DemoLogic>newBuilder()
        .withSubject(DemoInterface.class)
        .withRealClass(DemoLogic.class)
        .withServiceFactory(new DefaultConstructorServiceFactory<>(DemoLogic.class))
        .withServiceStrategyFactory(new ServiceStrategyFactoryNotThreadSafe<>());
  }

  @Test
  public void testGenerator003() throws Exception {
//    final DemoLogic demoInterface = ProxyGenerator.make(aClass, aClass, Concurrency.NO_DUPLICATES);
    final DemoInterface demoInterface = createBuilder4DemoLogic()
        .withCreationStrategy(CreationStrategy.SOME_DUPLICATES)
        .withType(ProxyType.STATIC)
        .build()
        .make();
    Assert.assertEquals("doSomething-> DemoLogic", demoInterface.doSomething());
    Assert.assertTrue(demoInterface.getClass().getSimpleName().contains("_SOME_DUPLICATES"));
    Assert.assertFalse(Proxy.isProxyClass(demoInterface.getClass()));
  }

  @Test
  @Ignore
  public void testGenerator004() throws Exception {
//    final DemoLogic demoInterface = ProxyGenerator.make(aClass, aClass, Concurrency.NO_DUPLICATES);
    final DemoInterface demoInterface = createBuilder4DemoLogic()
        .withCreationStrategy(CreationStrategy.OnExistingObject)
        .withType(ProxyType.STATIC)
        .build()
        .make();
    Assert.assertEquals("doSomething-> DemoLogic", demoInterface.doSomething());
    Assert.assertTrue(demoInterface.getClass().getSimpleName().contains("_OnExistingObject"));
    Assert.assertFalse(Proxy.isProxyClass(demoInterface.getClass()));
  }

  @Test
  @Ignore
  public void testGenerator005() throws Exception {
    DemoClassA demoClassA = new DemoClassA();
//        demoClassA.demoClassB = new DemoClassB();
    demoClassA.demoClassB = null;
//        demoClassA.demoClassB.value = " HoppelPoppel";
//        demoClassA.demoClassB.demoClassC = new DemoClassC();
//        demoClassA.demoClassB.demoClassC.setValue("DumDiDum");

    final DemoInterface demoInterface = proxy(demoClassA);
    Assert.assertNotNull(demoInterface);
    Assert.assertTrue(demoInterface.getClass().getSimpleName().contains("_OnExistingObject"));
    Assert.assertFalse(Proxy.isProxyClass(demoInterface.getClass()));

    DemoClassA demoClassA1 = (DemoClassA) demoInterface;


//    final DemoClassB demoClassB = demo.doSomething();
//    System.out.println("demoClassB = " + demoClassB);
//    Assert.assertTrue(demoClassB.toString().startsWith("NullObjectHolder"));
//    final DemoClassC demoClassC = demoClassB.getDemoClassC();
//    System.out.println("demoClassC = " + demoClassC);
//    Assert.assertTrue(demoClassC.toString().startsWith("NullObjectHolder"));
//    final String value = demoClassB.getValue();
//    Assert.assertNull(value);
//    final String value1 = demoClassC.getValue();
//    Assert.assertNull(value1);
  }

  private DemoInterface proxy(DemoClassA demoClassA) {
    final Class<DemoClassA> aClass = (Class<DemoClassA>) demoClassA.getClass();
//    final DemoClassA demo = ProxyGenerator.make(aClass, aClass, Concurrency.OnExistingObject);

    final ProxyGenerator.Builder<DemoInterface, DemoClassA> proxyBuilder = ProxyGenerator.<DemoInterface, DemoClassA>newBuilder()
        .withSubject(DemoInterface.class)
        .withRealClass(aClass)
        .withConcurrency(CreationStrategy.OnExistingObject)
        .withType(ProxyType.OnExistingObject)
        .withServiceFactory(new DefaultConstructorServiceFactory<>(DemoClassA.class))
        .withServiceStrategyFactory(new ServiceStrategyFactoryNotThreadSafe<>());

    final DemoInterface demo = proxyBuilder.build().make();

    final Class<? extends DemoInterface> aClassProxy = demo.getClass();
    try {
      aClassProxy.getDeclaredField("realSubject").set(demo, demoClassA);
    } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
      e.printStackTrace();
      System.out.println("e = " + e);
    }
    return demo;
  }


  @Test
  @Ignore
  public void testGenerator00X() throws Exception {
    DemoClassA demoClassA = new DemoClassA();
    demoClassA.demoClassB = null;
    final String value = ((DemoClassA) proxy(demoClassA)).getDemoClassB().getDemoClassC().getValue();
    Assert.assertNull(value);

  }


}
