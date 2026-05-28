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
import com.svenruppert.proxybuilder.annotations.DelegatesTo;
import com.svenruppert.proxybuilder.annotations.GeneratedByProxyBuilder;
import com.svenruppert.proxybuilder.annotations.ProxyBuilderVersion;
import com.svenruppert.proxybuilder.proxy.generated.StaticLoggingProxyAnnotationProcessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;
import java.util.HashMap;
import java.util.Map;

import static com.google.testing.compile.CompilationSubject.assertThat;

class AnnotationsModuleProcessorTest {

  @Test
  void generatedByProxyBuilderIsEmittedWithRuntimeRetention() throws Exception {
    final Compilation compilation = compile(
        "test.GenMetaTarget",
        "package test;",
        "import com.svenruppert.proxybuilder.proxy.generated.annotations.StaticLoggingProxy;",
        "@StaticLoggingProxy",
        "public interface GenMetaTarget {",
        "  String work(String input);",
        "}");

    assertThat(compilation).succeeded();
    assertThat(compilation)
        .generatedSourceFile("test.GenMetaTargetStaticLoggingProxy")
        .contentsAsUtf8String()
        .contains("@GeneratedByProxyBuilder");

    final Class<?> generated = loadClass(compilation, "test.GenMetaTargetStaticLoggingProxy");
    final GeneratedByProxyBuilder meta = generated.getAnnotation(GeneratedByProxyBuilder.class);
    Assertions.assertNotNull(meta, "RUNTIME-retained marker must be readable via reflection");
    Assertions.assertEquals(StaticLoggingProxyAnnotationProcessor.class.getName(), meta.processor());
    Assertions.assertEquals("test.GenMetaTarget", meta.sourceClass());
    Assertions.assertEquals(ProxyBuilderVersion.VERSION, meta.proxyBuilderVersion());
    Assertions.assertEquals("www.proxybuilder.org", meta.comments());
    Assertions.assertFalse(meta.date().isEmpty(), "date member must be populated");
  }

  @Test
  void skipProxySkipsMethod() {
    final Compilation compilation = compile(
        "test.SkipProxyTarget",
        "package test;",
        "import com.svenruppert.proxybuilder.annotations.SkipProxy;",
        "import com.svenruppert.proxybuilder.proxy.generated.annotations.StaticLoggingProxy;",
        "@StaticLoggingProxy",
        "public class SkipProxyTarget {",
        "  public String included() { return \"in\"; }",
        "  @SkipProxy(\"too hot\")",
        "  public String excluded() { return \"out\"; }",
        "}");

    assertThat(compilation).succeeded();
    final String generated = readGenerated(compilation, "test.SkipProxyTargetStaticLoggingProxy");
    Assertions.assertTrue(generated.contains("included("), generated);
    Assertions.assertFalse(generated.contains("excluded("),
                           "method annotated with @SkipProxy must not be generated:\n" + generated);
  }

  @Test
  void proxyBuilderOptionsSuffixOverridesGlobal() {
    final Compilation compilation = compile(
        "test.SuffixTarget",
        "package test;",
        "import com.svenruppert.proxybuilder.annotations.ProxyBuilderOptions;",
        "import com.svenruppert.proxybuilder.proxy.generated.annotations.StaticLoggingProxy;",
        "@StaticLoggingProxy",
        "@ProxyBuilderOptions(suffix = \"PerTypeSuffix\")",
        "public interface SuffixTarget {",
        "  String work();",
        "}");

    assertThat(compilation).succeeded();
    assertThat(compilation)
        .generatedSourceFile("test.SuffixTargetPerTypeSuffix")
        .contentsAsUtf8String()
        .contains("class SuffixTargetPerTypeSuffix");
  }

