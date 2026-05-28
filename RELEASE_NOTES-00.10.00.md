# Release Notes

## 00.10.00 — 2026-05-27

First release under the new `com.svenruppert:proxybuilder` coordinates.
This is a modernization release: new groupId, JDK 26, Maven 4, JPMS, hardened annotation processors, and a Maven-Central-ready release workflow.

### Coordinates

```xml
<dependency>
  <groupId>com.svenruppert</groupId>
  <artifactId>proxybuilder</artifactId>
  <version>00.10.00</version>
</dependency>
```

For annotation processing:

```xml
<annotationProcessorPaths>
  <path>
    <groupId>com.svenruppert</groupId>
    <artifactId>proxybuilder</artifactId>
    <version>00.10.00</version>
  </path>
</annotationProcessorPaths>
```

JPMS module name: `com.svenruppert.proxybuilder`.

### Breaking changes

- **New Maven coordinates.** The artifact has moved from the previous `00.09.05-RPM` line to `com.svenruppert:proxybuilder:00.10.00`. Update all `<groupId>`/`<artifactId>` references; the new artifact is not transitively compatible with the old one.
- **Java baseline raised to JDK 26.** The compiler `release` target is `26`; consumers must build on JDK 26 or newer.
- **Maven 4 required.** The build enforces Maven `4.0.0-rc-5` via the Maven Wrapper; older Maven versions are not supported.
- **JPMS module descriptor.** The implementation now ships `module-info.java` (`module com.svenruppert.proxybuilder`). Consumers using the module path must require it explicitly; classpath consumers are unaffected.
- **Kotlin sources removed.** All Kotlin code paths are gone; the artifact is pure Java again.
- **JUnit 4 usage removed.** Test code runs on JUnit 6 / Jupiter.
- **`javax.annotation-api` removed from `testusage`.** Demo `@PostConstruct` was replaced with a local `demo.reflections.PostConstruct`.
- **`slf4j-simple` is no longer a library dependency.** Consumers wiring the runtime proxies must supply their own SLF4J binding.
- **`reflections8` moved out of the main artifact** into `proxybuilder-testusage` test scope.
- **Generated-source marker renamed.** Generated classes now carry `@GeneratedByProxyBuilder` (previously `javax.annotation.Generated`-based).

### New features

- **Annotation-processor hardening.** Static-proxy processors now fail compilation for unsafe method shapes with clear `Messager` diagnostics:
  - `final` classes annotated for proxy generation.
  - `final`, `private`, and `static` methods on proxied types.
  - `static` failure can be downgraded to a warning with `-Aproxybuilder.failOnStaticMethods=false`.
  - `Object` methods (`equals`, `hashCode`, `toString`, `finalize`, `wait`, `notify`, `getClass`) are never generated.
- **Method-level decorator hooks.** Custom processors can now contribute code blocks per method via:
  - `beforeDelegation(...)`
  - `aroundDelegation(...)`
  - `afterDelegation(...)`

  This enables combining several method-level annotations on the same method (e.g. `@RequiresPolicy` + `@RequiresPermission` + `@RequiresRole`) without forking the processor.
- **Annotation lookup helpers** on `BasicAnnotationProcessor`:
  - `annotationsOn(ExecutableElement)`
  - `annotationsOn(ExecutableElement, Set<String>)`
  - `hasAnnotation(ExecutableElement, String)`
- **Generated-class-suffix hook.** Override `generatedClassSuffix(TypeElement)` or set `-Aproxybuilder.suffix=<Name>` to control the generated type's simple name.
- **Constructor-modifier hook.** Override `filterConstructorModifiers(Set<Modifier>)` to adjust generated constructor visibility.
- **`addStaticImports(JavaFile.Builder)` is now a default no-op** — implement it only when generated sources need static imports.
- **Processor options.** `@SupportedOptions` declares `proxybuilder.verbose`, `proxybuilder.suffix`, `proxybuilder.failOnStaticMethods`.
- **`MethodIdentifier` is a record** with defensive copies; the same applies to the reflection-based variant used by the object adapter.

