package javax.microedition.lcdui;

import android.graphics.Paint;
import android.graphics.Typeface;

public class Font {
    public static final int STYLE_PLAIN = 0;
    public static final int STYLE_BOLD = 1;
    public static final int STYLE_ITALIC = 2;
    public static final int STYLE_UNDERLINED = 4;

    public static final int SIZE_SMALL = 8;
    public static final int SIZE_MEDIUM = 0;
    public static final int SIZE_LARGE = 16;

    public static final int FACE_SYSTEM = 0;
    public static final int FACE_MONOSPACE = 32;
    public static final int FACE_PROPORTIONAL = 64;

    public static final int FONT_STATIC_TEXT = 0;
    public static final int FONT_INPUT_TEXT = 1;

    private static Font defaultFont;

    private final int face;
    private final int style;
    private final int size;
    private final Paint paint;
    private final int height;
    private final int baseline;

    public static Font getDefaultFont() {
        if (defaultFont == null) {
            defaultFont = new Font(FACE_SYSTEM, STYLE_PLAIN, SIZE_MEDIUM);
        }
        return defaultFont;
    }

    public static Font getFont(int face, int style, int size) {
        return new Font(face, style, size);
    }

    public static Font getFont(int fontSpecifier) {
        return getDefaultFont();
    }

    public Font(int face, int style, int size) {
        this.face = face;
        this.style = style;
        this.size = size;

        this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Typeface tf;
        if (face == FACE_MONOSPACE) {
            tf = Typeface.MONOSPACE;
        } else {
            tf = Typeface.SANS_SERIF;
        }

        int tfStyle = Typeface.NORMAL;
        if ((style & STYLE_BOLD) != 0 && (style & STYLE_ITALIC) != 0) {
            tfStyle = Typeface.BOLD_ITALIC;
        } else if ((style & STYLE_BOLD) != 0) {
            tfStyle = Typeface.BOLD;
        } else if ((style & STYLE_ITALIC) != 0) {
            tfStyle = Typeface.ITALIC;
        }
        this.paint.setTypeface(Typeface.create(tf, tfStyle));

        float textSize = 16f;
        if (size == SIZE_SMALL) {
            textSize = 12f;
        } else if (size == SIZE_LARGE) {
            textSize = 22f;
        }
        this.paint.setTextSize(textSize);

        Paint.FontMetrics fm = this.paint.getFontMetrics();
        this.height = (int) Math.ceil(fm.descent - fm.ascent);
        this.baseline = (int) Math.ceil(-fm.ascent);
    }

    public int getStyle() {
        return style;
    }

    public int getSize() {
        return size;
    }

    public int getFace() {
        return face;
    }

    public boolean isPlain() {
        return style == STYLE_PLAIN;
    }

    public boolean isBold() {
        return (style & STYLE_BOLD) != 0;
    }

    public boolean isItalic() {
        return (style & STYLE_ITALIC) != 0;
    }

    public boolean isUnderlined() {
        return (style & STYLE_UNDERLINED) != 0;
    }

    public int getHeight() {
        return height;
    }

    public int getBaselinePosition() {
        return baseline;
    }

    public int charWidth(char ch) {
        return (int) Math.ceil(paint.measureText(new char[]{ch}, 0, 1));
    }

    public int charsWidth(char[] ch, int offset, int length) {
        if (ch == null || length <= 0) return 0;
        return (int) Math.ceil(paint.measureText(ch, offset, length));
    }

    public int stringWidth(String str) {
        if (str == null || str.isEmpty()) return 0;
        return (int) Math.ceil(paint.measureText(str));
    }

    public int substringWidth(String str, int offset, int len) {
        if (str == null || len <= 0) return 0;
        return (int) Math.ceil(paint.measureText(str, offset, offset + len));
    }

    public Paint getPaint() {
        return paint;
    }
}
