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
/*
  Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package junit.org.rapidpm.proxybuilder.type.dynamic;

import org.junit.Test;
import org.rapidpm.proxybuilder.proxy.dymamic.DynamicProxyBuilder;
import org.rapidpm.proxybuilder.proxy.dymamic.virtual.CreationStrategy;

/**
 * Copyright (C) 2010 RapidPM
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by RapidPM - Team on 11.05.16.
 */
public class DynamicLoggingProxyTest {


  @Test
  public void test001() throws Exception {
    final InnerDemoInterface proxy = DynamicProxyBuilder
        .createBuilder(
            InnerDemoInterface.class,
            InnerDemoClass.class,
            CreationStrategy.NONE)
        .addLogging()
        .build();
    System.out.println("System.nanoTime() = " + System.nanoTime());
    final String s = proxy.doWork();
    System.out.println("System.nanoTime() = " + System.nanoTime());
  }

  @Test
  public void test002() throws Exception {
    final DemoInterface proxy = DynamicProxyBuilder
        .createBuilder(
            DemoInterface.class,
            Demo.class,
            CreationStrategy.NONE)
        .addLogging()
        .build();
    System.out.println("System.nanoTime() = " + System.nanoTime());
    final String s = proxy.doWork("cc");
    System.out.println("System.nanoTime() = " + System.nanoTime());
  }


  public interface DemoInterface {
    String doWork(String txt);
  }

  public static class Demo implements DemoInterface {
    public String doWork(String txt) {
      return "XX" + txt;
    }
  }


}
