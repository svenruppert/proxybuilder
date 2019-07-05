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
package org.rapidpm.proxybuilder.staticgenerated.processors;

import com.squareup.javapoet.*;
import com.squareup.javapoet.CodeBlock.Builder;
import org.rapidpm.proxybuilder.staticgenerated.annotations.IsGeneratedProxy;
import org.rapidpm.proxybuilder.staticgenerated.annotations.IsVirtualProxy;
import org.rapidpm.proxybuilder.staticgenerated.annotations.StaticVirtualProxy;
import org.rapidpm.proxybuilder.staticgenerated.proxy.virtual.InstanceFactory;
import org.rapidpm.proxybuilder.staticgenerated.proxy.virtual.InstanceStrategyFactory;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public class StaticVirtualProxyAnnotationProcessor extends BasicStaticProxyAnnotationProcessor<StaticVirtualProxy> {

  public static final String INSTANCE_STRATEGYFACTORY_FIELD_NAME = "instanceStrategyFactory";
  public static final String INSTANCE_FACTORY_FIELD_NAME = "instanceFactory";

  @Override
  public Class<StaticVirtualProxy> responsibleFor() {
    return StaticVirtualProxy.class;
  }

  @Override
  protected void addClassLevelSpecs(final TypeElement typeElement, final RoundEnvironment roundEnv) {
    typeSpecBuilderForTargetClass.addAnnotation(IsGeneratedProxy.class);
    typeSpecBuilderForTargetClass.addAnnotation(IsVirtualProxy.class);

    // add InstanceStrategyFactory field
    final ClassName instanceStrategyFactoryClassName = ClassName.get(InstanceStrategyFactory.class);
    final TypeName typedInstanceStrategyFactoryClassName = ParameterizedTypeName.get(instanceStrategyFactoryClassName, TypeName.get(typeElement.asType()));

    final FieldSpec instanceStrategyFactoryFieldSpec = FieldSpec
        .builder(typedInstanceStrategyFactoryClassName, INSTANCE_STRATEGYFACTORY_FIELD_NAME)
        .addModifiers(Modifier.PRIVATE)
        .build();
    typeSpecBuilderForTargetClass.addField(instanceStrategyFactoryFieldSpec);

    // add InstanceFactory field
    final ClassName instanceFactoryClassName = ClassName.get(InstanceFactory.class);
    final TypeName typedInstanceFactoryClassName = ParameterizedTypeName.get(instanceFactoryClassName, TypeName.get(typeElement.asType()));

    final FieldSpec instanceFactoryFieldSpec = FieldSpec
        .builder(typedInstanceFactoryClassName, INSTANCE_FACTORY_FIELD_NAME)
        .addModifiers(Modifier.PRIVATE)
        .build();
    typeSpecBuilderForTargetClass.addField(instanceFactoryFieldSpec);
  }

  @Override
  protected CodeBlock defineMethodImplementation(final ExecutableElement methodElement, final String methodName2Delegate, final TypeElement typeElementTargetClass) {
    final TypeMirror returnType = methodElement.getReturnType();
    final Builder codeBlockBuilder = CodeBlock.builder();
    if (returnType.getKind() == TypeKind.VOID) {

      //InstanceStrategyFactory.realSubject(DI.activate(ClassImplName.class));
      //InstanceStrategyFactory.realSubject(InstanceFactory.createInstance());

      codeBlockBuilder
          .addStatement(createMethodCall(methodElement, methodName2Delegate));
    } else {
      codeBlockBuilder
          .addStatement("$T result = " + createMethodCall(methodElement, methodName2Delegate), returnType)
          .addStatement("return result");
    }
    return codeBlockBuilder.build();
  }

  @Override
  protected void addStaticImports(final JavaFile.Builder builder) {

  }

  private String createMethodCall(final ExecutableElement methodElement, final String methodName2Delegate) {
    return INSTANCE_STRATEGYFACTORY_FIELD_NAME + ".realSubject(" + INSTANCE_FACTORY_FIELD_NAME + ")." + delegatorMethodCall(methodElement, methodName2Delegate);
  }


}
