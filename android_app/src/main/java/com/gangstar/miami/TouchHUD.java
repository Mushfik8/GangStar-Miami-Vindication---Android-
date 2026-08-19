package com.gangstar.miami;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import java.util.ArrayList;
import java.util.List;

public class TouchHUD {
    public static final int MODE_DRIVING = 0;
    public static final int MODE_ON_FOOT = 1;

    private int mode = MODE_DRIVING;
    private float opacity = 0.70f;
    private int screenWidth = 800;
    private int screenHeight = 480;

    private float gameX = 0;
    private float gameY = 0;
    private float gameWidth = 800;
    private float gameHeight = 480;
    private float gameScale = 1.0f;

    private final List<VirtualButton> allButtons = new ArrayList<VirtualButton>();
    private final List<VirtualButton> driveButtons = new ArrayList<VirtualButton>();
    private final List<VirtualButton> footButtons = new ArrayList<VirtualButton>();
    private final List<VirtualButton> commonButtons = new ArrayList<VirtualButton>();

    // Buttons
    private VirtualButton btnGas, btnBrake, btnLeft, btnRight, btnHandbrake, btnExitCar, btnHorn;
    private VirtualButton btnUpFoot, btnDownFoot, btnLeftFoot, btnRightFoot, btnShoot, btnEnterCar, btnSprint, btnPrevWep, btnNextWep;
    private VirtualButton btnModeToggle, btnOpacityToggle, btnPause, btnMenu;

    private int directPointerId = -1;

    public TouchHUD() {
        initButtons();
    }

