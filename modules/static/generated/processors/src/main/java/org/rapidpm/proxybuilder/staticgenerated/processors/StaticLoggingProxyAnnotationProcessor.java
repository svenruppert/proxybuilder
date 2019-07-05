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
import org.rapidpm.proxybuilder.staticgenerated.annotations.IsGeneratedProxy;
import org.rapidpm.proxybuilder.staticgenerated.annotations.IsLoggingProxy;
import org.rapidpm.proxybuilder.staticgenerated.annotations.StaticLoggingProxy;
import org.slf4j.Logger;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

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
 * <p>
 * Created by RapidPM - Team on 09.05.16.
 */
public class StaticLoggingProxyAnnotationProcessor extends BasicStaticProxyAnnotationProcessor<StaticLoggingProxy> {

  public static final String WITH_DELEGATOR = "withDelegator";
  public static final String LOGGER = "LOGGER";

  @Override
  public Class<StaticLoggingProxy> responsibleFor() {
    return StaticLoggingProxy.class;
  }

  @Override
  protected void addClassLevelSpecs(final TypeElement typeElement, final RoundEnvironment roundEnv) {
    final TypeName targetTypeName = TypeName.get(typeElement.asType());
    typeSpecBuilderForTargetClass.addAnnotation(IsGeneratedProxy.class);
    typeSpecBuilderForTargetClass.addAnnotation(IsLoggingProxy.class);

    typeSpecBuilderForTargetClass.addField(defineDelegatorField(typeElement));
    typeSpecBuilderForTargetClass.addField(defineLoggerField(typeElement));

    typeSpecBuilderForTargetClass
            .addMethod(MethodSpec.methodBuilder(WITH_DELEGATOR)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(targetTypeName, DELEGATOR_FIELD_NAME, Modifier.FINAL)
                    .addCode(CodeBlock.builder()
                            .addStatement("this." + DELEGATOR_FIELD_NAME + " = " + DELEGATOR_FIELD_NAME)
                            .addStatement("return this").build())
                    .returns(ClassName.get(pkgName(typeElement), targetClassNameSimpleForGeneratedClass(typeElement)))
                    .build());

  }

  protected FieldSpec defineLoggerField(final TypeElement typeElement) {
    final ClassName loggingClassName = ClassName.get(pkgName(typeElement), className(typeElement));
    final ClassName loggerClassName = ClassName.get(Logger.class);
    return FieldSpec
            .builder(loggerClassName, LOGGER)
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            .initializer("getLogger(" + loggingClassName.simpleName() + ".class)")
            .build();
  }

  @Override
  protected CodeBlock defineMethodImplementation(final ExecutableElement methodElement, final String methodName2Delegate, final TypeElement typeElementTargetClass) {
    final TypeMirror returnType = methodElement.getReturnType();
    final CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();

    final List<? extends VariableElement> methodElementParameters = methodElement.getParameters();

    codeBlockBuilder
            .beginControlFlow("if (" + LOGGER + ".isInfoEnabled())")
            .addStatement(LOGGER + ".info(\""
                    + DELEGATOR_FIELD_NAME + "." + delegatorMethodCall(methodElement, methodName2Delegate)
                    + ((methodElementParameters.isEmpty()) ? "\")" :
                    " values - \" + "
                            + joinString(methodElementParameters)
                            + ")"))
            .endControlFlow();

    if (returnType.getKind() == TypeKind.VOID) {
      codeBlockBuilder
              .addStatement(delegatorStatementWithOutReturn(methodElement, methodName2Delegate));

    } else {
      codeBlockBuilder
              .addStatement(delegatorStatementWithReturn(methodElement, methodName2Delegate));
    }
    return codeBlockBuilder.build();
  }

  private String joinString(List<? extends VariableElement> methodElementParameters) {
    Optional<String> reduce = methodElementParameters.stream()
            .map(Object::toString)
            .reduce((s1, s2) -> s1 + " + \" - \" + " + s2);
    return reduce.orElse("");

  }

  @Override
  protected void addStaticImports(final JavaFile.Builder builder) {
    builder.addStaticImport(org.slf4j.LoggerFactory.class, "*");
  }

}
