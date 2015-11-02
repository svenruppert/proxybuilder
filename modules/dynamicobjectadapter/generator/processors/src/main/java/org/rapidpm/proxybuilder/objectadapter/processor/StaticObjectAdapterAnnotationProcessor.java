package org.rapidpm.proxybuilder.objectadapter.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import org.rapidpm.proxybuilder.objectadapter.annotations.IsObjectAdapter;
import org.rapidpm.proxybuilder.objectadapter.annotations.staticobjectadapter.StaticObjectAdapter;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created by svenruppert on 24.10.15.
 */
@AutoService(Processor.class)
public class StaticObjectAdapterAnnotationProcessor extends BasicObjectAdapterAnnotationProcessor<StaticObjectAdapter> {


  @Override
  public Class<StaticObjectAdapter> responsibleFor() {
    return StaticObjectAdapter.class;
  }

  @Override
  protected CodeBlock defineMethodImplementation(final ExecutableElement methodElement, final String methodName2Delegate) {

    final TypeElement typeElement = (TypeElement) methodElement.getEnclosingElement();
    final TypeName interface2Implement = TypeName.get(typeElement.asType());

    final TypeSpec.Builder specBuilderForTargetClass = createTypeSpecBuilderForTargetClass(typeElement, interface2Implement);

    final TypeMirror returnType = methodElement.getReturnType();
    final List<ParameterSpec> parameterSpecList = defineParamsForMethod(methodElement);

    final MethodSpec.Builder methodSpecBuilder = MethodSpec
        .methodBuilder(methodElement.getSimpleName().toString())
        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
        .returns(TypeName.get(returnType));

    parameterSpecList.forEach(methodSpecBuilder::addParameter);

    final CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();

    final Optional<TypeSpec> functionalInterfaceSpec = writeFunctionalInterface( methodElement, methodSpecBuilder);
    functionalInterfaceSpec.ifPresent(f->{
      final String adapterAttributeName = f.name.substring(0, 1).toLowerCase() + f.name.substring(1);
      final ClassName className = ClassName.get(pkgName(typeElement), f.name);
      final FieldSpec adapterAttributFieldSpec = FieldSpec.builder(className, adapterAttributeName, Modifier.PRIVATE).build();
      specBuilderForTargetClass.addField(adapterAttributFieldSpec);

      codeBlockBuilder
          .beginControlFlow("if("+ adapterAttributeName  +" != null)")
            .addStatement("return " + adapterAttributeName+"."+delegatorMethodCall(methodElement,methodName2Delegate))
          .endControlFlow();

      //add method to set adapter
      specBuilderForTargetClass
          .addMethod(MethodSpec.methodBuilder("with"+className.simpleName())
              .addModifiers(Modifier.PUBLIC)
              .addParameter(className,adapterAttributeName,Modifier.FINAL)
              .addCode(CodeBlock.builder()
                  .addStatement("this."+adapterAttributeName+"="+adapterAttributeName)
                  .addStatement("return this").build())
              .returns(ClassName.get(pkgName(typeElement),targetClassNameSimple(typeElement)))
      .build());
    });

    final String delegateStatement = delegatorStatementWithReturn(methodElement, methodName2Delegate);

    return codeBlockBuilder
        .addStatement(delegateStatement)
        .build();
  }

  @Override
  protected void addClassLevelSpecs(final TypeElement typeElement, final RoundEnvironment roundEnv) {
    final TypeName interface2Implement = TypeName.get(typeElement.asType());
    final TypeSpec.Builder specBuilderForTargetClass = createTypeSpecBuilderForTargetClass(typeElement, interface2Implement);

//    final ClassName implClassName = ClassName.get(pkgName(typeElement), className(typeElement)+ "Impl");
//    final ClassName factoryClassName = ClassName.get(pkgName(typeElement), FACTORY);
//    final ClassName strategyFactoryClassName = ClassName.get(pkgName(typeElement), STRATEGY_FACTORY);

    specBuilderForTargetClass.addAnnotation(IsObjectAdapter.class);

    //add Adapter fields

    //add Adapter addMethods
    //add delegator method

    final FieldSpec delegatorFieldSpec = defineDelegatorField(typeElement);
    specBuilderForTargetClass.addField(delegatorFieldSpec);

    specBuilderForTargetClass
        .addMethod(MethodSpec.methodBuilder("with"+typeElement.getSimpleName())
            .addModifiers(Modifier.PUBLIC)
            .addParameter(interface2Implement,"delegator",Modifier.FINAL)
            .addCode(CodeBlock.builder()
                .addStatement("this."+"delegator"+"="+"delegator")
                .addStatement("return this").build())
            .returns(ClassName.get(pkgName(typeElement),targetClassNameSimple(typeElement)))
            .build());



  }


  @Override
  public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    roundEnv
        .getElementsAnnotatedWith(responsibleFor())
        .stream()
        .filter(e -> e.getKind() == ElementKind.INTERFACE)
        .map(e -> (TypeElement) e)
        .forEach(typeElement -> {
          final TypeName interface2Implement = TypeName.get(typeElement.asType());
          final TypeSpec.Builder forTargetClass = createTypeSpecBuilderForTargetClass(typeElement, interface2Implement);

          addClassLevelSpecs(typeElement,roundEnv);

          //iter over the Methods from the Interface
          typeElement
              .getEnclosedElements()
              .stream()
              .filter(e -> e.getKind() == ElementKind.METHOD)
              .map(methodElement -> (ExecutableElement) methodElement) //cast only
              .filter(methodElement -> methodElement.getModifiers().contains(Modifier.PUBLIC))
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

}
