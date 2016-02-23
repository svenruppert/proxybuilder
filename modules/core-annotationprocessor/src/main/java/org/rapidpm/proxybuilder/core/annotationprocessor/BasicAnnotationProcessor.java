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

package org.rapidpm.proxybuilder.core.annotationprocessor;

import com.google.common.base.Joiner;
import com.squareup.javapoet.*;
import com.squareup.javapoet.TypeSpec.Builder;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Generated;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.stream.Collectors.toList;

public abstract class BasicAnnotationProcessor<T extends Annotation> extends AbstractProcessor {


  public static final String METHOD_NAME_FINALIZE = "finalize";
  public static final String METHOD_NAME_HASH_CODE = "hashCode";
  public static final String METHOD_NAME_EQUALS = "equals";
  protected static final String CLASS_NAME = "CLASS_NAME";
  protected static final String DELEGATOR_FIELD_NAME = "delegator";
  private final Set<MethodIdentifier> executableElementSet = new HashSet<>();
  protected Filer filer;
  protected Messager messager;
  protected Elements elementUtils;
  protected Types typeUtils;
  protected Builder typeSpecBuilderForTargetClass;

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    Set<String> annotataions = new LinkedHashSet<>();
    annotataions.add(responsibleFor().getCanonicalName());
    return annotataions;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    typeUtils = processingEnv.getTypeUtils();
    elementUtils = processingEnv.getElementUtils();
    filer = processingEnv.getFiler();
    messager = processingEnv.getMessager();
  }

  @Override
  public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    roundEnv
        .getElementsAnnotatedWith(responsibleFor())
        .stream()
        .map(e -> (TypeElement) e)
        .forEach(typeElement -> {
          final TypeName interface2Implement = TypeName.get(typeElement.asType());
          final Builder forTargetClass = createTypeSpecBuilderForTargetClass(typeElement, interface2Implement);

          addClassLevelSpecs(typeElement, roundEnv);
          System.out.println(" ============================================================ ");
          executableElementSet.clear();
          defineNewGeneratedMethod(typeElement, forTargetClass);

          executableElementSet.clear();
          System.out.println(" ============================================================ ");

          writeDefinedClass(pkgName(typeElement), forTargetClass);
          typeSpecBuilderForTargetClass = null;
        });

    return true;
  }

  public abstract Class<T> responsibleFor();

  private void defineNewGeneratedMethod(final TypeElement typeElement, final Builder forTargetClass) {
    System.out.println("defineNewGeneratedMethod.typeElement = " + typeElement.getQualifiedName().toString());

    if (typeElement == null) {
      //loggen
    } else {

//      final Map<Boolean, List<ExecutableElement>> nativeNoneNativeMethodMap = typeElement
      typeElement
          .getEnclosedElements()
          .stream()
          .filter(e -> e.getKind() == ElementKind.METHOD)
          .map(methodElement -> (ExecutableElement) methodElement) //cast only
          .filter(methodElement -> methodElement.getModifiers().contains(Modifier.PUBLIC))
          .filter(methodElement -> !methodElement.getModifiers().contains(Modifier.FINAL))
          .filter(methodElement -> !methodElement.getSimpleName().toString().equals(METHOD_NAME_FINALIZE))
          .filter(methodElement -> !executableElementSet.contains(new MethodIdentifier(methodElement)))
          .forEach(
              methodElement -> {
                executableElementSet.add(new MethodIdentifier(methodElement));
                final String methodName2Delegate = methodElement.getSimpleName().toString();
                final CodeBlock codeBlock = defineMethodImplementation(methodElement, methodName2Delegate);
                final MethodSpec delegatedMethodSpec = defineDelegatorMethod(methodElement, methodName2Delegate, codeBlock);
                forTargetClass.addMethod(delegatedMethodSpec);
              }
          );

//      .collect(Collectors.groupingBy(methodElement -> methodElement.getModifiers().contains(Modifier.NATIVE)));

//      Optional - multipleMaps(MapStep... steps)
//      filter

      // NOW work on NON-native Methods
//      if (nativeNoneNativeMethodMap.get(false) != null)
//        nativeNoneNativeMethodMap
//            .get(false)
//            .forEach(
//                methodElement -> {
//                  executableElementSet.add(new MethodIdentifier(methodElement));
//                  final String methodName2Delegate = methodElement.getSimpleName().toString();
//                  System.out.println("methodName2Delegate non-native = " + methodName2Delegate);
//                  final CodeBlock codeBlock = defineMethodImplementation(methodElement, methodName2Delegate);
//
//                  final MethodSpec delegatedMethodSpec = defineDelegatorMethod(methodElement, methodName2Delegate, codeBlock);
//
//                  forTargetClass.addMethod(delegatedMethodSpec);
//                }
//            );
//
//      // NOW work on native Methods
//      if (nativeNoneNativeMethodMap.get(true) != null)
//        nativeNoneNativeMethodMap
//            .get(true)
//            .forEach(methodElement -> {
//              executableElementSet.add(new MethodIdentifier(methodElement));
//              final String methodName2Delegate = methodElement.getSimpleName().toString();
//              System.out.println("methodName2Delegate native = " + methodName2Delegate);
//              final CodeBlock codeBlock = defineMethodImplementation(methodElement, methodName2Delegate);
//              final MethodSpec delegatedMethodSpec = defineDelegatorMethod(methodElement, methodName2Delegate, codeBlock);
//              forTargetClass.addMethod(delegatedMethodSpec);
//            });

      // work on Parent class
      final TypeMirror superclass = typeElement.getSuperclass();
      if (superclass != null && !"none".equals(superclass.toString())) {
        final TypeElement typeElement1 = (TypeElement) typeUtils.asElement(superclass);
        defineNewGeneratedMethod(typeElement1, forTargetClass);
      }

      // work on Interfaces
      typeElement.getInterfaces()
          .stream()
          .forEach(t -> defineNewGeneratedMethod((TypeElement) typeUtils.asElement(t), forTargetClass));

    }

  }

  private Builder createTypeSpecBuilderForTargetClass(final TypeElement typeElement, final TypeName type2inherit) {
    if (typeSpecBuilderForTargetClass == null) {
      if (typeElement.getKind() == ElementKind.INTERFACE) {
        typeSpecBuilderForTargetClass = TypeSpec
            .classBuilder(targetClassNameSimple(typeElement))
            .addSuperinterface(type2inherit);
      } else if (typeElement.getKind() == ElementKind.CLASS) {
        typeSpecBuilderForTargetClass = TypeSpec
            .classBuilder(targetClassNameSimple(typeElement))
            .superclass(type2inherit);
//            .addModifiers(Modifier.PUBLIC);
      } else {
        throw new RuntimeException("alles doof");
      }
      typeElement.getModifiers()
          .stream()
          .filter(m -> !m.equals(Modifier.ABSTRACT))
          .forEach(m -> typeSpecBuilderForTargetClass.addModifiers(m));
    }


    typeSpecBuilderForTargetClass.addAnnotation(createAnnotationSpecGenerated());
    return typeSpecBuilderForTargetClass;
  }

  protected abstract void addClassLevelSpecs(final TypeElement typeElement, final RoundEnvironment roundEnv);

  protected abstract CodeBlock defineMethodImplementation(final ExecutableElement methodElement, final String methodName2Delegate);

  protected MethodSpec defineDelegatorMethod(final ExecutableElement methodElement, final String methodName2Delegate, final CodeBlock codeBlock) {
    final Set<Modifier> reducedMethodModifiers = EnumSet.copyOf(methodElement.getModifiers());
    reducedMethodModifiers.remove(Modifier.ABSTRACT);
    reducedMethodModifiers.remove(Modifier.NATIVE);

    return MethodSpec.methodBuilder(methodName2Delegate)
        .addModifiers(reducedMethodModifiers)
        .returns(TypeName.get(methodElement.getReturnType()))
        .addParameters(defineParamsForMethod(methodElement))
        .addExceptions(methodElement
            .getThrownTypes()
            .stream()
            .map(TypeName::get)
            .collect(toList()))
        .addCode(codeBlock)

        .build();
  }

  protected String targetClassNameSimple(final TypeElement typeElement) {
    return ClassName.get(pkgName(typeElement), className(typeElement) + classNamePostFix()).simpleName();
  }

  private String classNamePostFix() {
    return responsibleFor().getSimpleName();
  }

  protected Optional<TypeSpec> writeFunctionalInterface(ExecutableElement methodElement, MethodSpec.Builder methodSpecBuilder) {
    final String methodNameRaw = methodElement.getSimpleName().toString();
    final String firstCharUpper = (methodNameRaw.charAt(0) + "").toUpperCase();

    final String finalMethodName = firstCharUpper + methodNameRaw.substring(1);

    final Element typeElement = methodElement.getEnclosingElement();

    final Builder functionalInterfaceTypeSpecBuilder = TypeSpec
        .interfaceBuilder(typeElement.getSimpleName().toString() + "Method" + finalMethodName)
        .addAnnotation(createAnnotationSpecGenerated())
        .addAnnotation(FunctionalInterface.class)
        .addMethod(methodSpecBuilder.build())
        .addModifiers(Modifier.PUBLIC);

    final Element enclosingElement = typeElement.getEnclosingElement();
    final String packageName = enclosingElement.toString();
    return writeDefinedClass(packageName, functionalInterfaceTypeSpecBuilder);
  }

  @NotNull
  private AnnotationSpec createAnnotationSpecGenerated() {
    return AnnotationSpec.builder(Generated.class)
        .addMember("value", "$S", this.getClass().getSimpleName())
        .addMember("date", "$S", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
        .addMember("comments", "$S", "www.proxybuilder.org")
        .build();
  }

  protected Optional<TypeSpec> writeDefinedClass(String pkgName, Builder typeSpecBuilder) {
    final TypeSpec typeSpec = typeSpecBuilder.build();
    final JavaFile javaFile = JavaFile.builder(pkgName, typeSpec).skipJavaLangImports(true).build();
    final String className = javaFile.packageName + "." + javaFile.typeSpec.name;
    try {
      JavaFileObject jfo = filer.createSourceFile(className);
      Writer writer = jfo.openWriter();
      javaFile.writeTo(writer);
      writer.flush();
      return Optional.of(typeSpec);
    } catch (IOException e) {
      e.printStackTrace();
      if (e instanceof FilerException) {
        if (e.getMessage().contains("Attempt to recreate a file for type")) {
          return Optional.of(typeSpec);
        }
      }
      System.out.println("e = " + e);
    }
    return Optional.empty();
  }

  protected FieldSpec defineSimpleClassNameField(final TypeElement typeElement) {
    final ClassName className = ClassName.get(typeElement);
    return FieldSpec
        .builder(ClassName.get(String.class), CLASS_NAME)
        .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
//        .initializer("\"" + className.simpleName() + "\"")
        .initializer("\"" + className + "\"")
        .build();
  }

  protected FieldSpec defineDelegatorField(final TypeElement typeElement) {
    final ClassName delegatorClassName = ClassName.get(pkgName(typeElement), className(typeElement));
    return FieldSpec
        .builder(delegatorClassName, DELEGATOR_FIELD_NAME)
        .addModifiers(Modifier.PRIVATE)
        .build();
  }

  protected String pkgName(final TypeElement typeElement) {
    return elementUtils.getPackageOf(typeElement).getQualifiedName().toString();
  }

  private String className(final Element typeElement) {
    return typeElement.getSimpleName().toString();
  }

  protected String delegatorStatementWithReturn(final ExecutableElement methodElement, final String methodName2Delegate) {
    return "return " + DELEGATOR_FIELD_NAME + "." + delegatorMethodCall(methodElement, methodName2Delegate);
  }

  protected String delegatorMethodCall(final ExecutableElement methodElement, final String methodName2Delegate) {
    return methodName2Delegate + "(" +
        Joiner.on(", ")
            .skipNulls()
            .join(defineParamsForMethod(methodElement)
                .stream()
                .map(v -> v.name)
                .collect(toList())) +
        ")";
  }

  protected List<ParameterSpec> defineParamsForMethod(final ExecutableElement methodElement) {
    return methodElement
        .getParameters()
        .stream()
        .map(parameter -> {
          final Name simpleName = parameter.getSimpleName();
          final TypeMirror typeMirror = parameter.asType();
          TypeName typeName = TypeName.get(typeMirror);
          return ParameterSpec.builder(typeName, simpleName.toString(), Modifier.FINAL).build();
        })
        .collect(toList());
  }

  private static class MethodIdentifier {
    private final String name;
    private final TypeName[] parameters;

    public MethodIdentifier(final String name) {
      this.name = name;
      parameters = new TypeName[0];
    }

    public MethodIdentifier(final String name, TypeName... typeNames) {
      this.name = name;
      parameters = new TypeName[typeNames.length];
      System.arraycopy(typeNames, 0, parameters, 0, parameters.length);
    }


    public MethodIdentifier(final ExecutableElement methodElement) {
      final List<? extends VariableElement> p = methodElement.getParameters();
      parameters = new TypeName[p.size()];
      for (int i = 0; i < parameters.length; i++) {
        final VariableElement variableElement = p.get(i);
        parameters[i] = TypeName.get(variableElement.asType());
      }
      name = methodElement.getSimpleName().toString();
    }

    public int hashCode() {
      return name.hashCode();
    }

    // we can save time by assuming that we only compare against
    // other MethodIdentifier objects
    public boolean equals(Object o) {
      MethodIdentifier mid = (MethodIdentifier) o;
      return name.equals(mid.name) &&
          Arrays.equals(parameters, mid.parameters);
    }
  }

}
