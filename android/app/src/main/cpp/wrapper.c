/* Android entry wrapper for the NESRecomp cycle host.
 *
 * SDL2's Android backend starts the app on a dedicated thread by calling
 * SDL_main(argc, argv) (see SDL_android.c / SDLActivity.java nativeRunMain).
 * The recompiler host in nesrecomp/runner/cyc/cyc_host.c exports a plain
 * `main` (compiled with -Dmain=nesrecomp_main so the symbol does not clash
 * with the SDL bootstrap). This wrapper bridges the two:
 *   - forces NESRECOMP_NO_LAUNCHER so the desktop ROM picker is skipped
 *   - forwards the ROM path that TetrisActivity.getArguments() provides
 */
#include <stdlib.h>
#include <string.h>

/* Provided by cyc_settings.c / cyc_host.c (real `main`, kept un-macro'd via
 * -DSDL_MAIN_HANDLED on the cyc_host.c compile). */
extern int main(int argc, char **argv);
extern const char *SDL_AndroidGetInternalStoragePath(void);

int SDL_main(int argc, char **argv)
{
    /* SDL's internal-storage query can return NULL before SDL_Init is done.
     * argv[1] is the ROM that TetrisActivity copied into getFilesDir(), so
     * its directory is a reliable writable home; export it for the runner. */
    const char *rom = (argc > 1) ? argv[1] : NULL;
    const char *app = SDL_AndroidGetInternalStoragePath();
    if ((!app || !*app) && rom) {
        const char *slash = strrchr(rom, '/');
        static char rom_dir[512];
        if (slash && slash != rom) {
            size_t n = (size_t)(slash - rom);
            if (n >= sizeof(rom_dir)) n = sizeof(rom_dir) - 1;
            memcpy(rom_dir, rom, n);
            rom_dir[n] = 0;
            app = rom_dir;
        }
    }
    if (app && *app) {
        setenv("NESRECOMP_DATA_DIR", app, 1);
        char diag[512];
        snprintf(diag, sizeof(diag), "%s/diagnostics", app);
        setenv("NESRECOMP_DIAGNOSTICS_DIR", diag, 1);
    }
    setenv("NESRECOMP_NO_LAUNCHER", "1", 1);
    /* AAudio is the default backend again: SDL_aaudio.c PlayDevice now retries
     * partial writes with a sane timeout (the 1 ms drop-on-full bug is fixed
     * upstream-local). Remove this override once the framework ships it. */
    return main(argc, argv);
}

/* --- JNI stubs for GbaControlsView ---------------------------------------
 * The touch controls view (borrowed from the pokeemerald-multiplatform
 * project) asks the game host for its border-background option and platform
 * settings. The NESRecomp host has neither; the stubs make the view fall
 * back to plain transparent buttons. */
#include <jni.h>

JNIEXPORT jint JNICALL
Java_com_tetris_recomp_GbaControlsView_getBorderBackground(JNIEnv *env, jclass cls)
{
    (void)env; (void)cls;
    return 9999; /* beyond the backgrounds list: skip the border image */
}

JNIEXPORT jint JNICALL
Java_com_tetris_recomp_GbaControlsView_getPlatformSetting(JNIEnv *env, jclass cls, jint setting)
{
    (void)env; (void)cls; (void)setting;
    return 0; /* default: no platform extras */
}
