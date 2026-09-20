#!/bin/sh
set -eu
if [ "${OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED:-}" = "YES" ]; then exit 0; fi
if [ -z "${JAVA_HOME:-}" ]; then export JAVA_HOME="$(/usr/libexec/java_home -v 21)"; fi
if [ "${CONFIGURATION:-}" = "Release" ]; then
    case "${STUDY_COACH_API_URL:-}" in
        https://example.invalid*|https://) echo "Set STUDY_COACH_API_URL to your HTTPS production server" >&2; exit 1 ;;
        https://*) ;;
        *) echo "Release requires an HTTPS STUDY_COACH_API_URL" >&2; exit 1 ;;
    esac
fi
cd "$SRCROOT/.."
./gradlew :composeApp:embedAndSignAppleFrameworkForXcode
