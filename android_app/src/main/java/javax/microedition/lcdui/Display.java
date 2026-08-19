package javax.microedition.lcdui;

import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.content.Context;
import javax.microedition.midlet.MIDlet;

public class Display {
    public static final int LIST_ELEMENT = 1;
    public static final int CHOICE_GROUP_ELEMENT = 2;
    public static final int ALERT = 3;
    public static final int COLOR_BACKGROUND = 0;
    public static final int COLOR_FOREGROUND = 1;
    public static final int COLOR_HIGHLIGHTED_BACKGROUND = 2;
    public static final int COLOR_HIGHLIGHTED_FOREGROUND = 3;
    public static final int COLOR_BORDER = 4;
    public static final int COLOR_HIGHLIGHTED_BORDER = 5;

    private static Display activeDisplay;
    private static Context appContext;
    private static Handler mainHandler = new Handler(Looper.getMainLooper());
    private Displayable currentDisplayable;

    public static void setAppContext(Context context) {
        appContext = context;
    }

    public static Display getActiveDisplay() {
        return activeDisplay;
    }

    public static Display getDisplay(MIDlet m) {
        if (activeDisplay == null) {
            activeDisplay = new Display();
        }
        return activeDisplay;
    }

    public Displayable getCurrent() {
        return currentDisplayable;
    }

    public void setCurrent(Displayable nextDisplayable) {
        if (currentDisplayable != nextDisplayable) {
            if (currentDisplayable instanceof Canvas) {
                ((Canvas) currentDisplayable).hideNotifyInternal();
            }
            currentDisplayable = nextDisplayable;
            if (currentDisplayable instanceof Canvas) {
                ((Canvas) currentDisplayable).showNotifyInternal();
            }
        }
    }

    public void setCurrent(Alert alert, Displayable nextDisplayable) {
        setCurrent(nextDisplayable);
    }

    public void callSerially(Runnable r) {
        if (r != null) {
            mainHandler.post(r);
        }
    }

    public boolean isColor() {
        return true;
    }

    public int numColors() {
        return 16777216; // 24-bit/32-bit color
    }

    public int numAlphaLevels() {
        return 256;
    }

    public int getColor(int colorSpecifier) {
        switch (colorSpecifier) {
            case COLOR_BACKGROUND: return 0xFFFFFF;
            case COLOR_FOREGROUND: return 0x000000;
            case COLOR_HIGHLIGHTED_BACKGROUND: return 0x000080;
            case COLOR_HIGHLIGHTED_FOREGROUND: return 0xFFFFFF;
            case COLOR_BORDER: return 0x808080;
            case COLOR_HIGHLIGHTED_BORDER: return 0x000000;
            default: return 0;
        }
    }

    public int getBorderStyle(boolean highlighted) {
        return Graphics.SOLID;
    }

    public boolean vibrate(int duration) {
        if (appContext != null && duration > 0) {
            try {
                Vibrator v = (Vibrator) appContext.getSystemService(Context.VIBRATOR_SERVICE);
                if (v != null && v.hasVibrator()) {
                    v.vibrate(duration);
                    return true;
                }
            } catch (Throwable ignored) {}
        }
        return false;
    }

    public boolean flashBacklight(int duration) {
        return true;
    }
}
