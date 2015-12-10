package junit.org.rapidpm.proxybuilder.staticgenerated.processors;

import org.rapidpm.proxybuilder.staticgenerated.annotations.StaticMetricsProxy;

import java.time.LocalDateTime;

/**
 * Created by svenruppert on 09.12.15.
 */
@StaticMetricsProxy
public class ServiceImpl {

  public String doWork(String txt){
    return txt + LocalDateTime.now();
  }


}
