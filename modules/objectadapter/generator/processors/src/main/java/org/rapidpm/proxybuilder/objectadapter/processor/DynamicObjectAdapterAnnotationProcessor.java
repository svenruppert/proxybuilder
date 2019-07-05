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
package org.rapidpm.proxybuilder.objectadapter.processor;

import com.squareup.javapoet.*;
import com.squareup.javapoet.TypeSpec.Builder;
import org.rapidpm.proxybuilder.objectadapter.annotations.dynamicobjectadapter.AdapterBuilder;
import org.rapidpm.proxybuilder.objectadapter.annotations.dynamicobjectadapter.DynamicObjectAdapterBuilder;
import org.rapidpm.proxybuilder.objectadapter.annotations.dynamicobjectadapter.ExtendedInvocationHandler;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

//TODO refactoring
public class DynamicObjectAdapterAnnotationProcessor extends BasicObjectAdapterAnnotationProcessor<DynamicObjectAdapterBuilder> {

  public static final String INVOCATION_HANDLER_CLASSNAME_POST_FIX = "InvocationHandler";
  private static final String BUILDER_CLASSNAME_POST_FIX = "AdapterBuilder";


  private static class HolderStep001 {
    private final TypeElement typeElement;
    private final Builder invocationHandlerTypeSpecBuilder;

    public HolderStep001(final TypeElement typeElement, final Builder invocationHandlerTypeSpecBuilder) {
      this.typeElement = typeElement;
      this.invocationHandlerTypeSpecBuilder = invocationHandlerTypeSpecBuilder;
    }
  }

  private static class HolderStep002 {
    private final TypeElement typeElement;
    private final Builder invocationHandlerTypeSpecBuilder;
    private final Builder adapterBuilderTypeSpecBuilder;
    private final ClassName adapterBuilderClassname;

    public HolderStep002(final HolderStep001 step001,
                         final Builder adapterBuilderTypeSpecBuilder,
                         final ClassName adapterBuilderClassname) {
      this.typeElement = step001.typeElement;
      this.invocationHandlerTypeSpecBuilder = step001.invocationHandlerTypeSpecBuilder;

      this.adapterBuilderTypeSpecBuilder = adapterBuilderTypeSpecBuilder;
      this.adapterBuilderClassname = adapterBuilderClassname;
    }

    public String pkgName() {
      return typeElement.getEnclosingElement().toString();
    }
  }


  /**
   * for all elements marked with responsibleFor()
   * (01) check if interface
   * (02) start defining Builder
   * (03) for every method
   * (03a) - create FunctionalInterface and write it
   * (03b) - add method to Builder
   * (04) finish defining Builder
   *
   * @param annotations some comment
   * @param roundEnv some comment
   *
   * @return true - always
   */

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

