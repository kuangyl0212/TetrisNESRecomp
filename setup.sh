#!/bin/sh
# nesrecomp/ and recomp-ui/ are git submodules; fetch them at the commits this
# repo pins (the gitlinks recorded in the index; see .gitmodules).
set -e
git submodule update --init --recursive nesrecomp recomp-ui
echo "Ready - nesrecomp + recomp-ui checked out at their pinned commits."
