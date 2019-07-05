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
package org.rapidpm.proxybuilder.staticgenerated.proxy.virtual.strategy;


import org.rapidpm.proxybuilder.staticgenerated.proxy.virtual.InstanceFactory;
import org.rapidpm.proxybuilder.staticgenerated.proxy.virtual.InstanceStrategyFactory;

public class NotThreadSafeStrategyFactory<T> implements InstanceStrategyFactory<T> {

  private T delegator;

  @Override
  public T realSubject(final InstanceFactory<T> instanceFactory) {
    if (delegator == null) {
      delegator = instanceFactory.createInstance();
    }
    return delegator;
  }
}
