package org.rapidpm.proxybuilder.objectadapter.processor;

import com.google.common.base.Joiner;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * Created by svenruppert on 24.10.15.
 */
public abstract class BasicObjectAdapterAnnotationProcessor<T> extends AbstractProcessor {

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  public abstract Class<T> responsibleFor();

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    Set<String> annotataions = new LinkedHashSet<>();
    annotataions.add(responsibleFor().getCanonicalName());
    return annotataions;
  }

  protected Filer filer;
  protected Messager messager;
  protected Elements elementUtils;
  protected Types typeUtils;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    typeUtils = processingEnv.getTypeUtils();
    elementUtils = processingEnv.getElementUtils();
    filer = processingEnv.getFiler();
    messager = processingEnv.getMessager();
  }



  protected abstract CodeBlock defineMethodImplementation(final ExecutableElement methodElement , final String methodName2Delegate);
  protected abstract void addClassLevelSpecs(final TypeElement typeElement, final RoundEnvironment roundEnv);




  protected Optional<TypeSpec> writeFunctionalInterface(ExecutableElement methodElement, MethodSpec.Builder methodSpecBuilder) {
    final String methodNameRaw = methodElement.getSimpleName().toString();
    final String firstCharUpper = (methodNameRaw.charAt(0) + "").toUpperCase();

    final String finalMethodName = firstCharUpper + methodNameRaw.substring(1);

    final Element typeElement = methodElement.getEnclosingElement();
    final TypeSpec.Builder functionalInterfaceTypeSpecBuilder = TypeSpec
        .interfaceBuilder(typeElement.getSimpleName().toString() + "Method" + finalMethodName)
        .addAnnotation(FunctionalInterface.class)
        .addMethod(methodSpecBuilder.build())
        .addModifiers(Modifier.PUBLIC);

    final Element enclosingElement = typeElement.getEnclosingElement();
    final String packageName = enclosingElement.toString();
    return writeDefinedClass(packageName, functionalInterfaceTypeSpecBuilder);
  }

  private TypeSpec.Builder typeSpecBuilderForTargetClass;

  protected TypeSpec.Builder createTypeSpecBuilderForTargetClass(final TypeElement typeElement, final TypeName interface2Implement) {
    if (typeSpecBuilderForTargetClass == null) {
      typeSpecBuilderForTargetClass = TypeSpec
          .classBuilder(targetClassNameSimple(typeElement))
          .addSuperinterface(interface2Implement)
          .addModifiers(Modifier.PUBLIC);
    }
    return typeSpecBuilderForTargetClass;
  }

  protected String targetClassNameSimple(final TypeElement typeElement) {
    return ClassName.get(pkgName(typeElement), className(typeElement) + classNamePostFix()).simpleName();
  }

  protected String pkgName(final TypeElement typeElement) {
    return elementUtils.getPackageOf(typeElement).getQualifiedName().toString();
  }

  private String classNamePostFix() {
    return responsibleFor().getSimpleName();
  }

  protected String className(final Element typeElement) {
    return typeElement.getSimpleName().toString();
  }

  protected FieldSpec defineDelegatorField(final TypeElement typeElement) {
    final ClassName delegatorClassName = ClassName.get(pkgName(typeElement), className(typeElement));
    return FieldSpec
        .builder(delegatorClassName, "delegator")
        .addModifiers(Modifier.PRIVATE)
        .build();
  }

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
      if (e instanceof FilerException){
        if (e.getMessage().contains("Attempt to recreate a file for type")){
          return Optional.of(typeSpec);
        }
      }
      System.out.println("e = " + e);
    }
    return Optional.empty();
  }
}
