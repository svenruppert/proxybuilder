package org.rapidpm.proxybuilder.core.annotationprocessor;

import com.google.common.base.Joiner;
import com.squareup.javapoet.*;
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

/**
 * Created by svenruppert on 09.12.15.
 */
public abstract class BasicAnnotationProcessor<T extends Annotation> extends AbstractProcessor {


  protected Filer filer;
  protected Messager messager;
  protected Elements elementUtils;
  protected Types typeUtils;
  protected TypeSpec.Builder typeSpecBuilderForTargetClass;

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
//        .filter(e -> e.getKind() == ElementKind.INTERFACE)
        .map(e -> (TypeElement) e)
        .forEach(typeElement -> {
          final TypeName interface2Implement = TypeName.get(typeElement.asType());
          final TypeSpec.Builder forTargetClass = createTypeSpecBuilderForTargetClass(typeElement, interface2Implement);

          addClassLevelSpecs(typeElement, roundEnv);

          //iter over the Methods from the Interface
          typeElement
              .getEnclosedElements()
              .stream()
              .filter(e -> e.getKind() == ElementKind.METHOD)
              .map(methodElement -> (ExecutableElement) methodElement) //cast only
              .filter(methodElement -> methodElement.getModifiers().contains(Modifier.PUBLIC)) //nur public ? evtl auch protected
              .forEach(methodElement -> {

                final String methodName2Delegate = methodElement.getSimpleName().toString();

                final CodeBlock codeBlock = defineMethodImplementation(methodElement, methodName2Delegate);

                final MethodSpec delegatedMethodSpec = defineDelegatorMethod(methodElement, methodName2Delegate, codeBlock);

                forTargetClass.addMethod(delegatedMethodSpec);
              });

          writeDefinedClass(pkgName(typeElement), forTargetClass);
        });
    return true;
  }

  protected TypeSpec.Builder createTypeSpecBuilderForTargetClass(final TypeElement typeElement, final TypeName type2inherit) {
    if (typeSpecBuilderForTargetClass == null) {
      System.out.println("typeElement.getKind() = " + typeElement.getKind());
      if (typeElement.getKind() == ElementKind.INTERFACE) {
        typeSpecBuilderForTargetClass = TypeSpec
            .classBuilder(targetClassNameSimple(typeElement))
            .addSuperinterface(type2inherit)
            .addModifiers(Modifier.PUBLIC);
      } else if (typeElement.getKind() == ElementKind.CLASS) {
        typeSpecBuilderForTargetClass = TypeSpec
            .classBuilder(targetClassNameSimple(typeElement))
            .superclass(type2inherit)
            .addModifiers(Modifier.PUBLIC);
      } else {
        throw new RuntimeException("alles doof");
      }
    }
    typeSpecBuilderForTargetClass.addAnnotation(createAnnotationSpecGenerated());
    return typeSpecBuilderForTargetClass;
  }

  protected abstract void addClassLevelSpecs(final TypeElement typeElement, final RoundEnvironment roundEnv);

  protected abstract CodeBlock defineMethodImplementation(final ExecutableElement methodElement, final String methodName2Delegate);

  protected MethodSpec defineDelegatorMethod(final ExecutableElement methodElement, final String methodName2Delegate, final CodeBlock codeBlock) {
    final Set<Modifier> reducedMethodModifiers = new HashSet<>(methodElement.getModifiers());
    reducedMethodModifiers.remove(Modifier.ABSTRACT);

    return MethodSpec.methodBuilder(methodName2Delegate)
        .addModifiers(reducedMethodModifiers)
        .returns(TypeName.get(methodElement.getReturnType()))
        .addParameters(defineParamsForMethod(methodElement))
        .addCode(codeBlock)
        .build();
  }

  protected Optional<TypeSpec> writeDefinedClass(String pkgName, TypeSpec.Builder typeSpecBuilder) {
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

  protected String pkgName(final TypeElement typeElement) {
    return elementUtils.getPackageOf(typeElement).getQualifiedName().toString();
  }

  protected String targetClassNameSimple(final TypeElement typeElement) {
    return ClassName.get(pkgName(typeElement), className(typeElement) + classNamePostFix()).simpleName();
  }

  @NotNull
  private AnnotationSpec createAnnotationSpecGenerated() {
    return AnnotationSpec.builder(Generated.class)
        .addMember("value", "$S", this.getClass().getSimpleName())
        .addMember("date", "$S", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
        .addMember("comments", "$S", "www.proxybuilder.org")
        .build();
  }

  public List<ParameterSpec> defineParamsForMethod(final ExecutableElement methodElement) {
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

  protected String className(final Element typeElement) {
    return typeElement.getSimpleName().toString();
  }

  private String classNamePostFix() {
    return responsibleFor().getSimpleName();
  }

  public abstract Class<T> responsibleFor();

  protected Optional<TypeSpec> writeFunctionalInterface(ExecutableElement methodElement, MethodSpec.Builder methodSpecBuilder) {
    final String methodNameRaw = methodElement.getSimpleName().toString();
    final String firstCharUpper = (methodNameRaw.charAt(0) + "").toUpperCase();

    final String finalMethodName = firstCharUpper + methodNameRaw.substring(1);

    final Element typeElement = methodElement.getEnclosingElement();

    final TypeSpec.Builder functionalInterfaceTypeSpecBuilder = TypeSpec
        .interfaceBuilder(typeElement.getSimpleName().toString() + "Method" + finalMethodName)
        .addAnnotation(createAnnotationSpecGenerated())
        .addAnnotation(FunctionalInterface.class)
        .addMethod(methodSpecBuilder.build())
        .addModifiers(Modifier.PUBLIC);

    final Element enclosingElement = typeElement.getEnclosingElement();
    final String packageName = enclosingElement.toString();
    return writeDefinedClass(packageName, functionalInterfaceTypeSpecBuilder);
  }

  protected FieldSpec defineDelegatorField(final TypeElement typeElement) {
    final ClassName delegatorClassName = ClassName.get(pkgName(typeElement), className(typeElement));
    return FieldSpec
        .builder(delegatorClassName, "delegator")
        .addModifiers(Modifier.PRIVATE)
        .build();
  }

  public String delegatorStatementWithReturn(final ExecutableElement methodElement, final String methodName2Delegate) {
    return "return delegator." + delegatorMethodCall(methodElement, methodName2Delegate);
  }

  public String delegatorMethodCall(final ExecutableElement methodElement, final String methodName2Delegate) {
    return methodName2Delegate + "(" +
        Joiner.on(", ")
            .skipNulls()
            .join(defineParamsForMethod(methodElement)
                .stream()
                .map(v -> v.name)
                .collect(toList())) +
        ")";
  }


}
