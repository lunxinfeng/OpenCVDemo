package lxf.widget.tileview;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import java.util.ArrayList;

import lxf.widget.R;

/**
 * 音效池  (0:move  1:deadstoneless)
 *
 * @author lxf
 */
public class SoundPoolUtils {
    private SoundPool soundPool ;
    private ArrayList<Integer> musicIds;

    public SoundPoolUtils(Context context) {
        super();
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        musicIds = new ArrayList<Integer>();
        addSound(soundPool.load(context, R.raw.move, 1));
        addSound(soundPool.load(context, R.raw.deadstoneless, 1));
    }

    public void addSound(int musicId){
        musicIds.add(musicId);
    }

    public void playSound(int index){
        soundPool.play(musicIds.get(index), 1, 1, 0, 0, 1);
    }
}