    roundEnv.getElementsAnnotatedWith(responsibleFor())
        .stream()
        .filter(e -> e.getKind() == ElementKind.INTERFACE)
        .map(e -> {
          final TypeElement typeElement = (TypeElement) e;
          actualProcessedTypeElement = typeElement;
          final Builder typeSpecBuilder = createTypedDAOBuilderTypeSpecBuilder(typeElement, ExtendedInvocationHandler.class, INVOCATION_HANDLER_CLASSNAME_POST_FIX);
//          typeSpecBuilderForTargetClass = typeSpecBuilder;
          typeSpecBuilderForTargetClass = null;
          return new HolderStep001(typeElement, typeSpecBuilder);
        })
        .map(holderStep01 -> {
          final String pkgName = holderStep01.typeElement.getEnclosingElement().toString();
          final ClassName typeElementClassName = ClassName.get(pkgName, holderStep01.typeElement.getSimpleName().toString());

          final ClassName adapterBuilderClassname = ClassName.get(pkgName, holderStep01.typeElement.getSimpleName().toString() + BUILDER_CLASSNAME_POST_FIX);
          final ClassName invocationHandlerClassname = ClassName.get(pkgName, holderStep01.invocationHandlerTypeSpecBuilder.build().name);
          final Builder adapterBuilderTypeSpecBuilder = createAdapterBuilderBuilder(holderStep01.typeElement, typeElementClassName, adapterBuilderClassname, invocationHandlerClassname);

          return new HolderStep002(holderStep01, adapterBuilderTypeSpecBuilder, adapterBuilderClassname);
        })
        .peek(holderStep002 -> {
          Stream
              .concat(
                  Stream.of(holderStep002.typeElement),
                  holderStep002.typeElement
                      .getInterfaces()
                      .stream()
                      .map(i -> typeUtils.asElement(i)))
              .distinct()
              .peek(e -> {
                System.out.println("holderStep002.e = " + e.toString());
                System.out.println("holderStep002.typeElement = " + holderStep002.typeElement.getSimpleName());
              })
              .map(e -> (TypeElement) e)
              .forEach(element -> workEclosedElementsOnThisLevel(
                  holderStep002.typeElement,
                  holderStep002.invocationHandlerTypeSpecBuilder,
                  holderStep002.adapterBuilderClassname,
                  holderStep002.adapterBuilderTypeSpecBuilder,
                  element.getEnclosedElements()));
          System.out.println("process.holderStep002.adapterBuilderTypeSpecBuilder = " + holderStep002.adapterBuilderTypeSpecBuilder);
        })
        .forEach(holderStep002 -> {
          //write InvocationHandler
          writeDefinedClass(holderStep002.pkgName(), holderStep002.invocationHandlerTypeSpecBuilder);
          //write Builder
          writeDefinedClass(holderStep002.pkgName(), holderStep002.adapterBuilderTypeSpecBuilder);
          actualProcessedTypeElement = null;
          typeSpecBuilderForTargetClass = null;
        });
    return true;
  }


  @Override
  public Class<DynamicObjectAdapterBuilder> responsibleFor() {
    return DynamicObjectAdapterBuilder.class;
  }

  @Override
  protected void addClassLevelSpecs(final TypeElement typeElement, final RoundEnvironment roundEnv) {
  }

  @Override
  protected CodeBlock defineMethodImplementation(final ExecutableElement methodElement, final String methodName2Delegate, final TypeElement typeElementTargetClass) {
    return null;
  }

  @Override
  protected void addStaticImports(final JavaFile.Builder builder) {

  }

  private Builder createTypedDAOBuilderTypeSpecBuilder(TypeElement typeElement, Class class2Extend, String classnamePostFix) {
    // Get the full QualifiedTypeName
    final ClassName extendedInvocationHandlerClassName = ClassName.get(class2Extend);
    final ParameterizedTypeName typedExtendedInvocationHandler = ParameterizedTypeName.get(extendedInvocationHandlerClassName, TypeName.get(typeElement.asType()));

    return TypeSpec
        .classBuilder(typeElement.getSimpleName().toString() + classnamePostFix)
        .superclass(typedExtendedInvocationHandler)
        .addModifiers(Modifier.PUBLIC);
  }

  private Builder createAdapterBuilderBuilder(TypeElement typeElement, ClassName typeElementClassName, ClassName adapterBuilderClassname, ClassName invocationHandlerClassname) {
    final Builder adapterBuilderTypeSpecBuilder =
        createTypedDAOBuilderTypeSpecBuilder(typeElement, AdapterBuilder.class, BUILDER_CLASSNAME_POST_FIX);

    final FieldSpec invocationHandler = FieldSpec
        .builder(invocationHandlerClassname, "invocationHandler")
        .addModifiers(Modifier.FINAL, Modifier.PRIVATE)
        .initializer("new $T()", invocationHandlerClassname)
        .build();

    adapterBuilderTypeSpecBuilder
        //add static newBuilder method
        .addMethod(MethodSpec
            .methodBuilder("newBuilder")
            .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
            .addStatement("return new $T()", adapterBuilderClassname)
            .returns(adapterBuilderClassname)
            .build())
        //add class attributre
        .addField(invocationHandler)
        //add getInvocationHandler
        .addMethod(MethodSpec
            .methodBuilder("getInvocationHandler")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override.class)
            .addStatement(" return invocationHandler")
            .returns(invocationHandlerClassname)
            .build())
        //add setOriginal
        .addMethod(MethodSpec
            .methodBuilder("setOriginal")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(ParameterSpec.builder(typeElementClassName, "original", Modifier.FINAL).build())
            .addStatement("invocationHandler.setOriginal(original)")
            .addStatement("return this")
            .returns(adapterBuilderClassname)
            .build()
        );
    return adapterBuilderTypeSpecBuilder;
  }

  private void workEclosedElementsOnThisLevel(final TypeElement typeElementTargetClass,
                                              final Builder invocationHandlerBuilder,
                                              final ClassName adapterBuilderClassname,
                                              final Builder adapterBuilderTypeSpecBuilder,
                                              final List<? extends Element> enclosedElements) {
    //now all Delegator Methods
    enclosedElements
        .stream()
        .filter(enclosed -> enclosed.getKind() == ElementKind.METHOD)
        .forEach(enclosed -> {
          final ExecutableElement methodElement = (ExecutableElement) enclosed;
          System.out.println("workEclosedElementsOnThisLevel.methodElement.getSimpleName() = " + methodElement.getSimpleName());
          final boolean containsPublic = methodElement.getModifiers().contains(Modifier.PUBLIC);
          System.out.println("workEclosedElementsOnThisLevel.containsPublic = " + containsPublic);
          if (containsPublic) {
            final Optional<TypeSpec> functionalInterfaceSpec = writeFunctionalInterface(typeElementTargetClass, methodElement);

            addBuilderMethodForFunctionalInterface(invocationHandlerBuilder, methodElement, functionalInterfaceSpec, typeElementTargetClass);
            //nun alle Delegator Methods

            final String methodSimpleName = methodElement.getSimpleName().toString();
            final String methodimpleNameUpper = methodSimpleName.substring(0, 1).toUpperCase() + methodSimpleName.substring(1);

            final ClassName bestGuess = ClassName.get(pkgName(typeElementTargetClass), functionalInterfaceSpec.get().name);

            final ParameterSpec parameterSpec = ParameterSpec.builder(bestGuess, "adapter", Modifier.FINAL).build();

            final MethodSpec methodSpec = MethodSpec
                .methodBuilder("with" + methodimpleNameUpper)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(parameterSpec)
                .addStatement("invocationHandler." + methodSimpleName + "(adapter)")
                .addStatement("return this")
                .returns(adapterBuilderClassname)
                .build();
            adapterBuilderTypeSpecBuilder.addMethod(methodSpec);
//                .build();
          }
        });
  }

  private void addBuilderMethodForFunctionalInterface(Builder invocationHandlerBuilder,
                                                      ExecutableElement methodElement,
                                                      Optional<TypeSpec> functionalInterfaceSpec,
                                                      final TypeElement typeElementTargetClass) {

    System.out.println("addBuilderMethodForFunctionalInterface.functionalInterfaceSpec = " + functionalInterfaceSpec.isPresent());

    functionalInterfaceSpec
        .ifPresent(funcInterfaceSpec -> {

          final ClassName bestGuess = ClassName.get(pkgName(typeElementTargetClass), funcInterfaceSpec.name);
          final ParameterSpec parameterSpec = ParameterSpec.builder(bestGuess, "adapter", Modifier.FINAL).build();

          final MethodSpec adapterMethodSpec = MethodSpec
              .methodBuilder(methodElement.getSimpleName().toString())
              .addModifiers(Modifier.PUBLIC)
              .returns(void.class)
              .addParameter(parameterSpec)
              .addCode(CodeBlock.builder().addStatement("addAdapter(adapter)").build())
              .build();
          invocationHandlerBuilder.addMethod(adapterMethodSpec);
        });
  }
}