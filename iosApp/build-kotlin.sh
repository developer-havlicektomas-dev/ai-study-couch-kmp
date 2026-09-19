#!/bin/sh
set -eu
if [ "${OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED:-}" = "YES" ]; then exit 0; fi
if [ -z "${JAVA_HOME:-}" ]; then export JAVA_HOME="$(/usr/libexec/java_home -v 21)"; fi
cd "$SRCROOT/.."
./gradlew :composeApp:embedAndSignAppleFrameworkForXcode
