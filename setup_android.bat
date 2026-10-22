@echo off
setlocal

git submodule update --init --recursive nesrecomp recomp-ui
if errorlevel 1 ( echo Error: submodule update failed & exit /b 1 )

:: The Android build needs the runner's Android data-path fixes, which are not
:: upstream yet. Apply them to the nesrecomp submodule checkout.
git -C nesrecomp apply --check "%~dp0patches\nesrecomp-android.patch" 2>nul
if errorlevel 1 (
    echo nesrecomp-android.patch already applied or not applicable - skipping.
) else (
    git -C nesrecomp apply "%~dp0patches\nesrecomp-android.patch"
    if errorlevel 1 ( echo Error: patch failed & exit /b 1 )
    echo Applied patches\nesrecomp-android.patch to nesrecomp/.
)

git -C recomp-ui apply --check "%~dp0patches\recomp-ui-savemgr.patch" 2>nul
if errorlevel 1 (
    echo recomp-ui-savemgr.patch already applied or not applicable - skipping.
) else (
    git -C recomp-ui apply "%~dp0patches\recomp-ui-savemgr.patch"
    if errorlevel 1 ( echo Error: patch failed & exit /b 1 )
    echo Applied patches\recomp-ui-savemgr.patch to recomp-ui/.
)

echo Ready - build the PC target with CMake, or the APK from android/.
