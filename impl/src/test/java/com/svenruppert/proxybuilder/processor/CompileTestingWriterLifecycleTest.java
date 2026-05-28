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
import com.svenruppert.proxybuilder.proxy.generated.StaticLoggingProxyAnnotationProcessor;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;

class CompileTestingWriterLifecycleTest {

  @Test
  void generatedSourceIsReadableViaInMemoryFileManager() {
    final Compilation compilation = Compiler.javac()
        .withProcessors(new StaticLoggingProxyAnnotationProcessor())
        .compile(JavaFileObjects.forSourceLines(
            "test.WriterLifecycleTarget",
            "package test;",
            "",
            "import com.svenruppert.proxybuilder.proxy.generated.annotations.StaticLoggingProxy;",
            "",
            "@StaticLoggingProxy",
            "public interface WriterLifecycleTarget {",
            "  String work(String input);",
            "}"));

    assertThat(compilation).succeeded();
    assertThat(compilation)
        .generatedSourceFile("test.WriterLifecycleTargetStaticLoggingProxy")
        .contentsAsUtf8String()
        .contains("class WriterLifecycleTargetStaticLoggingProxy");
  }
}
