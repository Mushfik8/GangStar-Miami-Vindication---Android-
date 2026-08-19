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

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private Thread gameThread;
    private volatile boolean isRunning = false;
    private final TouchHUD touchHUD;
    private final Paint paint = new Paint();
    private final Paint bgPaint = new Paint();

    private final Rect srcRect = new Rect();
    private final RectF dstRect = new RectF();

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

        Display.setAppContext(context.getApplicationContext());
        RecordStore.init(context.getApplicationContext());
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

        // Calculate aspect-ratio fit for 800x480 J2ME game
        float gameAspect = 800f / 480f;
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
        float scale = destW / 800f;
        touchHUD.setGameViewport(destX, destY, destW, destH, scale);
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

                        // 2. Draw J2ME game frame
                        if (jCanvas != null) {
                            Bitmap bmp = jCanvas.getOffscreenBitmap();
                            if (bmp != null) {
                                srcRect.set(0, 0, bmp.getWidth(), bmp.getHeight());
                                canvas.drawBitmap(bmp, srcRect, dstRect, paint);
                            }
                        }

                        // 3. Draw GTA Vice City touch HUD
                        touchHUD.draw(canvas);
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
