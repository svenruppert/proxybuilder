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
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;


/**
 * Created by svenruppert on 09.12.15.
 *
 * final MetricRegistry metrics = MetricsRegistry.getInstance().getMetrics();
 */
public class StaticMetricsProxyAnnotationProcessor extends BasicStaticProxyAnnotationProcessor<StaticMetricsProxy> {

  @Override
  protected void addClassLevelSpecs(final TypeElement typeElement, final RoundEnvironment roundEnv) {
    final TypeName targetTypeName = TypeName.get(typeElement.asType());
    typeSpecBuilderForTargetClass.addAnnotation(IsGeneratedProxy.class);
    typeSpecBuilderForTargetClass.addAnnotation(IsMetricsProxy.class);

    typeSpecBuilderForTargetClass.addField(defineDelegatorField(typeElement));
    typeSpecBuilderForTargetClass.addField(defineMetricsField());
    typeSpecBuilderForTargetClass.addField(defineSimpleClassNameField(typeElement));

    typeSpecBuilderForTargetClass
        .addMethod(MethodSpec.methodBuilder("with" + typeElement.getSimpleName())
            .addModifiers(Modifier.PUBLIC)
            .addParameter(targetTypeName, DELEGATOR_FIELD_NAME, Modifier.FINAL)
            .addCode(CodeBlock.builder()
                .addStatement("this." + DELEGATOR_FIELD_NAME + " = " + DELEGATOR_FIELD_NAME)
                .addStatement("return this").build())
            .returns(ClassName.get(pkgName(typeElement), targetClassNameSimple(typeElement)))
            .build());

  }

  @Override
  protected CodeBlock defineMethodImplementation(final ExecutableElement methodElement, final String methodName2Delegate) {
    final TypeMirror returnType = methodElement.getReturnType();
    final CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
    if (returnType.getKind() == TypeKind.VOID) {
      codeBlockBuilder
          .addStatement("final long start = System.nanoTime()")
          .addStatement(DELEGATOR_FIELD_NAME + "." + delegatorMethodCall(methodElement, methodName2Delegate))
          .addStatement("final long stop = System.nanoTime()")
          .addStatement("final $T methodCalls = metrics.histogram(" + SIMPLE_CLASS_NAME + " + \".\" + \"" + methodElement.getSimpleName() + "\")", Histogram.class)
          .addStatement("methodCalls.update(stop - start)");
    } else {
      codeBlockBuilder
          .addStatement("final long start = System.nanoTime()")
          .addStatement("$T result = " + DELEGATOR_FIELD_NAME + "." + delegatorMethodCall(methodElement, methodName2Delegate), returnType)
          .addStatement("final long stop = System.nanoTime()")
          .addStatement("final $T methodCalls = metrics.histogram(" + SIMPLE_CLASS_NAME + " + \".\" + \"" + methodElement.getSimpleName() + "\")", Histogram.class)
          .addStatement("methodCalls.update(stop - start)")
          .addStatement("return result");
    }
    return codeBlockBuilder.build();
  }

  @Override
  public Class<StaticMetricsProxy> responsibleFor() {
    return StaticMetricsProxy.class;
  }

  protected FieldSpec defineMetricsField() {
    final ClassName metricsClassname = ClassName.get(MetricRegistry.class);
    return FieldSpec
        .builder(metricsClassname, "metrics")
        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
        .initializer("$T.getInstance().getMetrics()", RapidPMMetricsRegistry.class)
        .build();
  }

}
