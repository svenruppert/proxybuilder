/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the EUPL-1.2.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package junit.com.svenruppert.proxybuilder.proxy.dynamic.virtual;


import com.svenruppert.dependencies.core.logger.HasLogger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.svenruppert.proxybuilder.proxy.dymamic.DynamicProxyBuilder;
import com.svenruppert.proxybuilder.proxy.dymamic.virtual.strategy.CreationStrategy;


public class DynamicVirtualproxyTest001 {


  @Test
  public void test001() throws Exception {
    final Service service = DynamicProxyBuilder
        .createBuilder(Service.class, MyService.class, CreationStrategy.NONE)
        .build();
    Assertions.assertThrows(MyException.class, () -> work(service));
  }

  private void work(final Service service) throws MyException {
    try {
      service.doWork();
    } catch (MyException e) {
      Assertions.assertNotNull(e);
      Assertions.assertEquals(MyException.class, e.getClass());
      throw e;
    }
  }

  @Test
  public void test002() throws Exception {
    final Service service = DynamicProxyBuilder
        .createBuilder(Service.class, MyService.class, CreationStrategy.NONE)
        .addMetrics()
        .build();
    Assertions.assertThrows(MyException.class, () -> work(service));
  }

  @Test
  public void test003() throws Exception {
    final Service service = DynamicProxyBuilder
        .createBuilder(Service.class, MyService.class, CreationStrategy.NONE)
        .addSecurityRule(()->true)
        .build();

    Assertions.assertThrows(MyException.class, () -> work(service));
  }

  @Test
  public void test004() throws Exception {
    final Service service = DynamicProxyBuilder
        .createBuilder(Service.class, MyService.class, CreationStrategy.NONE)
        .addSecurityRule(()->true)
        .addMetrics()
        .build();

    Assertions.assertThrows(MyException.class, () -> work(service));
  }

  @Test
  public void test005() throws Exception {
    final Service service = DynamicProxyBuilder
        .createBuilder(Service.class, MyService.class, CreationStrategy.NONE)
        .addSecurityRule(()->true)
        .addMetrics()
        .addIPreAction((original, method, args) -> {
          throw new MyException();
        })
        .build();

    Assertions.assertThrows(MyException.class, () -> work(service));
  }

  public interface Service {
    String doWork() throws MyException;
  }

  public static class MyException extends Exception {

  }

  public static class MyService implements Service, HasLogger {
    @Override
    public String doWork() throws MyException {

      logger().info("MyService = {}", true);

      throw new MyException();
    }
  }

}