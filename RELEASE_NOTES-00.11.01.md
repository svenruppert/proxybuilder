# Release Notes

## 00.11.01 — 2026-05-28

Patch release that fixes the Maven Central deployment of the 00.11.00
feature set. **Consumers should upgrade directly from 00.10.01 to
00.11.01** — the 00.11.00 artifacts never made it to Central because
the new `proxybuilder-annotations` module was missing from the bundle.

No source / API changes since 00.11.00; the full feature description
(module split, nine new annotations, processor wiring, downstream
smoke test, pitest baseline) lives in `RELEASE_NOTES-00.11.00.md`.

### What was broken in 00.11.00

The release flow uses `scripts/clean-bundle-for-central.sh` because
`central-publishing-maven-plugin` 0.10.0 is not Maven-4-aware (see
the 00.10.00 release notes for the full story). That script copies
the publishable files directly out of `~/.m2/repository/com/svenruppert/<module>/<version>/`
based on a hardcoded `MODULES` array — and that array was not
updated when 00.11.00 introduced the new `proxybuilder-annotations`
module.

Consequence: `central-bundle.zip` shipped to the Central Portal
contained only `proxybuilder-parent` and `proxybuilder`. Anyone
adding `com.svenruppert:proxybuilder-annotations:00.11.00` to their
build got an unresolved dependency.

### Fixes

- **`scripts/clean-bundle-for-central.sh`**: `MODULES` now lists
  `proxybuilder-parent`, `proxybuilder-annotations`, `proxybuilder`
  (in that order — parent first, then the annotations dependency,
  then the processor).
- **`proxybuilder-annotations/pom.xml`**: inline `<licenses>`,
  `<scm>`, `<developers>` added. Central Portal validates each
  artifact's POM directly and does not resolve `<parent>` for these
  required fields; without them, the validator would have rejected
  the bundle even if the script bug had been fixed.
- **`impl/pom.xml`**: same three inline blocks added preventively,
  in case a future Central validator pass tightens the same check.
  This was already flagged in the 00.10.00 release notes as the
  recommended workaround if Central ever rejected the bundle for
  missing fields.

### Verification

After the release flow:

```bash
./mvnw clean install -P release,_release_prepare
./scripts/clean-bundle-for-central.sh

unzip -l target/central-publishing/central-bundle.zip \
    | grep -c proxybuilder-annotations    # expected: > 0
unzip -l target/central-publishing/central-bundle.zip \
    | grep -c build.pom                   # expected: 0
unzip -l target/central-publishing/central-bundle.zip \
    | grep -c consumer                    # expected: 0
```

### Coordinates

```xml
<dependency>
  <groupId>com.svenruppert</groupId>
  <artifactId>proxybuilder-annotations</artifactId>
  <version>00.11.01</version>
</dependency>
```

```xml
<annotationProcessorPaths>
  <path>
    <groupId>com.svenruppert</groupId>
    <artifactId>proxybuilder</artifactId>
    <version>00.11.01</version>
  </path>
</annotationProcessorPaths>
```

### Recommended for future releases

The audit in `Anforderungen-proxybuilder-00.12.00.md` collects the
known correctness, test-coverage, architecture and feature gaps that
surfaced while preparing this release.
