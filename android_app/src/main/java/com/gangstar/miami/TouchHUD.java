package com.gangstar.miami;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.view.MotionEvent;
import java.util.ArrayList;
import java.util.List;

public class TouchHUD {
    private float opacity = 0.65f;
    private int screenWidth = 800;
    private int screenHeight = 480;

    private float gameX = 0;
    private float gameY = 0;
    private float gameWidth = 800;
    private float gameHeight = 480;
    private float gameScale = 1.0f;
    private final Matrix touchInvertMatrix = new Matrix();

    private final List<VirtualButton> allButtons = new ArrayList<VirtualButton>();

    // Movement / Steering (Left cluster)
    private VirtualButton btnUp, btnDown, btnLeft, btnRight;

    // Action Cluster (Right cluster)
    private VirtualButton btnAction;    // KEY_NUM5 (Shoot / Attack on foot; Exit car when driving)
    private VirtualButton btnEnterCar;  // KEY_STAR (Enter / Hijack car)
    private VirtualButton btnSprint;    // KEY_NUM7 (Sprint on foot; Handbrake/Drift when driving)
    private VirtualButton btnJump;      // KEY_POUND (Jump / Climb on foot; Horn when driving)

    private int directPointerId = -1;

    public TouchHUD() {
        initButtons();
    }

    private void initButtons() {
        allButtons.clear();

        // Direction Buttons (Clean text, NO EMOJIS)
        btnUp = new VirtualButton("up", "UP", "GAS", javax.microedition.lcdui.Canvas.KEY_NUM2, VirtualButton.SHAPE_ROUND_RECT, 0, 0, 75, 75);
        btnDown = new VirtualButton("down", "DOWN", "BRAKE", javax.microedition.lcdui.Canvas.KEY_NUM8, VirtualButton.SHAPE_ROUND_RECT, 0, 0, 75, 75);
        btnLeft = new VirtualButton("left", "LEFT", "STEER", javax.microedition.lcdui.Canvas.KEY_NUM4, VirtualButton.SHAPE_ROUND_RECT, 0, 0, 75, 75);
        btnRight = new VirtualButton("right", "RIGHT", "STEER", javax.microedition.lcdui.Canvas.KEY_NUM6, VirtualButton.SHAPE_ROUND_RECT, 0, 0, 75, 75);

        // Action Buttons (Clean text, NO EMOJIS)
        btnAction = new VirtualButton("action", "ACTION", "", javax.microedition.lcdui.Canvas.KEY_NUM5, VirtualButton.SHAPE_CIRCLE, 0, 0, 90, 90);
        btnEnterCar = new VirtualButton("enter_car", "ENTER", "", javax.microedition.lcdui.Canvas.KEY_STAR, VirtualButton.SHAPE_CIRCLE, 0, 0, 72, 72);
        btnSprint = new VirtualButton("sprint", "DRIFT", "", javax.microedition.lcdui.Canvas.KEY_NUM7, VirtualButton.SHAPE_CIRCLE, 0, 0, 72, 72);
        btnJump = new VirtualButton("jump", "JUMP", "", javax.microedition.lcdui.Canvas.KEY_POUND, VirtualButton.SHAPE_CIRCLE, 0, 0, 68, 68);

        allButtons.add(btnUp);
        allButtons.add(btnDown);
        allButtons.add(btnLeft);
        allButtons.add(btnRight);

        allButtons.add(btnAction);
        allButtons.add(btnEnterCar);
        allButtons.add(btnSprint);
        allButtons.add(btnJump);

        for (VirtualButton b : allButtons) {
            b.setVisible(true);
        }
    }

    public void setGameViewport(float x, float y, float w, float h, float scale) {
        this.gameX = x;
        this.gameY = y;
        this.gameWidth = w;
        this.gameHeight = h;
        this.gameScale = scale;
    }

    public void setTouchInvertMatrix(Matrix m) {
        if (m != null) {
            this.touchInvertMatrix.set(m);
        }
    }

    public void updateLayout(int w, int h) {
        this.screenWidth = w;
        this.screenHeight = h;

        // Dynamic scale factor based on screen height for landscape display
        float scale = (float) h / 480f;
        if (scale < 0.75f) scale = 0.75f;

        float margin = 20f * scale;
        float bottomMargin = 22f * scale;

        // --- Left Side: Direction D-Pad (Walk & Drive) ---
        float dpadBtnSize = 75f * scale;
        float dpadSpacing = 8f * scale;
        float dpadCenterX = margin + dpadBtnSize * 1.5f + dpadSpacing;
        float dpadCenterY = h - bottomMargin - dpadBtnSize * 1.5f - dpadSpacing;

        btnUp.setPosition(dpadCenterX - dpadBtnSize / 2f, dpadCenterY - dpadBtnSize - dpadSpacing, dpadBtnSize, dpadBtnSize);
        btnDown.setPosition(dpadCenterX - dpadBtnSize / 2f, dpadCenterY + dpadSpacing, dpadBtnSize, dpadBtnSize);
        btnLeft.setPosition(dpadCenterX - dpadBtnSize - dpadSpacing, dpadCenterY - dpadBtnSize / 2f, dpadBtnSize, dpadBtnSize);
        btnRight.setPosition(dpadCenterX + dpadSpacing, dpadCenterY - dpadBtnSize / 2f, dpadBtnSize, dpadBtnSize);

        // --- Right Side: Action Cluster (Action, Enter Car, Drift, Jump) ---
        float actionBtnSize = 92f * scale;
        float subBtnSize = 72f * scale;
        float actionCenterX = w - margin - actionBtnSize * 1.35f;
        float actionCenterY = h - bottomMargin - actionBtnSize * 1.15f;

        // Big Primary Action button (ACTION - Fire / Shoot / Attack / Exit car)
        btnAction.setPosition(actionCenterX, actionCenterY, actionBtnSize, actionBtnSize);

        // ENTER (Enter / Hijack car)
        btnEnterCar.setPosition(actionCenterX - subBtnSize - 16f * scale, actionCenterY + (actionBtnSize - subBtnSize) / 2f, subBtnSize, subBtnSize);

        // DRIFT / SPRINT (Sprint on foot; Handbrake/Drift when driving)
        btnSprint.setPosition(actionCenterX + (actionBtnSize - subBtnSize) / 2f, actionCenterY - subBtnSize - 16f * scale, subBtnSize, subBtnSize);

        // JUMP / CLIMB (Jump / Climb on foot; Horn when driving)
        btnJump.setPosition(actionCenterX - subBtnSize * 0.8f, actionCenterY - subBtnSize * 0.8f, subBtnSize * 0.9f, subBtnSize * 0.9f);
    }