    private void initButtons() {
        allButtons.clear();
        driveButtons.clear();
        footButtons.clear();
        commonButtons.clear();

        // Driving Controls (GTA Vice City Mobile Style)
        btnGas = new VirtualButton("gas", "GAS", "FRONT", javax.microedition.lcdui.Canvas.KEY_NUM2, VirtualButton.SHAPE_PEDAL_GAS, 0, 0, 90, 160);
        btnBrake = new VirtualButton("brake", "BRAKE", "REVERSE", javax.microedition.lcdui.Canvas.KEY_NUM8, VirtualButton.SHAPE_PEDAL_BRAKE, 0, 0, 120, 85);
        btnLeft = new VirtualButton("left", "◀", "STEER", javax.microedition.lcdui.Canvas.KEY_NUM4, VirtualButton.SHAPE_CIRCLE, 0, 0, 85, 85);
        btnRight = new VirtualButton("right", "▶", "STEER", javax.microedition.lcdui.Canvas.KEY_NUM6, VirtualButton.SHAPE_CIRCLE, 0, 0, 85, 85);
        btnHandbrake = new VirtualButton("handbrake", "✋", "DRIFT", javax.microedition.lcdui.Canvas.KEY_NUM7, VirtualButton.SHAPE_CIRCLE, 0, 0, 75, 75);
        btnExitCar = new VirtualButton("exit_car", "🚗", "EXIT", javax.microedition.lcdui.Canvas.KEY_NUM5, VirtualButton.SHAPE_CIRCLE, 0, 0, 75, 75);
        btnHorn = new VirtualButton("horn", "📢", "HORN", javax.microedition.lcdui.Canvas.KEY_POUND, VirtualButton.SHAPE_CIRCLE, 0, 0, 65, 65);

        driveButtons.add(btnGas);
        driveButtons.add(btnBrake);
        driveButtons.add(btnLeft);
        driveButtons.add(btnRight);
        driveButtons.add(btnHandbrake);
        driveButtons.add(btnExitCar);
        driveButtons.add(btnHorn);

        // On-Foot Controls (GTA Vice City Mobile Style)
        btnUpFoot = new VirtualButton("up_foot", "▲", "MOVE", javax.microedition.lcdui.Canvas.KEY_NUM2, VirtualButton.SHAPE_ROUND_RECT, 0, 0, 75, 75);
        btnDownFoot = new VirtualButton("down_foot", "▼", "BACK", javax.microedition.lcdui.Canvas.KEY_NUM8, VirtualButton.SHAPE_ROUND_RECT, 0, 0, 75, 75);
        btnLeftFoot = new VirtualButton("left_foot", "◀", "LEFT", javax.microedition.lcdui.Canvas.KEY_NUM4, VirtualButton.SHAPE_ROUND_RECT, 0, 0, 75, 75);
        btnRightFoot = new VirtualButton("right_foot", "▶", "RIGHT", javax.microedition.lcdui.Canvas.KEY_NUM6, VirtualButton.SHAPE_ROUND_RECT, 0, 0, 75, 75);

        btnShoot = new VirtualButton("shoot", "🎯", "FIRE", javax.microedition.lcdui.Canvas.KEY_NUM5, VirtualButton.SHAPE_CIRCLE, 0, 0, 95, 95);
        btnEnterCar = new VirtualButton("enter_car", "🚗", "ENTER", javax.microedition.lcdui.Canvas.KEY_STAR, VirtualButton.SHAPE_CIRCLE, 0, 0, 75, 75);
        btnSprint = new VirtualButton("sprint", "⚡", "SPRINT", javax.microedition.lcdui.Canvas.KEY_NUM7, VirtualButton.SHAPE_CIRCLE, 0, 0, 75, 75);
        btnPrevWep = new VirtualButton("prev_wep", "◄ WEP", "", javax.microedition.lcdui.Canvas.KEY_NUM1, VirtualButton.SHAPE_ROUND_RECT, 0, 0, 75, 55);
        btnNextWep = new VirtualButton("next_wep", "WEP ►", "", javax.microedition.lcdui.Canvas.KEY_NUM3, VirtualButton.SHAPE_ROUND_RECT, 0, 0, 75, 55);

        footButtons.add(btnUpFoot);
        footButtons.add(btnDownFoot);
        footButtons.add(btnLeftFoot);
        footButtons.add(btnRightFoot);
        footButtons.add(btnShoot);
        footButtons.add(btnEnterCar);
        footButtons.add(btnSprint);
        footButtons.add(btnPrevWep);
        footButtons.add(btnNextWep);

        // Header / Common Utility Controls
        btnModeToggle = new VirtualButton("mode", "🚗 DRIVE", "TOGGLE", 0, VirtualButton.SHAPE_ROUND_RECT, 0, 0, 110, 45);
        btnOpacityToggle = new VirtualButton("opacity", "👁 HUD", "70%", 0, VirtualButton.SHAPE_ROUND_RECT, 0, 0, 95, 45);
        btnPause = new VirtualButton("pause", "❚❚ PAUSE", "", javax.microedition.lcdui.Canvas.KEY_SOFTKEY1, VirtualButton.SHAPE_ROUND_RECT, 0, 0, 100, 45);
        btnMenu = new VirtualButton("menu", "🗺 MENU", "", javax.microedition.lcdui.Canvas.KEY_SOFTKEY2, VirtualButton.SHAPE_ROUND_RECT, 0, 0, 100, 45);

        commonButtons.add(btnModeToggle);
        commonButtons.add(btnOpacityToggle);
        commonButtons.add(btnPause);
        commonButtons.add(btnMenu);

        allButtons.addAll(driveButtons);
        allButtons.addAll(footButtons);
        allButtons.addAll(commonButtons);

        updateVisibility();
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

        float margin = 20f;
        float bottomMargin = 25f;

        // --- Common Controls (Top Bar) ---
        btnModeToggle.setPosition(margin + 5, margin, 120, 48);
        btnOpacityToggle.setPosition(margin + 135, margin, 100, 48);
        btnMenu.setPosition(screenWidth - margin - 110, margin, 110, 48);
        btnPause.setPosition(screenWidth - margin - 230, margin, 110, 48);

        // --- Driving Mode Layout ---
        // Left Side: Steer Left & Right
        float steerY = screenHeight - bottomMargin - 100;
        btnLeft.setPosition(margin + 10, steerY, 100, 100);
        btnRight.setPosition(margin + 125, steerY, 100, 100);
        btnHorn.setPosition(margin + 75, steerY - 95, 80, 80);

        // Right Side: Pedals + Actions
        float pedalGasX = screenWidth - margin - 100;
        float pedalGasY = screenHeight - bottomMargin - 200;
        btnGas.setPosition(pedalGasX, pedalGasY, 95, 200);

        float pedalBrakeX = screenWidth - margin - 235;
        float pedalBrakeY = screenHeight - bottomMargin - 115;
        btnBrake.setPosition(pedalBrakeX, pedalBrakeY, 120, 115);

        btnHandbrake.setPosition(pedalBrakeX - 95, screenHeight - bottomMargin - 100, 85, 85);
        btnExitCar.setPosition(pedalBrakeX - 25, screenHeight - bottomMargin - 205, 85, 85);

        // --- On-Foot Mode Layout ---
        // Left Side: D-Pad
        float dpadCenterX = margin + 110;
        float dpadCenterY = screenHeight - bottomMargin - 110;
        float btnDpadSize = 80;

        btnUpFoot.setPosition(dpadCenterX - btnDpadSize / 2f, dpadCenterY - btnDpadSize - 10, btnDpadSize, btnDpadSize);
        btnDownFoot.setPosition(dpadCenterX - btnDpadSize / 2f, dpadCenterY + 10, btnDpadSize, btnDpadSize);
        btnLeftFoot.setPosition(dpadCenterX - btnDpadSize - 10, dpadCenterY - btnDpadSize / 2f, btnDpadSize, btnDpadSize);
        btnRightFoot.setPosition(dpadCenterX + 10, dpadCenterY - btnDpadSize / 2f, btnDpadSize, btnDpadSize);

        // Right Side: Action Cluster
        float actionCenterX = screenWidth - margin - 110;
        float actionCenterY = screenHeight - bottomMargin - 110;

        btnShoot.setPosition(actionCenterX - 50, actionCenterY - 50, 105, 105);
        btnEnterCar.setPosition(actionCenterX - 145, actionCenterY - 80, 85, 85);
        btnSprint.setPosition(actionCenterX + 15, actionCenterY - 145, 85, 85);

        btnPrevWep.setPosition(screenWidth - margin - 200, margin + 65, 90, 50);
        btnNextWep.setPosition(screenWidth - margin - 100, margin + 65, 90, 50);
    }

    private void updateVisibility() {
        for (VirtualButton b : driveButtons) {
            b.setVisible(mode == MODE_DRIVING);
        }
        for (VirtualButton b : footButtons) {
            b.setVisible(mode == MODE_ON_FOOT);
        }
        for (VirtualButton b : commonButtons) {
            b.setVisible(true);
        }
    }

    public void toggleMode() {
        if (mode == MODE_DRIVING) {
            mode = MODE_ON_FOOT;
        } else {
            mode = MODE_DRIVING;
        }
        releaseAllGameKeys();
        updateVisibility();
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
                    if (b == btnModeToggle) {
                        toggleMode();
                    } else if (b == btnOpacityToggle) {
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
                        // CRITICAL: Immediately release key when touch is lifted!
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
