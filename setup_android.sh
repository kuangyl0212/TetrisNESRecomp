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

# The save manager rides recomp-ui's card-strip / single-page presentation,
# which is not upstream yet either (idempotent).
if git -C recomp-ui apply --check "$(dirname "$0")/patches/recomp-ui-savemgr.patch" 2>/dev/null; then
    git -C recomp-ui apply "$(dirname "$0")/patches/recomp-ui-savemgr.patch"
    echo "Applied patches/recomp-ui-savemgr.patch to recomp-ui/."
else
    echo "recomp-ui-savemgr.patch already applied or not applicable - skipping."
fi

echo "Ready - build the PC target with CMake, or the APK from android/."
