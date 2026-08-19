package javax.microedition.lcdui;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;

public class Graphics {
    public static final int HCENTER = 1;
    public static final int VCENTER = 2;
    public static final int LEFT = 4;
    public static final int RIGHT = 8;
    public static final int TOP = 16;
    public static final int BOTTOM = 32;
    public static final int BASELINE = 64;

    public static final int SOLID = 0;
    public static final int DOTTED = 1;

    private android.graphics.Canvas canvas;
    private final Paint paint;
    private Font currentFont;
    private int currentColor = 0xFF000000;
    private int transX = 0;
    private int transY = 0;
    private int strokeStyle = SOLID;

    private int clipX;
    private int clipY;
    private int clipWidth;
    private int clipHeight;
    private final int width;
    private final int height;

    private final Rect srcRect = new Rect();
    private final Rect dstRect = new Rect();
    private final RectF rectF = new RectF();
    private final Path path = new Path();

    public Graphics(android.graphics.Canvas canvas, int width, int height) {
        this.canvas = canvas;
        this.width = width;
        this.height = height;
        this.clipX = 0;
        this.clipY = 0;
        this.clipWidth = width;
        this.clipHeight = height;

        this.paint = new Paint();
        this.paint.setStyle(Paint.Style.FILL);
        this.paint.setAntiAlias(false); // Sharp pixel graphics for retro/J2ME
        this.currentFont = Font.getDefaultFont();
    }

    public void setCanvas(android.graphics.Canvas c) {
        this.canvas = c;
    }

    public android.graphics.Canvas getAndroidCanvas() {
        return canvas;
    }

    public void translate(int x, int y) {
        this.transX += x;
        this.transY += y;
    }

    public int getTranslateX() {
        return transX;
    }

    public int getTranslateY() {
        return transY;
    }

    public void setClip(int x, int y, int width, int height) {
        this.clipX = x;
        this.clipY = y;
        this.clipWidth = width;
        this.clipHeight = height;
        if (canvas != null) {
            canvas.restoreToCount(1);
            canvas.save();
            canvas.clipRect(x + transX, y + transY, x + width + transX, y + height + transY);
        }
    }

    public void clipRect(int x, int y, int width, int height) {
        int x1 = Math.max(clipX, x);
        int y1 = Math.max(clipY, y);
        int x2 = Math.min(clipX + clipWidth, x + width);
        int y2 = Math.min(clipY + clipHeight, y + height);
        int w = Math.max(0, x2 - x1);
        int h = Math.max(0, y2 - y1);
        setClip(x1, y1, w, h);
    }

    public int getClipX() {
        return clipX;
    }

    public int getClipY() {
        return clipY;
    }

    public int getClipWidth() {
        return clipWidth;
    }

    public int getClipHeight() {
        return clipHeight;
    }

    public void setColor(int RGB) {
        this.currentColor = 0xFF000000 | (RGB & 0xFFFFFF);
        this.paint.setColor(this.currentColor);
    }