  @Test
  void proxyBuilderOptionsFailOnStaticFalseDowngradesError() {
    final Compilation compilation = compile(
        "test.StaticTolerantTarget",
        "package test;",
        "import com.svenruppert.proxybuilder.annotations.ProxyBuilderOptions;",
        "import com.svenruppert.proxybuilder.proxy.generated.annotations.StaticLoggingProxy;",
        "@StaticLoggingProxy",
        "@ProxyBuilderOptions(failOnStaticMethods = ProxyBuilderOptions.FailOnStatic.FALSE)",
        "public class StaticTolerantTarget {",
        "  public static String staticWork() { return \"x\"; }",
        "  public String work() { return \"y\"; }",
        "}");

    assertThat(compilation).succeeded();
    assertThat(compilation).hadWarningContaining("cannot proxy static method staticWork");
  }

  @Test
  void proxyBuilderOptionsExcludeMethodNamesSkipsMatch() {
    final Compilation compilation = compile(
        "test.ExcludeTarget",
        "package test;",
        "import com.svenruppert.proxybuilder.annotations.ProxyBuilderOptions;",
        "import com.svenruppert.proxybuilder.proxy.generated.annotations.StaticLoggingProxy;",
        "@StaticLoggingProxy",
        "@ProxyBuilderOptions(excludeMethodNames = {\"skipMe\"})",
        "public class ExcludeTarget {",
        "  public String keepMe() { return \"keep\"; }",
        "  public String skipMe() { return \"skip\"; }",
        "}");

    assertThat(compilation).succeeded();
    final String generated = readGenerated(compilation, "test.ExcludeTargetStaticLoggingProxy");
    Assertions.assertTrue(generated.contains("keepMe("), generated);
    Assertions.assertFalse(generated.contains("skipMe("),
                           "excluded method must not appear in wrapper:\n" + generated);
  }

  @Test
  void delegatesToIsEmittedOnGeneratedMethods() throws Exception {
    final Compilation compilation = compile(
        "test.DelegatesToTarget",
        "package test;",
        "import com.svenruppert.proxybuilder.proxy.generated.annotations.StaticLoggingProxy;",
        "@StaticLoggingProxy",
        "public interface DelegatesToTarget {",
        "  String work(String input);",
        "}");

    assertThat(compilation).succeeded();
    final String generated = readGenerated(compilation, "test.DelegatesToTargetStaticLoggingProxy");
    Assertions.assertTrue(generated.contains("DelegatesTo"),
                          "wrapper must carry @DelegatesTo:\n" + generated);
    Assertions.assertTrue(generated.contains("test.DelegatesToTarget#work(java.lang.String)"),
                          "JLS-style source-method reference missing:\n" + generated);

    final Class<?> generatedClass = loadClass(compilation, "test.DelegatesToTargetStaticLoggingProxy");
    final var workMethod = generatedClass.getDeclaredMethod("work", String.class);
    final DelegatesTo runtime = workMethod.getAnnotation(DelegatesTo.class);
    Assertions.assertNotNull(runtime);
    Assertions.assertEquals("test.DelegatesToTarget#work(java.lang.String)", runtime.value());
  }

  @Test
  void suppressDelegatesToOptionStripsAnnotation() {
    final Compilation compilation = Compiler.javac()
        .withProcessors(new StaticLoggingProxyAnnotationProcessor())
        .withOptions("-Aproxybuilder.suppressDelegatesTo=true")
        .compile(JavaFileObjects.forSourceLines(
            "test.SuppressDelegatesTarget",
            "package test;",
            "import com.svenruppert.proxybuilder.proxy.generated.annotations.StaticLoggingProxy;",
            "@StaticLoggingProxy",
            "public interface SuppressDelegatesTarget {",
            "  String work(String input);",
            "}"));

    assertThat(compilation).succeeded();
    final String generated = readGenerated(compilation, "test.SuppressDelegatesTargetStaticLoggingProxy");
    Assertions.assertFalse(generated.contains("DelegatesTo"),
                           "suppressDelegatesTo=true must strip @DelegatesTo:\n" + generated);
  }

