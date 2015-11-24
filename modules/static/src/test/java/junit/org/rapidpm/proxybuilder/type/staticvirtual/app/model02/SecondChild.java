package junit.org.rapidpm.proxybuilder.type.staticvirtual.app.model02;

import java.time.LocalDateTime;

/**
 * Created by svenruppert on 23.10.15.
 */
public class SecondChild {
  public SecondChild() {
    System.out.println( this.getClass().getSimpleName() + " = " + LocalDateTime.now());
  }

  public ThirdChild thirdChild;

  public ThirdChild getThirdChild() {
    return thirdChild;
  }

  public String doWork(String txt){
    return " 2 - " + thirdChild.doWork(txt);
  }




}
