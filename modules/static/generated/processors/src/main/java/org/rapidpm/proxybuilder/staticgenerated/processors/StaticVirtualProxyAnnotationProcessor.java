/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.rapidpm.proxybuilder.staticgenerated.processors;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import org.rapidpm.proxybuilder.staticgenerated.annotations.IsGeneratedProxy;
import org.rapidpm.proxybuilder.staticgenerated.annotations.IsVirtualProxy;
import org.rapidpm.proxybuilder.staticgenerated.annotations.StaticVirtualProxy;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

public class StaticVirtualProxyAnnotationProcessor extends BasicStaticProxyAnnotationProcessor<StaticVirtualProxy> {
  @Override
  protected void addClassLevelSpecs(final TypeElement typeElement, final RoundEnvironment roundEnv) {
    typeSpecBuilderForTargetClass.addAnnotation(IsGeneratedProxy.class);
    typeSpecBuilderForTargetClass.addAnnotation(IsVirtualProxy.class);

    final FieldSpec delegatorFieldSpec = defineDelegatorField(typeElement);
    typeSpecBuilderForTargetClass.addField(delegatorFieldSpec);
  }

  @Override
  protected CodeBlock defineMethodImplementation(final ExecutableElement methodElement, final String methodName2Delegate) {
    return null;
  }

  @Override
  public Class<StaticVirtualProxy> responsibleFor() {
    return StaticVirtualProxy.class;
  }
}
