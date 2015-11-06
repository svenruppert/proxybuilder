package junit.org.rapidpm.proxybuilder.type.dynamic;

import junit.org.rapidpm.proxybuilder.model.DemoInterface;
import junit.org.rapidpm.proxybuilder.model.DemoLogic;
import org.junit.Assert;
import org.junit.Test;
import org.rapidpm.proxybuilder.type.dymamic.virtual.CreationStrategy;
import org.rapidpm.proxybuilder.type.dymamic.virtual.DefaultConstructorServiceFactory;
import org.rapidpm.proxybuilder.type.dymamic.virtual.DynamicProxyGenerator;
import org.rapidpm.proxybuilder.type.dymamic.virtual.VirtualDynamicProxyInvocationHandler;

/**
 * Created by svenruppert on 22.07.15.
 */
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

  private DynamicProxyGenerator.Builder<DemoInterface, DemoLogic> createBuilder4DemoLogic() {
    return DynamicProxyGenerator.<DemoInterface, DemoLogic>newBuilder()
        .withSubject(DemoInterface.class)
        .withServiceFactory(new DefaultConstructorServiceFactory<>(DemoLogic.class));
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
