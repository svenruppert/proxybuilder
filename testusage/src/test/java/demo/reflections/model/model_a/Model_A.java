/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the EUPL-1.2.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package demo.reflections.model.model_a;


import demo.reflections.Inject;
import demo.reflections.PostConstruct;

public class Model_A {


  public interface Service {
    String work(String txt);

    SubService getSubService();

    boolean isPostconstructed();
  }

  public static class BusinessModule {

    @Inject
    Service service;

    public String work(String txt) {
      return service.work(txt);
    }
  }

  public static class ServiceImplA implements Service {
    @Inject
    SubService subService;
    boolean postconstructed;

    public String work(String txt) {
      return subService.work(txt);
    }

    @PostConstruct
    public void postconstruct() {
      postconstructed = true;
    }    @Override
    public SubService getSubService() {
      return subService;
    }

    public boolean isPostconstructed() {
      return postconstructed;
    }


  }

  public static class ServiceImplB implements Service {
    @Inject
    SubService subService;
    boolean postconstructed;

    @PostConstruct
    public void postconstruct() {
      postconstructed = true;
    }    public String work(String txt) {
      return subService.work(txt);
    }

    @Override
    public SubService getSubService() {
      return subService;
    }

    public boolean isPostconstructed() {
      return postconstructed;
    }


  }


  public static class SubService {
    @Inject
    SubSubService subSubService;
    boolean postconstructed;

    public String work(final String txt) {
      return subSubService.work(txt);
    }

    public boolean isPostconstructed() {
      return postconstructed;
    }

    @PostConstruct
    public void postconstruct() {
      postconstructed = true;
    }
  }


  public static class SubSubService {
    public String work(final String txt) {
      return "SubSubService " + txt;
    }
  }


}