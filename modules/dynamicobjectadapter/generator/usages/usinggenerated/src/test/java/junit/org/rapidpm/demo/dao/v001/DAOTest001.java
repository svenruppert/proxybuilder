package junit.org.rapidpm.demo.dao.v001;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by svenruppert on 07.08.15.
 */
public class DAOTest001 {

  @Test
  public void test001() throws Exception {
    final Service target = ServiceAdapterBuilder.newBuilder()
        .setOriginal(null)
        .withDoWorkA(() -> "mocked-A")
        .buildForTarget(Service.class);

    Assert.assertNotNull(target);

    final String hello = target.doWorkA();
    Assert.assertNotNull(hello);
    Assert.assertFalse(hello.isEmpty());
    Assert.assertEquals("mocked-A", hello);
  }

  @Test
  public void test002() throws Exception {
    final Service target = ServiceAdapterBuilder.newBuilder()
        .setOriginal(null)
        .withDoWorkB(() -> "mocked-B")
        .buildForTarget(Service.class);

    Assert.assertNotNull(target);

    final String hello = target.doWorkB();
    Assert.assertNotNull(hello);
    Assert.assertFalse(hello.isEmpty());
    Assert.assertEquals("mocked-B", hello);
  }

  @Test(expected = NullPointerException.class)
  public void test003() throws Exception {
    final Service target = ServiceAdapterBuilder.newBuilder()
        .setOriginal(null)
        .withDoWorkA(() -> "mocked-A")
        .buildForTarget(Service.class);

    Assert.assertNotNull(target);
    final String hello = target.doWorkB();
  }


}
