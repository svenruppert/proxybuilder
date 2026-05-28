# Release Notes

## 00.11.00 — 2026-05-28

Minor release. Splits annotations into a new tiny `proxybuilder-annotations` JAR and ships nine new annotations plus a runtime version constant. The processor honours the new per-type generator-control annotations and emits the marker on every generated type at `RetentionPolicy.RUNTIME`, so consumers can introspect generated wrappers at runtime.

### Module split

- New module **`com.svenruppert:proxybuilder-annotations`** holds every annotation the project ships. Reactor layout:

  ```text
  proxybuilder-parent
  ├── proxybuilder-annotations   (NEW — annotations-only JAR)
  ├── proxybuilder               (annotation processor)
  └── proxybuilder-testusage     (integration / usage examples)
  ```

- Consumers add `proxybuilder-annotations` as a regular compile dependency and keep `proxybuilder` on `<annotationProcessorPaths>`. JavaPoet / Metrics / SLF4J no longer leak into the consumer compile classpath. The `DownstreamSmokeTest` regression test parses the generated wrapper's `import` block and asserts zero imports from `com.svenruppert.proxybuilder.*` outside `.annotations.*`.
- JPMS module name: `com.svenruppert.proxybuilder.annotations`. The processor module now `requires transitive com.svenruppert.proxybuilder.annotations;`.

### Breaking changes

- **`com.svenruppert.proxybuilder.GeneratedByProxyBuilder` removed.** Replaced by `com.svenruppert.proxybuilder.annotations.GeneratedByProxyBuilder`. The retention bumped from `SOURCE` to `RUNTIME`, the single `value()` member was dropped, and five named members were added: `processor`, `sourceClass`, `proxyBuilderVersion`, `date`, `comments`. No runtime consumers of the old `SOURCE`-retention copy existed by definition, so removal is safe — but the package change is breaking for any source that imported it.

### New annotations (Tier 1 — must)

- **`@GeneratedByProxyBuilder`** (RUNTIME, TYPE) — extended marker emitted on every wrapper; readable via reflection.
- **`@SkipProxy`** (SOURCE, METHOD/CONSTRUCTOR) — skip a single member during proxy generation without resorting to `final`/`private`/`static`. Honoured on both methods and constructors.
- **`@ProxyBuilderOptions`** (SOURCE, TYPE) — per-type overrides for `suffix`, `failOnStaticMethods` (`DEFAULT`/`TRUE`/`FALSE`), and `excludeMethodNames`.

### New annotations (Tier 2 — nice-to-have)

- **`@DelegatesTo`** (RUNTIME, METHOD) — auto-emitted on every generated wrapper method, value is a JLS-style source-method reference (e.g. `"test.Target#work(java.lang.String)"`). Can be suppressed with `-Aproxybuilder.suppressDelegatesTo=true`.
- **`@WrappedBy`** (RUNTIME, TYPE) — hand-applied on the original; documents the wrapper linkage. Pair with the `ProxyEnforcement.requireWrapped(...)` helper to refuse un-wrapped instances at runtime.
- **`@Internal`** (CLASS, TYPE/METHOD/CONSTRUCTOR/FIELD) — marks proxybuilder-internal API not intended for consumers. Applied to `MethodIdentifier`, the `addReturnTypeVariables` helper family, the new `@SkipProxy` / `excludeMethodNames` filters, the `@ProxyEntry` note emitter, and the `ResolvedOptions` record.

### New annotations (Tier 3 — experimental / minimal processor support)

- **`@ProxyEntry`** (SOURCE, METHOD) — recognised; processor emits a `Diagnostic.Kind.NOTE` ("@ProxyEntry is experimental, no-op in 00.11.00").
- **`@GeneratedSource`** (SOURCE, METHOD/CONSTRUCTOR/FIELD) — shipped for downstream documentation use; processor does not emit it.
- **`@ProxyName`** (SOURCE, TYPE) — per-class name pattern with `{Original}` placeholder; beats `@ProxyBuilderOptions.suffix()` and the global `-Aproxybuilder.suffix` option.

### Processor changes

