package org.rapidpm.demo.annotationprocessing;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * Created by sven on 18.05.15.
 */
public class Main {


  public static void main(String[] args) {

    Service service = ServiceAdapterBuilder.newBuilder()
        .setOriginal(new ServiceImpl())
        .withDoWork_A((txt) -> txt + "_part")
//        .withTarget(Service.class) //leider als letztes.....
        .buildForTarget(Service.class);

    System.out.println(service.doWork_A("Hallo Adapter"));


    final boolean proxyClass = Proxy.isProxyClass(service.getClass());
    System.out.println("proxyClass = " + proxyClass);

    //Interface auf den InvocactionHandler
    final InvocationHandler invocationHandler = Proxy.getInvocationHandler(service);
    final ServiceInvocationHandler serviceInvocationHandler = (ServiceInvocationHandler) invocationHandler;

    serviceInvocationHandler.doWork_A((txt) -> txt + "_part_modified");
    System.out.println(service.doWork_A("Hallo Adapter"));




    final Service serviceX = ServiceAdapterBuilder
        .newBuilder()
        .setOriginal(new ServiceImpl())
        .withDoWork_A(txt -> "DOA-Builder Method A " + txt)
        .buildForTarget(Service.class);

    System.out.println(serviceX.doWork_A("XX"));

  }
}
