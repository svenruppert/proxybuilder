# proxybuilder-annotations

Annotations consumed and emitted by the [proxybuilder](http://www.proxybuilder.org) annotation processor.

Use this module on the **compile classpath** of any consumer that wants to reference the annotations directly (e.g. read `@GeneratedByProxyBuilder` at runtime, hand-apply `@SkipProxy`, tune behaviour with `@ProxyBuilderOptions`). The processor itself stays on `<annotationProcessorPaths>` so its transitive dependencies (JavaPoet, Metrics, SLF4J) never enter the consumer's compile classpath.

```xml
<dependency>
  <groupId>com.svenruppert</groupId>
  <artifactId>proxybuilder-annotations</artifactId>
  <version>00.11.00-SNAPSHOT</version>
</dependency>

<build>
  <plugins>
    <plugin>
      <artifactId>maven-compiler-plugin</artifactId>
      <configuration>
        <annotationProcessorPaths>
          <path>
            <groupId>com.svenruppert</groupId>
            <artifactId>proxybuilder</artifactId>
            <version>00.11.00-SNAPSHOT</version>
          </path>
        </annotationProcessorPaths>
      </configuration>
    </plugin>
  </plugins>
</build>
```

JPMS module name: `com.svenruppert.proxybuilder.annotations`.

## Annotations

| Annotation                | Target                                | Retention | Tier | Notes                                                                 |
|---------------------------|---------------------------------------|-----------|------|-----------------------------------------------------------------------|
| `@GeneratedByProxyBuilder`| TYPE                                  | RUNTIME   | 1    | Emitted on every wrapper; readable via reflection.                    |
| `@SkipProxy`              | METHOD, CONSTRUCTOR                   | SOURCE    | 1    | Skip a member during proxy generation.                                |
| `@ProxyBuilderOptions`    | TYPE                                  | SOURCE    | 1    | Per-class overrides for suffix / failOnStatic / excludeMethodNames.   |
| `@DelegatesTo`            | METHOD                                | RUNTIME   | 2    | Auto-emitted on every generated method; JLS-style source ref.         |
| `@WrappedBy`              | TYPE                                  | RUNTIME   | 2    | Hand-applied on the original; documents wrapper linkage.              |
| `@Internal`               | TYPE, METHOD, CONSTRUCTOR, FIELD      | CLASS     | 2    | Marks proxybuilder-internal API not intended for consumers.           |
| `@ProxyEntry`             | METHOD                                | SOURCE    | 3    | Reserved for future generator policies; no-op in 00.11.00.            |
| `@GeneratedSource`        | METHOD, CONSTRUCTOR, FIELD            | SOURCE    | 3    | Manual marker for partially-generated classes; processor does not emit. |
| `@ProxyName`              | TYPE                                  | SOURCE    | 3    | Per-class name pattern with `{Original}` placeholder; beats `suffix`. |

## Runtime helper

`ProxyEnforcement.requireWrapped(instance)` throws `IllegalStateException` if `instance.getClass()` carries `@WrappedBy(X)` and `instance` is not an `X`. Use in factories / DI graphs to refuse the un-wrapped original.

## Versioning

`ProxyBuilderVersion.VERSION` exposes the proxybuilder release this module belongs to.
