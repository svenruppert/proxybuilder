/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the EUPL-1.2.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package com.svenruppert.proxybuilder.proxy.generated;


import com.squareup.javapoet.*;
import com.svenruppert.dependencies.core.logger.HasLogger;
import com.svenruppert.proxybuilder.proxy.generated.annotations.IsGeneratedProxy;
import com.svenruppert.proxybuilder.proxy.generated.annotations.IsLoggingProxy;
import com.svenruppert.proxybuilder.proxy.generated.annotations.StaticLoggingProxy;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

public class StaticLoggingProxyAnnotationProcessor
    extends BasicStaticProxyAnnotationProcessor<StaticLoggingProxy> {

  public static final String WITH_DELEGATOR = "withDelegator";

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

    typeSpecBuilderForTargetClass.addMethod(MethodSpec.methodBuilder(WITH_DELEGATOR)
                                                      .addModifiers(Modifier.PUBLIC)
                                                      .addParameter(targetTypeName, DELEGATOR_FIELD_NAME,
                                                                    Modifier.FINAL)
                                                      .addCode(CodeBlock.builder()
                                                                        .addStatement("this."
                                                                                      + DELEGATOR_FIELD_NAME
                                                                                      + " = "
                                                                                      + DELEGATOR_FIELD_NAME)
                                                                        .addStatement("return this")
                                                                        .build())
                                                      .returns(ClassName.get(pkgName(typeElement),
                                                                             targetClassNameSimpleForGeneratedClass(
                                                                                 typeElement)))
                                                      .build());

  }

  @Override
  protected CodeBlock defineMethodImplementation(final ExecutableElement methodElement,
                                                 final String methodName2Delegate,
                                                 final TypeElement typeElementTargetClass) {
    final TypeMirror        returnType       = methodElement.getReturnType();
    final CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
    final List<? extends VariableElement> methodElementParameters = methodElement.getParameters();

    codeBlockBuilder.beginControlFlow("if ($T.staticLogger().isInfoEnabled())", HasLogger.class)
                    .addStatement("$T.staticLogger().info(\""
                                  + DELEGATOR_FIELD_NAME
                                  + "."
                                  + delegatorMethodCall(methodElement, methodName2Delegate)
                                  + ((methodElementParameters.isEmpty())
                                     ? "\")"
                                     : " values - \" + " + joinString(methodElementParameters) + ")"), HasLogger.class)
                    .endControlFlow();

    if (returnType.getKind() == TypeKind.VOID) {
      codeBlockBuilder.addStatement(delegatorStatementWithOutReturn(methodElement, methodName2Delegate));

    } else {
      codeBlockBuilder.addStatement(delegatorStatementWithReturn(methodElement, methodName2Delegate));
    }
    return codeBlockBuilder.build();
  }

  private String joinString(List<? extends VariableElement> methodElementParameters) {
    Optional<String> reduce = methodElementParameters.stream()
                                                     .map(Object::toString)
                                                     .reduce((s1, s2) -> s1 + " + \" - \" + " + s2);
    return reduce.orElse("");

  }

}