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
import com.squareup.javapoet.CodeBlock.Builder;
import com.svenruppert.proxybuilder.proxy.dymamic.virtual.factory.ServiceFactory;
import com.svenruppert.proxybuilder.proxy.dymamic.virtual.strategy.ServiceStrategyFactory;
import com.svenruppert.proxybuilder.proxy.generated.annotations.IsGeneratedProxy;
import com.svenruppert.proxybuilder.proxy.generated.annotations.IsVirtualProxy;
import com.svenruppert.proxybuilder.proxy.generated.annotations.StaticVirtualProxy;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public class StaticVirtualProxyAnnotationProcessor extends BasicStaticProxyAnnotationProcessor<StaticVirtualProxy> {

  public static final String INSTANCE_STRATEGYFACTORY_FIELD_NAME = "creationStrategyFactory";
  public static final String INSTANCE_FACTORY_FIELD_NAME = "serviceFactory";

  @Override
  public Class<StaticVirtualProxy> responsibleFor() {
    return StaticVirtualProxy.class;
  }

  @Override
  protected void addClassLevelSpecs(final TypeElement typeElement, final RoundEnvironment roundEnv) {
    typeSpecBuilderForTargetClass.addAnnotation(IsGeneratedProxy.class);
    typeSpecBuilderForTargetClass.addAnnotation(IsVirtualProxy.class);

    // add InstanceStrategyFactory field
    final ClassName instanceStrategyFactoryClassName = ClassName.get(ServiceStrategyFactory.class);
    final TypeName typedInstanceStrategyFactoryClassName = ParameterizedTypeName.get(instanceStrategyFactoryClassName, TypeName.get(typeElement.asType()));

    final FieldSpec instanceStrategyFactoryFieldSpec = FieldSpec
        .builder(typedInstanceStrategyFactoryClassName, INSTANCE_STRATEGYFACTORY_FIELD_NAME)
        .addModifiers(Modifier.PRIVATE)
        .build();
    typeSpecBuilderForTargetClass.addField(instanceStrategyFactoryFieldSpec);

    // add InstanceFactory field
    final ClassName instanceFactoryClassName = ClassName.get(ServiceFactory.class);
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

  private String createMethodCall(final ExecutableElement methodElement, final String methodName2Delegate) {
    return INSTANCE_STRATEGYFACTORY_FIELD_NAME + ".realSubject(" + INSTANCE_FACTORY_FIELD_NAME + ")." + delegatorMethodCall(methodElement, methodName2Delegate);
  }


}