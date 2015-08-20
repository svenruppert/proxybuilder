package junit.org.rapidpm.proxybuilder;

import org.junit.Assert;
import org.junit.Test;
import org.rapidpm.proxybuilder.VirtualProxyBuilder;
import org.rapidpm.proxybuilder.type.virtual.Concurrency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by svenruppert on 20.08.15.
 */
public class AddPreActionTest {


  public interface DemoService {
    String doWork(String txt);
  }

  public static class DemoServiceImplementation implements DemoService {
    @Override
    public String doWork(final String txt) {
      return txt + "impl";
    }
  }

  @Test
  public void test001() throws Exception {
    final DemoService build = VirtualProxyBuilder
        .createBuilder(DemoService.class, DemoServiceImplementation.class, Concurrency.NONE)
        .addIPreAction((original, method, args) -> {
          System.out.println("original = " + original);
        })
        .build();
    Assert.assertEquals("hhimpl", build.doWork("hh"));
  }


  @Test
  public void test002() throws Exception {
    final List<Boolean> done = new ArrayList<>();

    final Map build = VirtualProxyBuilder
        .createBuilder(Map.class, HashMap.class, Concurrency.NONE)
        .addIPreAction((original, method, args) -> {
          System.out.println("original = " + original);
          done.add(true);
        })
        .build();
    Assert.assertTrue(done.isEmpty());
    final int size = build.size();
    Assert.assertFalse(done.isEmpty());
  }










}
