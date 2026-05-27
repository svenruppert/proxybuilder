#!/usr/bin/env bash
#
# Builds a Central Portal-compatible bundle from artifacts in the local Maven
# repository.
#
# Background: central-publishing-maven-plugin 0.10.0 has no stage-only mode
# (skipPublishing=true skips staging entirely), and its normal flow uploads
# the build POM as the primary .pom and the consumer POM as a consumer-
# classified artefact, which the Central Portal validator rejects with
# "Failed to associate file with coordinates". This script bypasses the plugin
# and assembles the bundle directly from ~/.m2/repository, where Maven 4's
# installer already names the consumer POM correctly (<artifact>-<version>.pom)
# and keeps the build POM as a separate -build classifier we drop here.
#
# Prerequisite:
#   ./mvnw clean install -P release,_release_prepare
# That populates ~/.m2/repository/com/svenruppert/<module>/<version>/ with the
# signed jar / sources / javadoc / pom (plus their .asc signatures).

set -euo pipefail

REPO_ROOT=$(cd "$(dirname "$0")/.." && pwd)
M2_REPO="${HOME}/.m2/repository"
GROUP_PATH="com/svenruppert"

# Modules to bundle. proxybuilder-testusage is intentionally excluded — it's an
# integration / usage example, not a published artefact.
MODULES=(
    "proxybuilder-parent"
    "proxybuilder"
)

VERSION=$(grep -m 1 -E "^  <version>" "$REPO_ROOT/pom.xml" \
    | sed -E 's:^[[:space:]]*<version>([^<]+)</version>.*:\1:')

if [ -z "$VERSION" ]; then
    echo "ERROR: could not read project <version> from pom.xml." >&2
    exit 1
fi

STAGING="$REPO_ROOT/target/central-bundle-staging"
BUNDLE_DIR="$REPO_ROOT/target/central-publishing"
BUNDLE="$BUNDLE_DIR/central-bundle.zip"

rm -rf "$STAGING"
mkdir -p "$STAGING" "$BUNDLE_DIR"

generate_checksums() {
    local file="$1"
    md5 -q       "$file"                       > "$file.md5"
    shasum -a 1   "$file" | awk '{print $1}'   > "$file.sha1"
    shasum -a 256 "$file" | awk '{print $1}'   > "$file.sha256"
    shasum -a 512 "$file" | awk '{print $1}'   > "$file.sha512"
}

total_files=0
for module in "${MODULES[@]}"; do
    src="$M2_REPO/$GROUP_PATH/$module/$VERSION"
    dst="$STAGING/$GROUP_PATH/$module/$VERSION"

    if [ ! -d "$src" ]; then
        echo "ERROR: $src does not exist." >&2
        echo "       Run './mvnw clean install -P release,_release_prepare' first." >&2
        exit 1
    fi

    mkdir -p "$dst"

    # Keep: consumer pom (*.pom — Maven 4's local-repo name), main jar, sources,
    # javadoc, plus the .asc signature for each.
    # Drop:  *-build.pom (Maven 4 build POM), tests + test-sources (not for
    #        Central), _remote.repositories, *.lastUpdated.
    shopt -s nullglob
    for f in "$src"/*; do
        name=$(basename "$f")
        case "$name" in
            *-build.pom|*-build.pom.asc) continue ;;
            *-tests.jar|*-tests.jar.asc) continue ;;
            *-test-sources.jar|*-test-sources.jar.asc) continue ;;
            _remote.repositories|*.lastUpdated) continue ;;
            *.pom|*.pom.asc|*.jar|*.jar.asc)
                cp "$f" "$dst/"
                ;;
        esac
    done
    shopt -u nullglob

    module_files=0
    for f in "$dst"/*; do
        case "$f" in
            *.md5|*.sha1|*.sha256|*.sha512) continue ;;
        esac
        generate_checksums "$f"
        module_files=$((module_files + 1))
    done

    echo "Staged $module $VERSION ($module_files primary files + checksums)"
    total_files=$((total_files + module_files))
done

rm -f "$BUNDLE"
(cd "$STAGING" && zip -qr "$BUNDLE" .)
ls -lh "$BUNDLE"

cat <<EOF

Bundle ready: $BUNDLE
Staged $total_files primary files across ${#MODULES[@]} modules (plus signatures and four checksum types each).

Next steps:
  1. Open https://central.sonatype.com/publishing
  2. Click 'Publish Component'
  3. Upload the bundle above
  4. Wait for validation, then publish
EOF
