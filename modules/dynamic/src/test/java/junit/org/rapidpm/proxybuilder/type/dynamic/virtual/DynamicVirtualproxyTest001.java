package junit.org.rapidpm.proxybuilder.type.dynamic.virtual;

import org.junit.Assert;
import org.junit.Test;
import org.rapidpm.proxybuilder.type.dymamic.DynamicProxyBuilder;
import org.rapidpm.proxybuilder.type.dymamic.virtual.CreationStrategy;


/**
 * Created by svenruppert on 10.11.15.
 */
public class DynamicVirtualproxyTest001 {


  public static class MyException extends Exception {

  }

  public interface Service {
    String doWork() throws MyException;
  }

  public static class MyService implements Service {
    @Override
    public String doWork() throws MyException {

      System.out.println("MyService = " + true);

      throw new MyException();
    }
  }

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

}
