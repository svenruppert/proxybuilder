package junit.org.rapidpm.proxybuilder.type.staticvirtual.app.model01;


import java.time.LocalDateTime;

/**
 * Created by svenruppert on 23.10.15.
 */
public class ServiceImpl implements Service {

  public ServiceImpl() {
    System.out.println("ServiceImpl created = " + LocalDateTime.now());
  }

  @Override
  public String doWork(final String txt) {
    return "ServiceImpl - " + txt;
  }
}
