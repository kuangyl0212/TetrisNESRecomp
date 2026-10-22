cmake_minimum_required(VERSION 3.22)
project(tetris_android C CXX)

# TetrisAndroid/app/src/main/cpp -> TetrisAndroid (x4 up)
set(ANDROID_ROOT "${CMAKE_CURRENT_LIST_DIR}/../../../..")
set(NESRECOMP "${ANDROID_ROOT}/../nesrecomp")
set(RECOMP_UI "${ANDROID_ROOT}/../recomp-ui")
set(CYC "${NESRECOMP}/runner/cyc")
set(RUI_SRC "${RECOMP_UI}/src")
set(RUI_IMGUI "${RUI_SRC}/third_party/imgui")

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

# The recomp-ui in-game menu (the same runtime menu the PC build shows).
# Minimal set: the runtime model + its ImGui presentation, the host's menu
# rows, and the launcher core that cyc_ui_launcher.c links against.
set(RUI_MENU_SOURCES
    "${CYC}/cyc_ui_menu.c"
    "${CYC}/cyc_ui_launcher.c"
    "${CYC}/cyc_ui_imgui.cpp"
    "${RUI_SRC}/common/recomp_runtime_ui.c"
    "${RUI_SRC}/common/recomp_runtime_settings.c"
    "${RUI_SRC}/common/backends/imgui/runtime_ui_imgui.cpp"
    "${RUI_SRC}/common/launcher_ng_capi.c"
    "${RUI_SRC}/common/launcher_model.c"
    "${RUI_SRC}/common/launcher_platform_sdl2.c"
    "${RUI_SRC}/common/launcher_gl.c"
    "${RUI_SRC}/common/launcher_input.c"
    "${RUI_SRC}/common/launcher_files.c"
    "${RUI_SRC}/common/launcher_debug.c"
    "${RUI_SRC}/common/launcher_binds.c"
    "${RUI_SRC}/common/pad_binds.c"
    "${RUI_SRC}/common/launcher_udp_port.c"
    "${RUI_SRC}/common/launcher_boot_timing.c"
    "${RUI_SRC}/common/launcher_i18n.cpp"
    "${RUI_SRC}/common/launcher_mod_visibility.c"
    "${RUI_SRC}/common/recomp_moderation.c"
    "${RUI_SRC}/common/recomp_frame_blend.c"
    "${RUI_SRC}/common/recomp_flash_guard.c"
    "${RUI_SRC}/common/emoji/recomp_emoji.c"
    "${RUI_SRC}/common/emoji/recomp_emoji_freetype.c"
    "${RUI_SRC}/common/emoji/recomp_emoji_flags.c"
    "${RUI_SRC}/common/crc32.c"
    "${RUI_SRC}/common/sha256.c"
    "${RUI_SRC}/common/sha1.c"
    "${RUI_SRC}/common/keybinds.c"
    "${RUI_SRC}/common/ips_patch.c"
    "${RUI_SRC}/consoles/nes/nes_binds.c"
    "${RUI_SRC}/consoles/psx/psx_binds.c"
    "${RUI_SRC}/consoles/psx/psx_pad_binds.c"
    "${RUI_SRC}/consoles/psx/memcard_format.c"
    "${RUI_SRC}/consoles/snes/snes_pad_binds.c"
    "${RUI_SRC}/consoles/n64/n64_binds.c"
    "${RUI_SRC}/consoles/n64/n64_pad_binds.c"
    "${RUI_SRC}/consoles/genesis/genesis_binds.c"
    "${RUI_SRC}/consoles/gb/gb_binds.c"
    "${RUI_SRC}/third_party/tinyfiledialogs.c"
    "${RUI_IMGUI}/imgui.cpp"
    "${RUI_IMGUI}/imgui_draw.cpp"
    "${RUI_IMGUI}/imgui_tables.cpp"
    "${RUI_IMGUI}/imgui_widgets.cpp"
    "${RUI_IMGUI}/backends/imgui_impl_sdl2.cpp"
    "${RUI_IMGUI}/backends/imgui_impl_sdlrenderer2.cpp"
    "${RUI_IMGUI}/backends/imgui_impl_opengl3.cpp"
    "${RUI_SRC}/common/backends/imgui/launcher_imgui.cpp"
)

file(GLOB GAME_SOURCES CONFIGURE_DEPENDS
    "${CMAKE_CURRENT_LIST_DIR}/generated/game_cyc*.c")

add_library(main SHARED
    ${CYC_SOURCES}
    ${RUI_MENU_SOURCES}
    ${GAME_SOURCES}
    "${CMAKE_CURRENT_LIST_DIR}/wrapper.c")

target_include_directories(main PRIVATE
    "${CYC}"
    "${NESRECOMP}/runner/include"
    "${RECOMP_UI}/src"
    "${RECOMP_UI}/src/common"
    "${RUI_IMGUI}"
    "${RUI_IMGUI}/backends"
    "${ANDROID_ROOT}/SDL2/include")

target_compile_definitions(main PRIVATE
    CYC_WITH_SDL
    CYC_WITH_RECOMP_UI
    RECOMP_LAUNCHER
    RECOMP_UI_ENABLE_MODS=0
    SDL_MAIN_HANDLED
    CYC_GAME_REGION=0
    CYC_GAME_PLAYERS=2
    NESRECOMP_GAME_PLAYERS=2
    LNG_GLES2=1
    IMGUI_IMPL_OPENGL_ES2=1
    "CYC_BUILD_VERSION=\"0.1.0\""
    "CYC_BUILD_REVISION=\"android\"")

# cyc_host.c includes <SDL.h>, which on Android #defines main -> SDL_main.
# SDL_MAIN_HANDLED keeps the real `main` symbol; wrapper.c bridges SDL_main
# (called by SDLActivity's nativeRunMain) to it.
set_source_files_properties("${CYC}/cyc_host.c" PROPERTIES
    COMPILE_OPTIONS "-DSDL_MAIN_HANDLED")

find_library(LOG_LIB log)
find_library(GLES2_LIB GLESv2 REQUIRED)
find_library(EGL_LIB EGL REQUIRED)
target_link_libraries(main PRIVATE SDL2::SDL2 m ${LOG_LIB} ${GLES2_LIB} ${EGL_LIB})
