# ProxyBuilder

ProxyBuilder contains runtime proxy builders and annotation processors for generated static proxies.
It is intended both as a ready-to-use proxy utility and as a base for custom annotation processors, for example security wrappers.

## Coordinates

```xml
<dependency>
  <groupId>com.svenruppert</groupId>
  <artifactId>proxybuilder</artifactId>
  <version>00.11.00</version>
</dependency>
```

For annotation processing in Maven:

```xml
<plugin>
  <artifactId>maven-compiler-plugin</artifactId>
  <configuration>
    <annotationProcessorPaths>
      <path>
        <groupId>com.svenruppert</groupId>
        <artifactId>proxybuilder</artifactId>
        <version>00.11.00</version>
      </path>
    </annotationProcessorPaths>
  </configuration>
</plugin>
```

## Requirements

- JDK 26
- Maven 4 via the repository Maven Wrapper
- EUPL-1.2

Build:

```bash
./mvnw clean install
```

Release artifact checks:

```bash
./mvnw -P release -Dgpg.skip verify
```

## Modules

- `proxybuilder-parent`: reactor parent.
- `proxybuilder-annotations`: tiny annotations-only JAR consumed and emitted by the processor. Put this on the consumer compile classpath; see [`proxybuilder-annotations/README.md`](proxybuilder-annotations/README.md) for the annotation catalogue.
- `proxybuilder`: implementation artifact with runtime proxy utilities, annotation processors, and generated-proxy base APIs. Add via `<annotationProcessorPaths>`.
- `proxybuilder-testusage`: integration and usage examples.

## Runtime Dynamic Proxies

`DynamicProxyBuilder` creates JDK dynamic proxies around an interface and implementation instance.

```java
Service proxy = DynamicProxyBuilder
    .createBuilder(Service.class, new ServiceImpl())
    .addIPreAction((original, method, args) -> {
      // executed before every invocation
    })
    .addIPostAction((original, method, args) -> {
      // executed after every invocation
    })
    .build();
```

Supported runtime features:

- Pre-actions before method invocation.
- Post-actions after method invocation.
- Security rules via `SecurityRule`.
- Logging proxy behavior.
- Metrics proxy behavior using Dropwizard Metrics.
- Virtual proxy creation strategies.

### Runtime Security Rule

```java
Service proxy = DynamicProxyBuilder
    .createBuilder(Service.class, new ServiceImpl())
    .addSecurityRule(() -> currentUserCanCallService())
    .build();
```

If the rule returns `false`, the invocation is blocked by the proxy.

### Runtime Metrics

```java
Service proxy = DynamicProxyBuilder
    .createBuilder(Service.class, new ServiceImpl())
    .addMetrics()
    .build();
```

Metrics are collected through `RapidPMMetricsRegistry`.
`metrics-jmx` intentionally remains a dependency because JMX/console reporting is part of the metrics support.

## Static Proxy Annotations

Static proxies are generated during Java compilation through annotation processing.

### `@StaticLoggingProxy`

Generates a wrapper that logs method calls through the project logger infrastructure.

```java
@StaticLoggingProxy
public interface Service {
  String work(String input);
}
```

Generated class:

```java
ServiceStaticLoggingProxy proxy = new ServiceStaticLoggingProxy()
    .withDelegator(new ServiceImpl());
```

### `@StaticMetricsProxy`

Generates a wrapper that records method timings with Dropwizard Metrics.

```java
@StaticMetricsProxy
public interface Service {
  String work(String input);
}
```

Generated class:

```java
ServiceStaticMetricsProxy proxy = new ServiceStaticMetricsProxy()
    .withDelegator(new ServiceImpl());
```

### `@StaticVirtualProxy`

Generates a static virtual proxy backed by the virtual proxy strategy infrastructure.

```java
@StaticVirtualProxy
public interface Service {
  String work(String input);
}
```

### `@StaticObjectAdapter`

Generates a static object adapter. For each adapted method, a functional interface is generated and can be installed on the adapter.

```java
@StaticObjectAdapter
public interface Service {
  String work(String input);
}
```

Typical generated API shape:

```java
ServiceStaticObjectAdapter adapter = new ServiceStaticObjectAdapter()
    .withService(new ServiceImpl())
    .withServiceMethodWorkString(input -> "adapted " + input);
```

### `@DynamicObjectAdapterBuilder`

Generates a builder and invocation-handler pair for dynamic object adapter scenarios.

```java
@DynamicObjectAdapterBuilder
public interface Service {
  String work(String input);
}
```

Typical generated API shape:

```java
ServiceAdapterBuilder builder = ServiceAdapterBuilder.newBuilder()
    .setOriginal(new ServiceImpl())
    .withWork(input -> "adapted " + input);
```

## Processor Safety Rules

The static proxy processors fail compilation for method shapes that cannot be safely proxied:

- `final` classes annotated for static proxy generation.
- `final` methods.
- `private` methods.
- `static` methods, unless `-Aproxybuilder.failOnStaticMethods=false` is used.

Methods declared by `java.lang.Object` are not generated as proxy methods:

