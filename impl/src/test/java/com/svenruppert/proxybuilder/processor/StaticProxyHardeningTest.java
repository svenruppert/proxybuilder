/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the EUPL-1.2.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package com.svenruppert.proxybuilder.processor;

import com.squareup.javapoet.CodeBlock;
import com.svenruppert.proxybuilder.objectadapter.DynamicObjectAdapterAnnotationProcessor;
import com.svenruppert.proxybuilder.objectadapter.StaticObjectAdapterAnnotationProcessor;
import com.svenruppert.proxybuilder.proxy.generated.BasicStaticProxyAnnotationProcessor;
import com.svenruppert.proxybuilder.proxy.generated.StaticLoggingProxyAnnotationProcessor;
import com.svenruppert.proxybuilder.proxy.generated.StaticMetricsProxyAnnotationProcessor;
import com.svenruppert.proxybuilder.proxy.generated.StaticVirtualProxyAnnotationProcessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.net.URI;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

class StaticProxyHardeningTest {

  @TempDir
  Path tempDir;

  @Test
  void finalClassProducesCompileError() throws Exception {
    final CompilationResult result = compile("""
        package test;

        import com.svenruppert.proxybuilder.proxy.generated.annotations.StaticLoggingProxy;

        @StaticLoggingProxy
        final class FinalTarget {
          public String work() {
            return "done";
          }
        }
        """);

    assertCompilationFailedWith(result, "cannot be applied to final class");
  }

  @Test
  void finalMethodProducesCompileError() throws Exception {
    final CompilationResult result = compile("""
        package test;

        import com.svenruppert.proxybuilder.proxy.generated.annotations.StaticLoggingProxy;

        @StaticLoggingProxy
        class FinalMethodTarget {
          public final String work() {
            return "done";
          }
        }
        """);

    assertCompilationFailedWith(result, "cannot proxy final method work");
  }

  @Test
  void privateMethodProducesCompileError() throws Exception {
    final CompilationResult result = compile("""
        package test;

        import com.svenruppert.proxybuilder.proxy.generated.annotations.StaticLoggingProxy;

        @StaticLoggingProxy
        class PrivateMethodTarget {
          private String work() {
            return "done";
          }
        }
        """);

    assertCompilationFailedWith(result, "cannot proxy private method work");
  }

  @Test
  void staticMethodProducesCompileError() throws Exception {
    final CompilationResult result = compile("""
        package test;

        import com.svenruppert.proxybuilder.proxy.generated.annotations.StaticLoggingProxy;

        @StaticLoggingProxy
        class StaticMethodTarget {
          public static String work() {
            return "done";
          }
        }
        """);

    assertCompilationFailedWith(result, "cannot proxy static method work");
  }

  @Test
  void objectMethodsAreNotGenerated() throws Exception {
    final CompilationResult result = compile("""
        package test;

        import com.svenruppert.proxybuilder.proxy.generated.annotations.StaticLoggingProxy;

        @StaticLoggingProxy
        class ObjectMethodTarget {
          public String work() {
            return "done";
          }

          @Override
          public String toString() {
            return "ObjectMethodTarget";
          }

          @Override
          public int hashCode() {
            return 7;
          }

          @Override
          public boolean equals(Object other) {
            return this == other;
          }
        }
        """);

    Assertions.assertTrue(result.success(), result.messages());
    final Path generatedSource = result.generatedSourceDirectory()
        .resolve("test/ObjectMethodTargetStaticLoggingProxy.java");
    final String generated = Files.readString(generatedSource);

    Assertions.assertFalse(generated.contains("toString("), generated);
    Assertions.assertFalse(generated.contains("hashCode("), generated);
    Assertions.assertFalse(generated.contains("equals("), generated);
    Assertions.assertTrue(generated.contains("work("), generated);
  }

  @Test
  void methodLevelAnnotationsCanContributeMultipleCodeBlocks() throws Exception {
    final CompilationResult result = compile("""
        package com.svenruppert.proxybuilder.processor;

        @TestProxy
        class DecoratorTarget {
          @FirstDecorator
          @SecondDecorator
          public String work() {
            return "source";
          }
        }
        """, new DecoratorProxyProcessor());

    Assertions.assertTrue(result.success(), result.messages());
    final Path generatedSource = result.generatedSourceDirectory()
        .resolve("com/svenruppert/proxybuilder/processor/DecoratorTargetTestProxy.java");
    final String generated = Files.readString(generatedSource);

    Assertions.assertTrue(generated.contains("before FirstDecorator"), generated);
    Assertions.assertTrue(generated.contains("around FirstDecorator,SecondDecorator"), generated);
    Assertions.assertTrue(generated.contains("after SecondDecorator"), generated);
  }

