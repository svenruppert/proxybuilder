/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the EUPL-1.2.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package com.svenruppert.proxybuilder.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import com.squareup.javapoet.CodeBlock;
import com.svenruppert.proxybuilder.proxy.generated.BasicStaticProxyAnnotationProcessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.List;

import static com.google.testing.compile.CompilationSubject.assertThat;

class DownstreamSmokeTest {

  @Test
  void generatedWrapperImportsOnlyAnnotationsModuleNotProxybuilderImpl() {
    final Compilation compilation = Compiler.javac()
        .withProcessors(new DownstreamCustomProcessor())
        .compile(JavaFileObjects.forSourceLines(
            "test.DownstreamTarget",
            "package test;",
            "import com.svenruppert.proxybuilder.processor.DownstreamTrigger;",
            "@DownstreamTrigger",
            "public interface DownstreamTarget {",
            "  String work(String input);",
            "}"));

    assertThat(compilation).succeeded();
    final String generated = readGenerated(compilation, "test.DownstreamTargetDownstreamTrigger");

    final List<String> leakedImports = generated.lines()
        .map(String::trim)
        .filter(line -> line.startsWith("import "))
        .map(line -> line.substring("import ".length()).replace(";", "").trim())
        .filter(fqn -> fqn.startsWith("com.svenruppert.proxybuilder."))
        .filter(fqn -> !fqn.startsWith("com.svenruppert.proxybuilder.annotations."))
        .toList();

    Assertions.assertTrue(leakedImports.isEmpty(),
        "Consumer compile classpath would need proxybuilder for these imports:\n  "
            + String.join("\n  ", leakedImports)
            + "\nFull generated source:\n" + generated);
  }

  private static String readGenerated(final Compilation compilation, final String fqn) {
    try {
      return compilation.generatedSourceFile(fqn)
          .orElseThrow(() -> new AssertionError("no generated source for " + fqn))
          .getCharContent(true)
          .toString();
    } catch (Exception e) {
      throw new AssertionError("failed to read generated source for " + fqn, e);
    }
  }

  private static final class DownstreamCustomProcessor
      extends BasicStaticProxyAnnotationProcessor<DownstreamTrigger> {

    @Override
    public Class<DownstreamTrigger> responsibleFor() {
      return DownstreamTrigger.class;
    }

    @Override
    protected void addClassLevelSpecs(final TypeElement typeElement, final RoundEnvironment roundEnv) {
    }

    @Override
    protected CodeBlock defineMethodImplementation(final ExecutableElement methodElement,
                                                   final String methodName2Delegate,
                                                   final TypeElement typeElementTargetClass) {
      return CodeBlock.of("return null;\n");
    }
  }
}
