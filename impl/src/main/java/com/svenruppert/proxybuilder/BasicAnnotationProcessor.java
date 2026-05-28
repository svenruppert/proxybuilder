/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the EUPL-1.2.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package com.svenruppert.proxybuilder;


import com.squareup.javapoet.*;
import com.squareup.javapoet.TypeSpec.Builder;
import com.svenruppert.dependencies.core.logger.HasLogger;
import com.svenruppert.proxybuilder.annotations.DelegatesTo;
import com.svenruppert.proxybuilder.annotations.GeneratedByProxyBuilder;
import com.svenruppert.proxybuilder.annotations.Internal;
import com.svenruppert.proxybuilder.annotations.ProxyBuilderOptions;
import com.svenruppert.proxybuilder.annotations.ProxyBuilderVersion;
import com.svenruppert.proxybuilder.annotations.ProxyEntry;
import com.svenruppert.proxybuilder.annotations.ProxyName;
import com.svenruppert.proxybuilder.annotations.SkipProxy;


import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
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

@SupportedOptions({
    BasicAnnotationProcessor.OPTION_VERBOSE,
    BasicAnnotationProcessor.OPTION_GENERATED_CLASS_SUFFIX,
    BasicAnnotationProcessor.OPTION_FAIL_ON_STATIC_METHODS,
    BasicAnnotationProcessor.OPTION_SUPPRESS_DELEGATES_TO
})
public abstract class BasicAnnotationProcessor<T extends Annotation> extends AbstractProcessor implements HasLogger {


  public static final String OPTION_VERBOSE = "proxybuilder.verbose";
  public static final String OPTION_GENERATED_CLASS_SUFFIX = "proxybuilder.suffix";
  public static final String OPTION_FAIL_ON_STATIC_METHODS = "proxybuilder.failOnStaticMethods";
  public static final String OPTION_SUPPRESS_DELEGATES_TO = "proxybuilder.suppressDelegatesTo";
  public static final String METHOD_NAME_FINALIZE = "finalize";
  public static final String METHOD_NAME_TO_STRING = "toString";
  public static final String METHOD_NAME_HASH_CODE = "hashCode";
  public static final String METHOD_NAME_EQUALS = "equals";
  protected static final String CLASS_NAME = "CLASS_NAME";
  protected static final String DELEGATOR_FIELD_NAME = "delegator";
  private static final ResolvedOptions EMPTY_OPTIONS =
      new ResolvedOptions(null, null, Set.of(), null);
  private final Set<MethodIdentifier> executableElementSet = new HashSet<>();
  protected Filer filer;
  protected Elements elementUtils;
  protected Types typeUtils;
  protected Messager messager;
  protected Builder typeSpecBuilderForTargetClass;
  protected TypeElement actualProcessedTypeElement;
  private ResolvedOptions currentOptions = EMPTY_OPTIONS;

  @Internal(reason = "Per-type resolved options cache; populated from @ProxyBuilderOptions / @ProxyName.")
  private record ResolvedOptions(String suffix,
                                 Boolean failOnStatic,
                                 Set<String> excludeMethodNames,
                                 String nameOverride) {
  }

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
        .filter(this::isValidAnnotatedElement)
        .map(TypeElement.class::cast)
        .filter(this::validateTargetType)
        .forEach(typeElement -> {
          applyProxyBuilderOptions(typeElement);
          try {
            actualProcessedTypeElement = typeElement;
            final TypeName interface2Implement = TypeName.get(typeElement.asType());
            createTypeSpecBuilderForTargetClass(typeElement, interface2Implement);

            addClassLevelSpecs(typeElement, roundEnv);
            logger().debug("============================================================");
            executableElementSet.clear();
            defineNewGeneratedMethod(typeElement);
            defineGeneratedConstructorMethod(typeElement, typeSpecBuilderForTargetClass);
            executableElementSet.clear();
            logger().debug("============================================================");

            writeDefinedClass(pkgName(typeElement), typeSpecBuilderForTargetClass);
            typeSpecBuilderForTargetClass = null;
            actualProcessedTypeElement = null;
          } finally {
            resetProxyBuilderOptions();
          }
        });

