package com.tetris.recomp;

import android.graphics.Rect;
import android.hardware.input.InputManager;
import android.os.Bundle;
import android.os.Build;
import android.view.InputDevice;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.libsdl.app.SDLActivity;

public class TetrisActivity extends SDLActivity {
    private GbaControlsView mControlsView;
    private InputManager mInputManager;
    private File mRomFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Extract the bundled ROM into internal storage before SDL starts.
        mRomFile = new File(getFilesDir(), "tetris.nes");
        if (!mRomFile.exists() || mRomFile.length() == 0) {
            try (InputStream in = getAssets().open("tetris.nes");
                 OutputStream out = new FileOutputStream(mRomFile)) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) > 0) {
                    out.write(buf, 0, n);
                }
            } catch (Exception e) {
                mRomFile = null;
            }
        }

        super.onCreate(savedInstanceState);
        mControlsView = new GbaControlsView(this);
        mLayout.addView(mControlsView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mInputManager = (InputManager) getSystemService(INPUT_SERVICE);
    }

    @Override
    protected String[] getArguments() {
        // NESRecomp cycle host: argv[1] is the ROM path. NESRECOMP_NO_LAUNCHER
        // is set in the native wrapper (SDL_main) to skip the desktop launcher.
        if (mRomFile != null) {
            return new String[] { mRomFile.getAbsolutePath() };
        }
        return new String[0];
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mInputManager != null) {
            mInputManager.registerInputDeviceListener(mDeviceListener, null);
        }
        // Handle a gamepad that is already connected when resuming.
        updateControlsVisibility();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mInputManager != null) {
            mInputManager.unregisterInputDeviceListener(mDeviceListener);
        }
    }

    private final InputManager.InputDeviceListener mDeviceListener =
            new InputManager.InputDeviceListener() {
                @Override
                public void onInputDeviceAdded(int deviceId) {
                    updateControlsVisibility();
                }
                @Override
                public void onInputDeviceRemoved(int deviceId) {
                    updateControlsVisibility();
                }
                @Override
                public void onInputDeviceChanged(int deviceId) {
                    updateControlsVisibility();
                }
            };

    /** Hide the on-screen controls while a physical gamepad is connected. */
    private void updateControlsVisibility() {
        if (mControlsView == null || mInputManager == null) return;

        boolean gamepadConnected = false;
        for (int deviceId : mInputManager.getInputDeviceIds()) {
            InputDevice device = mInputManager.getInputDevice(deviceId);
            if (device == null || device.isVirtual()) continue;
            int sources = device.getSources();
            if ((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD
                    || (sources & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK) {
                gamepadConnected = true;
                break;
            }
        }

        if (gamepadConnected) {
            mControlsView.setVisibility(View.GONE);
        } else {
            mControlsView.setVisibility(View.VISIBLE);
            mControlsView.animate().alpha(1f).setDuration(200);
        }
    }

    @Override
    public void setOrientationBis(int width, int height, boolean resizable, String hint) {
        // The manifest already keeps this activity in sensor landscape mode.
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            return;
        }

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    @Override
    protected String[] getLibraries() {
        return new String[] { "SDL2", "main" };
    }
}
