package org.rapidpm.proxybuilder.staticgenerated.processors;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.squareup.javapoet.*;
import org.rapidpm.proxybuilder.core.metrics.RapidPMMetricsRegistry;
import org.rapidpm.proxybuilder.staticgenerated.annotations.IsGeneratedProxy;
import org.rapidpm.proxybuilder.staticgenerated.annotations.IsMetricsProxy;
import org.rapidpm.proxybuilder.staticgenerated.annotations.StaticMetricsProxy;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;


/**
 * Created by svenruppert on 09.12.15.
 *
 * final MetricRegistry metrics = MetricsRegistry.getInstance().getMetrics();
 *
 * final long start = System.nanoTime();
 * //        final Object invoke = method.invoke(original, args);
 * final Object invoke = DynamicProxyBuilder.invoke(original, method, args);
 * final long stop = System.nanoTime();
 * Histogram methodCalls = metrics.histogram(clazz.getSimpleName() + "." + method.getName());
 * methodCalls.update((stop - start));
 * return invoke;
 * }
 * };
 */
public class StaticMetricsProxyAnnotationProcessor extends BasicStaticProxyAnnotationProcessor<StaticMetricsProxy> {
  @Override
  protected CodeBlock defineMethodImplementation(final ExecutableElement methodElement, final String methodName2Delegate) {

    final TypeMirror returnType = methodElement.getReturnType();
    final List<ParameterSpec> parameterSpecList = defineParamsForMethod(methodElement);

    final MethodSpec.Builder methodSpecBuilder = MethodSpec
        .methodBuilder(methodElement.getSimpleName().toString())
        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
        .returns(TypeName.get(returnType));

    parameterSpecList.forEach(methodSpecBuilder::addParameter);
    final CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();

    codeBlockBuilder
        .addStatement("final long start = System.nanoTime();")
        .addStatement("$T result = " + "delegator" + "." + delegatorMethodCall(methodElement, methodName2Delegate), returnType)
        .addStatement("final long stop = System.nanoTime();")
        .addStatement("final $T methodCalls = metrics.histogram(this.getClass().getSimpleName() + \".\" + \"" + methodElement.getSimpleName() + "\");", Histogram.class)
        .addStatement("return result");

    return codeBlockBuilder.build();
  }


  protected FieldSpec defineMetricsField() {
    final ClassName metricsClassname = ClassName.get(MetricRegistry.class);
    return FieldSpec
        .builder(metricsClassname, "metrics")
        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
        .initializer(

            CodeBlock.builder()
                .addStatement("$T.getInstance().getMetrics()", RapidPMMetricsRegistry.class)
                .build())

        .build();
  }


  @Override
  protected void addClassLevelSpecs(final TypeElement typeElement, final RoundEnvironment roundEnv) {
    final TypeName targetTypeName = TypeName.get(typeElement.asType());
    final TypeSpec.Builder specBuilderForTargetClass = createTypeSpecBuilderForTargetClass(typeElement, targetTypeName);

    specBuilderForTargetClass.addAnnotation(IsGeneratedProxy.class);
    specBuilderForTargetClass.addAnnotation(IsMetricsProxy.class);


    final FieldSpec delegatorFieldSpec = defineDelegatorField(typeElement);
    specBuilderForTargetClass.addField(delegatorFieldSpec);

    final FieldSpec metricsField = defineMetricsField();
    specBuilderForTargetClass.addField(metricsField);

    specBuilderForTargetClass
        .addMethod(MethodSpec.methodBuilder("with" + typeElement.getSimpleName())
            .addModifiers(Modifier.PUBLIC)
            .addParameter(targetTypeName, "delegator", Modifier.FINAL)
            .addCode(CodeBlock.builder()
                .addStatement("this." + "delegator" + "=" + "delegator")
                .addStatement("return this").build())
            .returns(ClassName.get(pkgName(typeElement), targetClassNameSimple(typeElement)))
            .build());

  }


  @Override
  public Class<StaticMetricsProxy> responsibleFor() {
    return StaticMetricsProxy.class;
  }

}