### Improvements

- **JavaPoet 1.13.0** (was a much older release).
- **Records, `var`, and modern Java syntax** used in the processor codebase where it improves readability.
- **Generic return-type handling** in the static processors is refactored into focused helper methods (behavior preserved).
- **`FilerException` is handled explicitly** instead of by matching exception messages, making incremental rounds robust.
- **License headers deduplicated.** Single EUPL-1.2 header per file across the project.
- **README rewritten as a developer reference** covering runtime proxies, static-proxy annotations, custom-processor authoring, decorators, JPMS, processor options, annotation inheritance, and incremental compilation.

### Build & toolchain

- **Maven parent `com.svenruppert:dependencies` upgraded to `06.02.01`**, which fixes a duplicate `attach-sources/jar-no-fork` execution that surfaced when `_release_prepare` was active alongside the project's `release` profile.
- **`maven-compiler-plugin` raised to 3.15.0** for JDK 26 / class-file version 70 support (3.13.0's bundled ASM only reads up to v68 and broke during deploy-phase module-path inspection).
- **Maven Wrapper** pinned to Maven 4.0.0-rc-5 via `requireMavenVersion`.
- **`release` profile** attaches sources, javadoc, and GPG-signed artefacts; the parent's `_release_prepare` profile contributes the `jar-no-fork`/`test-jar-no-fork` variants.
- **JPMS validated with `jdeps --check com.svenruppert.proxybuilder`** as part of the development checks.
- **Tests:** JDK-compiler-based processor hardening tests (positive + negative) cover logging, metrics, virtual proxy, static object adapter, dynamic object adapter, suffix option, and method-level decorator composition.

### Maven Central — current limitation

`central-publishing-maven-plugin` 0.10.0 (current latest at the time of release) is not Maven-4-aware:

- It emits the Maven 4 consumer POM as a `*-consumer.pom` classifier and uploads the build POM as the primary `.pom`, which Central Portal's validator rejects with `Failed to associate file with coordinates …`.
- It has no stage-only mode: `skipPublishing=true` skips staging entirely (`No files to stage for artifact` in the log).

**Workaround** (used to publish 00.10.00):

```bash
./mvnw clean install -P release,_release_prepare
./scripts/clean-bundle-for-central.sh
# Upload target/central-publishing/central-bundle.zip via
# https://central.sonatype.com/publishing → Publish Component
```

The script reads `~/.m2/repository/com/svenruppert/<module>/<version>/` (Maven 4's local installer already names the consumer POM correctly as `<artifact>-<version>.pom` and keeps the build POM under `-build.pom`), drops the non-publishable files, generates the four checksum types Central expects, and zips a bundle for manual upload. See the *Releasing* section of the README for the full release flow.

Central Portal also validates each artifact's POM directly and does not resolve `<parent>` for required metadata, so `impl/pom.xml` now carries `<name>`, `<description>`, and `<url>` inline.

### Known limitations & non-goals

- **`proxybuilder-testusage`** intentionally keeps a small `maven-antrun-plugin` step that compiles annotation-generated sources before test compilation; removing it currently breaks `testCompile` because some tests reference generated classes directly. The module is excluded from Central publishing.
- **Gradle incremental annotation-processing metadata is not published.** Maven incremental compilation is verified.
- **No GitHub Actions workflow** is part of this release.
- **`HasLogger` (from `com.svenruppert:core`) remains the logging seam** — `Messager` is used only for compiler diagnostics, not for general logging.

### Project decisions carried into this release

- External parent `com.svenruppert:dependencies` stays.
- `com.svenruppert:core` stays in `compile` scope (required for `HasLogger`).
- `metrics-jmx` stays as an intentional dependency because JMX/console reporting is part of the metrics support.
- `slf4j-simple` is not provided as a transitive dependency.
- `reflections8` is only a test dependency in `proxybuilder-testusage`.