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

package org.rapidpm.proxybuilder.objectadapter.processor;

import com.squareup.javapoet.*;
import com.squareup.javapoet.CodeBlock.Builder;
import org.rapidpm.proxybuilder.objectadapter.annotations.IsObjectAdapter;
import org.rapidpm.proxybuilder.objectadapter.annotations.staticobjectadapter.StaticObjectAdapter;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.Optional;

public class StaticObjectAdapterAnnotationProcessor extends BasicObjectAdapterAnnotationProcessor<StaticObjectAdapter> {


  @Override
  public Class<StaticObjectAdapter> responsibleFor() {
    return StaticObjectAdapter.class;
  }

  @Override
  protected void addClassLevelSpecs(final TypeElement typeElement, final RoundEnvironment roundEnv) {
    final TypeName interface2Implement = TypeName.get(typeElement.asType());
    typeSpecBuilderForTargetClass.addAnnotation(IsObjectAdapter.class);

    final FieldSpec delegatorFieldSpec = defineDelegatorField(typeElement);
    typeSpecBuilderForTargetClass.addField(delegatorFieldSpec);

    typeSpecBuilderForTargetClass
        .addMethod(MethodSpec.methodBuilder("with" + typeElement.getSimpleName())
            .addModifiers(Modifier.PUBLIC)
            .addParameter(interface2Implement, "delegator", Modifier.FINAL)
            .addCode(CodeBlock.builder()
                .addStatement("this." + "delegator" + "=" + "delegator")
                .addStatement("return this").build())
            .returns(ClassName.get(pkgName(typeElement), targetClassNameSimpleForGeneratedClass(typeElement)))
            .build());


  }

  @Override
  protected CodeBlock defineMethodImplementation(final ExecutableElement methodElement, final String methodName2Delegate) {
    final TypeElement typeElement = (TypeElement) methodElement.getEnclosingElement();
    final Builder codeBlockBuilder = CodeBlock.builder();

    final Optional<TypeSpec> functionalInterfaceSpec = writeFunctionalInterface(methodElement);
    functionalInterfaceSpec.ifPresent(f -> {
      final String adapterAttributeName = f.name.substring(0, 1).toLowerCase() + f.name.substring(1);
      final ClassName className = ClassName.get(pkgName(typeElement), f.name);
      final FieldSpec adapterAttributFieldSpec = FieldSpec.builder(className, adapterAttributeName, Modifier.PRIVATE).build();
      typeSpecBuilderForTargetClass.addField(adapterAttributFieldSpec);

      codeBlockBuilder
          .beginControlFlow("if(" + adapterAttributeName + " != null)")
          .addStatement("return " + adapterAttributeName + "." + delegatorMethodCall(methodElement, methodName2Delegate))
          .endControlFlow();

      //add method to set adapter
      typeSpecBuilderForTargetClass
          .addMethod(MethodSpec.methodBuilder("with" + className.simpleName())
              .addModifiers(Modifier.PUBLIC)
              .addParameter(className, adapterAttributeName, Modifier.FINAL)
              .addCode(CodeBlock.builder()
                  .addStatement("this." + adapterAttributeName + "=" + adapterAttributeName)
                  .addStatement("return this").build())
              .returns(ClassName.get(pkgName(typeElement), targetClassNameSimpleForGeneratedClass(typeElement)))
              .build());
    });

    final String delegateStatement = delegatorStatementWithReturn(methodElement, methodName2Delegate);

    return codeBlockBuilder
        .addStatement(delegateStatement)
        .build();
  }

  @Override
  protected void addStaticImports(final JavaFile.Builder builder) {

  }

}
