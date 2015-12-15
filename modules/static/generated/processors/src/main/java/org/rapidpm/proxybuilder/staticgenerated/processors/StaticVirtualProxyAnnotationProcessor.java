package org.rapidpm.proxybuilder.staticgenerated.processors;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import org.rapidpm.proxybuilder.staticgenerated.annotations.IsGeneratedProxy;
import org.rapidpm.proxybuilder.staticgenerated.annotations.IsVirtualProxy;
import org.rapidpm.proxybuilder.staticgenerated.annotations.StaticVirtualProxy;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

/**
 * Created by svenruppert on 14.12.15.
 */
public class StaticVirtualProxyAnnotationProcessor extends BasicStaticProxyAnnotationProcessor<StaticVirtualProxy> {
  @Override
  protected void addClassLevelSpecs(final TypeElement typeElement, final RoundEnvironment roundEnv) {
    typeSpecBuilderForTargetClass.addAnnotation(IsGeneratedProxy.class);
    typeSpecBuilderForTargetClass.addAnnotation(IsVirtualProxy.class);

    final FieldSpec delegatorFieldSpec = defineDelegatorField(typeElement);
    typeSpecBuilderForTargetClass.addField(delegatorFieldSpec);
  }

  @Override
  protected CodeBlock defineMethodImplementation(final ExecutableElement methodElement, final String methodName2Delegate) {
    return null;
  }

  @Override
  public Class<StaticVirtualProxy> responsibleFor() {
    return StaticVirtualProxy.class;
  }
}
