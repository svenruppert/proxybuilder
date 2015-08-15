package junit.org.rapidpm.demo.annotationprocessing;

import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import static org.junit.Assert.*;

/**
 * Created by svenruppert on 06.07.15.
 */
public class MainTest {


  @Test
  public void testMain001() throws Exception {
    Service service = ServiceAdapterBuilder.newBuilder()
        .setOriginal(new ServiceImpl())
        .withDoWorkA((txt) -> txt + "_part")
//        .withTarget(Service.class) //leider als letztes.....
        .buildForTarget(Service.class);
    assertEquals("Hallo Adapter_part", service.doWorkA("Hallo Adapter"));

    final boolean proxyClass = Proxy.isProxyClass(service.getClass());
    assertTrue(proxyClass);

    //Interface auf den InvocactionHandler
    final InvocationHandler invocationHandler = Proxy.getInvocationHandler(service);
    final ServiceInvocationHandler serviceInvocationHandler = (ServiceInvocationHandler) invocationHandler;

    serviceInvocationHandler.doWorkA((txt) -> txt + "_part_modified");
    assertEquals("Hallo Adapter_part_modified", service.doWorkA("Hallo Adapter"));

    final Service serviceX = ServiceAdapterBuilder
        .newBuilder()
        .setOriginal(new ServiceImpl())
        .withDoWorkA(txt -> "DOA-Builder Method A " + txt)
        .buildForTarget(Service.class);

    assertEquals("DOA-Builder Method A XX",serviceX.doWorkA("XX"));


  }
}