- `equals`
- `hashCode`
- `toString`
- `finalize`
- other direct `Object` methods such as `wait`, `notify`, and `getClass`

## Writing A Custom Processor

Use `BasicStaticProxyAnnotationProcessor<T>` as the extension base.
Implement `responsibleFor()`, class-level additions, and method-generation logic.

```java
public final class SecuredAnnotationProcessor
    extends BasicStaticProxyAnnotationProcessor<Secured> {

  @Override
  public Class<Secured> responsibleFor() {
    return Secured.class;
  }

  @Override
  protected void addClassLevelSpecs(TypeElement typeElement, RoundEnvironment roundEnv) {
    // Add fields, marker annotations, helper methods, or constructor details.
  }

  @Override
  protected CodeBlock defineMethodImplementation(ExecutableElement methodElement,
                                                 String methodName2Delegate,
                                                 TypeElement targetType) {
    return CodeBlock.builder()
        .addStatement(delegatorStatementWithReturn(methodElement, methodName2Delegate))
        .build();
  }
}
```

### Generated Class Suffix

Override the class suffix programmatically:

```java
@Override
protected String generatedClassSuffix(TypeElement typeElement) {
  return "Secured";
}
```

Or use a processor option:

```bash
-Aproxybuilder.suffix=Secured
```

### Constructor Modifiers

Adjust generated constructor visibility:

```java
@Override
protected Set<Modifier> filterConstructorModifiers(Set<Modifier> modifiers) {
  modifiers.remove(Modifier.PUBLIC);
  modifiers.add(Modifier.PROTECTED);
  return modifiers;
}
```

### Static Imports

`addStaticImports(JavaFile.Builder)` is a no-op by default.
Override it only when generated sources need static imports.

```java
@Override
protected void addStaticImports(JavaFile.Builder builder) {
  builder.addStaticImport(MySecurityApi.class, "requireRole");
}
```

## Method-Level Decorators

Use `beforeDelegation`, `aroundDelegation`, and `afterDelegation` to combine several method-level annotations on the same method.

```java
@Override
protected List<CodeBlock> beforeDelegation(ExecutableElement methodElement,
                                           String methodName2Delegate,
                                           TypeElement targetType) {
  if (hasAnnotation(methodElement, RequiresRole.class.getCanonicalName())) {
    return List.of(CodeBlock.of("securityEnforcer.requireRole();\n"));
  }
  return List.of();
}

@Override
protected Optional<CodeBlock> aroundDelegation(ExecutableElement methodElement,
                                               String methodName2Delegate,
                                               TypeElement targetType) {
  return Optional.of(CodeBlock.of("$L;\n", delegatorStatementWithReturn(methodElement, methodName2Delegate)));
}

@Override
protected List<CodeBlock> afterDelegation(ExecutableElement methodElement,
                                          String methodName2Delegate,
                                          TypeElement targetType) {
  return List.of(CodeBlock.of("securityAudit.recordSuccess();\n"));
}
```

Security-style target:

```java
public interface AccountService {

  @RequiresPolicy("account")
  @RequiresPermission("account:read")
  @RequiresRole("admin")
  Account loadAccount(String accountId);
}
```

Annotation lookup helpers:

- `annotationsOn(ExecutableElement)`
- `annotationsOn(ExecutableElement, Set<String>)`
- `hasAnnotation(ExecutableElement, String)`

## Useful Base Helpers

`BasicAnnotationProcessor` exposes helpers used by custom processors:

- `pkgName(TypeElement)`: package name of the target type.
- `className(Element)`: simple class name.
- `targetClassNameSimpleForGeneratedClass(TypeElement)`: generated class simple name.
- `targetClassNameSimpleForSourceClass(TypeElement)`: source class simple name.
- `defineDelegatorField(TypeElement)`: standard delegator field.
- `defineSimpleClassNameField(TypeElement)`: static class-name field.
- `defineParamsForMethod(ExecutableElement)`: JavaPoet parameter specs.
- `extractArgumentTypeListFromMethod(ExecutableElement)`: raw parameter type mirrors.
- `delegatorMethodCall(ExecutableElement, String)`: method-call text.
- `delegatorStatementWithReturn(ExecutableElement, String)`: `return delegator.method(...)`.
- `delegatorStatementWithOutReturn(ExecutableElement, String)`: `delegator.method(...)`.
- `delegatorStatementWithLocalVariableResult(ExecutableElement, String)`: local `result` assignment.
- `error`, `warning`, `note`: compiler diagnostics through `Messager`.

## Processor Options

The base processor declares:

- `proxybuilder.verbose`: emits `Diagnostic.Kind.NOTE` messages for `@SkipProxy` and `excludeMethodNames` skips.
- `proxybuilder.suffix`: overrides the generated class suffix.
- `proxybuilder.failOnStaticMethods`: defaults to `true`; set to `false` to downgrade static-method findings to warnings.
- `proxybuilder.suppressDelegatesTo`: defaults to `false`; set to `true` to strip the auto-emitted `@DelegatesTo` annotation from generated wrapper methods.

For per-type overrides see `@ProxyBuilderOptions` and `@ProxyName` in `proxybuilder-annotations`.

