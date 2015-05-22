package org.rapidpm.demo.annotationprocessing;

/**
 * Created by sven on 13.05.15.
 */
public class ServiceImpl implements Service {
  @Override
  public String doWork_A(String txt) {
    return "doWorkd_A_Original";
  }

  @Override
  public String doWork_B(String txt) {
    return "doWorkd_B_Original";
  }
}
