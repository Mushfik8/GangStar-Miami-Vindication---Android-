package com.gangstar.miami;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import javax.microedition.rms.RecordStore;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable, Thread.UncaughtExceptionHandler {
    private Thread gameThread;
    private volatile boolean isRunning = false;
    private final TouchHUD touchHUD;
    private final Paint paint = new Paint();
    private final Paint bgPaint = new Paint();

    private final Rect srcRect = new Rect();
    private final RectF dstRect = new RectF();
    private final android.graphics.Matrix drawMatrix = new android.graphics.Matrix();
    private final android.graphics.Matrix touchInvertMatrix = new android.graphics.Matrix();

    public static String crashStackTrace = null;
    private final Paint errorPaint = new Paint();

    private int viewWidth = 800;
    private int viewHeight = 480;
    private boolean isGameStarted = false;

    public GameView(Context context) {
        super(context);
        touchHUD = new TouchHUD();
        init(context);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        touchHUD = new TouchHUD();
        init(context);
    }

    private void init(Context context) {
        getHolder().addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);

        paint.setFilterBitmap(true);
        paint.setAntiAlias(true);
        bgPaint.setColor(Color.BLACK);

        errorPaint.setColor(Color.RED);
        errorPaint.setTextSize(30f);
        errorPaint.setAntiAlias(true);
        Thread.setDefaultUncaughtExceptionHandler(this);

        Display.setAppContext(context.getApplicationContext());
        RecordStore.init(context.getApplicationContext());
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        java.io.StringWriter sw = new java.io.StringWriter();
        e.printStackTrace(new java.io.PrintWriter(sw));
        crashStackTrace = "Crash on thread " + t.getName() + ":\n" + sw.toString();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isRunning = true;
        gameThread = new Thread(this, "GameLoopThread");
        gameThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        this.viewWidth = width;
        this.viewHeight = height;

        // Visual portrait aspect ratio is 480x800
        float visualW = 480f;
        float visualH = 800f;
        float gameAspect = visualW / visualH;
        float viewAspect = (float) width / (float) height;

        float destW, destH, destX, destY;
        if (viewAspect > gameAspect) {
            destH = height;
            destW = height * gameAspect;
            destX = (width - destW) / 2f;
            destY = 0;
        } else {
            destW = width;
            destH = width / gameAspect;
            destX = 0;
            destY = (height - destH) / 2f;
        }

        dstRect.set(destX, destY, destX + destW, destY + destH);

        // Transform original 800x480 J2ME frame -> 90 deg clockwise to 480x800 visual portrait
        drawMatrix.reset();
        drawMatrix.postRotate(90);
        drawMatrix.postTranslate(480, 0);
        drawMatrix.postScale(destW / visualW, destH / visualH);
        drawMatrix.postTranslate(destX, destY);

        drawMatrix.invert(touchInvertMatrix);

        touchHUD.setGameViewport(destX, destY, destW, destH, destW / visualW);
        touchHUD.setTouchInvertMatrix(touchInvertMatrix);
        touchHUD.updateLayout(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRunning = false;
        if (gameThread != null) {
            try {
                gameThread.join(500);
            } catch (InterruptedException ignored) {}
            gameThread = null;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (touchHUD.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void startMidlet() {
        if (isGameStarted) return;
        isGameStarted = true;

        Thread t = new Thread(new MidletRunner());
        t.setName("MIDletLauncher");
        t.start();
    }

    private static class MidletRunner implements Runnable {
        @Override
        public void run() {
            try {
                // Set default Gameloft application properties
                MIDlet.setAppProperty("MIDlet-Name", "GangStar: Miami Vindication");
                MIDlet.setAppProperty("MIDlet-Vendor", "Gameloft SA");
                MIDlet.setAppProperty("MIDlet-Version", "1.0.9");
                MIDlet.setAppProperty("MicroEdition-Configuration", "CLDC-1.1");
                MIDlet.setAppProperty("MicroEdition-Profile", "MIDP-2.0");
                MIDlet.setAppProperty("MIDlet-Touch-Support", "true");

                // Instantiate and launch GloftGAN3 MIDlet dynamically
                Class<?> midletClass = Class.forName("GloftGAN3");
                MIDlet midlet = (MIDlet) midletClass.getDeclaredConstructor().newInstance();
                midlet.startApp();
            } catch (Throwable t) {
                t.printStackTrace();
                java.io.StringWriter sw = new java.io.StringWriter();
                t.printStackTrace(new java.io.PrintWriter(sw));
                crashStackTrace = sw.toString();
            }
        }
    }

    @Override
    public void run() {
        startMidlet();

        final long targetFrameTime = 1000 / 40; // 40 FPS target for smooth J2ME rendering

        while (isRunning) {
            long startTime = System.currentTimeMillis();

            javax.microedition.lcdui.Canvas jCanvas = javax.microedition.lcdui.Canvas.getActiveCanvas();
            if (jCanvas != null) {
                try {
                    jCanvas.renderFrame();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            SurfaceHolder holder = getHolder();
            if (holder != null && holder.getSurface().isValid()) {
                Canvas canvas = null;
                try {
                    canvas = holder.lockCanvas();
                    if (canvas != null) {
                        // 1. Draw black background
                        canvas.drawRect(0, 0, viewWidth, viewHeight, bgPaint);

                        if (crashStackTrace != null) {
                            String[] lines = crashStackTrace.split("\n");
                            float y = 50f;
                            for (String line : lines) {
                                canvas.drawText(line, 20f, y, errorPaint);
                                y += 35f;
                            }
                        } else {
                            // 2. Draw J2ME game frame
                            if (jCanvas != null) {
                                Bitmap bmp = jCanvas.getOffscreenBitmap();
                                if (bmp != null) {
                                    canvas.drawBitmap(bmp, drawMatrix, paint);
                                }
                            }

                            // 3. Draw GTA Vice City touch HUD
                            touchHUD.draw(canvas);
                        }
                    }
                } finally {
                    if (canvas != null) {
                        try {
                            holder.unlockCanvasAndPost(canvas);
                        } catch (Exception ignored) {}
                    }
                }
            }

            long elapsed = System.currentTimeMillis() - startTime;
            long sleepTime = targetFrameTime - elapsed;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ignored) {}
            }
        }
    }

    public void onPause() {
        MIDlet midlet = MIDlet.getActiveMidlet();
        if (midlet != null) {
            try {
                midlet.pauseApp();
            } catch (Throwable ignored) {}
        }
    }

    public void onResume() {
        MIDlet midlet = MIDlet.getActiveMidlet();
        if (midlet != null) {
            try {
                midlet.startApp();
            } catch (Throwable ignored) {}
        }
    }

    public void onDestroy() {
        MIDlet midlet = MIDlet.getActiveMidlet();
        if (midlet != null) {
            try {
                midlet.destroyApp(true);
            } catch (Throwable ignored) {}
        }
    }
}
