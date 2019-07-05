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
package junit.org.rapidpm.proxybuilder.type;

import org.junit.Assert;
import org.junit.Test;
import org.rapidpm.proxybuilder.proxy.dymamic.DynamicProxyBuilder;
import org.rapidpm.proxybuilder.proxy.dymamic.virtual.CreationStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddPreActionTest {


  @Test
  public void test001() throws Exception {
    final DemoService build = DynamicProxyBuilder
        .createBuilder(DemoService.class, DemoServiceImplementation.class, CreationStrategy.NONE)
        .addIPreAction((original, method, args) -> {
          System.out.println("original = " + original);
        })
        .build();
    Assert.assertEquals("hhimpl", build.doWork("hh"));
  }

  @Test
  public void test002() throws Exception {
    final List<Boolean> done = new ArrayList<>();

    final Map build = DynamicProxyBuilder
        .createBuilder(Map.class, HashMap.class, CreationStrategy.NONE)
        .addIPreAction((original, method, args) -> {
          System.out.println("original = " + original);
          done.add(true);
        })
        .build();
    Assert.assertTrue(done.isEmpty());
    final int size = build.size();
    Assert.assertFalse(done.isEmpty());
  }

  public interface DemoService {
    String doWork(String txt);
  }

  public static class DemoServiceImplementation implements DemoService {
    @Override
    public String doWork(final String txt) {
      return txt + "impl";
    }
  }


}
