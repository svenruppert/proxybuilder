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

- Consumers add `proxybuilder-annotations` as a regular compile dependency and keep `proxybuilder` on `<annotationProcessorPaths>`. JavaPoet / Metrics / SLF4J no longer leak into the consumer compile classpath.
- JPMS module name: `com.svenruppert.proxybuilder.annotations`. The processor module now `requires transitive com.svenruppert.proxybuilder.annotations;`.

### Breaking changes

- **`com.svenruppert.proxybuilder.GeneratedByProxyBuilder` removed.** Replaced by `com.svenruppert.proxybuilder.annotations.GeneratedByProxyBuilder`. The retention bumped from `SOURCE` to `RUNTIME`, the single `value()` member was dropped, and five named members were added: `processor`, `sourceClass`, `proxyBuilderVersion`, `date`, `comments`. No runtime consumers of the old `SOURCE`-retention copy existed by definition, so removal is safe — but the package change is breaking for any source that imported it.

### New annotations (Tier 1 — must)

- **`@GeneratedByProxyBuilder`** (RUNTIME, TYPE) — extended marker emitted on every wrapper; readable via reflection.
- **`@SkipProxy`** (SOURCE, METHOD/CONSTRUCTOR) — skip a single member during proxy generation without resorting to `final`/`private`/`static`.
- **`@ProxyBuilderOptions`** (SOURCE, TYPE) — per-type overrides for `suffix`, `failOnStaticMethods` (`DEFAULT`/`TRUE`/`FALSE`), and `excludeMethodNames`.

### New annotations (Tier 2 — nice-to-have)

- **`@DelegatesTo`** (RUNTIME, METHOD) — auto-emitted on every generated wrapper method, value is a JLS-style source-method reference (e.g. `"test.Target#work(java.lang.String)"`). Can be suppressed with `-Aproxybuilder.suppressDelegatesTo=true`.
- **`@WrappedBy`** (RUNTIME, TYPE) — hand-applied on the original; documents the wrapper linkage. Pair with the `ProxyEnforcement.requireWrapped(...)` helper to refuse un-wrapped instances at runtime.
- **`@Internal`** (CLASS, TYPE/METHOD/CONSTRUCTOR/FIELD) — marks proxybuilder-internal API not intended for consumers.

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

- New `AnnotationsModuleProcessorTest` (9 tests) covering: runtime introspection of `@GeneratedByProxyBuilder`, `@SkipProxy`, per-type suffix / failOnStatic / excludeMethodNames overrides, `@DelegatesTo` emission + suppression, `@ProxyName` precedence, and a migration smoke test for sources that use no new annotations.
- Existing `StaticProxyHardeningTest` (11 tests) and `CompileTestingWriterLifecycleTest` (1 test) unchanged and green.

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
