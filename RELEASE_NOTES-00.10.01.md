# Release Notes

## 00.10.01 — 2026-05-28

Patch release on top of `00.10.00`. No API changes.

### Fixes

- **`BasicAnnotationProcessor.writeDefinedClass`**: close the generated-source writer so `com.google.testing.compile`'s `InMemoryJavaFileManager` publishes the written content. The previous code only `flush()`-ed the writer; OpenJDK's real `Filer` tolerates that, but `InMemoryJavaFileManager` only makes the file readable once the writer is closed. Downstream projects that exercise proxybuilder processors via `compile-testing` 0.21.0 saw an unmessaged `java.io.FileNotFoundException` from `JavaFileObject.openInputStream()` / `contentsAsUtf8String()`; that path now works.

  The same path is used by `writeFunctionalInterface(...)`, so the object-adapter functional-interface output is fixed as well.

### Tests

- New regression test `CompileTestingWriterLifecycleTest` runs `StaticLoggingProxyAnnotationProcessor` through `Compiler.javac()` and asserts `contentsAsUtf8String()` on the generated source — the exact call that threw on `00.10.00`.
- `com.google.testing.compile:compile-testing:0.21.0` added as a `test`-scoped dependency on the `proxybuilder` module.