  @Test
  void metricsProxySourceIsGenerated() throws Exception {
    final CompilationResult result = compile("""
        package test;

        import com.svenruppert.proxybuilder.proxy.generated.annotations.StaticMetricsProxy;

        @StaticMetricsProxy
        interface MetricsTarget {
          String work(String input);
        }
        """, new StaticMetricsProxyAnnotationProcessor());

    Assertions.assertTrue(result.success(), result.messages());
    assertGeneratedSourceContains(result, "test/MetricsTargetStaticMetricsProxy.java", "Histogram");
  }

  @Test
  void virtualProxySourceIsGenerated() throws Exception {
    final CompilationResult result = compile("""
        package test;

        import com.svenruppert.proxybuilder.proxy.generated.annotations.StaticVirtualProxy;

        @StaticVirtualProxy
        interface VirtualTarget {
          String work(String input);
        }
        """, new StaticVirtualProxyAnnotationProcessor());

    Assertions.assertTrue(result.success(), result.messages());
    assertGeneratedSourceContains(result, "test/VirtualTargetStaticVirtualProxy.java", "realSubject");
  }

  @Test
  void staticObjectAdapterSourcesAreGenerated() throws Exception {
    final CompilationResult result = compile("""
        package test;

        import com.svenruppert.proxybuilder.objectadapter.generated.StaticObjectAdapter;

        @StaticObjectAdapter
        interface AdapterTarget {
          String work(String input);
        }
        """, new StaticObjectAdapterAnnotationProcessor());

    Assertions.assertTrue(result.success(), result.messages());
    assertGeneratedSourceContains(result, "test/AdapterTargetStaticObjectAdapter.java", "withAdapterTargetMethodWorkString");
  }

  @Test
  void dynamicObjectAdapterSourcesAreGenerated() throws Exception {
    final CompilationResult result = compile("""
        package test;

        import com.svenruppert.proxybuilder.objectadapter.dynamic.DynamicObjectAdapterBuilder;

        @DynamicObjectAdapterBuilder
        interface DynamicAdapterTarget {
          String work(String input);
        }
        """, new DynamicObjectAdapterAnnotationProcessor());

    Assertions.assertTrue(result.success(), result.messages());
    assertGeneratedSourceContains(result, "test/DynamicAdapterTargetAdapterBuilder.java", "withWork");
    assertGeneratedSourceContains(result, "test/DynamicAdapterTargetMethodWorkString.java", "interface");
  }

  @Test
  void generatedClassSuffixOptionIsUsed() throws Exception {
    final CompilationResult result = compile("""
        package test;

        import com.svenruppert.proxybuilder.proxy.generated.annotations.StaticLoggingProxy;

        @StaticLoggingProxy
        interface SuffixTarget {
          String work(String input);
        }
        """, new StaticLoggingProxyAnnotationProcessor(), List.of("-Aproxybuilder.suffix=Secured"));

    Assertions.assertTrue(result.success(), result.messages());
    assertGeneratedSourceContains(result, "test/SuffixTargetSecured.java", "class SuffixTargetSecured");
  }

  private CompilationResult compile(final String source) throws Exception {
    return compile(source, new StaticLoggingProxyAnnotationProcessor());
  }

  private CompilationResult compile(final String source, final Processor processor) throws Exception {
    return compile(source, processor, List.of());
  }

