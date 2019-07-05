/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package junit.org.rapidpm.proxybuilder.type.staticvirtual.app.model02;


import org.rapidpm.proxybuilder.type.staticruntime.virtual.CreationStrategy;
import org.rapidpm.proxybuilder.type.staticruntime.virtual.StaticProxyGenerator;

public class Main002 {
  public static void main(String[] args) {

    final Parent parent = new Parent();
    final FirstChild firstChild = new FirstChild();
    final SecondChild secondChild = new SecondChild();
    final ThirdChild thirdChild = new ThirdChild();

    parent.firstChild = firstChild;
    firstChild.secondChild = secondChild;
    secondChild.thirdChild = thirdChild;

    final String hello = parent.doWork("Hello");
    System.out.println("hello = " + hello);
    System.out.println("value = " + parent.firstChild.secondChild.thirdChild.getValue());

    final Parent orig = new Parent();

    // NPE
    // orig.getFirstChild().getSecondChild().getThirdChild().doWork("ups NPE");

    // ugly code
    if (orig.getFirstChild() != null)
      if(orig.getFirstChild().getSecondChild() != null)
        if(orig.getFirstChild().getSecondChild().getThirdChild() != null)
          orig.getFirstChild().getSecondChild().getThirdChild().doWork("ups NPE");

    final Parent proxy = proxy(orig);
    final ThirdChild child = proxy.getFirstChild().getSecondChild().getThirdChild();
    if (child != null) {
      final String value = child.getValue();
      System.out.println("value = " + value);

      //extend the proxy ;-)
      //final String proxy1 = child.doWork("Proxy");
      //System.out.println("proxy1 = " + proxy1);
    }

  }


  private static <T> T proxy( T orig) {
    final Class<T> aClass = (Class<T>) orig.getClass();
    final T demo = StaticProxyGenerator.make(aClass, aClass, CreationStrategy.OnExistingObject);

    final Class<?> aClassProxy = demo.getClass();
    try {
      aClassProxy.getDeclaredField("realSubject").set(demo, orig);
    } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
      e.printStackTrace();
    }
    return demo;
  }


}
