/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the EUPL-1.2.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package junit.com.svenruppert.proxybuilder.proxy.dynamic;


import com.svenruppert.dependencies.core.logger.HasLogger;
import junit.com.svenruppert.proxybuilder.model.DemoInterface;
import junit.com.svenruppert.proxybuilder.model.DemoLogic;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.svenruppert.proxybuilder.proxy.dymamic.virtual.strategy.CreationStrategy;
import com.svenruppert.proxybuilder.proxy.dymamic.virtual.factory.DefaultConstructorServiceFactory;
import com.svenruppert.proxybuilder.proxy.dymamic.virtual.DynamicProxyGenerator;
import com.svenruppert.proxybuilder.proxy.dymamic.virtual.DynamicProxyGenerator.Builder;
import com.svenruppert.proxybuilder.proxy.dymamic.virtual.factory.ServiceFactory;

public class DynamicProxyGeneratorVirtualTest implements HasLogger {


  @Test
  public void test001() throws Exception {
    final DynamicProxyGenerator<DemoInterface, DemoLogic> build = createBuilder4DemoLogic()
        .withCreationStrategy(CreationStrategy.NONE)
        .build();
    final DemoInterface demoInterface = build.make();


    Assertions.assertNotNull(demoInterface);
    Assertions.assertEquals("doSomething-> DemoLogic", demoInterface.doSomething());
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
        logger().warn("Could not create DemoLogic instance", e);
      }
      return newInstance;
    };

    final DemoInterface demoInterface = createBuilder4DemoLogic()
        .withCreationStrategy(CreationStrategy.NONE)
        .withServiceFactory(serviceFactory)
        .build()
        .make();

    Assertions.assertNotNull(demoInterface);
    Assertions.assertEquals("doSomething-> DemoLogic", demoInterface.doSomething());
  }

  @Test
  public void test003() throws Exception {

    final DemoInterface demoInterface = createBuilder4DemoLogic()
        .withCreationStrategy(CreationStrategy.NONE)
        .withServiceFactory(new MyServiceFactory())
        .build()
        .make();

    Assertions.assertNotNull(demoInterface);
    Assertions.assertEquals("doSomething-> DemoLogic", demoInterface.doSomething());
  }

  private static class MyServiceFactory implements ServiceFactory<DemoLogic>, HasLogger {

    @Override
    public DemoLogic createInstance() {
      DemoLogic newInstance = null;
      try {
        newInstance = DemoLogic.class.newInstance();
      } catch (InstantiationException | IllegalAccessException e) {
        logger().warn("Could not create DemoLogic instance", e);
      }
      return newInstance;
    }
  }


}