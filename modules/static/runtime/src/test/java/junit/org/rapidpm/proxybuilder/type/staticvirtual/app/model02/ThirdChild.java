package junit.org.rapidpm.proxybuilder.type.staticvirtual.app.model02;

import java.time.LocalDateTime;

/**
 * Created by svenruppert on 23.10.15.
 */
public class ThirdChild {

  public ThirdChild() {
    System.out.println( this.getClass().getSimpleName() + " = " + LocalDateTime.now());
  }

  public String doWork(String txt){
    return " 3 - " + txt;
  }

  public String getValue(){
    return "value";
  }

}
