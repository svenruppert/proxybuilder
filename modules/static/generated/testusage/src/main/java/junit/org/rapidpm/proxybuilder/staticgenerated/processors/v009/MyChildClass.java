package junit.org.rapidpm.proxybuilder.staticgenerated.processors.v009;

import org.rapidpm.proxybuilder.staticgenerated.annotations.StaticLoggingProxy;
import org.rapidpm.proxybuilder.staticgenerated.annotations.StaticMetricsProxy;

@StaticMetricsProxy
@StaticLoggingProxy
public class MyChildClass extends MyClass {

  public MyChildClass(String name) {
    super(name);
  }

  protected MyChildClass(final int i) {
    super(i);
  }
}