    public void setColor(int red, int green, int blue) {
        setColor(((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF));
    }

    public int getColor() {
        return currentColor & 0xFFFFFF;
    }

    public int getRedComponent() {
        return (currentColor >> 16) & 0xFF;
    }

    public int getGreenComponent() {
        return (currentColor >> 8) & 0xFF;
    }

    public int getBlueComponent() {
        return currentColor & 0xFF;
    }

    public void setGrayScale(int value) {
        setColor(value, value, value);
    }

    public int getGrayScale() {
        return (getRedComponent() + getGreenComponent() + getBlueComponent()) / 3;
    }

    public void setStrokeStyle(int style) {
        this.strokeStyle = style;
    }

    public int getStrokeStyle() {
        return strokeStyle;
    }

    public void setFont(Font font) {
        this.currentFont = (font != null) ? font : Font.getDefaultFont();
    }

    public Font getFont() {
        return currentFont;
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        if (canvas == null) return;
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(currentColor);
        canvas.drawLine(x1 + transX, y1 + transY, x2 + transX, y2 + transY, paint);
    }

    public void drawRect(int x, int y, int width, int height) {
        if (canvas == null || width < 0 || height < 0) return;
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(currentColor);
        canvas.drawRect(x + transX, y + transY, x + width + transX, y + height + transY, paint);
    }

    public void fillRect(int x, int y, int width, int height) {
        if (canvas == null || width <= 0 || height <= 0) return;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(currentColor);
        canvas.drawRect(x + transX, y + transY, x + width + transX, y + height + transY, paint);
    }

    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        if (canvas == null || width < 0 || height < 0) return;
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(currentColor);
        rectF.set(x + transX, y + transY, x + width + transX, y + height + transY);
        canvas.drawRoundRect(rectF, arcWidth, arcHeight, paint);
    }

    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        if (canvas == null || width <= 0 || height <= 0) return;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(currentColor);
        rectF.set(x + transX, y + transY, x + width + transX, y + height + transY);
        canvas.drawRoundRect(rectF, arcWidth, arcHeight, paint);
    }

    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        if (canvas == null || width <= 0 || height <= 0) return;
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(currentColor);
        rectF.set(x + transX, y + transY, x + width + transX, y + height + transY);
        canvas.drawArc(rectF, -startAngle, -arcAngle, false, paint);
    }

    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        if (canvas == null || width <= 0 || height <= 0) return;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(currentColor);
        rectF.set(x + transX, y + transY, x + width + transX, y + height + transY);
        canvas.drawArc(rectF, -startAngle, -arcAngle, true, paint);
    }

    public void drawTriangle(int x1, int y1, int x2, int y2, int x3, int y3) {
        if (canvas == null) return;
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(currentColor);
        path.reset();
        path.moveTo(x1 + transX, y1 + transY);
        path.lineTo(x2 + transX, y2 + transY);
        path.lineTo(x3 + transX, y3 + transY);
        path.close();
        canvas.drawPath(path, paint);
    }

    public void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3) {
        if (canvas == null) return;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(currentColor);
        path.reset();
        path.moveTo(x1 + transX, y1 + transY);
        path.lineTo(x2 + transX, y2 + transY);
        path.lineTo(x3 + transX, y3 + transY);
        path.close();
        canvas.drawPath(path, paint);
    }

    public void drawString(String str, int x, int y, int anchor) {
        if (str == null || canvas == null) return;
        Paint textPaint = currentFont.getPaint();
        textPaint.setColor(currentColor);

        int strWidth = currentFont.stringWidth(str);
        int strHeight = currentFont.getHeight();
        int baseline = currentFont.getBaselinePosition();

        float drawX = x + transX;
        if ((anchor & HCENTER) != 0) {
            drawX -= strWidth / 2f;
        } else if ((anchor & RIGHT) != 0) {
            drawX -= strWidth;
        }

        float drawY = y + transY;
        if ((anchor & BASELINE) != 0) {
            // y is baseline
        } else if ((anchor & BOTTOM) != 0) {
            drawY -= (strHeight - baseline);
        } else if ((anchor & VCENTER) != 0) {
            drawY += baseline - (strHeight / 2f);
        } else { // TOP
            drawY += baseline;
        }

        canvas.drawText(str, drawX, drawY, textPaint);
    }

    public void drawSubstring(String str, int offset, int len, int x, int y, int anchor) {
        if (str == null) return;
        if (offset < 0 || len < 0 || offset + len > str.length()) {
            throw new StringIndexOutOfBoundsException();
        }
        drawString(str.substring(offset, offset + len), x, y, anchor);
    }

    public void drawChar(char character, int x, int y, int anchor) {
        drawString(String.valueOf(character), x, y, anchor);
    }

    public void drawChars(char[] data, int offset, int length, int x, int y, int anchor) {
        if (data == null) return;
        drawString(new String(data, offset, length), x, y, anchor);
    }

    public void drawImage(Image img, int x, int y, int anchor) {
        if (img == null || img.getBitmap() == null || canvas == null) return;
        Bitmap bmp = img.getBitmap();
        int w = bmp.getWidth();
        int h = bmp.getHeight();

        float drawX = x + transX;
        if ((anchor & HCENTER) != 0) {
            drawX -= w / 2f;
        } else if ((anchor & RIGHT) != 0) {
            drawX -= w;
        }

        float drawY = y + transY;
        if ((anchor & VCENTER) != 0) {
            drawY -= h / 2f;
        } else if ((anchor & BOTTOM) != 0) {
            drawY -= h;
        }

        canvas.drawBitmap(bmp, drawX, drawY, null);
    }

    public void drawRegion(Image src, int x_src, int y_src, int width, int height, int transform, int x_dest, int y_dest, int anchor) {
        if (src == null || src.getBitmap() == null || canvas == null) return;
        if (width <= 0 || height <= 0) return;

        Bitmap srcBmp = src.getBitmap();
        int outW = ((transform & 4) != 0) ? height : width;
        int outH = ((transform & 4) != 0) ? width : height;

        float drawX = x_dest + transX;
        if ((anchor & HCENTER) != 0) {
            drawX -= outW / 2f;
        } else if ((anchor & RIGHT) != 0) {
            drawX -= outW;
        }

        float drawY = y_dest + transY;
        if ((anchor & VCENTER) != 0) {
            drawY -= outH / 2f;
        } else if ((anchor & BOTTOM) != 0) {
            drawY -= outH;
        }

        if (transform == javax.microedition.lcdui.game.Sprite.TRANS_NONE) {
            srcRect.set(x_src, y_src, x_src + width, y_src + height);
            dstRect.set((int) drawX, (int) drawY, (int) drawX + width, (int) drawY + height);
            canvas.drawBitmap(srcBmp, srcRect, dstRect, null);
        } else {
            canvas.save();
            Matrix m = new Matrix();
            switch (transform) {
                case javax.microedition.lcdui.game.Sprite.TRANS_ROT90:
                    m.postRotate(90);
                    m.postTranslate(height, 0);
                    break;
                case javax.microedition.lcdui.game.Sprite.TRANS_ROT180:
                    m.postRotate(180);
                    m.postTranslate(width, height);
                    break;
                case javax.microedition.lcdui.game.Sprite.TRANS_ROT270:
                    m.postRotate(270);
                    m.postTranslate(0, width);
                    break;
                case javax.microedition.lcdui.game.Sprite.TRANS_MIRROR:
                    m.postScale(-1, 1);
                    m.postTranslate(width, 0);
                    break;
                case javax.microedition.lcdui.game.Sprite.TRANS_MIRROR_ROT90:
                    m.postScale(-1, 1);
                    m.postRotate(90);
                    m.postTranslate(height, 0);
                    break;
                case javax.microedition.lcdui.game.Sprite.TRANS_MIRROR_ROT180:
                    m.postScale(-1, 1);
                    m.postRotate(180);
                    m.postTranslate(width, height);
                    break;
                case javax.microedition.lcdui.game.Sprite.TRANS_MIRROR_ROT270:
                    m.postScale(-1, 1);
                    m.postRotate(270);
                    m.postTranslate(0, width);
                    break;
            }

            canvas.translate(drawX, drawY);
            canvas.concat(m);
            srcRect.set(x_src, y_src, x_src + width, y_src + height);
            dstRect.set(0, 0, width, height);
            canvas.drawBitmap(srcBmp, srcRect, dstRect, null);
            canvas.restore();
        }
    }

    public void drawRGB(int[] rgbData, int offset, int scanlength, int x, int y, int width, int height, boolean processAlpha) {
        if (rgbData == null || canvas == null || width <= 0 || height <= 0) return;
        canvas.drawBitmap(rgbData, offset, scanlength, x + transX, y + transY, width, height, processAlpha, null);
    }

    public void copyArea(int x_src, int y_src, int width, int height, int x_dest, int y_dest, int anchor) {
        // Optional screen area copy
    }
}
