# TetrisNESRecomp

A static recompilation of **Tetris (NES, Nintendo 1989)** for modern PCs, built with
[nesrecomp](https://github.com/mstan/nesrecomp). The original 6502 machine code is
translated to C at build time and compiled to native code for direct execution —
**this is not an emulator**. NES hardware (PPU, APU, MMC1 cartridge) is simulated by
the cycle-accurate runner library.

Tested route coverage: **100.0% native CPU cycles** (title → game type → level →
music → gameplay → piece movement/rotation), zero interpreter fallback.

## Features

- Native executable — no emulator core, no ROM file needed at runtime
- Cycle-accurate LLE PPU/APU and MMC1 cartridge model (NESRecomp cycle backend)
- Launcher window with ROM picker, in-game menu, settings persistence (recomp-ui)
- Gamepad support (SDL2, XInput/PlayStation/Switch Pro, hotplug)
- Save states, fast-forward, integer-scale rendering, fullscreen

## Building

Prerequisites: CMake 3.20+, Python 3.11+, a C11/C++17 compiler.

```bat
git clone https://github.com/kuangyl0212/TetrisNESRecomp.git
cd TetrisNESRecomp

:: fetch the pinned nesrecomp + recomp-ui checkouts
setup.bat

:: configure with your own Tetris (USA) ROM and build
cmake -S . -B build -G "MinGW Makefiles" -DCMAKE_BUILD_TYPE=Release -DNESRECOMP_ROM=C:/path/to/tetris.nes
cmake --build build --config Release --parallel 8
```

- Verified toolchain: [w64devkit](https://github.com/skeeto/w64devkit) (GCC 16.2) +
  CMake 4.4.4 on Windows x64. MSVC (Visual Studio 2022, `-G "Visual Studio 17 2022" -A x64`)
  is the upstream-primary toolchain and should work equally well.
- `cyc_seeds.txt` ships the dispatch coverage collected from tested playthrough
  routes, so the generated code compiles the full route natively out of the box.
  Re-collect any time: `nes_game ROM --frames 3000 --input route.txt --miss-log cyc_seeds.txt`
  and rebuild.
- The build produces `nes_game.exe` next to an `SDL2.dll` copy and the launcher
  `assets/` folder — keep them together.

## Android build (experimental)

The same source tree also builds an Android APK (Gradle + NDK + vendored SDL2,
modeled after the pokeemerald-multiplatform project layout).

Prerequisites: Android SDK (platform 36, NDK 26.3.11579264, CMake 3.22.1 via the
SDK manager), Gradle 8.5, Python 3.11+.

```bash
./setup_android.sh                     # fetch submodules + apply the runner's Android patch
cp /path/to/tetris.nes android/app/src/main/assets/tetris.nes   # your own ROM (not in the repo)
cd android
gradle assembleRelease
# -> app/build/outputs/apk/release/app-release.apk
```

Notes:

- `SDL2/` is vendored (not a submodule) because it carries an **AAudio
  partial-write fix**: `aaudio_PlayDevice` used a 1 ms write timeout and
  silently dropped unwritten frames on low-latency devices, garbling playback.
  The fix retries partial writes with a 20 ms budget.
- `patches/nesrecomp-android.patch` redirects the runner's config/save paths to
  app-internal storage (`/proc/self/exe` is read-only APK content on Android).
- The bundled ROM is copied from APK assets into internal storage at first
  launch; the on-screen controls auto-hide while a physical gamepad is
  connected.

## Usage

Double-click `nes_game.exe` and pick your ROM in the launcher, or run it directly:

```
nes_game.exe C:/path/to/tetris.nes
```

Default keyboard bindings (editable in `keybinds.ini` / the in-game menu):

| Action | Key |
|---|---|
| D-pad | Arrow keys |
| A / B | Z / X |
| Start / Select | Enter / `\` |
| Fast-forward | Tab |
| Fullscreen | F11 |
| Save / load state | F8 / F9 |
| Screenshot | F12 |
| In-game menu | Esc |

Gamepads work out of the box (first pad = player 1, hotplug supported).

## Repository layout

| Path | Purpose |
|------|---------|
| `CMakeLists.txt` | Cycle-backend game target (`nesrecomp_add_cycle_game`) |
| `cyc_seeds.txt` | Interpreter-miss coverage seeds from tested playthrough routes |
| `nesrecomp/` | Static recompiler framework (git submodule, pinned) |
| `recomp-ui/` | Launcher / in-game UI (git submodule, pinned) |
| `android/` | Android APK project (Gradle + vendored SDL2 + touch controls) |
| `patches/` | Local runner fixes needed by the Android build (applied by `setup_android`) |

## Legal

- **No copyrighted ROM data is included** in this repository or its releases.
  You must supply your own legally obtained copy of Tetris (USA).
- Tetris (NES) is © Nintendo. This project is an interoperability/educational
  work; the game itself is not distributed or claimed.
- Licensed under the [PolyForm Noncommercial License 1.0.0](LICENSE) — the same
  license as the upstream nesrecomp ecosystem. Noncommercial use only.

## Credits

- [nesrecomp](https://github.com/mstan/nesrecomp) and
  [recomp-ui](https://github.com/mstan/recomp-ui) by Matthew Stanley ([@mstan](https://github.com/mstan))
- TriCNES PPU/APU model by Chris Siebert (MIT)

---

## 中文说明

本项目将 NES 版《俄罗斯方块》（任天堂，1989）静态重编译为现代平台原生程序：
构建期把 6502 机器码翻译为 C 并编译为原生代码，PPU/APU/MMC1 由周期精确的硬件模型模拟（非模拟器）。
已验证游玩路线 **100% 原生执行**。构建需自备正版 ROM（仓库与 Release 均不含 ROM 数据），
按上文 Building 步骤操作即可，Windows 下推荐 w64devkit 或 Visual Studio 2022 工具链。
采用 PolyForm Noncommercial 1.0.0 许可证（与上游 nesrecomp 生态一致），仅限非商业用途。
