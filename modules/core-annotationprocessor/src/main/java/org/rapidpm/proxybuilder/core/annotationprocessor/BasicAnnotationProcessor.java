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

import com.squareup.javapoet.*;
import com.squareup.javapoet.TypeSpec.Builder;

import javax.annotation.Generated;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
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
import java.util.stream.Collectors;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.type.TypeKind.ARRAY;

public abstract class BasicAnnotationProcessor<T extends Annotation> extends AbstractProcessor {


  public static final String METHOD_NAME_FINALIZE = "finalize";
  public static final String METHOD_NAME_HASH_CODE = "hashCode";
  public static final String METHOD_NAME_EQUALS = "equals";
  protected static final String CLASS_NAME = "CLASS_NAME";
  protected static final String DELEGATOR_FIELD_NAME = "delegator";
  private final Set<MethodIdentifier> executableElementSet = new HashSet<>();
  private final Set<MethodIdentifier> finalMethodElementSet = new HashSet<>();
  protected Filer filer;
  protected Messager messager;
  protected Elements elementUtils;
  protected Types typeUtils;
  protected Builder typeSpecBuilderForTargetClass;
  protected TypeElement actualProcessedTypeElement;

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    final Set<String> annotataions = new LinkedHashSet<>();
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
          actualProcessedTypeElement = typeElement;
          final TypeName interface2Implement = TypeName.get(typeElement.asType());
          createTypeSpecBuilderForTargetClass(typeElement, interface2Implement);

          addClassLevelSpecs(typeElement, roundEnv);
          System.out.println(" ============================================================ ");
          executableElementSet.clear();
          finalMethodElementSet.clear();
          defineNewGeneratedMethod(typeElement);
          defineGeneratedConstructorMethod(typeElement, typeSpecBuilderForTargetClass);
          executableElementSet.clear();
          finalMethodElementSet.clear();
          System.out.println(" ============================================================ ");

