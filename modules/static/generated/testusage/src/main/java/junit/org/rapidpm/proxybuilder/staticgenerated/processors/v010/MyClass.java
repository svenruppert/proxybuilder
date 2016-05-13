package junit.org.rapidpm.proxybuilder.staticgenerated.processors.v010;

import org.rapidpm.proxybuilder.staticgenerated.annotations.StaticLoggingProxy;

@StaticLoggingProxy
public interface MyClass {

  default void doSomething() {
    System.out.println("I did something");
  }

}
