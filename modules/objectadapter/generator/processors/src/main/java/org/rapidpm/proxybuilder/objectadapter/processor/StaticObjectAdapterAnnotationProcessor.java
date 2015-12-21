package org.rapidpm.proxybuilder.objectadapter.processor;

import com.squareup.javapoet.*;
import org.rapidpm.proxybuilder.objectadapter.annotations.IsObjectAdapter;
import org.rapidpm.proxybuilder.objectadapter.annotations.staticobjectadapter.StaticObjectAdapter;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

/**
 * Created by svenruppert on 24.10.15.
 */
public class StaticObjectAdapterAnnotationProcessor extends BasicObjectAdapterAnnotationProcessor<StaticObjectAdapter> {


  @Override
  protected void addClassLevelSpecs(final TypeElement typeElement, final RoundEnvironment roundEnv) {
    final TypeName interface2Implement = TypeName.get(typeElement.asType());
    typeSpecBuilderForTargetClass.addAnnotation(IsObjectAdapter.class);

    final FieldSpec delegatorFieldSpec = defineDelegatorField(typeElement);
    typeSpecBuilderForTargetClass.addField(delegatorFieldSpec);

    typeSpecBuilderForTargetClass
        .addMethod(MethodSpec.methodBuilder("with" + typeElement.getSimpleName())
            .addModifiers(Modifier.PUBLIC)
            .addParameter(interface2Implement, "delegator", Modifier.FINAL)
            .addCode(CodeBlock.builder()
                .addStatement("this." + "delegator" + "=" + "delegator")
                .addStatement("return this").build())
            .returns(ClassName.get(pkgName(typeElement), targetClassNameSimple(typeElement)))
            .build());


  }

  @Override
  protected CodeBlock defineMethodImplementation(final ExecutableElement methodElement, final String methodName2Delegate) {

    final TypeElement typeElement = (TypeElement) methodElement.getEnclosingElement();
    final TypeMirror returnType = methodElement.getReturnType();
    final List<ParameterSpec> parameterSpecList = defineParamsForMethod(methodElement);

    final MethodSpec.Builder methodSpecBuilder = MethodSpec
        .methodBuilder(methodElement.getSimpleName().toString())
        .addModifiers(Modifier.PUBLIC)
        .returns(TypeName.get(returnType));

    parameterSpecList.forEach(methodSpecBuilder::addParameter);

    final CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();

    final Optional<TypeSpec> functionalInterfaceSpec = writeFunctionalInterface(methodElement, methodSpecBuilder);
    functionalInterfaceSpec.ifPresent(f -> {
      final String adapterAttributeName = f.name.substring(0, 1).toLowerCase() + f.name.substring(1);
      final ClassName className = ClassName.get(pkgName(typeElement), f.name);
      final FieldSpec adapterAttributFieldSpec = FieldSpec.builder(className, adapterAttributeName, Modifier.PRIVATE).build();
      typeSpecBuilderForTargetClass.addField(adapterAttributFieldSpec);

      codeBlockBuilder
          .beginControlFlow("if(" + adapterAttributeName + " != null)")
          .addStatement("return " + adapterAttributeName + "." + delegatorMethodCall(methodElement, methodName2Delegate))
          .endControlFlow();

      //add method to set adapter
      typeSpecBuilderForTargetClass
          .addMethod(MethodSpec.methodBuilder("with" + className.simpleName())
              .addModifiers(Modifier.PUBLIC)
              .addParameter(className, adapterAttributeName, Modifier.FINAL)
              .addCode(CodeBlock.builder()
                  .addStatement("this." + adapterAttributeName + "=" + adapterAttributeName)
                  .addStatement("return this").build())
              .returns(ClassName.get(pkgName(typeElement), targetClassNameSimple(typeElement)))
              .build());
    });

    final String delegateStatement = delegatorStatementWithReturn(methodElement, methodName2Delegate);

    return codeBlockBuilder
        .addStatement(delegateStatement)
        .build();
  }

  @Override
  public Class<StaticObjectAdapter> responsibleFor() {
    return StaticObjectAdapter.class;
  }

}
