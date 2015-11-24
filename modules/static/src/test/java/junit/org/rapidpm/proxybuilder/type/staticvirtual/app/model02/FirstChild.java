package junit.org.rapidpm.proxybuilder.type.staticvirtual.app.model02;

import java.time.LocalDateTime;

/**
 * Created by svenruppert on 23.10.15.
 */
public class FirstChild {
  public FirstChild() {
    System.out.println( this.getClass().getSimpleName() + " = " + LocalDateTime.now());
  }

  public SecondChild secondChild;

  public SecondChild getSecondChild() {
    return secondChild;
  }


  public String doWork(String txt){
    return " 1 - " + secondChild.doWork(txt);
  }
}
