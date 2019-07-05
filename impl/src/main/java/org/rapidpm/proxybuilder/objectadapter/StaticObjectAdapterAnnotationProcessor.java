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
package org.rapidpm.proxybuilder.objectadapter;

import com.squareup.javapoet.*;
import com.squareup.javapoet.CodeBlock.Builder;
import org.rapidpm.proxybuilder.objectadapter.generated.StaticObjectAdapter;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
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
  protected CodeBlock defineMethodImplementation(final ExecutableElement methodElement,
                                                 final String methodName2Delegate,
                                                 final TypeElement typeElementTargetClass) {
//    final TypeElement typeElement = (TypeElement) methodElement.getEnclosingElement();
    final Builder codeBlockBuilder = CodeBlock.builder();

    final Optional<TypeSpec> functionalInterfaceSpec = writeFunctionalInterface(typeElementTargetClass, methodElement);

    functionalInterfaceSpec
        .ifPresent(f -> {
          final String adapterAttributeName = f.name.substring(0, 1).toLowerCase() + f.name.substring(1);
          final ClassName className = ClassName.get(pkgName(typeElementTargetClass), f.name);
          final FieldSpec adapterAttributFieldSpec = FieldSpec.builder(className, adapterAttributeName, Modifier.PRIVATE).build();
          typeSpecBuilderForTargetClass.addField(adapterAttributFieldSpec);


          final boolean isVoid = methodElement.getReturnType().getKind() == TypeKind.VOID;
          final String statement = ((isVoid) ? "" : "return ") + adapterAttributeName + "." + delegatorMethodCall(methodElement, methodName2Delegate);

          codeBlockBuilder
              .beginControlFlow("if(" + adapterAttributeName + " != null)")
              .addStatement(statement)
              .endControlFlow();

          //add method to set adapter
          typeSpecBuilderForTargetClass
              .addMethod(MethodSpec.methodBuilder("with" + className.simpleName())
                  .addModifiers(Modifier.PUBLIC)
                  .addParameter(className, adapterAttributeName, Modifier.FINAL)
                  .addCode(CodeBlock.builder()
                      .addStatement("this." + adapterAttributeName + " = " + adapterAttributeName)
                      .addStatement("return this").build())
                  .returns(ClassName.get(pkgName(actualProcessedTypeElement), targetClassNameSimpleForGeneratedClass(actualProcessedTypeElement)))
//                  .returns(ClassName.get(pkgName(actualProcessedTypeElement), targetClassNameSimpleForGeneratedClass(typeElementTargetClass)))
                  .build());
        });

    final String delegateStatement;

    if (methodElement.getReturnType().getKind() == TypeKind.VOID) {
      delegateStatement = delegatorStatementWithOutReturn(methodElement, methodName2Delegate);
    } else {
      delegateStatement = delegatorStatementWithReturn(methodElement, methodName2Delegate);
    }


    return codeBlockBuilder
        .addStatement(delegateStatement)
        .build();
  }

  @Override
  protected void addStaticImports(final JavaFile.Builder builder) {

  }

}
