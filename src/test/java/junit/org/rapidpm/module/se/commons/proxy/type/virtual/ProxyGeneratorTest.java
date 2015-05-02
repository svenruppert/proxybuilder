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
import org.junit.Test;
import org.rapidpm.module.se.commons.proxy.type.virtual.Concurrency;
import org.rapidpm.module.se.commons.proxy.type.virtual.ProxyGenerator;

/**
 * Created by ts40 on 19.02.14.
 */
public class ProxyGeneratorTest {


    @Test
    public void testGenerator001() throws  Exception {
        final DemoInterface demoInterface = ProxyGenerator.make(DemoInterface.class, DemoLogic.class, Concurrency.NO_DUPLICATES);
        demoInterface.doSomething();
    }

    @Test
    public void testGenerator002() throws  Exception {
        final DemoLogic demoInterface = ProxyGenerator.make(DemoLogic.class, DemoLogic.class, Concurrency.NO_DUPLICATES);
        demoInterface.doSomething();
        demoInterface.getSomething();
    }

    @Test
    public void testGenerator003() throws  Exception {
        DemoLogic logic = new DemoLogic();

        final Class<DemoLogic> aClass = (Class<DemoLogic>) logic.getClass();
        final DemoLogic demoInterface = ProxyGenerator.make(aClass, aClass, Concurrency.NO_DUPLICATES);
        demoInterface.doSomething();
        final String something = demoInterface.getSomething();

        final String value = demoInterface.getValue();
        System.out.println("value = " + value);
    }
    @Test
    public void testGenerator004() throws  Exception {
        DemoClassA demoClassA = new DemoClassA();
//        demoClassA.demoClassB = new DemoClassB();
        demoClassA.demoClassB = null;
//        demoClassA.demoClassB.value = " HoppelPoppel";
//        demoClassA.demoClassB.demoClassC = new DemoClassC();
//        demoClassA.demoClassB.demoClassC.setValue("DumDiDum");

        final DemoClassA demo = proxy(demoClassA);


        final DemoClassB demoClassB = demo.getDemoClassB();
        System.out.println("demoClassB = " + demoClassB);
        final DemoClassC demoClassC = demoClassB.getDemoClassC();
        System.out.println("demoClassC = " + demoClassC);
        final String value = demoClassB.getValue();
        System.out.println("value = " + value);

        System.out.println("demoClassC = " + demoClassC);
        final String value1 = demoClassC.getValue();
        System.out.println("value1 = " + value1);
    }

    private DemoClassA proxy(DemoClassA demoClassA) {
        final Class<DemoClassA> aClass = (Class<DemoClassA>) demoClassA.getClass();
        final DemoClassA demo = ProxyGenerator.make(aClass, aClass, Concurrency.OnExistingObject);

        final Class<? extends DemoClassA> aClassProxy = demo.getClass();
        try {
            aClassProxy.getDeclaredField("realSubject").set(demo,demoClassA );
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }
        return demo;
    }


    @Test
    public void testGenerator00X() throws  Exception {
        DemoClassA demoClassA = new DemoClassA();
        demoClassA.demoClassB = null;

        final String value = proxy(demoClassA).getDemoClassB().getDemoClassC().getValue();
        System.out.println("value = " + value);

    }

}
