/*
 * Copyright [2014] [www.rapidpm.org / Sven Ruppert (sven.ruppert@rapidpm.org)]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package demo.reflections.model.model_a;

import demo.reflections.Inject;

import javax.annotation.PostConstruct;

/**
 * Created by Sven Ruppert on 07.12.2014.
 */
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
    boolean postconstructed = false;

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
    boolean postconstructed = false;

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
    boolean postconstructed = false;

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
