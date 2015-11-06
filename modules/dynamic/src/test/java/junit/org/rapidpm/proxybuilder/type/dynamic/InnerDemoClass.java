package junit.org.rapidpm.proxybuilder.type.dynamic;

/**
 * Created by sven on 28.04.15.
 */
public class InnerDemoClass implements InnerDemoInterface {

  public InnerDemoClass() {
    System.out.println("InnerDemoClass = init");
  }

  @Override
  public String doWork() {
    return "InnerDemoClass.doWork()";
  }
}
