package com.gangstar.miami;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import java.util.ArrayList;
import java.util.List;

public class TouchHUD {
    private float opacity = 0.70f;
    private int screenWidth = 800;
    private int screenHeight = 480;

    private float gameX = 0;
    private float gameY = 0;
    private float gameWidth = 800;
    private float gameHeight = 480;
    private float gameScale = 1.0f;

    private final List<VirtualButton> allButtons = new ArrayList<VirtualButton>();

    // Movement / Steering (Left cluster)
    private VirtualButton btnUp, btnDown, btnLeft, btnRight;
    private VirtualButton btnHorn;

    // Action Cluster (Right cluster)
    private VirtualButton btnAction;    // KEY_NUM5 (Shoot / Attack on foot; Exit car when driving)
    private VirtualButton btnEnterCar;  // KEY_STAR (Enter / Hijack car)
    private VirtualButton btnSprint;    // KEY_NUM7 (Sprint on foot; Handbrake/Drift when driving)
    private VirtualButton btnJump;      // KEY_POUND (Jump / Climb on foot; Horn when driving)
    private VirtualButton btnPrevWep;   // KEY_NUM1
    private VirtualButton btnNextWep;   // KEY_NUM3

    // System Utility (Top bar)
    private VirtualButton btnOpacityToggle, btnPause, btnMenu;

    private int directPointerId = -1;

    public TouchHUD() {
        initButtons();
    }

