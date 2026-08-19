package com.gangstar.miami;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public class VirtualButton {
    public static final int SHAPE_CIRCLE = 0;
    public static final int SHAPE_ROUND_RECT = 1;
    public static final int SHAPE_PEDAL_GAS = 2;
    public static final int SHAPE_PEDAL_BRAKE = 3;

    private final String id;
    private final String label;
    private final String subLabel;
    private final int keyCode;
    private final int shape;

    private float x;
    private float y;
    private float width;
    private float height;
    private float radius;

    private boolean pressed = false;
    private int pointerId = -1;
    private boolean visible = true;

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint subTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF rectF = new RectF();
    private final Path pedalPath = new Path();

    public VirtualButton(String id, String label, String subLabel, int keyCode, int shape, float x, float y, float width, float height) {
        this.id = id;
        this.label = label;
        this.subLabel = subLabel;
        this.keyCode = keyCode;
        this.shape = shape;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.radius = Math.min(width, height) / 2f;

        initPaints();
    }

    private void initPaints() {
        bgPaint.setStyle(Paint.Style.FILL);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(3f);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        subTextPaint.setColor(Color.LTGRAY);
        subTextPaint.setTextAlign(Paint.Align.CENTER);
        subTextPaint.setTextSize(18f);

        iconPaint.setStyle(Paint.Style.STROKE);
        iconPaint.setStrokeWidth(4f);
        iconPaint.setColor(Color.WHITE);
        iconPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setPosition(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.radius = Math.min(width, height) / 2f;
        this.rectF.set(x, y, x + width, y + height);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean v) {
        this.visible = v;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public String getId() {
        return id;
    }

    public boolean isPressed() {
        return pressed;
    }

    public int getPointerId() {
        return pointerId;
    }

    public boolean contains(float px, float py) {
        if (!visible) return false;
        if (shape == SHAPE_CIRCLE) {
            float cx = x + width / 2f;
            float cy = y + height / 2f;
            float dx = px - cx;
            float dy = py - cy;
            return (dx * dx + dy * dy) <= (radius * radius * 1.4f); // generous touch padding
        } else {
            // Include 15px touch padding for comfortable gaming
            return px >= (x - 15) && px <= (x + width + 15) && py >= (y - 15) && py <= (y + height + 15);
        }
    }

    public boolean onTouchDown(int ptrId, float px, float py) {
        if (!visible) return false;
        if (contains(px, py) && pointerId == -1) {
            this.pointerId = ptrId;
            this.pressed = true;
            return true;
        }
        return false;
    }

    public boolean onTouchMove(int ptrId, float px, float py) {
        if (!visible || this.pointerId != ptrId) return false;
        boolean stillIn = contains(px, py);
        if (this.pressed != stillIn) {
            this.pressed = stillIn;
            return true; // state changed
        }
        return false;
    }

    public boolean onTouchUp(int ptrId) {
        if (this.pointerId == ptrId) {
            this.pointerId = -1;
            boolean wasPressed = this.pressed;
            this.pressed = false;
            return wasPressed;
        }
        return false;
    }

    public void forceRelease() {
        this.pointerId = -1;
        this.pressed = false;
    }

    public void draw(Canvas canvas, float opacity) {
        if (!visible) return;

        rectF.set(x, y, x + width, y + height);
        float cx = x + width / 2f;
        float cy = y + height / 2f;

        // GTA Vice City neon color styling
        int baseAlpha = (int) (opacity * 255);
        int bgAlpha = pressed ? Math.min(255, baseAlpha + 80) : Math.min(255, (int)(baseAlpha * 0.6f));
        int strokeAlpha = pressed ? 255 : baseAlpha;

        int strokeColor = Color.argb(strokeAlpha, 0, 220, 255); // Neon Cyan default
        int fillColor = Color.argb(bgAlpha, 20, 30, 45);

        if (shape == SHAPE_PEDAL_GAS) {
            strokeColor = Color.argb(strokeAlpha, 50, 255, 120); // Neon Green
            fillColor = pressed ? Color.argb(bgAlpha, 20, 80, 40) : Color.argb(bgAlpha, 15, 40, 25);
        } else if (shape == SHAPE_PEDAL_BRAKE) {
            strokeColor = Color.argb(strokeAlpha, 255, 60, 80); // Neon Red
            fillColor = pressed ? Color.argb(bgAlpha, 90, 20, 30) : Color.argb(bgAlpha, 45, 15, 20);
        } else if (id.equals("action") || id.equals("shoot")) {
            strokeColor = Color.argb(strokeAlpha, 255, 180, 0); // Neon Amber/Gold
            fillColor = pressed ? Color.argb(bgAlpha, 80, 60, 10) : Color.argb(bgAlpha, 40, 30, 10);
        } else if (id.equals("handbrake") || id.equals("sprint")) {
            strokeColor = Color.argb(strokeAlpha, 255, 0, 180); // Neon Magenta
            fillColor = pressed ? Color.argb(bgAlpha, 80, 10, 60) : Color.argb(bgAlpha, 40, 10, 30);
        }

        bgPaint.setColor(fillColor);
        strokePaint.setColor(strokeColor);
        textPaint.setColor(pressed ? strokeColor : Color.WHITE);
        textPaint.setAlpha(Math.min(255, baseAlpha + 50));

        if (shape == SHAPE_CIRCLE) {
            canvas.drawCircle(cx, cy, radius, bgPaint);
            canvas.drawCircle(cx, cy, radius, strokePaint);
            if (pressed) {
                strokePaint.setStrokeWidth(5f);
                canvas.drawCircle(cx, cy, radius - 4, strokePaint);
                strokePaint.setStrokeWidth(3f);
            }
        } else if (shape == SHAPE_ROUND_RECT) {
            canvas.drawRoundRect(rectF, 18f, 18f, bgPaint);
            canvas.drawRoundRect(rectF, 18f, 18f, strokePaint);
        } else if (shape == SHAPE_PEDAL_GAS) {
            // Vertical gas pedal
            canvas.drawRoundRect(rectF, 22f, 22f, bgPaint);
            canvas.drawRoundRect(rectF, 22f, 22f, strokePaint);
            // Draw pedal ridges
            iconPaint.setColor(strokeColor);
            iconPaint.setAlpha(baseAlpha);
            for (float rY = y + 25; rY <= y + height - 25; rY += 16) {
                canvas.drawLine(x + 16, rY, x + width - 16, rY, iconPaint);
            }
        } else if (shape == SHAPE_PEDAL_BRAKE) {
            // Wide brake pedal
            canvas.drawRoundRect(rectF, 18f, 18f, bgPaint);
            canvas.drawRoundRect(rectF, 18f, 18f, strokePaint);
            // Draw brake pedal ridges
            iconPaint.setColor(strokeColor);
            iconPaint.setAlpha(baseAlpha);
            for (float rX = x + 20; rX <= x + width - 20; rX += 16) {
                canvas.drawLine(rX, y + 16, rX, y + height - 16, iconPaint);
            }
        }

        // Draw Labels / Icons
        float fontSize = Math.min(width, height) * 0.36f;
        textPaint.setTextSize(fontSize);

        float textY = cy + (fontSize * 0.35f);
        if (subLabel != null && !subLabel.isEmpty()) {
            textY = cy - 4;
            canvas.drawText(label, cx, textY, textPaint);
            subTextPaint.setTextSize(fontSize * 0.55f);
            subTextPaint.setAlpha(baseAlpha);
            canvas.drawText(subLabel, cx, cy + (fontSize * 0.75f), subTextPaint);
        } else {
            canvas.drawText(label, cx, textY, textPaint);
        }
    }
}
