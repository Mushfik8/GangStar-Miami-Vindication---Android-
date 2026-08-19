package javax.microedition.lcdui;

import android.graphics.Bitmap;

public abstract class Canvas extends Displayable {
    public static final int UP = 1;
    public static final int LEFT = 2;
    public static final int RIGHT = 5;
    public static final int DOWN = 6;
    public static final int FIRE = 8;
    public static final int GAME_A = 9;
    public static final int GAME_B = 10;
    public static final int GAME_C = 11;
    public static final int GAME_D = 12;

    public static final int KEY_NUM0 = 48;
    public static final int KEY_NUM1 = 49;
    public static final int KEY_NUM2 = 50;
    public static final int KEY_NUM3 = 51;
    public static final int KEY_NUM4 = 52;
    public static final int KEY_NUM5 = 53;
    public static final int KEY_NUM6 = 54;
    public static final int KEY_NUM7 = 55;
    public static final int KEY_NUM8 = 56;
    public static final int KEY_NUM9 = 57;
    public static final int KEY_STAR = 42;
    public static final int KEY_POUND = 35;

    public static final int KEY_UP = -1;
    public static final int KEY_DOWN = -2;
    public static final int KEY_LEFT = -3;
    public static final int KEY_RIGHT = -4;
    public static final int KEY_FIRE = -5;
    public static final int KEY_SOFTKEY1 = -6;
    public static final int KEY_SOFTKEY2 = -7;
    public static final int KEY_CLEAR = -8;

    private static Canvas activeCanvas;

    private Bitmap offscreenBitmap;
    private android.graphics.Canvas offscreenCanvas;
    private Graphics offscreenGraphics;
    private boolean fullScreen = true;

    public static Canvas getActiveCanvas() {
        return activeCanvas;
    }

    protected Canvas() {
        activeCanvas = this;
        this.width = 800;
        this.height = 480;
        initBuffer(width, height);
    }

    private synchronized void initBuffer(int w, int h) {
        if (offscreenBitmap == null || offscreenBitmap.getWidth() != w || offscreenBitmap.getHeight() != h) {
            offscreenBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            offscreenCanvas = new android.graphics.Canvas(offscreenBitmap);
            offscreenGraphics = new Graphics(offscreenCanvas, w, h);
        }
    }

    public synchronized Bitmap getOffscreenBitmap() {
        return offscreenBitmap;
    }

    public synchronized void renderFrame() {
        if (offscreenGraphics != null) {
            offscreenGraphics.setClip(0, 0, width, height);
            paint(offscreenGraphics);
        }
    }

    protected abstract void paint(Graphics g);

    public void repaint() {
        // Handled in game loop
    }

    public void repaint(int x, int y, int width, int height) {
        // Handled in game loop
    }

    public void serviceRepaints() {
        // Handled in game loop
    }

    public void setFullScreenMode(boolean mode) {
        this.fullScreen = mode;
    }

    public boolean isDoubleBuffered() {
        return true;
    }

    public boolean hasPointerEvents() {
        return true;
    }

    public boolean hasPointerMotionEvents() {
        return true;
    }

    public boolean hasRepeatEvents() {
        return true;
    }

    public int getGameAction(int keyCode) {
        switch (keyCode) {
            case KEY_UP:
            case KEY_NUM2:
                return UP;
            case KEY_DOWN:
            case KEY_NUM8:
                return DOWN;
            case KEY_LEFT:
            case KEY_NUM4:
                return LEFT;
            case KEY_RIGHT:
            case KEY_NUM6:
                return RIGHT;
            case KEY_FIRE:
            case KEY_NUM5:
                return FIRE;
            case KEY_NUM1:
                return GAME_A;
            case KEY_NUM3:
                return GAME_B;
            case KEY_NUM7:
                return GAME_C;
            case KEY_NUM9:
                return GAME_D;
            default:
                return 0;
        }
    }

    public int getKeyCode(int gameAction) {
        switch (gameAction) {
            case UP: return KEY_UP;
            case DOWN: return KEY_DOWN;
            case LEFT: return KEY_LEFT;
            case RIGHT: return KEY_RIGHT;
            case FIRE: return KEY_FIRE;
            case GAME_A: return KEY_NUM1;
            case GAME_B: return KEY_NUM3;
            case GAME_C: return KEY_NUM7;
            case GAME_D: return KEY_NUM9;
            default: return 0;
        }
    }

    public String getKeyName(int keyCode) {
        switch (keyCode) {
            case KEY_UP: return "UP";
            case KEY_DOWN: return "DOWN";
            case KEY_LEFT: return "LEFT";
            case KEY_RIGHT: return "RIGHT";
            case KEY_FIRE: return "FIRE";
            case KEY_SOFTKEY1: return "SOFT1";
            case KEY_SOFTKEY2: return "SOFT2";
            default: return String.valueOf((char) keyCode);
        }
    }

    public void keyPressedPublic(int keyCode) {
        keyPressed(keyCode);
    }

    public void keyReleasedPublic(int keyCode) {
        keyReleased(keyCode);
    }

    public void pointerPressedPublic(int x, int y) {
        pointerPressed(x, y);
    }

    public void pointerReleasedPublic(int x, int y) {
        pointerReleased(x, y);
    }

    public void pointerDraggedPublic(int x, int y) {
        pointerDragged(x, y);
    }

    protected void keyPressed(int keyCode) {}
    protected void keyReleased(int keyCode) {}
    protected void keyRepeated(int keyCode) {}

    protected void pointerPressed(int x, int y) {}
    protected void pointerReleased(int x, int y) {}
    protected void pointerDragged(int x, int y) {}

    public void showNotifyInternal() {
        showNotify();
    }

    public void hideNotifyInternal() {
        hideNotify();
    }

    protected void showNotify() {}
    protected void hideNotify() {}

    @Override
    protected void sizeChanged(int w, int h) {
        super.sizeChanged(w, h);
        initBuffer(w, h);
    }
}
