@echo off
setlocal

:: nesrecomp/ and recomp-ui/ are git submodules; fetch them at the commits this
:: repo pins (the gitlinks recorded in the index; see .gitmodules).
git submodule update --init --recursive nesrecomp recomp-ui
if errorlevel 1 ( echo Error: submodule update failed & exit /b 1 )

echo Ready - nesrecomp + recomp-ui checked out at their pinned commits.
