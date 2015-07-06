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

package org.rapidpm.proxybuilder.type.virtual;

import java.io.PrintWriter;

/**
 * Created by Sven Ruppert on 19.02.14.
 */
public class VirtualProxySourceGeneratorOnExistingObject extends VirtualProxySourceGenerator {

  public VirtualProxySourceGeneratorOnExistingObject(Class subject, Class realSubject) {
    super(subject, realSubject, Concurrency.OnExistingObject);
  }

  protected void addRealSubjectCreation(PrintWriter out, String name, String realName) {
    out.printf(" public %s realSubject;%n", name);
    out.println();
    out.printf(" private %s realSubject() {%n", name);
    out.println(" return realSubject;");
    out.println(" }");
  }
}