Maven example:

```xml
<compilerArgs>
  <arg>-Aproxybuilder.suffix=Secured</arg>
  <arg>-Aproxybuilder.failOnStaticMethods=true</arg>
</compilerArgs>
```

## JPMS

The implementation artifact provides `module-info.java` with module name:

```java
module com.svenruppert.proxybuilder
```

`jdeps --check com.svenruppert.proxybuilder` is used to validate the descriptor.
API-visible dependencies such as JavaPoet, `java.compiler`, `core`, and Metrics are marked as transitive where needed.

## Annotation Inheritance

Trigger annotations are evaluated on the elements returned by the annotation-processing round.
Put trigger annotations directly on the type that should produce generated sources.

The processors walk methods declared on superclasses and implemented interfaces for generation and validation.
Trigger annotations on a superclass do not automatically trigger generation for subclasses.

## Incremental Compilation

The processors generate sources through the standard `Filer` API and tolerate duplicate generation attempts across rounds.
The project is verified with Maven's normal incremental compile behavior through `./mvnw clean install`.

Gradle incremental annotation-processing metadata is not published yet.

## Testusage Generated Sources

`proxybuilder-testusage` intentionally keeps a small `maven-antrun-plugin` step that compiles annotation-generated sources before test compilation.
Removing that step currently breaks `testCompile`, because some tests reference generated classes directly.

## Development Checks

Run the focused processor tests:

```bash
./mvnw -pl impl test
```

Run the full reactor:

```bash
./mvnw clean install
```

Validate the release profile without signing:

```bash
./mvnw -P release -Dgpg.skip verify
```

Validate JPMS after a build:

```bash
./mvnw -pl impl dependency:build-classpath -Dmdep.outputFile=target/classpath.txt
jdeps --multi-release 26 \
  --module-path "impl/target/proxybuilder-00.11.00-SNAPSHOT.jar:$(cat impl/target/classpath.txt)" \
  --check com.svenruppert.proxybuilder
```

## Releasing

### Current limitation — Maven 4 + central-publishing-maven-plugin

`central-publishing-maven-plugin` 0.10.0 (the current latest) is not Maven-4-aware.
It emits the Maven 4 consumer POM as a consumer-classified artefact (`*-consumer.pom`) and uploads the build POM as the primary `.pom`, which the Central Portal validator rejects with `Failed to associate file with coordinates …`.
Plugin v0.10.0 also offers no "stage but don't upload" mode — `skipPublishing=true` skips staging altogether ("No files to stage for artifact" in the log).

The workaround bypasses the plugin: `./mvnw install` already produces all signed artefacts in `~/.m2/repository/com/svenruppert/<module>/<version>/` with Maven 4's correct local-repo naming (the consumer POM as `<artifact>-<version>.pom`, the build POM as `<artifact>-<version>-build.pom`).
The script in `scripts/clean-bundle-for-central.sh` copies the publishable files (drops `*-build.pom`, `*-tests*`, etc.), generates the four checksum types Central expects, and zips a bundle that is uploaded by hand through the Central Portal web UI.

### Central Portal POM requirements

Central's validator inspects each artifact's POM directly and does **not** resolve `<parent>` for the metadata it requires. Every published module's `pom.xml` must therefore declare these fields inline:

- `<name>`
- `<description>`
- `<url>`
- `<licenses>` (at least one)
- `<scm>`
- `<developers>` (at least one)

`impl/pom.xml` already carries `<name>`, `<description>`, and `<url>`. The remaining fields are currently inherited from `proxybuilder-parent`/`com.svenruppert:dependencies`; if a future Central validation pass rejects the bundle with a missing-field error (`Error: Project <field> is missing`), copy that field into `impl/pom.xml` and re-bundle.

### Working release flow

```bash
./mvnw clean verify
./mvnw versions:set -DnewVersion=<RELEASE> -DprocessAllModules=true -DgenerateBackupPoms=false
./mvnw clean verify
git commit -am "release <RELEASE>"
git tag <RELEASE>
./mvnw clean install -P release,_release_prepare
./scripts/clean-bundle-for-central.sh
# Upload target/central-publishing/central-bundle.zip via
# https://central.sonatype.com/publishing → Publish Component
./mvnw versions:set -DnewVersion=<NEXT>-SNAPSHOT -DprocessAllModules=true -DgenerateBackupPoms=false
git commit -am "prepare <NEXT>-SNAPSHOT"
git push origin <branch> <RELEASE>
```

Verify the bundle layout with:

```bash
unzip -l target/central-publishing/central-bundle.zip | grep -c build.pom   # expected: 0
unzip -l target/central-publishing/central-bundle.zip | grep -c consumer    # expected: 0
```

## Project Decisions

- The external parent `com.svenruppert:dependencies` remains in use.
- Logging remains based on `HasLogger`.
- `com.svenruppert:core` remains compile scope.
- `metrics-jmx` remains because JMX/console reporting is part of the metrics support.
- `slf4j-simple` is not a main library dependency.
- `reflections8` is only a test dependency in `proxybuilder-testusage`.
- No GitHub Actions workflow is part of the current scope.