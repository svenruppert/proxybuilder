package org.rapidpm.proxybuilder.dynamicobjectadapter;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * Created by sven on 13.05.15.
 */

@AutoService(Processor.class)
public class AnnotationProcessor extends AbstractProcessor {

  private static final String BUILDER_CLASSNAME_POST_FIX = "AdapterBuilder";
  public static final String INVOCATION_HANDLER_CLASSNAME_POST_FIX = "InvocationHandler";


  private Filer filer;
  private Messager messager;
//  private Map<String, FactoryGroupedClasses> factoryClasses = new LinkedHashMap<String, FactoryGroupedClasses>();

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    Set<String> annotataions = new LinkedHashSet<>();
    annotataions.add(DynamicObjectAdapterBuilder.class.getCanonicalName());
    return annotataions;
  }


  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    final Types typeUtils = processingEnv.getTypeUtils();
    final Elements elementUtils = processingEnv.getElementUtils();
    filer = processingEnv.getFiler();
    messager = processingEnv.getMessager();
  }


  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(DynamicObjectAdapterBuilder.class)) {
      //Service
      // Check if an interface has been annotated with @Factory
      if (annotatedElement.getKind() != ElementKind.INTERFACE) {
        error(annotatedElement, "Only interfaces can be annotated with @%s", DynamicObjectAdapterBuilder.class.getSimpleName());
        return true; // Exit processing
      }
      // We can cast it, because we know that it of ElementKind.INTERFACE
      TypeElement typeElement = (TypeElement) annotatedElement;
      final String pkgName = typeElement.getEnclosingElement().toString();
      final ClassName typeElementClassName = ClassName.get(pkgName, typeElement.getSimpleName().toString());

//      final TypeSpec.Builder invocationHandlerBuilder = createInvocationHandlerTypeSpecBuilder(typeElement);
      final TypeSpec.Builder invocationHandlerBuilder = createTypedTypeSpecBuilder(typeElement, ExtendedInvocationHandler.class, INVOCATION_HANDLER_CLASSNAME_POST_FIX);

      final ClassName adapterBuilderClassname = ClassName.get(pkgName, typeElement.getSimpleName().toString() + BUILDER_CLASSNAME_POST_FIX);
      final ClassName invocationHandlerClassname = ClassName.get(pkgName, invocationHandlerBuilder.build().name);

      final TypeSpec.Builder adapterBuilderTypeSpecBuilder = createAdapterBuilderBuilder(typeElement, typeElementClassName, adapterBuilderClassname, invocationHandlerClassname);

      for (Element enclosed : annotatedElement.getEnclosedElements()) {
        if (enclosed.getKind() == ElementKind.METHOD) {
          ExecutableElement methodElement = (ExecutableElement) enclosed;
          if (methodElement.getModifiers().contains(Modifier.PUBLIC)) {
            final TypeMirror returnType = methodElement.getReturnType();

            final List<? extends VariableElement> parameters = methodElement.getParameters();
            List<ParameterSpec> parameterSpecs = new ArrayList<>();
            String params = "";
            for (VariableElement parameter : parameters) {
              final Name simpleName = parameter.getSimpleName();
              final TypeMirror typeMirror = parameter.asType();
              params = params + " " + typeMirror + " " + simpleName;
              TypeName typeName = TypeName.get(typeMirror);

              final ParameterSpec parameterSpec = ParameterSpec.builder(typeName, simpleName.toString(), Modifier.FINAL).build();
              parameterSpecs.add(parameterSpec);
            }

            final MethodSpec.Builder methodSpecBuilder = createMethodSpecBuilder(methodElement, returnType, parameterSpecs);

            final Optional<TypeSpec> functionalInterfaceSpec = writeFunctionalInterface(typeElement, methodElement, methodSpecBuilder);
            addBuilderMethodForFunctionalInterface(pkgName, invocationHandlerBuilder, methodElement, functionalInterfaceSpec);
            //nun alle Delegator Methods

            final String methodSimpleName = methodElement.getSimpleName().toString();
            String methodimpleNameUpper = methodSimpleName.substring(0, 1).toUpperCase() + methodSimpleName.substring(1);

            final ClassName bestGuess = ClassName.get(pkgName, functionalInterfaceSpec.get().name);
            final ParameterSpec parameterSpec = ParameterSpec.builder(bestGuess, "adapter", Modifier.FINAL).build();

            adapterBuilderTypeSpecBuilder
                .addMethod(MethodSpec
                    .methodBuilder("with" + methodimpleNameUpper)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(parameterSpec)
                    .addStatement("invocationHandler." + methodSimpleName + "(adapter)")
                    .addStatement("return this")
                    .returns(adapterBuilderClassname)
                    .build())
                .build();
          }
        }
      }

      //write InvocationHandler
      writeDefinedClass(pkgName, invocationHandlerBuilder);
      //write Builder
      writeDefinedClass(pkgName, adapterBuilderTypeSpecBuilder);


    }
    return true;
  }


  private TypeSpec.Builder createAdapterBuilderBuilder(TypeElement typeElement, ClassName typeElementClassName, ClassName adapterBuilderClassname, ClassName invocationHandlerClassname) {
    final TypeSpec.Builder adapterBuilderTypeSpecBuilder = createTypedTypeSpecBuilder(typeElement, AdapterBuilder.class, BUILDER_CLASSNAME_POST_FIX);

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


  private void addBuilderMethodForFunctionalInterface(String pkgName, TypeSpec.Builder invocationHandlerBuilder, ExecutableElement methodElement, Optional<TypeSpec> functionalInterfaceSpec) {
    functionalInterfaceSpec.ifPresent(f -> {
      final TypeSpec funcInterfaceSpec = functionalInterfaceSpec.get();

      final ClassName bestGuess = ClassName.get(pkgName, funcInterfaceSpec.name);
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

  private TypeSpec.Builder createTypedTypeSpecBuilder(TypeElement typeElement, Class class2Extend, String classnamePostFix) {
    // Get the full QualifiedTypeName
    final ClassName extendedInvocationHandlerClassName = ClassName.get(class2Extend);
    final ParameterizedTypeName typedExtendedInvocationHandler = ParameterizedTypeName.get(extendedInvocationHandlerClassName, TypeName.get(typeElement.asType()));

    return TypeSpec
        .classBuilder(typeElement.getSimpleName().toString() + classnamePostFix)
        .superclass(typedExtendedInvocationHandler)
        .addModifiers(Modifier.PUBLIC);
  }

  private MethodSpec.Builder createMethodSpecBuilder(ExecutableElement methodElement, TypeMirror returnType, List<ParameterSpec> parameterSpecs) {
    final MethodSpec.Builder methodSpecBuilder = MethodSpec
        .methodBuilder(methodElement.getSimpleName().toString())
        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
        .returns(TypeName.get(returnType));

    for (ParameterSpec parameterSpec : parameterSpecs) {
      methodSpecBuilder.addParameter(parameterSpec);
    }
    return methodSpecBuilder;
  }


  /**
   * @param typeElement
   * @param methodElement
   * @param methodSpecBuilder
   *
   * @return the functional Interface spec
   */
  private Optional<TypeSpec> writeFunctionalInterface(TypeElement typeElement, ExecutableElement methodElement, MethodSpec.Builder methodSpecBuilder) {
    final String methodNameRaw = methodElement.getSimpleName().toString();
    final String firstCharUpper = (methodNameRaw.charAt(0) + "").toUpperCase();

    final String finalMethodName = firstCharUpper + methodNameRaw.substring(1);


    final TypeSpec functionalInterface = TypeSpec
        .interfaceBuilder(typeElement.getSimpleName().toString() + "Method" + finalMethodName)
        .addAnnotation(FunctionalInterface.class)
        .addMethod(methodSpecBuilder.build())
        .addModifiers(Modifier.PUBLIC)
        .build();

    final Element enclosingElement = typeElement.getEnclosingElement();
    final String packageName = enclosingElement.toString();
    final JavaFile javaFile = JavaFile.builder(packageName, functionalInterface).skipJavaLangImports(true).build();
    final String className = typeElement.getQualifiedName().toString() + "Method" + finalMethodName;
    try {
      JavaFileObject jfo = filer.createSourceFile(className);
      Writer writer = jfo.openWriter();
      javaFile.writeTo(writer);
      writer.flush();
      return Optional.of(functionalInterface);

    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("e = " + e);
    }
    return Optional.empty();
  }

  private void writeDefinedClass(String pkgName, TypeSpec.Builder typeSpecBuilder) {
    final TypeSpec typeSpec = typeSpecBuilder.build();
    final JavaFile javaFile = JavaFile.builder(pkgName, typeSpec).skipJavaLangImports(true).build();
    final String className = javaFile.packageName + "." + javaFile.typeSpec.name;
    try {
      JavaFileObject jfo = filer.createSourceFile(className);
      Writer writer = jfo.openWriter();
      javaFile.writeTo(writer);
      writer.flush();
//        return Optional.of(functionalInterface);

    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("e = " + e);
    }
  }


  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  public void error(Element e, String msg, Object... args) {
    messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
  }

}