- `BasicAnnotationProcessor.createAnnotationSpecGenerated(TypeElement)` now populates `processor`, `sourceClass`, `proxyBuilderVersion`, `date`, `comments` on the marker.
- New `applyProxyBuilderOptions(TypeElement)` caches the resolved suffix / static-method policy / excludes per type; `validateTargetType` and `process(...)` both call it.
- `@SkipProxy` and `excludeMethodNames` integrate into the existing `defineNewGeneratedMethod` / `defineGeneratedConstructorMethod` filters.
- `defineDelegatorMethod` emits `@DelegatesTo` on each generated wrapper method (functional-interface contracts are skipped).
- New compiler option `proxybuilder.suppressDelegatesTo` (also added to `@SupportedOptions`).
- `targetClassNameSimpleForGeneratedClass` consults `@ProxyName` first, then `@ProxyBuilderOptions.suffix()`, then the global suffix option, then the default.
- `failOnStaticMethods()` consults the per-type `@ProxyBuilderOptions.failOnStaticMethods()` override before the global option.

### Tests

- **`proxybuilder-annotations` (15 tests)** — `AnnotationContractTest` (retention + target contract per annotation, plus the `FailOnStatic` enum's three states), `ProxyEnforcementTest` (worked `@WrappedBy` / `ProxyEnforcement.requireWrapped` example with three cases), `ProxyBuilderVersionTest` (constant smoke).
- **`proxybuilder` (24 tests)** —
  - `AnnotationsModuleProcessorTest` (11 cases): runtime introspection of `@GeneratedByProxyBuilder`, `@SkipProxy` on method and on constructor, `@ProxyBuilderOptions` suffix / failOnStaticMethods / excludeMethodNames overrides, `@DelegatesTo` emission + suppression, `@ProxyName` precedence, `@ProxyEntry` NOTE diagnostic, and a migration smoke test for sources that use no new annotations.
  - `StaticProxyHardeningTest` (11 cases) — existing.
  - `CompileTestingWriterLifecycleTest` (1 case) — existing.
  - `DownstreamSmokeTest` (1 case) — proves the module-split architectural goal by asserting the generated wrapper source has zero imports from `com.svenruppert.proxybuilder.*` outside `.annotations.*`.

### Mutation testing

- `pitest-maven 1.25.2` is wired into the parent `pluginManagement` with `pitest-junit5-plugin 1.2.3` plus `junit-platform-launcher 1.13.4` and `junit-jupiter-engine 5.13.4` as compatibility overrides (the JUnit 5 plugin was built against Platform 1.x and is not yet JUnit Jupiter 6 aware).
- Each module supplies its own `targetClasses` / `targetTests` (the parent's defaults get cross-derived from `testusage`'s `junit.*` test package and miss the real tests).
- Baseline scores:

  | Module | Mutation | Line | Test strength |
  |---|---|---|---|
  | `proxybuilder-annotations` | **100 %** (3/3) | 78 % (7/9) | 100 % |
  | `proxybuilder` | **50 %** (175/347) | 67 % (673/1009) | 76 % |

- Run with:

  ```bash
  ./mvnw -pl proxybuilder-annotations org.pitest:pitest-maven:mutationCoverage
  ./mvnw -pl impl                     org.pitest:pitest-maven:mutationCoverage
  ```

  Reports land at `<module>/target/pit-reports/index.html`. The 117 `NO_COVERAGE` mutations in `proxybuilder` live in the runtime dynamic-proxy / object-adapter classes (`DynamicProxyBuilder`, `proxy.dymamic.virtual.*`, `RapidPMMetricsRegistry`, `objectadapter.dynamic.AdapterBuilder`) that currently carry no tests in the impl module.

### Coordinates

```xml
<dependency>
  <groupId>com.svenruppert</groupId>
  <artifactId>proxybuilder-annotations</artifactId>
  <version>00.11.00</version>
</dependency>
```

```xml
<annotationProcessorPaths>
  <path>
    <groupId>com.svenruppert</groupId>
    <artifactId>proxybuilder</artifactId>
    <version>00.11.00</version>
  </path>
</annotationProcessorPaths>
```