  private CompilationResult compile(final String source, final Processor processor, final List<String> additionalOptions) throws Exception {
    final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    Assertions.assertNotNull(compiler, "JDK compiler is required");

    final Path classes = Files.createDirectories(tempDir.resolve("classes-" + System.nanoTime()));
    final Path generatedSources = Files.createDirectories(tempDir.resolve("generated-" + System.nanoTime()));
    final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

    try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, Locale.ROOT, null)) {
      final List<String> options = new java.util.ArrayList<>(List.of(
          "-classpath", System.getProperty("java.class.path"),
          "-d", classes.toString(),
          "-s", generatedSources.toString()));
      options.addAll(additionalOptions);

      final JavaCompiler.CompilationTask task = compiler.getTask(
          null,
          fileManager,
          diagnostics,
          options,
          null,
          List.of(new SourceFile("test.ObjectMethodTarget", source)));
      task.setProcessors(List.of(processor));

      return new CompilationResult(Boolean.TRUE.equals(task.call()), diagnostics.getDiagnostics(), generatedSources);
    }
  }

  private void assertCompilationFailedWith(final CompilationResult result, final String expectedMessage) {
    Assertions.assertFalse(result.success(), result.messages());
    Assertions.assertTrue(result.messages().contains(expectedMessage), result.messages());
  }

  private void assertGeneratedSourceContains(final CompilationResult result,
                                             final String generatedSourcePath,
                                             final String expectedContent) throws Exception {
    final Path generatedSource = result.generatedSourceDirectory().resolve(generatedSourcePath);
    Assertions.assertTrue(Files.exists(generatedSource), generatedSource.toString());
    final String generated = Files.readString(generatedSource);
    Assertions.assertTrue(generated.contains(expectedContent), generated);
  }

  private record CompilationResult(boolean success,
                                   List<Diagnostic<? extends JavaFileObject>> diagnostics,
                                   Path generatedSourceDirectory) {
    String messages() {
      return diagnostics.stream()
          .map(diagnostic -> diagnostic.getMessage(Locale.ROOT))
          .collect(Collectors.joining("\n"));
    }
  }

  private static final class SourceFile extends SimpleJavaFileObject {
    private final String source;

    private SourceFile(final String className, final String source) {
      super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
      this.source = source;
    }

    @Override
    public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
      return source;
    }
  }

  private static final class DecoratorProxyProcessor extends BasicStaticProxyAnnotationProcessor<TestProxy> {

    @Override
    public Class<TestProxy> responsibleFor() {
      return TestProxy.class;
    }

    @Override
    protected void addClassLevelSpecs(final TypeElement typeElement, final RoundEnvironment roundEnv) {
    }

    @Override
    protected CodeBlock defineMethodImplementation(final ExecutableElement methodElement,
                                                   final String methodName2Delegate,
                                                   final TypeElement typeElementTargetClass) {
      return CodeBlock.of("return $S;\n", "fallback");
    }

    @Override
    protected List<CodeBlock> beforeDelegation(final ExecutableElement methodElement,
                                               final String methodName2Delegate,
                                               final TypeElement typeElementTargetClass) {
      if (hasAnnotation(methodElement, FirstDecorator.class.getCanonicalName())) {
        return List.of(CodeBlock.of("// before FirstDecorator\n"));
      }
      return List.of();
    }

    @Override
    protected Optional<CodeBlock> aroundDelegation(final ExecutableElement methodElement,
                                                   final String methodName2Delegate,
                                                   final TypeElement typeElementTargetClass) {
      final Set<String> decorators = Set.of(
          FirstDecorator.class.getCanonicalName(),
          SecondDecorator.class.getCanonicalName());
      final String marker = annotationsOn(methodElement, decorators)
          .stream()
          .map(annotationMirror -> annotationMirror.getAnnotationType().asElement().getSimpleName().toString())
          .collect(Collectors.joining(","));

      if (hasAnnotation(methodElement, FirstDecorator.class.getCanonicalName())
          && hasAnnotation(methodElement, SecondDecorator.class.getCanonicalName())) {
        return Optional.of(CodeBlock.of("// around $L\nreturn $S;\n", marker, marker));
      }

      return super.aroundDelegation(methodElement, methodName2Delegate, typeElementTargetClass);
    }

    @Override
    protected List<CodeBlock> afterDelegation(final ExecutableElement methodElement,
                                              final String methodName2Delegate,
                                              final TypeElement typeElementTargetClass) {
      if (hasAnnotation(methodElement, SecondDecorator.class.getCanonicalName())) {
        return List.of(CodeBlock.of("// after SecondDecorator\n"));
      }
      return List.of();
    }

  }
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface TestProxy {
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface FirstDecorator {
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface SecondDecorator {
}
