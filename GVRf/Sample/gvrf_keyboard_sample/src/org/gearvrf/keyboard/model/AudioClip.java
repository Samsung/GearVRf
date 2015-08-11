
package org.gearvrf.keyboard.model;

import org.gearvrf.keyboard.R;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;

public class AudioClip {

    private static AudioClip instance;
    private boolean plays = false;
    private boolean loaded = false;
    private int priority;

    private Context context;
    private SoundPool soundPool;
    private static int keyEnterSoundID;
    private static int keyHoverSoundID;
    private static int exceptionSoundID;
    private static int selectionSoundID;
    private static int snapSoundID;
    private static int sucessSoundID;
    private static int spinnerSoundID;
    private static int wrongSoundID;

    public static synchronized AudioClip getInstance(Context androidContext) {
        if (instance == null) {
            instance = new AudioClip(10, AudioManager.STREAM_MUSIC, 0, androidContext);
        }
        return instance;
    }

    private AudioClip(int maxStreams, int streamType, int srcQuality, Context context) {

        this.context = context;

        soundPool = new SoundPool(maxStreams, streamType, srcQuality);
        soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });

        loadinSounds();

    }

    public void playSound(int soundID, float leftVolume, float rightVolume) {

        if (loaded) {
            soundPool.play(soundID, leftVolume, rightVolume, 1, 0, 1f);
            priority = priority++;
            plays = true;
        }
    }

    public void playLoop(int soundID, float leftVolume, float rightVolume) {

        if (loaded) {
            soundPool.play(soundID, leftVolume, rightVolume, 1, -1, 1f);
            priority = priority++;
            plays = true;
        }
    }

    public void pauseSound(int soundID) {
        if (plays) {
            soundPool.pause(soundID);
            soundID = soundPool.load(context, soundID, priority);
            plays = false;
        }
    }

    public void stopSound(int soundID) {
        if (plays) {
            soundPool.stop(soundID);
            soundID = soundPool.load(context, soundID, priority);
            plays = false;
        }
    }

    private void loadinSounds() {
        keyEnterSoundID = soundPool.load(context, R.raw.key_enter_sound, 1);
        keyHoverSoundID = soundPool.load(context, R.raw.key_hover_sound, 1);
        exceptionSoundID = soundPool.load(context, R.raw.exception_sound, 1);
        selectionSoundID = soundPool.load(context, R.raw.selection_sound, 1);
        snapSoundID = soundPool.load(context, R.raw.snap_sound, 1);
        sucessSoundID = soundPool.load(context, R.raw.sucess_sound, 1);
        spinnerSoundID = soundPool.load(context, R.raw.spinner_audio, 1);
        wrongSoundID = soundPool.load(context, R.raw.wrong_sound, 1);
    }

    public static int getKeyEnterSoundID() {
        return keyEnterSoundID;
    }

    public static int getKeyHoverSoundID() {
        return keyHoverSoundID;
    }

    public static int getExceptionSoundID() {
        return exceptionSoundID;
    }

    public static int getSelectionSoundID() {
        return selectionSoundID;
    }

    public static int getSnapSoundID() {
        return snapSoundID;
    }

    public static int getSucessSoundID() {
        return sucessSoundID;
    }

    public static int getSpinnerSoundID() {
        return spinnerSoundID;
    }

    public static int getWrongSoundID() {
        return wrongSoundID;
    }

}
