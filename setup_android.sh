#!/bin/sh
set -e

git submodule update --init --recursive nesrecomp recomp-ui

# The Android build needs the runner's Android data-path fixes, which are not
# upstream yet. Apply them to the nesrecomp submodule checkout (idempotent).
if git -C nesrecomp apply --check "$(dirname "$0")/patches/nesrecomp-android.patch" 2>/dev/null; then
    git -C nesrecomp apply "$(dirname "$0")/patches/nesrecomp-android.patch"
    echo "Applied patches/nesrecomp-android.patch to nesrecomp/."
else
    echo "nesrecomp-android.patch already applied or not applicable - skipping."
fi

echo "Ready - build the PC target with CMake, or the APK from android/."
