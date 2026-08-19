package javax.microedition.media;

import android.media.MediaPlayer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import javax.microedition.media.control.VolumeControl;

public class Player implements VolumeControl {
    public static final int UNREALIZED = 100;
    public static final int REALIZED = 200;
    public static final int PREFETCHED = 300;
    public static final int STARTED = 400;
    public static final int CLOSED = 0;

    private int state = UNREALIZED;
    private MediaPlayer mediaPlayer;
    private File tempAudioFile;
    private int volume = 100;
    private boolean muted = false;
    private int loopCount = 1;

    public Player(InputStream is, String type) {
        try {
            if (is != null) {
                tempAudioFile = File.createTempFile("j2me_audio_", ".mid");
                tempAudioFile.deleteOnExit();
                FileOutputStream fos = new FileOutputStream(tempAudioFile);
                byte[] buf = new byte[4096];
                int r;
                while ((r = is.read(buf)) != -1) {
                    fos.write(buf, 0, r);
                }
                fos.close();
            }
            state = REALIZED;
        } catch (Exception e) {
            state = REALIZED;
        }
    }

    public synchronized void realize() {
        if (state == CLOSED) return;
        state = REALIZED;
    }

    public synchronized void prefetch() {
        if (state == CLOSED) return;
        try {
            if (tempAudioFile != null && tempAudioFile.exists()) {
                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(tempAudioFile.getAbsolutePath());
                    mediaPlayer.prepare();
                }
            }
        } catch (Exception ignored) {}
        state = PREFETCHED;
    }

    public synchronized void start() {
        if (state == CLOSED) return;
        try {
            prefetch();
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(loopCount == -1 || loopCount > 1);
                applyVolume();
                mediaPlayer.start();
                state = STARTED;
            }
        } catch (Exception ignored) {}
    }

    public synchronized void stop() {
        if (state == CLOSED) return;
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        } catch (Exception ignored) {}
        state = PREFETCHED;
    }

    public synchronized void deallocate() {
        stop();
        state = REALIZED;
    }

    public synchronized void close() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
            if (tempAudioFile != null && tempAudioFile.exists()) {
                tempAudioFile.delete();
            }
        } catch (Exception ignored) {}
        state = CLOSED;
    }

    public int getState() {
        return state;
    }

    public void setLoopCount(int count) {
        this.loopCount = count;
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(count == -1 || count > 1);
        }
    }

    public Control getControl(String controlType) {
        if (controlType != null && controlType.endsWith("VolumeControl")) {
            return this;
        }
        return null;
    }

    public Control[] getControls() {
        return new Control[]{this};
    }

    public long setMediaTime(long now) {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.seekTo((int) (now / 1000));
            } catch (Exception ignored) {}
        }
        return now;
    }

    public long getMediaTime() {
        if (mediaPlayer != null) {
            try {
                return (long) mediaPlayer.getCurrentPosition() * 1000L;
            } catch (Exception ignored) {}
        }
        return 0;
    }

    public long getDuration() {
        if (mediaPlayer != null) {
            try {
                return (long) mediaPlayer.getDuration() * 1000L;
            } catch (Exception ignored) {}
        }
        return 0;
    }

    public String getContentType() {
        return "audio/midi";
    }

    @Override
    public int getLevel() {
        return volume;
    }

    @Override
    public int setLevel(int level) {
        this.volume = Math.max(0, Math.min(100, level));
        applyVolume();
        return this.volume;
    }

    @Override
    public boolean isMuted() {
        return muted;
    }

    @Override
    public void setMute(boolean mute) {
        this.muted = mute;
        applyVolume();
    }

    private void applyVolume() {
        if (mediaPlayer != null) {
            try {
                float vol = muted ? 0.0f : (volume / 100.0f);
                mediaPlayer.setVolume(vol, vol);
            } catch (Exception ignored) {}
        }
    }
}
