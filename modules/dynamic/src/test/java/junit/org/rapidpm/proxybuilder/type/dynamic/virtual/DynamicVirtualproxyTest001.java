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
package junit.org.rapidpm.proxybuilder.type.dynamic.virtual;

import org.junit.Assert;
import org.junit.Test;
import org.rapidpm.proxybuilder.proxy.dymamic.DynamicProxyBuilder;
import org.rapidpm.proxybuilder.proxy.dymamic.virtual.CreationStrategy;


public class DynamicVirtualproxyTest001 {


  @Test(expected = MyException.class)
  public void test001() throws Exception {
    final Service service = DynamicProxyBuilder
        .createBuilder(Service.class, MyService.class, CreationStrategy.NONE)
        .build();
    work(service);
  }

  private void work(final Service service) throws MyException {
    try {
      service.doWork();
    } catch (MyException e) {
      Assert.assertNotNull(e);
      Assert.assertEquals(MyException.class, e.getClass());
      throw e;
    }
  }

  @Test(expected = MyException.class)
  public void test002() throws Exception {
    final Service service = DynamicProxyBuilder
        .createBuilder(Service.class, MyService.class, CreationStrategy.NONE)
        .addMetrics()
        .build();
    work(service);
  }

  @Test(expected = MyException.class)
  public void test003() throws Exception {
    final Service service = DynamicProxyBuilder
        .createBuilder(Service.class, MyService.class, CreationStrategy.NONE)
        .addSecurityRule(()->true)
        .build();

    work(service);
  }

  @Test(expected = MyException.class)
  public void test004() throws Exception {
    final Service service = DynamicProxyBuilder
        .createBuilder(Service.class, MyService.class, CreationStrategy.NONE)
        .addSecurityRule(()->true)
        .addMetrics()
        .build();

    work(service);
  }

  @Test(expected = MyException.class)
  public void test005() throws Exception {
    final Service service = DynamicProxyBuilder
        .createBuilder(Service.class, MyService.class, CreationStrategy.NONE)
        .addSecurityRule(()->true)
        .addMetrics()
        .addIPreAction((original, method, args) -> {
          throw new MyException();
        })
        .build();

    work(service);
  }

  public interface Service {
    String doWork() throws MyException;
  }

  public static class MyException extends Exception {

  }

  public static class MyService implements Service {
    @Override
    public String doWork() throws MyException {

      System.out.println("MyService = " + true);

      throw new MyException();
    }
  }

}
