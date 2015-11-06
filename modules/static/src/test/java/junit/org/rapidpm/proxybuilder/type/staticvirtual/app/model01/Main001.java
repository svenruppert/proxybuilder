package junit.org.rapidpm.proxybuilder.type.staticvirtual.app.model01;


import org.rapidpm.proxybuilder.type.staticvirtual.CreationStrategy;
import org.rapidpm.proxybuilder.type.staticvirtual.ProxyGenerator;

/**
 * Created by svenruppert on 23.10.15.
 */
public class Main001 {

  public static void main(String[] args) {

    final Service proxy01 = ProxyGenerator.make(Service.class, ServiceImpl.class, CreationStrategy.NO_DUPLICATES);
    final String result01 = proxy01.doWork("proxy01");

    final Service proxy02 = ProxyGenerator.make(Service.class, ServiceImpl.class, CreationStrategy.SOME_DUPLICATES);
    final String result02 = proxy01.doWork("proxy02");

  }
}
