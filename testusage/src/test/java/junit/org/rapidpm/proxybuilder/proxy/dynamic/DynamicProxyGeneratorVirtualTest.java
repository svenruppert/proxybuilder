/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
  Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package junit.org.rapidpm.proxybuilder.proxy.dynamic;

import junit.org.rapidpm.proxybuilder.model.DemoInterface;
import junit.org.rapidpm.proxybuilder.model.DemoLogic;
import org.junit.Assert;
import org.junit.Test;
import org.rapidpm.proxybuilder.proxy.dymamic.virtual.strategy.CreationStrategy;
import org.rapidpm.proxybuilder.proxy.dymamic.virtual.factory.DefaultConstructorServiceFactory;
import org.rapidpm.proxybuilder.proxy.dymamic.virtual.DynamicProxyGenerator;
import org.rapidpm.proxybuilder.proxy.dymamic.virtual.DynamicProxyGenerator.Builder;
import org.rapidpm.proxybuilder.proxy.dymamic.virtual.factory.ServiceFactory;

public class DynamicProxyGeneratorVirtualTest {


  @Test
  public void test001() throws Exception {
    final DynamicProxyGenerator<DemoInterface, DemoLogic> build = createBuilder4DemoLogic()
        .withCreationStrategy(CreationStrategy.NONE)
        .build();
    final DemoInterface demoInterface = build.make();


    Assert.assertNotNull(demoInterface);
    Assert.assertEquals("doSomething-> DemoLogic", demoInterface.doSomething());
  }

  private Builder<DemoInterface, DemoLogic> createBuilder4DemoLogic() {
    return DynamicProxyGenerator.<DemoInterface, DemoLogic>newBuilder()
        .withSubject(DemoInterface.class)
        .withServiceFactory(new DefaultConstructorServiceFactory<>(DemoLogic.class));
  }

  @Test
  public void test002() throws Exception {

    final ServiceFactory<DemoLogic> serviceFactory = () -> {
      DemoLogic newInstance = null;
      try {
        newInstance = DemoLogic.class.newInstance();
      } catch (InstantiationException | IllegalAccessException e) {
        e.printStackTrace();
      }
      return newInstance;
    };

    final DemoInterface demoInterface = createBuilder4DemoLogic()
        .withCreationStrategy(CreationStrategy.NONE)
        .withServiceFactory(serviceFactory)
        .build()
        .make();

    Assert.assertNotNull(demoInterface);
    Assert.assertEquals("doSomething-> DemoLogic", demoInterface.doSomething());
  }

  @Test
  public void test003() throws Exception {

    final DemoInterface demoInterface = createBuilder4DemoLogic()
        .withCreationStrategy(CreationStrategy.NONE)
        .withServiceFactory(new MyServiceFactory())
        .build()
        .make();

    Assert.assertNotNull(demoInterface);
    Assert.assertEquals("doSomething-> DemoLogic", demoInterface.doSomething());
  }

  private static class MyServiceFactory implements ServiceFactory<DemoLogic> {

    @Override
    public DemoLogic createInstance() {
      DemoLogic newInstance = null;
      try {
        newInstance = DemoLogic.class.newInstance();
      } catch (InstantiationException | IllegalAccessException e) {
        e.printStackTrace();
      }
      return newInstance;
    }
  }


}
