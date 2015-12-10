package junit.org.rapidpm.proxybuilder.type.staticvirtual.app.model02;

import java.time.LocalDateTime;

/**
 * Created by svenruppert on 23.10.15.
 */
public class Parent {


  public Parent() {
    System.out.println( this.getClass().getSimpleName() + " = " + LocalDateTime.now());
  }

  public FirstChild firstChild;

  public FirstChild getFirstChild() {
    return firstChild;
  }

  public String doWork(String txt){
    return " 0 - " + firstChild.doWork(txt);
  }

}
