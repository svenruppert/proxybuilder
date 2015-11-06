package junit.org.rapidpm.proxybuilder.type.virtual.dynamic;

import junit.org.rapidpm.proxybuilder.model.DemoInterface;
import junit.org.rapidpm.proxybuilder.model.DemoLogic;
import org.junit.Assert;
import org.junit.Test;
import org.rapidpm.proxybuilder.type.virtual.CreationStrategy;
import org.rapidpm.proxybuilder.type.virtual.ProxyGenerator;
import org.rapidpm.proxybuilder.type.virtual.ProxyType;
import org.rapidpm.proxybuilder.type.virtual.dynamic.DefaultConstructorServiceFactory;
import org.rapidpm.proxybuilder.type.virtual.dynamic.creationstrategy.ServiceStrategyFactoryNotThreadSafe;
import org.rapidpm.proxybuilder.type.virtual.dynamic.VirtualDynamicProxyInvocationHandler;

/**
 * Created by svenruppert on 22.07.15.
 */
public class ProxyGeneratorVirtualTest {


  @Test
  public void test001() throws Exception {
    final ProxyGenerator<DemoInterface, DemoLogic> build = createBuilder4DemoLogic()
        .withCreationStrategy(CreationStrategy.NONE)
//        .withType(ProxyType.DYNAMIC)
        .build();
    final DemoInterface demoInterface = build.make();


    Assert.assertNotNull(demoInterface);
    Assert.assertEquals("doSomething-> DemoLogic", demoInterface.doSomething());
  }

  private ProxyGenerator.Builder<DemoInterface, DemoLogic> createBuilder4DemoLogic() {
    return ProxyGenerator.<DemoInterface, DemoLogic>newBuilder()
        .withSubject(DemoInterface.class)
//        .withRealClass(DemoLogic.class)
        .withServiceFactory(new DefaultConstructorServiceFactory<>(DemoLogic.class));
//        .withServiceStrategyFactory(new ServiceStrategyFactoryNotThreadSafe<>());
  }

  @Test
  public void test002() throws Exception {

    final VirtualDynamicProxyInvocationHandler.ServiceFactory<DemoLogic> serviceFactory = () -> {
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
//        .withType(ProxyType.DYNAMIC)
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
//        .withType(ProxyType.DYNAMIC)
        .withServiceFactory(new MyServiceFactory())
        .build()
        .make();

    Assert.assertNotNull(demoInterface);
    Assert.assertEquals("doSomething-> DemoLogic", demoInterface.doSomething());
  }

  private static class MyServiceFactory implements VirtualDynamicProxyInvocationHandler.ServiceFactory<DemoLogic> {

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
