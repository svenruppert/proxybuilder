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
package junit.org.rapidpm.proxybuilder.staticgenerated.processors.v008.kotlin

import org.rapidpm.proxybuilder.proxy.generated.annotations.StaticLoggingProxy
import org.rapidpm.proxybuilder.proxy.generated.annotations.StaticMetricsProxy

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

@StaticLoggingProxy
@StaticMetricsProxy
interface MyLoggingInterface {

  @Throws(java.sql.SQLException::class)
  fun <T> unwrap(iface: Class<T>): T

  fun <T : List<*>> unwrapList(type: T): T
  fun <T : List<*>> unwrapList(type: T, str: String): T


  //  <X extends List>  void unwrapVoid(java.lang.Class<X> iface) throws java.sql.SQLException;


}