  @Test
  void proxyNameBeatsSuffix() {
    final Compilation compilation = compile(
        "test.NamedTarget",
        "package test;",
        "import com.svenruppert.proxybuilder.annotations.ProxyBuilderOptions;",
        "import com.svenruppert.proxybuilder.annotations.ProxyName;",
        "import com.svenruppert.proxybuilder.proxy.generated.annotations.StaticLoggingProxy;",
        "@StaticLoggingProxy",
        "@ProxyName(\"{Original}WithLogging\")",
        "@ProxyBuilderOptions(suffix = \"ShouldNotWin\")",
        "public interface NamedTarget {",
        "  String work();",
        "}");

    assertThat(compilation).succeeded();
    assertThat(compilation)
        .generatedSourceFile("test.NamedTargetWithLogging")
        .contentsAsUtf8String()
        .contains("class NamedTargetWithLogging");
  }

  @Test
  void proxyEntryEmitsExperimentalNote() {
    final Compilation compilation = compile(
        "test.ProxyEntryTarget",
        "package test;",
        "import com.svenruppert.proxybuilder.annotations.ProxyEntry;",
        "import com.svenruppert.proxybuilder.proxy.generated.annotations.StaticLoggingProxy;",
        "@StaticLoggingProxy",
        "public class ProxyEntryTarget {",
        "  @ProxyEntry(\"trace me\")",
        "  public String work() { return \"x\"; }",
        "}");

    assertThat(compilation).succeeded();
    assertThat(compilation).hadNoteContaining("@ProxyEntry is experimental");
    assertThat(compilation).hadNoteContaining(ProxyBuilderVersion.VERSION);
  }

  @Test
  void migrationSmokeProducesUnchangedShape() {
    final Compilation compilation = compile(
        "test.MigrationTarget",
        "package test;",
        "import com.svenruppert.proxybuilder.proxy.generated.annotations.StaticLoggingProxy;",
        "@StaticLoggingProxy",
        "public interface MigrationTarget {",
        "  String work(String input);",
        "}");

    assertThat(compilation).succeeded();
    final String generated = readGenerated(compilation, "test.MigrationTargetStaticLoggingProxy");
    Assertions.assertTrue(generated.contains("withDelegator"), generated);
    Assertions.assertTrue(generated.contains("class MigrationTargetStaticLoggingProxy"), generated);
  }

  private static Compilation compile(final String unused, final String... sourceLines) {
    return Compiler.javac()
        .withProcessors(new StaticLoggingProxyAnnotationProcessor())
        .compile(JavaFileObjects.forSourceLines(unused, sourceLines));
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

  private static Class<?> loadClass(final Compilation compilation, final String fqn) throws Exception {
    final Map<String, byte[]> bytecode = new HashMap<>();
    for (final JavaFileObject jfo : compilation.generatedFiles()) {
      if (jfo.getKind() != JavaFileObject.Kind.CLASS) {
        continue;
      }
      final String name = jfo.getName();
      final int marker = name.indexOf("/CLASS_OUTPUT/");
      final String trimmed = (marker >= 0)
          ? name.substring(marker + "/CLASS_OUTPUT/".length())
          : name.replaceFirst("^/+", "");
      final String className = trimmed.replace(".class", "").replace('/', '.');
      try (var in = jfo.openInputStream()) {
        bytecode.put(className, in.readAllBytes());
      }
    }
    final ClassLoader cl = new ClassLoader(AnnotationsModuleProcessorTest.class.getClassLoader()) {
      @Override
      protected Class<?> findClass(final String name) throws ClassNotFoundException {
        final byte[] bytes = bytecode.get(name);
        if (bytes == null) {
          throw new ClassNotFoundException(name);
        }
        return defineClass(name, bytes, 0, bytes.length);
      }
    };
    return cl.loadClass(fqn);
  }
}
