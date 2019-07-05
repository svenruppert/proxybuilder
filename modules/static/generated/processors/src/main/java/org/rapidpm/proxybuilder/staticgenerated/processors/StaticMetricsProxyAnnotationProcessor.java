/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
  Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package org.rapidpm.proxybuilder.staticgenerated.processors;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.squareup.javapoet.*;
import com.squareup.javapoet.CodeBlock.Builder;
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

  public static final String WITH_DELEGATOR = "withDelegator";
  public static final String RAPID_PMMETRICS_REGISTRY = RapidPMMetricsRegistry.class.getSimpleName();

  @Override
  public Class<StaticMetricsProxy> responsibleFor() {
    return StaticMetricsProxy.class;
  }

  @Override
  protected void addClassLevelSpecs(final TypeElement typeElement, final RoundEnvironment roundEnv) {
    final TypeName targetTypeName = TypeName.get(typeElement.asType());
    typeSpecBuilderForTargetClass.addAnnotation(IsGeneratedProxy.class);
    typeSpecBuilderForTargetClass.addAnnotation(IsMetricsProxy.class);

    typeSpecBuilderForTargetClass.addField(defineDelegatorField(typeElement));
    typeSpecBuilderForTargetClass.addField(defineMetricsField());
    typeSpecBuilderForTargetClass.addField(defineSimpleClassNameField(typeElement));

    typeSpecBuilderForTargetClass
//        .addMethod(MethodSpec.methodBuilder("with" + typeElement.getSimpleName())
        .addMethod(MethodSpec.methodBuilder(WITH_DELEGATOR)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(targetTypeName, DELEGATOR_FIELD_NAME, Modifier.FINAL)
            .addCode(CodeBlock.builder()
                .addStatement("this." + DELEGATOR_FIELD_NAME + " = " + DELEGATOR_FIELD_NAME)
                .addStatement("return this").build())
            .returns(ClassName.get(pkgName(typeElement), targetClassNameSimpleForGeneratedClass(typeElement)))
            .build());
  }

  @Override
  protected CodeBlock defineMethodImplementation(final ExecutableElement methodElement, final String methodName2Delegate, final TypeElement typeElementTargetClass) {
    final TypeMirror returnType = methodElement.getReturnType();
    final Builder codeBlockBuilder = CodeBlock.builder();

    final String metricsReference = (methodElement.getModifiers().contains(Modifier.STATIC)) ?
        RAPID_PMMETRICS_REGISTRY + ".getInstance().getMetrics()"
        : "metrics";

    if (returnType.getKind() == TypeKind.VOID) {
      codeBlockBuilder
          .addStatement("final long start = System.nanoTime()")
          .addStatement(delegatorStatementWithOutReturn(methodElement, methodName2Delegate))
          .addStatement("final long stop = System.nanoTime()")
          .addStatement("final $T methodCalls = " + metricsReference + ".histogram(" + CLASS_NAME + " + \".\" + \"" + methodElement.getSimpleName() + "\")", Histogram.class)
          .addStatement("methodCalls.update(stop - start)");
    } else {
      codeBlockBuilder
          .addStatement("final long start = System.nanoTime()")
          .addStatement(delegatorStatementWithLocalVariableResult(methodElement, methodName2Delegate), returnType)
          .addStatement("final long stop = System.nanoTime()")
          .addStatement("final $T methodCalls = " + metricsReference + ".histogram(" + CLASS_NAME + " + \".\" + \"" + methodElement.getSimpleName() + "\")", Histogram.class)
          .addStatement("methodCalls.update(stop - start)")
          .addStatement("return result");
    }
    return codeBlockBuilder.build();
  }

  @Override
  protected void addStaticImports(final JavaFile.Builder builder) {

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
