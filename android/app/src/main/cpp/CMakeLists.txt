cmake_minimum_required(VERSION 3.22)
project(tetris_android C)

# TetrisAndroid/app/src/main/cpp -> TetrisAndroid (x4 up)
set(ANDROID_ROOT "${CMAKE_CURRENT_LIST_DIR}/../../../..")
set(NESRECOMP "${ANDROID_ROOT}/../nesrecomp")
set(CYC "${NESRECOMP}/runner/cyc")

set(SDL_STATIC ON CACHE BOOL "" FORCE)
set(SDL_SHARED ON CACHE BOOL "" FORCE)
set(SDL_TEST OFF CACHE BOOL "" FORCE)
add_subdirectory("${ANDROID_ROOT}/SDL2" "${CMAKE_BINARY_DIR}/SDL2")

# The NESRecomp cycle runner, mirroring NESRECOMP_CYC_SOURCES in
# runner/cyc/cyc.cmake plus the SDL host additions from
# nesrecomp_cyc_enable_sdl(). The generated game code in generated/ was
# produced at PC build time by the same nesrecomp checkout, so runner and
# generated sources always match.
set(CYC_SOURCES
    cpu6502.c
    cpu6502_interp.c
    hw_machine.c
    hw_mapper.c
    hw_fds.c
    hw_fds_audio.c
    hw_fds_hle.c
    vendor/emu2413/emu2413.c
    hw_ppu.c
    hw_apu.c
    hw_palette.c
    cyc_trace.c
    cyc_ring.c
    cyc_run.c
    cyc_ramview.c
    cyc_host.c
    cyc_diagnostics.c
    cyc_accuracycoin.c
    cyc_png.c
    cyc_disk_action.c
    cyc_overlay.c
    cyc_fds_bios.c
    cyc_host_extras_none.c
    cyc_session.c
    cyc_hooks.c
    cyc_mod.c
    cyc_state.c
    cyc_video.c
    cyc_render.c
    cyc_sdl.c
    cyc_input.c
    cyc_settings.c
    cyc_tcp.c
)
list(TRANSFORM CYC_SOURCES PREPEND "${CYC}/")
list(APPEND CYC_SOURCES
    "${CYC}/../src/mod_function_hooks.c"
    "${CYC}/../src/mod_savestate.c"
    "${CYC}/../src/rollback/nes_session_config.c")

file(GLOB GAME_SOURCES CONFIGURE_DEPENDS
    "${CMAKE_CURRENT_LIST_DIR}/generated/game_cyc*.c")

add_library(main SHARED
    ${CYC_SOURCES}
    ${GAME_SOURCES}
    "${CMAKE_CURRENT_LIST_DIR}/wrapper.c")

target_include_directories(main PRIVATE
    "${CYC}"
    "${NESRECOMP}/runner/include"
    "${ANDROID_ROOT}/SDL2/include")

target_compile_definitions(main PRIVATE
    CYC_WITH_SDL
    CYC_STARTUP_DIAGNOSTICS=1
    CYC_GAME_REGION=0
    CYC_GAME_PLAYERS=2
    NESRECOMP_GAME_PLAYERS=2
    "CYC_BUILD_VERSION=\"0.1.0\""
    "CYC_BUILD_REVISION=\"android\"")

# cyc_host.c includes <SDL.h>, which on Android #defines main -> SDL_main.
# SDL_MAIN_HANDLED keeps the real `main` symbol; wrapper.c bridges SDL_main
# (called by SDLActivity's nativeRunMain) to it.
set_source_files_properties("${CYC}/cyc_host.c" PROPERTIES
    COMPILE_OPTIONS "-DSDL_MAIN_HANDLED")

find_library(LOG_LIB log)
target_link_libraries(main PRIVATE SDL2::SDL2 m ${LOG_LIB})