    private void initButtons() {
        allButtons.clear();

        // Direction / Steering Buttons
        btnUp = new VirtualButton("up", "▲", "GAS", javax.microedition.lcdui.Canvas.KEY_NUM2, VirtualButton.SHAPE_ROUND_RECT, 0, 0, 75, 75);
        btnDown = new VirtualButton("down", "▼", "BRAKE", javax.microedition.lcdui.Canvas.KEY_NUM8, VirtualButton.SHAPE_ROUND_RECT, 0, 0, 75, 75);
        btnLeft = new VirtualButton("left", "◀", "LEFT", javax.microedition.lcdui.Canvas.KEY_NUM4, VirtualButton.SHAPE_ROUND_RECT, 0, 0, 75, 75);
        btnRight = new VirtualButton("right", "▶", "RIGHT", javax.microedition.lcdui.Canvas.KEY_NUM6, VirtualButton.SHAPE_ROUND_RECT, 0, 0, 75, 75);
        btnHorn = new VirtualButton("horn", "📢", "HORN", javax.microedition.lcdui.Canvas.KEY_POUND, VirtualButton.SHAPE_CIRCLE, 0, 0, 65, 65);

        // Action Buttons (Context-sensitive: works for both Walk and Drive automatically!)
        btnAction = new VirtualButton("action", "🎯", "ACTION", javax.microedition.lcdui.Canvas.KEY_NUM5, VirtualButton.SHAPE_CIRCLE, 0, 0, 95, 95);
        btnEnterCar = new VirtualButton("enter_car", "🚗", "ENTER", javax.microedition.lcdui.Canvas.KEY_STAR, VirtualButton.SHAPE_CIRCLE, 0, 0, 75, 75);
        btnSprint = new VirtualButton("sprint", "⚡", "DRIFT", javax.microedition.lcdui.Canvas.KEY_NUM7, VirtualButton.SHAPE_CIRCLE, 0, 0, 75, 75);
        btnJump = new VirtualButton("jump", "🤾", "JUMP", javax.microedition.lcdui.Canvas.KEY_POUND, VirtualButton.SHAPE_CIRCLE, 0, 0, 70, 70);

        // Weapon Selector
        btnPrevWep = new VirtualButton("prev_wep", "◄ WEP", "", javax.microedition.lcdui.Canvas.KEY_NUM1, VirtualButton.SHAPE_ROUND_RECT, 0, 0, 75, 45);
        btnNextWep = new VirtualButton("next_wep", "WEP ►", "", javax.microedition.lcdui.Canvas.KEY_NUM3, VirtualButton.SHAPE_ROUND_RECT, 0, 0, 75, 45);

        // Header Controls
        btnOpacityToggle = new VirtualButton("opacity", "👁 HUD", "70%", 0, VirtualButton.SHAPE_ROUND_RECT, 0, 0, 95, 45);
        btnPause = new VirtualButton("pause", "❚❚ PAUSE", "", javax.microedition.lcdui.Canvas.KEY_SOFTKEY1, VirtualButton.SHAPE_ROUND_RECT, 0, 0, 100, 45);
        btnMenu = new VirtualButton("menu", "🗺 MENU", "", javax.microedition.lcdui.Canvas.KEY_SOFTKEY2, VirtualButton.SHAPE_ROUND_RECT, 0, 0, 100, 45);

        allButtons.add(btnUp);
        allButtons.add(btnDown);
        allButtons.add(btnLeft);
        allButtons.add(btnRight);
        allButtons.add(btnHorn);

        allButtons.add(btnAction);
        allButtons.add(btnEnterCar);
        allButtons.add(btnSprint);
        allButtons.add(btnJump);
        allButtons.add(btnPrevWep);
        allButtons.add(btnNextWep);

        allButtons.add(btnOpacityToggle);
        allButtons.add(btnPause);
        allButtons.add(btnMenu);

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

    public void updateLayout(int w, int h) {
        this.screenWidth = w;
        this.screenHeight = h;

        // Dynamic scale factor based on screen height and width for full responsiveness on all phone sizes
        float scale = Math.min((float) w / 800f, (float) h / 480f);
        if (scale < 0.8f) scale = 0.8f;

        float margin = 20f * scale;
        float bottomMargin = 25f * scale;

        // --- Header Controls (Top Bar) ---
        float topBtnW = 100f * scale;
        float topBtnH = 44f * scale;
        btnOpacityToggle.setPosition(margin, margin, topBtnW, topBtnH);
        btnMenu.setPosition(w - margin - topBtnW, margin, topBtnW, topBtnH);
        btnPause.setPosition(w - margin - topBtnW * 2f - 15f * scale, margin, topBtnW, topBtnH);

        // Weapon Selector (Top Right below Menu)
        float wepBtnW = 85f * scale;
        float wepBtnH = 42f * scale;
        btnNextWep.setPosition(w - margin - wepBtnW, margin + topBtnH + 15f * scale, wepBtnW, wepBtnH);
        btnPrevWep.setPosition(w - margin - wepBtnW * 2f - 10f * scale, margin + topBtnH + 15f * scale, wepBtnW, wepBtnH);

        // --- Left Side: Direction D-Pad (Walk & Drive) ---
        float dpadBtnSize = 78f * scale;
        float dpadSpacing = 8f * scale;
        float dpadCenterX = margin + dpadBtnSize * 1.5f + dpadSpacing;
        float dpadCenterY = h - bottomMargin - dpadBtnSize * 1.5f - dpadSpacing;

        btnUp.setPosition(dpadCenterX - dpadBtnSize / 2f, dpadCenterY - dpadBtnSize - dpadSpacing, dpadBtnSize, dpadBtnSize);
        btnDown.setPosition(dpadCenterX - dpadBtnSize / 2f, dpadCenterY + dpadSpacing, dpadBtnSize, dpadBtnSize);
        btnLeft.setPosition(dpadCenterX - dpadBtnSize - dpadSpacing, dpadCenterY - dpadBtnSize / 2f, dpadBtnSize, dpadBtnSize);
        btnRight.setPosition(dpadCenterX + dpadSpacing, dpadCenterY - dpadBtnSize / 2f, dpadBtnSize, dpadBtnSize);
        btnHorn.setPosition(dpadCenterX - dpadBtnSize / 2f, dpadCenterY - dpadBtnSize / 2f, dpadBtnSize, dpadBtnSize);

        // --- Right Side: Action Cluster (Walk, Drive, Jump, Enter/Exit Car, Drift) ---
        float actionBtnSize = 98f * scale;
        float subBtnSize = 75f * scale;
        float actionCenterX = w - margin - actionBtnSize * 1.4f;
        float actionCenterY = h - bottomMargin - actionBtnSize * 1.2f;

        // Big Primary Action (🎯 Shoot/Attack on foot; Exit car when driving)
        btnAction.setPosition(actionCenterX, actionCenterY, actionBtnSize, actionBtnSize);

        // 🚗 Enter / Hijack Car (Positioned left of Action button)
        btnEnterCar.setPosition(actionCenterX - subBtnSize - 18f * scale, actionCenterY + (actionBtnSize - subBtnSize) / 2f, subBtnSize, subBtnSize);

        // ⚡ Sprint / Handbrake Drift (Positioned above Action button)
        btnSprint.setPosition(actionCenterX + (actionBtnSize - subBtnSize) / 2f, actionCenterY - subBtnSize - 18f * scale, subBtnSize, subBtnSize);

        // 🤾 Jump / Climb (Positioned top-left diagonal of Action button)
        btnJump.setPosition(actionCenterX - subBtnSize * 0.8f, actionCenterY - subBtnSize * 0.8f, subBtnSize * 0.9f, subBtnSize * 0.9f);
    }

    public void cycleOpacity() {
        if (opacity >= 0.9f) {
            opacity = 0.30f;
        } else if (opacity >= 0.7f) {
            opacity = 0.90f;
        } else if (opacity >= 0.5f) {
            opacity = 0.70f;
        } else {
            opacity = 0.50f;
        }
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

            // Check UI buttons
            boolean handled = false;
            for (VirtualButton b : allButtons) {
                if (b.onTouchDown(pointerId, px, py)) {
                    handled = true;
                    if (b == btnOpacityToggle) {
                        cycleOpacity();
                    } else if (b.getKeyCode() != 0 && canvas != null) {
                        canvas.keyPressedPublic(b.getKeyCode());
                    }
                    break;
                }
            }

            // If not handled by HUD buttons, check if inside game canvas for direct touch/pointer events
            if (!handled && canvas != null && directPointerId == -1) {
                if (px >= gameX && px <= gameX + gameWidth && py >= gameY && py <= gameY + gameHeight) {
                    directPointerId = pointerId;
                    int j2meX = (int) ((px - gameX) / gameScale);
                    int j2meY = (int) ((py - gameY) / gameScale);
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
                    int j2meX = (int) ((px - gameX) / gameScale);
                    int j2meY = (int) ((py - gameY) / gameScale);
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
                int j2meX = (int) ((px - gameX) / gameScale);
                int j2meY = (int) ((py - gameY) / gameScale);
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
