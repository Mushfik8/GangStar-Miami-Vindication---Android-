package javax.microedition.lcdui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Image {
    private Bitmap bitmap;
    private final boolean mutable;
    private Graphics graphics;

    public static Image createImage(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive");
        }
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        return new Image(bmp, true);
    }

    public static Image createImage(String name) throws IOException {
        if (name == null) throw new NullPointerException("Resource name is null");
        InputStream is = null;
        try {
            // Try with leading slash
            String path = name.startsWith("/") ? name : "/" + name;
            is = Image.class.getResourceAsStream(path);
            if (is == null) {
                // Try without leading slash
                String pathNoSlash = name.startsWith("/") ? name.substring(1) : name;
                is = Image.class.getClassLoader().getResourceAsStream(pathNoSlash);
            }
            if (is == null) {
                // Try Android AssetManager if available via Display
                is = getAssetStream(name);
            }
            if (is == null) {
                throw new IOException("Resource not found: " + name);
            }
            return createImage(is);
        } finally {
            if (is != null) {
                try { is.close(); } catch (Exception ignored) {}
            }
        }
    }

    public static InputStream getAssetStream(String name) {
        try {
            String clean = name.startsWith("/") ? name.substring(1) : name;
            return Image.class.getClassLoader().getResourceAsStream("assets/" + clean);
        } catch (Exception ignored) {}
        return null;
    }

    public static Image createImage(InputStream stream) throws IOException {
        if (stream == null) throw new NullPointerException("InputStream is null");
        Bitmap bmp = BitmapFactory.decodeStream(stream);
        if (bmp == null) {
            throw new IOException("Failed to decode image from stream");
        }
        return new Image(bmp, false);
    }

    public static Image createImage(byte[] imageData, int imageOffset, int imageLength) {
        if (imageData == null) throw new NullPointerException("Image data is null");
        if (imageOffset < 0 || imageLength <= 0 || imageOffset + imageLength > imageData.length) {
            throw new ArrayIndexOutOfBoundsException("Invalid offset or length");
        }
        Bitmap bmp = BitmapFactory.decodeByteArray(imageData, imageOffset, imageLength);
        if (bmp == null) {
            throw new IllegalArgumentException("Failed to decode byte array to image");
        }
        return new Image(bmp, false);
    }

    public static Image createImage(Image source) {
        if (source == null) throw new NullPointerException("Source image is null");
        Bitmap bmp = source.bitmap.copy(Bitmap.Config.ARGB_8888, false);
        return new Image(bmp, false);
    }

    public static Image createImage(Image image, int x, int y, int width, int height, int transform) {
        if (image == null) throw new NullPointerException("Source image is null");
        if (x < 0 || y < 0 || width <= 0 || height <= 0 || x + width > image.getWidth() || y + height > image.getHeight()) {
            throw new IllegalArgumentException("Bounds out of range");
        }

        Matrix matrix = new Matrix();
        switch (transform) {
            case javax.microedition.lcdui.game.Sprite.TRANS_NONE:
                break;
            case javax.microedition.lcdui.game.Sprite.TRANS_ROT90:
                matrix.postRotate(90);
                break;
            case javax.microedition.lcdui.game.Sprite.TRANS_ROT180:
                matrix.postRotate(180);
                break;
            case javax.microedition.lcdui.game.Sprite.TRANS_ROT270:
                matrix.postRotate(270);
                break;
            case javax.microedition.lcdui.game.Sprite.TRANS_MIRROR:
                matrix.postScale(-1, 1);
                break;
            case javax.microedition.lcdui.game.Sprite.TRANS_MIRROR_ROT90:
                matrix.postScale(-1, 1);
                matrix.postRotate(90);
                break;
            case javax.microedition.lcdui.game.Sprite.TRANS_MIRROR_ROT180:
                matrix.postScale(-1, 1);
                matrix.postRotate(180);
                break;
            case javax.microedition.lcdui.game.Sprite.TRANS_MIRROR_ROT270:
                matrix.postScale(-1, 1);
                matrix.postRotate(270);
                break;
        }

        Bitmap subBmp = Bitmap.createBitmap(image.bitmap, x, y, width, height, matrix, false);
        return new Image(subBmp, false);
    }

    public static Image createRGBImage(int[] rgb, int width, int height, boolean processAlpha) {
        if (rgb == null) throw new NullPointerException("rgb array is null");
        if (width <= 0 || height <= 0 || rgb.length < width * height) {
            throw new IllegalArgumentException("Invalid dimensions or array size");
        }

        int[] pixels = rgb;
        if (!processAlpha) {
            pixels = new int[width * height];
            for (int i = 0; i < pixels.length; i++) {
                pixels[i] = rgb[i] | 0xFF000000;
            }
        }

        Bitmap bmp = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
        return new Image(bmp, false);
    }

    public Image(Bitmap bitmap, boolean mutable) {
        this.bitmap = bitmap;
        this.mutable = mutable;
    }

    public Graphics getGraphics() {
        if (!mutable) {
            throw new IllegalStateException("Cannot get graphics of immutable image");
        }
        if (graphics == null) {
            graphics = new Graphics(new android.graphics.Canvas(bitmap), bitmap.getWidth(), bitmap.getHeight());
        }
        return graphics;
    }

    public int getWidth() {
        return bitmap != null ? bitmap.getWidth() : 0;
    }

    public int getHeight() {
        return bitmap != null ? bitmap.getHeight() : 0;
    }

    public boolean isMutable() {
        return mutable;
    }

    public void getRGB(int[] rgbData, int offset, int scanlength, int x, int y, int width, int height) {
        if (rgbData == null) throw new NullPointerException("rgbData is null");
        if (bitmap != null) {
            bitmap.getPixels(rgbData, offset, scanlength, x, y, width, height);
        }
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
