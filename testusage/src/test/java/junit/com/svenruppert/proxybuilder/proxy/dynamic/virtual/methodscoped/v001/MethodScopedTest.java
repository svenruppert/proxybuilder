/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the EUPL-1.2.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package junit.com.svenruppert.proxybuilder.proxy.dynamic.virtual.methodscoped.v001;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.svenruppert.proxybuilder.proxy.dymamic.DynamicProxyBuilder;
import com.svenruppert.proxybuilder.proxy.dymamic.virtual.strategy.CreationStrategy;

public class MethodScopedTest {


  static int constructorCounter = 0;


  @Test
  public void test001() throws Exception {

    final Service service = DynamicProxyBuilder
        .createBuilder(Service.class, ServiceImpl.class, CreationStrategy.METHOD_SCOPED)
        .build();

    service.doWork("");
    Assertions.assertEquals(1, constructorCounter);

    service.doWork("");
    Assertions.assertEquals(2, constructorCounter);

    service.doWork("");
    Assertions.assertEquals(3, constructorCounter);
  }


  public interface Service {
    String doWork(String txt);
  }

  public static class ServiceImpl implements Service {

    public ServiceImpl() {
      constructorCounter += 1;
    }

    @Override
    public String doWork(final String txt) {
      return txt + " - impl";
    }
  }
}