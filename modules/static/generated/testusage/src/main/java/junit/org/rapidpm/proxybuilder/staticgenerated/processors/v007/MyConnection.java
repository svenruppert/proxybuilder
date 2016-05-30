package junit.org.rapidpm.proxybuilder.staticgenerated.processors.v007;

import org.rapidpm.proxybuilder.staticgenerated.annotations.StaticMetricsProxy;

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
 * Created by RapidPM - Team on 04.05.16.
 */


@StaticMetricsProxy
public interface MyConnection {

  <T> T unwrap(java.lang.Class<T> iface) throws java.sql.SQLException;

  <T extends List> T unwrapList(T type);


//  default String getDatabaseProductName() {
//    try {
//      return getMetaData().getDatabaseProductName() == null ? "" : getMetaData().getDatabaseProductName().toLowerCase();
//    } catch (SQLException e) {
//      e.printStackTrace();
//      return "";
//    }
//  }


}