    return true;
  }

  private void applyProxyBuilderOptions(final TypeElement typeElement) {
    final ProxyBuilderOptions options = typeElement.getAnnotation(ProxyBuilderOptions.class);
    final ProxyName nameOverride = typeElement.getAnnotation(ProxyName.class);
    String suffix = null;
    Boolean failOnStatic = null;
    Set<String> excludes = Set.of();
    if (options != null) {
      if (!options.suffix().isEmpty()) {
        suffix = options.suffix();
      }
      switch (options.failOnStaticMethods()) {
        case TRUE -> failOnStatic = Boolean.TRUE;
        case FALSE -> failOnStatic = Boolean.FALSE;
        case DEFAULT -> {
        }
        default -> {
        }
      }
      if (options.excludeMethodNames().length > 0) {
        excludes = Set.of(options.excludeMethodNames());
      }
    }
    final String name = (nameOverride != null) ? nameOverride.value() : null;
    currentOptions = new ResolvedOptions(suffix, failOnStatic, excludes, name);
  }

  private void resetProxyBuilderOptions() {
    currentOptions = EMPTY_OPTIONS;
  }

  public abstract Class<T> responsibleFor();

  private boolean isValidAnnotatedElement(final Element element) {
    if (element instanceof TypeElement) {
      return true;
    }
    error(element, "@%s can only be applied to classes or interfaces", responsibleFor().getSimpleName());
    return false;
  }

  protected boolean validateTargetType(final TypeElement typeElement) {
    applyProxyBuilderOptions(typeElement);
    boolean valid = true;
    if (typeElement.getModifiers().contains(Modifier.FINAL)) {
      error(typeElement, "@%s cannot be applied to final class %s",
            responsibleFor().getSimpleName(), typeElement.getQualifiedName());
      valid = false;
    }
    return validateMethodsForProxyGeneration(typeElement) && valid;
  }

  protected boolean validateMethodsForProxyGeneration(final TypeElement typeElement) {
    boolean valid = true;
    for (final Element enclosedElement : typeElement.getEnclosedElements()) {
      if (enclosedElement.getKind() == ElementKind.METHOD) {
        valid = validateMethodForProxyGeneration((ExecutableElement) enclosedElement) && valid;
      }
    }
    final TypeMirror superclass = typeElement.getSuperclass();
    if (superclass != null && !"none".equals(superclass.toString())) {
      final Element superclassElement = typeUtils.asElement(superclass);
      if (superclassElement instanceof TypeElement) {
        valid = validateMethodsForProxyGeneration((TypeElement) superclassElement) && valid;
      }
    }
    for (final TypeMirror interfaceType : typeElement.getInterfaces()) {
      final Element interfaceElement = typeUtils.asElement(interfaceType);
      if (interfaceElement instanceof TypeElement) {
        valid = validateMethodsForProxyGeneration((TypeElement) interfaceElement) && valid;
      }
    }
    return valid;
  }

  protected boolean validateMethodForProxyGeneration(final ExecutableElement methodElement) {
    if (isObjectMethod(methodElement)) {
      return true;
    }

    boolean valid = true;
    final Set<Modifier> modifiers = methodElement.getModifiers();
    if (modifiers.contains(Modifier.FINAL)) {
      error(methodElement, "@%s cannot proxy final method %s",
            responsibleFor().getSimpleName(), methodElement.getSimpleName());
      valid = false;
    }
    if (modifiers.contains(Modifier.PRIVATE)) {
      error(methodElement, "@%s cannot proxy private method %s",
            responsibleFor().getSimpleName(), methodElement.getSimpleName());
      valid = false;
    }
    if (modifiers.contains(Modifier.STATIC)) {
      final String message = String.format("@%s cannot proxy static method %s",
                                           responsibleFor().getSimpleName(), methodElement.getSimpleName());
      if (failOnStaticMethods()) {
        error(methodElement, message);
        valid = false;
      } else {
        warning(methodElement, message);
      }
    }
    return valid;
  }


  private void defineGeneratedConstructorMethod(final TypeElement typeElement, final Builder forTargetClass) {
    logger().debug("defineGeneratedConstructorMethod.typeElement = {}", typeElement.getQualifiedName());
    // create the constructors
    typeElement
        .getEnclosedElements()
        .stream()
        .filter(e -> e.getKind() == ElementKind.CONSTRUCTOR)
        .map(methodElement -> (ExecutableElement) methodElement) //cast only
        .filter(methodElement -> !methodElement.getModifiers().contains(Modifier.PRIVATE))
        .filter(methodElement -> !methodElement.getModifiers().contains(Modifier.FINAL))
        .filter(methodElement -> !methodElement.isDefault())
        .filter(this::keepAfterSkipProxy)
        .filter(methodElement -> !executableElementSet.contains(MethodIdentifier.of(methodElement)))
        .forEach(
            methodElement -> {
              executableElementSet.add(MethodIdentifier.of(methodElement));
              final Set<Modifier> reducedMethodModifiers = mutableModifierSet(methodElement.getModifiers());
              reducedMethodModifiers.remove(Modifier.ABSTRACT);
              reducedMethodModifiers.remove(Modifier.NATIVE);
              final List<ParameterSpec> parameterSpecList = defineParamsForMethod(methodElement);

              final MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                  .addModifiers(filterConstructorModifiers(reducedMethodModifiers))
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
    logger().debug("defineNewGeneratedMethod.typeElement = {}", typeElement.getQualifiedName());

    typeElement
        .getEnclosedElements()
        .stream()
        .filter(e -> e.getKind() == ElementKind.METHOD)
        .map(methodElement -> (ExecutableElement) methodElement) //cast only
        .filter(methodElement -> methodElement.getModifiers().contains(Modifier.PUBLIC))
        .filter(methodElement -> !methodElement.getModifiers().contains(Modifier.STATIC))
        .filter(methodElement -> !methodElement.getModifiers().contains(Modifier.FINAL))
        .filter(methodElement -> !isObjectMethod(methodElement))
        .filter(this::keepAfterSkipProxy)
        .filter(this::keepAfterExcludeMethodNames)
        .filter(methodElement -> !executableElementSet.contains(MethodIdentifier.of(methodElement)))
        .forEach(
            methodElement -> {
              executableElementSet.add(MethodIdentifier.of(methodElement));
              noteProxyEntry(methodElement);
              final String methodName2Delegate = methodElement.getSimpleName().toString();
              final CodeBlock codeBlock = defineMethodImplementations(methodElement, methodName2Delegate, actualProcessedTypeElement);
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
        error(typeElement, "@%s cannot be applied to element kind %s",
              responsibleFor().getSimpleName(), typeElement.getKind());
        return typeSpecBuilderForTargetClass;
      }
      typeElement.getModifiers()
          .stream()
          .filter(m -> !m.equals(Modifier.ABSTRACT))
          .forEach(m -> typeSpecBuilderForTargetClass.addModifiers(m));
    }


    typeSpecBuilderForTargetClass.addAnnotation(createAnnotationSpecGenerated(typeElement));
    return typeSpecBuilderForTargetClass;
  }

  @Internal(reason = "Stream filter for @SkipProxy.")
  private boolean keepAfterSkipProxy(final ExecutableElement methodElement) {
    if (methodElement.getAnnotation(SkipProxy.class) == null) {
      return true;
    }
    if (verbose()) {
      final String reason = methodElement.getAnnotation(SkipProxy.class).value();
      note(methodElement, "skipping (@SkipProxy): %s",
           reason.isEmpty() ? methodElement.getSimpleName() : reason);
    }
    return false;
  }

  @Internal(reason = "Stream filter for @ProxyBuilderOptions.excludeMethodNames.")
  private boolean keepAfterExcludeMethodNames(final ExecutableElement methodElement) {
    final Set<String> excludes = currentOptions.excludeMethodNames();
    if (excludes.isEmpty()) {
      return true;
    }
    if (!excludes.contains(methodElement.getSimpleName().toString())) {
      return true;
    }
    if (verbose()) {
      note(methodElement, "skipping (excludeMethodNames): %s", methodElement.getSimpleName());
    }
    return false;
  }

  @Internal(reason = "@ProxyEntry NOTE-diagnostic emission.")
  private void noteProxyEntry(final ExecutableElement methodElement) {
    if (methodElement.getAnnotation(ProxyEntry.class) != null) {
      note(methodElement, "@ProxyEntry is experimental, no-op in %s", ProxyBuilderVersion.VERSION);
    }
  }

  protected abstract void addClassLevelSpecs(final TypeElement typeElement, final RoundEnvironment roundEnv);

  protected abstract CodeBlock defineMethodImplementation(final ExecutableElement methodElement, final String methodName2Delegate, final TypeElement typeElementTargetClass);

  protected List<CodeBlock> defineMethodImplementationBlocks(final ExecutableElement methodElement,
                                                             final String methodName2Delegate,
                                                             final TypeElement typeElementTargetClass) {
    return List.of(defineMethodImplementation(methodElement, methodName2Delegate, typeElementTargetClass));
  }

  protected List<CodeBlock> beforeDelegation(final ExecutableElement methodElement,
                                             final String methodName2Delegate,
                                             final TypeElement typeElementTargetClass) {
    return List.of();
  }

  protected Optional<CodeBlock> aroundDelegation(final ExecutableElement methodElement,
                                                 final String methodName2Delegate,
                                                 final TypeElement typeElementTargetClass) {
    final CodeBlock.Builder builder = CodeBlock.builder();
    defineMethodImplementationBlocks(methodElement, methodName2Delegate, typeElementTargetClass)
        .stream()
        .filter(Objects::nonNull)
        .forEach(builder::add);
    return Optional.of(builder.build());
  }

  protected List<CodeBlock> afterDelegation(final ExecutableElement methodElement,
                                            final String methodName2Delegate,
                                            final TypeElement typeElementTargetClass) {
    return List.of();
  }

  private CodeBlock defineMethodImplementations(final ExecutableElement methodElement,
                                                final String methodName2Delegate,
                                                final TypeElement typeElementTargetClass) {
    final CodeBlock.Builder builder = CodeBlock.builder();
    beforeDelegation(methodElement, methodName2Delegate, typeElementTargetClass)
        .stream()
        .filter(Objects::nonNull)
        .forEach(builder::add);
    aroundDelegation(methodElement, methodName2Delegate, typeElementTargetClass)
        .ifPresent(builder::add);
    afterDelegation(methodElement, methodName2Delegate, typeElementTargetClass)
        .stream()
        .filter(Objects::nonNull)
        .forEach(builder::add);
    return builder.build();
  }

  protected MethodSpec defineDelegatorMethod(final ExecutableElement methodElement, final String methodName2Delegate, final CodeBlock codeBlock) {
    logger().debug("defineDelegatorMethod.methodElement = {}", methodElement);
    final MethodSpec.Builder methodSpecBuilder = defineDelegatorMethodSpec(methodElement, methodName2Delegate, codeBlock);
    if (!suppressDelegatesTo()) {
      methodSpecBuilder.addAnnotation(createDelegatesToAnnotation(methodElement));
    }
    return methodSpecBuilder.build();
  }

  protected MethodSpec.Builder defineDelegatorMethodSpec(final ExecutableElement methodElement, final String methodName2Delegate, final CodeBlock codeBlock) {
    final Set<Modifier> reducedMethodModifiers = mutableModifierSet(methodElement.getModifiers());
    reducedMethodModifiers.remove(Modifier.ABSTRACT);
    reducedMethodModifiers.remove(Modifier.NATIVE);
    reducedMethodModifiers.remove(Modifier.DEFAULT);

    final MethodSpec.Builder methodBuilder = methodBuilder(methodName2Delegate);
    final TypeMirror returnType = methodElement.getReturnType();
    addReturnTypeVariables(methodBuilder, returnType);

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

  @Internal(reason = "Generic-return-type plumbing for the generated method spec.")
  private void addReturnTypeVariables(final MethodSpec.Builder methodBuilder, final TypeMirror returnType) {
    if (!needsTypeVariableDeclaration(returnType)) {
      return;
    }

    final List<TypeName> bounds = nonObjectDirectSupertypes(returnType);
    final TypeName typeVariableName = TypeName.get(returnType);
    if (bounds.isEmpty()) {
      methodBuilder.addTypeVariable(TypeVariableName.get(typeVariableName.toString()));
    } else if (isTypeVariableLike(returnType)) {
      methodBuilder.addTypeVariable(TypeVariableName.get(typeVariableName.toString(), bounds.toArray(new TypeName[0])));
    }
  }

  @Internal(reason = "Part of the addReturnTypeVariables family.")
  private boolean needsTypeVariableDeclaration(final TypeMirror returnType) {
    return !returnType.getKind().isPrimitive()
        && returnType.getKind() != TypeKind.VOID
        && !(returnType instanceof DeclaredType);
  }

  @Internal(reason = "Part of the addReturnTypeVariables family.")
  private List<TypeName> nonObjectDirectSupertypes(final TypeMirror returnType) {
    return typeUtils.directSupertypes(returnType)
        .stream()
        .filter(typeMirror -> !"java.lang.Object".equals(typeMirror.toString()))
        .map(TypeName::get)
        .collect(toList());
  }

  @Internal(reason = "Part of the addReturnTypeVariables family.")
  private boolean isTypeVariableLike(final TypeMirror returnType) {
    final Element element = typeUtils.asElement(returnType);
    return element == null
        || (element.getKind() != ElementKind.CLASS && element.getKind() != ElementKind.INTERFACE);
  }

  private Set<Modifier> mutableModifierSet(final Set<Modifier> modifiers) {
    final Set<Modifier> result = EnumSet.noneOf(Modifier.class);
    result.addAll(modifiers);
    return result;
  }

  protected Set<Modifier> filterConstructorModifiers(final Set<Modifier> modifiers) {
    return modifiers;
  }

  protected String targetClassNameSimpleForGeneratedClass(final TypeElement typeElement) {
    final String original = className(typeElement);
    if (currentOptions.nameOverride() != null) {
      final String expanded = currentOptions.nameOverride().replace("{Original}", original);
      return ClassName.get(pkgName(typeElement), expanded).simpleName();
    }
    return ClassName.get(pkgName(typeElement), original + generatedClassSuffix(typeElement)).simpleName();
  }

  protected String targetClassNameSimpleForSourceClass(final TypeElement typeElement) {
    return ClassName.get(pkgName(typeElement), className(typeElement)).simpleName();
  }

  protected String generatedClassSuffix(final TypeElement typeElement) {
    if (currentOptions.suffix() != null) {
      return currentOptions.suffix();
    }
    return processorOption(OPTION_GENERATED_CLASS_SUFFIX)
        .filter(option -> !option.isBlank())
        .orElseGet(() -> responsibleFor().getSimpleName());
  }

  protected boolean verbose() {
    return processorOption(OPTION_VERBOSE)
        .map(Boolean::parseBoolean)
        .orElse(false);
  }

  protected boolean failOnStaticMethods() {
    if (currentOptions.failOnStatic() != null) {
      return currentOptions.failOnStatic();
    }
    return processorOption(OPTION_FAIL_ON_STATIC_METHODS)
        .map(Boolean::parseBoolean)
        .orElse(true);
  }

  protected boolean suppressDelegatesTo() {
    return processorOption(OPTION_SUPPRESS_DELEGATES_TO)
        .map(Boolean::parseBoolean)
        .orElse(false);
  }

  protected Optional<String> processorOption(final String name) {
    return Optional.ofNullable(processingEnv.getOptions().get(name));
  }

  protected Optional<TypeSpec> writeFunctionalInterface(final TypeElement typeElementTargetClass,
                                                        final ExecutableElement methodElement) {

    final MethodSpec.Builder methodSpecBuilder = defineDelegatorMethodSpec(methodElement, methodElement.getSimpleName().toString(), null);
    methodSpecBuilder.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

    final String methodNameRaw = methodElement.getSimpleName().toString();
    final String firstCharUpper = (methodNameRaw.charAt(0) + "").toUpperCase();
    final List<TypeMirror> argumentTypeListFromMethod = extractArgumentTypeListFromMethod(methodElement);

    final String methodNamePostFix = (argumentTypeListFromMethod.isEmpty()) ? "" : "" + argumentTypeListFromMethod
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
            logger().debug("writeFunctionalInterface t = {}", t);
            final Element element = typeUtils.asElement(t);
            final ClassName className = ClassName.get((TypeElement) element);
            final String[] split = className.simpleName().split("\\.");
            postFix = split[split.length - 1];
          }
          return postFix.substring(0, 1).toUpperCase() + postFix.substring(1);
        }).collect(Collectors.joining(""));


    final String finalMethodName = firstCharUpper + methodNameRaw.substring(1) + methodNamePostFix;

    final Builder functionalInterfaceTypeSpecBuilder = TypeSpec
        .interfaceBuilder(typeElementTargetClass.getSimpleName().toString() + "Method" + finalMethodName)
        .addAnnotation(createAnnotationSpecGenerated(typeElementTargetClass))
        .addMethod(methodSpecBuilder.build())
        .addModifiers(Modifier.PUBLIC);

    if (isObjectMethod(methodElement)) {
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


  private AnnotationSpec createAnnotationSpecGenerated(final TypeElement source) {
    return AnnotationSpec.builder(GeneratedByProxyBuilder.class)
        .addMember("processor", "$S", this.getClass().getName())
        .addMember("sourceClass", "$S", elementUtils.getBinaryName(source).toString())
        .addMember("proxyBuilderVersion", "$S", ProxyBuilderVersion.VERSION)
        .addMember("date", "$S", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
        .addMember("comments", "$S", "www.proxybuilder.org")
        .build();
  }

  private AnnotationSpec createDelegatesToAnnotation(final ExecutableElement methodElement) {
    final TypeElement decl = (TypeElement) methodElement.getEnclosingElement();
    final StringBuilder sb = new StringBuilder()
        .append(elementUtils.getBinaryName(decl).toString())
        .append('#')
        .append(methodElement.getSimpleName())
        .append('(');
    boolean first = true;
    for (final var param : methodElement.getParameters()) {
      if (!first) {
        sb.append(',');
      }
      sb.append(TypeName.get(param.asType()).toString());
      first = false;
    }
    sb.append(')');
    return AnnotationSpec.builder(DelegatesTo.class)
        .addMember("value", "$S", sb.toString())
        .build();
  }


  protected void addStaticImports(JavaFile.Builder builder) {
  }

  protected Optional<TypeSpec> writeDefinedClass(String pkgName, Builder typeSpecBuilder) {

    logger().debug("typeSpecBuilder = {}", typeSpecBuilder);


    final TypeSpec typeSpec = typeSpecBuilder.build();
    final JavaFile.Builder javaFileBuilder = JavaFile
        .builder(pkgName, typeSpec)
        .skipJavaLangImports(true);

    addStaticImports(javaFileBuilder);

    final JavaFile javaFile = javaFileBuilder.build();

    final String className = javaFile.packageName + "." + javaFile.typeSpec.name;
    try {
      JavaFileObject jfo = filer.createSourceFile(className);
      try (Writer writer = jfo.openWriter()) {
        javaFile.writeTo(writer);
      }
    } catch (FilerException e) {
      return Optional.of(typeSpec);
    } catch (IOException e) {
      logger().warn("Could not write generated source file {}", className, e);
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
    logger().debug("defineMethodReferenzePoint.methodElement = {}", methodElement);
    // static -> ClassName non-static DELEGATOR_FIELD_NAME
    if (methodElement.getModifiers().contains(Modifier.STATIC)) {
      logger().debug("defineMethodReferenzePoint.isSTATIC");
      boolean isClass = methodElement.getEnclosingElement().getKind().equals(ElementKind.CLASS);

      logger().debug("defineMethodReferenzePoint.isClass - {}", isClass);
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

  protected List<? extends AnnotationMirror> annotationsOn(final ExecutableElement methodElement) {
    return methodElement.getAnnotationMirrors();
  }

  protected List<AnnotationMirror> annotationsOn(final ExecutableElement methodElement, final Set<String> qualifiedNames) {
    return methodElement.getAnnotationMirrors()
        .stream()
        .filter(annotationMirror -> qualifiedNames.contains(annotationMirror.getAnnotationType().toString()))
        .collect(toList());
  }

  protected boolean hasAnnotation(final ExecutableElement methodElement, final String qualifiedName) {
    return methodElement.getAnnotationMirrors()
        .stream()
        .anyMatch(annotationMirror -> qualifiedName.equals(annotationMirror.getAnnotationType().toString()));
  }

  protected boolean isObjectMethod(final ExecutableElement methodElement) {
    final Element enclosingElement = methodElement.getEnclosingElement();
    if (enclosingElement instanceof TypeElement
        && "java.lang.Object".equals(((TypeElement) enclosingElement).getQualifiedName().toString())) {
      return true;
    }

    final String name = methodElement.getSimpleName().toString();
    final int parameterCount = methodElement.getParameters().size();
    return (METHOD_NAME_HASH_CODE.equals(name) && parameterCount == 0)
        || (METHOD_NAME_TO_STRING.equals(name) && parameterCount == 0)
        || (METHOD_NAME_FINALIZE.equals(name) && parameterCount == 0)
        || (METHOD_NAME_EQUALS.equals(name)
        && parameterCount == 1
        && "java.lang.Object".equals(methodElement.getParameters().get(0).asType().toString()));
  }

  protected void error(final Element element, final String message, final Object... args) {
    messager.printMessage(Diagnostic.Kind.ERROR, String.format(message, args), element);
  }

  protected void warning(final Element element, final String message, final Object... args) {
    messager.printMessage(Diagnostic.Kind.WARNING, String.format(message, args), element);
  }

  protected void note(final Element element, final String message, final Object... args) {
    messager.printMessage(Diagnostic.Kind.NOTE, String.format(message, args), element);
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

}
