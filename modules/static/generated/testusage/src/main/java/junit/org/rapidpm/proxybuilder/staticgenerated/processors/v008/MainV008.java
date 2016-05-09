package junit.org.rapidpm.proxybuilder.staticgenerated.processors.v008;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

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
 * Created by RapidPM - Team on 09.05.16.
 */
public class MainV008 {


  public static void main(String[] args) {

    final MyLoggingInterface demo
        = new MyLoggingInterfaceStaticLoggingProxy()
        .withDelegator(new LoggerExample());


    final List<Integer> ints = Arrays.asList(1,2,3,4);

    final List<Integer> list = demo.unwrapList(ints, "AEAEA");
    System.out.println("list = " + list);


  }

  public static class LoggerExample implements MyLoggingInterface {


    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
      return null;
    }

    @Override
    public <T extends List> T unwrapList(final T type) {
      return null;
    }

    @Override
    public <T extends List> T unwrapList(final T type, final String str) {
      return null;
    }
  }

}