          writeDefinedClass(pkgName(typeElement), typeSpecBuilderForTargetClass);
          typeSpecBuilderForTargetClass = null;
          actualProcessedTypeElement = null;
        });

    return true;
  }

  public abstract Class<T> responsibleFor();


  private void defineGeneratedConstructorMethod(final TypeElement typeElement, final Builder forTargetClass) {
    System.out.println("defineGeneratedConstructorMethod.typeElement = " + typeElement.getQualifiedName().toString());
    // create the constructors
    typeElement
        .getEnclosedElements()
        .stream()
        .filter(e -> e.getKind() == ElementKind.CONSTRUCTOR)
        .map(methodElement -> (ExecutableElement) methodElement) //cast only
        .filter(methodElement -> !methodElement.getModifiers().contains(Modifier.PRIVATE))
        .filter(methodElement -> !methodElement.getModifiers().contains(Modifier.FINAL))
        .filter(methodElement -> !methodElement.isDefault())
        .filter(methodElement -> !executableElementSet.contains(new MethodIdentifier(methodElement)))
        .peek(methodElement -> executableElementSet.add(new MethodIdentifier(methodElement)))
        .forEach(
            methodElement -> {
              final Set<Modifier> reducedMethodModifiers = EnumSet.copyOf(methodElement.getModifiers());
              reducedMethodModifiers.remove(Modifier.ABSTRACT);
              reducedMethodModifiers.remove(Modifier.NATIVE);
              final List<ParameterSpec> parameterSpecList = defineParamsForMethod(methodElement);

              final MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                  .addModifiers(reducedMethodModifiers)
                  .addParameters(parameterSpecList);

              final String delegatorMethodCall = delegatorMethodCall(methodElement, "super");
              constructorBuilder.addStatement(delegatorMethodCall);

              final MethodSpec constructorMethodSpec = constructorBuilder.build();
              forTargetClass.addMethod(constructorMethodSpec);
            }
        );
  }


  //  private void defineNewGeneratedMethod(final TypeElement typeElement, final Builder forTargetClass) {
  private void defineNewGeneratedMethod(final TypeElement typeElement) {
    System.out.println("defineNewGeneratedMethod.typeElement = " + typeElement.getQualifiedName().toString());

    if (typeElement == null) {
      //loggen
    } else {

      typeElement
          .getEnclosedElements()
          .stream()
          .filter(e -> e.getKind() == ElementKind.METHOD)
          .map(methodElement -> (ExecutableElement) methodElement) //cast only
          .filter(methodElement -> methodElement.getModifiers().contains(Modifier.PUBLIC))
          .peek(methodElement -> {
            final boolean contains = methodElement.getModifiers().contains(Modifier.FINAL);
            if (contains) {
              finalMethodElementSet.add(new MethodIdentifier(methodElement));
            }
          })
          .filter(methodElement -> !finalMethodElementSet.contains(new MethodIdentifier(methodElement)))
//          .filter(methodElement -> !methodElement.getModifiers().contains(Modifier.FINAL))
          .filter(methodElement -> !methodElement.getSimpleName().toString().equals(METHOD_NAME_FINALIZE))
          .filter(methodElement -> !executableElementSet.contains(new MethodIdentifier(methodElement)))
          .peek(methodElement -> executableElementSet.add(new MethodIdentifier(methodElement)))
          .forEach(
              methodElement -> {
                final String methodName2Delegate = methodElement.getSimpleName().toString();
                final CodeBlock codeBlock = defineMethodImplementation(methodElement, methodName2Delegate, actualProcessedTypeElement);
                final MethodSpec delegatedMethodSpec = defineDelegatorMethod(methodElement, methodName2Delegate, codeBlock);
                typeSpecBuilderForTargetClass.addMethod(delegatedMethodSpec);
              }
          );

      // work on Parent class
      final TypeMirror superclass = typeElement.getSuperclass();
      if (superclass != null && !"none".equals(superclass.toString())) {
        final TypeElement typeElementSuperClass = (TypeElement) typeUtils.asElement(superclass);
        defineNewGeneratedMethod(typeElementSuperClass);
      }

      // work on Interfaces
      typeElement.getInterfaces()
          .forEach(t -> defineNewGeneratedMethod((TypeElement) typeUtils.asElement(t)));

    }

  }

  private Builder createTypeSpecBuilderForTargetClass(final TypeElement typeElement, final TypeName type2inherit) {
    if (typeSpecBuilderForTargetClass == null) {
      if (typeElement.getKind() == ElementKind.INTERFACE) {
        typeSpecBuilderForTargetClass = TypeSpec
            .classBuilder(targetClassNameSimpleForGeneratedClass(typeElement))
            .addSuperinterface(type2inherit);
      } else if (typeElement.getKind() == ElementKind.CLASS) {
        typeSpecBuilderForTargetClass = TypeSpec
            .classBuilder(targetClassNameSimpleForGeneratedClass(typeElement))
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

  protected abstract CodeBlock defineMethodImplementation(final ExecutableElement methodElement, final String methodName2Delegate, final TypeElement typeElementTargetClass);

  protected MethodSpec defineDelegatorMethod(final ExecutableElement methodElement, final String methodName2Delegate, final CodeBlock codeBlock) {
    System.out.println("defineDelegatorMethod.methodElement = " + methodElement);
    final MethodSpec.Builder methodSpecBuilder = defineDelegatorMethodSpec(methodElement, methodName2Delegate, codeBlock);

    return methodSpecBuilder.build();
  }

  protected MethodSpec.Builder defineDelegatorMethodSpec(final ExecutableElement methodElement, final String methodName2Delegate, final CodeBlock codeBlock) {
    final Set<Modifier> reducedMethodModifiers = EnumSet.copyOf(methodElement.getModifiers());
    reducedMethodModifiers.remove(Modifier.ABSTRACT);
    reducedMethodModifiers.remove(Modifier.NATIVE);
    reducedMethodModifiers.remove(Modifier.DEFAULT);

    final MethodSpec.Builder methodBuilder = methodBuilder(methodName2Delegate);

    final TypeMirror returnType = methodElement.getReturnType();
    final TypeKind returnTypeKind = returnType.getKind();
    final boolean primitive = returnTypeKind.isPrimitive();

    final boolean isNotPrimitiveAndReturnsNonVoid = !primitive && !returnType.toString().equals("void");
    final boolean isNotPrimitiveAndReturnsVoid = !primitive && returnType.toString().equals("void");

    if (isNotPrimitiveAndReturnsNonVoid) {
      System.out.println("isNotPrimitiveAndReturnsNonVoid = " + isNotPrimitiveAndReturnsNonVoid);
      final boolean isDeclaredType = returnType instanceof DeclaredType;
      if (!isDeclaredType) { // <T extends List>
        System.out.println("defineDelegatorMethod.returnType " + returnType);

        final String name = TypeName.get(returnType).toString();
        System.out.println("defineDelegatorMethod.name = " + name);

        final List<? extends TypeMirror> directSupertypes = typeUtils.directSupertypes(returnType);
        if (directSupertypes != null && directSupertypes.size() > 1) { // <T extends List>
          System.out.println("defineDelegatorMethod.directSupertypes = " + directSupertypes);

          final List<TypeName> typeNames = directSupertypes
              .stream()
              .filter((typeMirror) -> !typeMirror.toString().equals("java.lang.Object"))
              .map(TypeName::get)
              .collect(Collectors.toList());

          System.out.println("defineDelegatorMethod.typeNames = " + typeNames);
          final ElementKind elementKind = typeUtils.asElement(returnType).getKind();
          if (!elementKind.equals(ElementKind.CLASS) && !elementKind.equals(ElementKind.INTERFACE)) {
            System.out.println("defineDelegatorMethod.kind (no Class no Interface ) = " + returnTypeKind);
            final TypeName typeName = TypeVariableName.get(returnType);
            methodBuilder.addTypeVariable(
                TypeVariableName.get(
                    typeName.toString(),
                    typeNames.toArray(new TypeName[typeNames.size()])));
          }
        } else { // <T>
          final TypeName typeName = TypeVariableName.get(returnType);
          System.out.println("defineDelegatorMethod.typeName (else) = " + typeName);
          methodBuilder.addTypeVariable(TypeVariableName.get(typeName.toString()));
        }
      }
    } else if (isNotPrimitiveAndReturnsVoid) {
      System.out.println("isNotPrimitiveAndReturnsVoid = " + isNotPrimitiveAndReturnsVoid);

    } else {
      System.out.println("defineDelegatorMethod. return is Primitive = ");
    }

    final MethodSpec.Builder methodSpecBuilder = methodBuilder
        .addModifiers(reducedMethodModifiers)
        .returns(TypeName.get(returnType))
        .addParameters(defineParamsForMethod(methodElement))
        .addExceptions(methodElement
            .getThrownTypes()
            .stream()
            .map(TypeName::get)
            .collect(toList()));
    if (codeBlock != null)
      methodSpecBuilder.addCode(codeBlock);
    return methodSpecBuilder;
  }

  protected String targetClassNameSimpleForGeneratedClass(final TypeElement typeElement) {
    return ClassName.get(pkgName(typeElement), className(typeElement) + classNamePostFix()).simpleName();
  }

  protected String targetClassNameSimpleForSourceClass(final TypeElement typeElement) {
    return ClassName.get(pkgName(typeElement), className(typeElement)).simpleName();
  }

  private String classNamePostFix() {
    return responsibleFor().getSimpleName();
  }

  protected Optional<TypeSpec> writeFunctionalInterface(final TypeElement typeElementTargetClass,
                                                        final ExecutableElement methodElement) {

    final MethodSpec.Builder methodSpecBuilder = defineDelegatorMethodSpec(methodElement, methodElement.getSimpleName().toString(), null);
    methodSpecBuilder.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

    final String methodNameRaw = methodElement.getSimpleName().toString();
    final String firstCharUpper = (methodNameRaw.charAt(0) + "").toUpperCase();
    final List<TypeMirror> argumentTypeListFromMethod = extractArgumentTypeListFromMethod(methodElement);

    final String methodNamePostFix = (argumentTypeListFromMethod.isEmpty()) ? "" : "" +
        String.join("", argumentTypeListFromMethod
            .stream()
            .map(t -> {
              String postFix = "";
              if (t.getKind().isPrimitive()) {
                final TypeName boxedTypeName = TypeName.get(t).box();
                final String[] split = boxedTypeName.toString().split("\\.");
                postFix = split[split.length - 1];
              } else if (t.getKind().equals(ARRAY)) {
                final String[] split = t.toString().replace("[]", "Array").split("\\.");
                postFix = split[split.length - 1];
              } else {
                System.out.println("writeFunctionalInterface t = " + t);
                final Element element = typeUtils.asElement(t);
                final ClassName className = ClassName.get((TypeElement) element);
                final String[] split = className.simpleName().split("\\.");
                postFix = split[split.length - 1];
              }
              return postFix.substring(0, 1).toUpperCase() + postFix.substring(1);
            }).collect(Collectors.toList()));


    final String finalMethodName = firstCharUpper + methodNameRaw.substring(1) + methodNamePostFix;

    final Builder functionalInterfaceTypeSpecBuilder = TypeSpec
        .interfaceBuilder(typeElementTargetClass.getSimpleName().toString() + "Method" + finalMethodName)
        .addAnnotation(createAnnotationSpecGenerated())
        .addMethod(methodSpecBuilder.build())
        .addModifiers(Modifier.PUBLIC);

    if (methodNameRaw.equals("hashCode") || methodNameRaw.equals("toString") || methodNameRaw.equals("equals")) {
      //
    } else {
      functionalInterfaceTypeSpecBuilder.addAnnotation(FunctionalInterface.class);
    }
    return writeDefinedClass(pkgName(typeElementTargetClass), functionalInterfaceTypeSpecBuilder);
  }

  //TODO Define as Lambda
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

  //TODO Define as Lambda
  protected List<TypeMirror> extractArgumentTypeListFromMethod(final ExecutableElement methodElement) {
    return methodElement
        .getParameters()
        .stream()
        .map(Element::asType)
        .collect(toList());
  }


  private AnnotationSpec createAnnotationSpecGenerated() {
    return AnnotationSpec.builder(Generated.class)
        .addMember("value", "$S", this.getClass().getSimpleName())
        .addMember("date", "$S", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
        .addMember("comments", "$S", "www.proxybuilder.org")
        .build();
  }


  protected abstract void addStaticImports(JavaFile.Builder builder);

  protected Optional<TypeSpec> writeDefinedClass(String pkgName, Builder typeSpecBuilder) {

    System.out.println("typeSpecBuilder = " + typeSpecBuilder);


    final TypeSpec typeSpec = typeSpecBuilder.build();
//    System.out.println("writeDefinedClass.typeSpec = " + typeSpec);
    final JavaFile.Builder javaFileBuilder = JavaFile
        .builder(pkgName, typeSpec)
        .skipJavaLangImports(true);

    addStaticImports(javaFileBuilder);

    final JavaFile javaFile = javaFileBuilder.build();

    final String className = javaFile.packageName + "." + javaFile.typeSpec.name;
    try {
      JavaFileObject jfo = filer.createSourceFile(className);
      Writer writer = jfo.openWriter();
      javaFile.writeTo(writer);
      writer.flush();
      //return Optional.of(typeSpec);
    } catch (IOException e) {
      e.printStackTrace();
      if (e instanceof FilerException) {
        if (e.getMessage().contains("Attempt to recreate a file for type")) {
          return Optional.of(typeSpec);
        }
      }
      System.out.println("e = " + e);
    }
    return Optional.of(typeSpec);
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

  protected String className(final Element typeElement) {
    return typeElement.getSimpleName().toString();
  }


  protected String defineMethodReferenzePoint(final ExecutableElement methodElement) {
    System.out.println("defineMethodReferenzePoint.methodElement = " + methodElement);
    // static -> ClassName non-static DELEGATOR_FIELD_NAME
    if (methodElement.getModifiers().contains(Modifier.STATIC)) {
      System.out.println("defineMethodReferenzePoint.isSTATIC");
      boolean isClass = methodElement.getEnclosingElement().getKind().equals(ElementKind.CLASS);

      System.out.println("defineMethodReferenzePoint.isClass - " + isClass);
      return targetClassNameSimpleForSourceClass((TypeElement) methodElement.getEnclosingElement());
    } else {
      return DELEGATOR_FIELD_NAME;
    }
  }


  protected String delegatorStatementWithReturn(final ExecutableElement methodElement, final String methodName2Delegate) {
    return "return " + delegatorStatementWithOutReturn(methodElement, methodName2Delegate);
  }

  protected String delegatorStatementWithOutReturn(final ExecutableElement methodElement, final String methodName2Delegate) {
    return defineMethodReferenzePoint(methodElement) + "." + delegatorMethodCall(methodElement, methodName2Delegate);
  }

  protected String delegatorStatementWithLocalVariableResult(final ExecutableElement methodElement, final String methodName2Delegate) {
    return "final $T result = " + defineMethodReferenzePoint(methodElement) + "." + delegatorMethodCall(methodElement, methodName2Delegate);
  }

  protected String delegatorMethodCall(final ExecutableElement methodElement, final String methodName2Delegate) {
    return methodName2Delegate + "(" +

        defineParamsForMethod(methodElement)
            .stream()
            .map(v -> v.name)
            .filter(name -> name != null && !name.isEmpty())
            .reduce((name1, name2) -> name1 + ", " + name2)
            .orElseGet(String::new)
        +
        ")";
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
