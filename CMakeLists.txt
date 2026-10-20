cmake_minimum_required(VERSION 3.20)
project(TetrisRecomp C CXX)

set(CMAKE_C_STANDARD 11)
set(CMAKE_CXX_STANDARD 17)

set(NESRECOMP_ROOT "${CMAKE_CURRENT_SOURCE_DIR}/nesrecomp" CACHE PATH "NESRecomp engine checkout (git submodule)")
set(NESRECOMP_ROM "" CACHE FILEPATH "Tetris (NES, USA) ROM image - not distributed, supply your own")
set(NESRECOMP_CYCLE_SEEDS "${CMAKE_CURRENT_SOURCE_DIR}/cyc_seeds.txt" CACHE FILEPATH "Cycle coverage seeds from tested playthrough routes")

if(NOT EXISTS "${NESRECOMP_ROM}")
    message(FATAL_ERROR
        "Set NESRECOMP_ROM to your own Tetris (NES, USA) ROM image, e.g.:\n"
        "  cmake -S . -B build -G \"MinGW Makefiles\" -DCMAKE_BUILD_TYPE=Release -DNESRECOMP_ROM=C:/path/to/tetris.nes\n"
        "No copyrighted ROM data is included in this repository.")
endif()

# recomp-ui (git submodule) is picked up automatically from the source root:
# it provides the launcher window, the in-game menu and settings persistence.

include("${NESRECOMP_ROOT}/runner/cyc/project.cmake")
nesrecomp_add_cycle_game(TetrisRecomp
    ROM "${NESRECOMP_ROM}"
    SEED_FILE "${NESRECOMP_CYCLE_SEEDS}"
    REGION NTSC
    PLAYERS 2)
