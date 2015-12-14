package org.rapidpm.proxybuilder.staticgenerated.processors;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
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
  protected CodeBlock defineMethodImplementation(final ExecutableElement methodElement, final String methodName2Delegate) {
    return null;
  }

  @Override
  protected void addClassLevelSpecs(final TypeElement typeElement, final RoundEnvironment roundEnv) {

    final TypeName targetTypeName = TypeName.get(typeElement.asType());
    final TypeSpec.Builder specBuilderForTargetClass = createTypeSpecBuilderForTargetClass(typeElement, targetTypeName);

    specBuilderForTargetClass.addAnnotation(IsGeneratedProxy.class);
    specBuilderForTargetClass.addAnnotation(IsVirtualProxy.class);


    final FieldSpec delegatorFieldSpec = defineDelegatorField(typeElement);
    specBuilderForTargetClass.addField(delegatorFieldSpec);


  }

  @Override
  public Class<StaticVirtualProxy> responsibleFor() {
    return StaticVirtualProxy.class;
  }
}