    private void releaseAllGameKeys() {
        javax.microedition.lcdui.Canvas canvas = javax.microedition.lcdui.Canvas.getActiveCanvas();
        for (VirtualButton b : allButtons) {
            if (b.isPressed() && b.getKeyCode() != 0) {
                b.forceRelease();
                if (canvas != null) {
                    canvas.keyReleasedPublic(b.getKeyCode());
                }
            }
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        javax.microedition.lcdui.Canvas canvas = javax.microedition.lcdui.Canvas.getActiveCanvas();
        int action = event.getActionMasked();
        int actionIndex = event.getActionIndex();

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            int pointerId = event.getPointerId(actionIndex);
            float px = event.getX(actionIndex);
            float py = event.getY(actionIndex);

            // Check virtual HUD buttons first
            boolean handled = false;
            for (VirtualButton b : allButtons) {
                if (b.onTouchDown(pointerId, px, py)) {
                    handled = true;
                    if (b.getKeyCode() != 0 && canvas != null) {
                        canvas.keyPressedPublic(b.getKeyCode());
                    }
                    break;
                }
            }

            // If not touching a HUD button, pass touch directly to the J2ME game canvas
            if (!handled && canvas != null && directPointerId == -1) {
                float[] pts = new float[] { px, py };
                touchInvertMatrix.mapPoints(pts);
                int j2meX = (int) pts[0];
                int j2meY = (int) pts[1];
                if (j2meX >= 0 && j2meX <= 800 && j2meY >= 0 && j2meY <= 480) {
                    directPointerId = pointerId;
                    canvas.pointerPressedPublic(j2meX, j2meY);
                    handled = true;
                }
            }
            return true;

        } else if (action == MotionEvent.ACTION_MOVE) {
            int pointerCount = event.getPointerCount();
            for (int i = 0; i < pointerCount; i++) {
                int pointerId = event.getPointerId(i);
                float px = event.getX(i);
                float py = event.getY(i);

                for (VirtualButton b : allButtons) {
                    if (b.getPointerId() == pointerId) {
                        boolean wasPressed = b.isPressed();
                        b.onTouchMove(pointerId, px, py);
                        boolean isPressed = b.isPressed();
                        if (wasPressed != isPressed && b.getKeyCode() != 0 && canvas != null) {
                            if (isPressed) {
                                canvas.keyPressedPublic(b.getKeyCode());
                            } else {
                                canvas.keyReleasedPublic(b.getKeyCode());
                            }
                        }
                    }
                }

                if (pointerId == directPointerId && canvas != null) {
                    float[] pts = new float[] { px, py };
                    touchInvertMatrix.mapPoints(pts);
                    int j2meX = (int) pts[0];
                    int j2meY = (int) pts[1];
                    canvas.pointerDraggedPublic(j2meX, j2meY);
                }
            }
            return true;

        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_CANCEL) {
            int pointerId = event.getPointerId(actionIndex);

            for (VirtualButton b : allButtons) {
                if (b.getPointerId() == pointerId) {
                    boolean wasPressed = b.onTouchUp(pointerId);
                    if (wasPressed && b.getKeyCode() != 0 && canvas != null) {
                        canvas.keyReleasedPublic(b.getKeyCode());
                    }
                }
            }

            if (pointerId == directPointerId && canvas != null) {
                float px = event.getX(actionIndex);
                float py = event.getY(actionIndex);
                float[] pts = new float[] { px, py };
                touchInvertMatrix.mapPoints(pts);
                int j2meX = (int) pts[0];
                int j2meY = (int) pts[1];
                canvas.pointerReleasedPublic(j2meX, j2meY);
                directPointerId = -1;
            }

            if (action == MotionEvent.ACTION_CANCEL) {
                releaseAllGameKeys();
                directPointerId = -1;
            }
            return true;
        }

        return false;
    }

    public void draw(Canvas canvas) {
        for (VirtualButton b : allButtons) {
            b.draw(canvas, opacity);
        }
    }
}
