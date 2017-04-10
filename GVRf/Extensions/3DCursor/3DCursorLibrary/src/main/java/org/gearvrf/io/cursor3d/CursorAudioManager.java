/*
 * Copyright 2016 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.io.cursor3d;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import org.gearvrf.io.cursor3d.CursorAsset.Action;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that manages audio sounds for a given preset and also offers playback
 * based on the {@link CursorAsset}.
 */
class CursorAudioManager {
    // TODO check for a good number
    private static final int MAX_STREAMS = 5;
    public static final float DEFAULT_VOLUME = 1.0f;
    public static final int STREAM_PRIORITY = 0;
    public static final int NO_LOOP = 0;
    public static final float DEFAULT_PLAYBACK_RATE = 1.0f;

    private static CursorAudioManager instance;
    private final SoundPool soundPool;
    private final Context context;
    private final Map<Action, Integer> assetSoundMap;

    CursorAudioManager(Context context) {
        this.context = context;
        assetSoundMap = new HashMap<Action, Integer>(MAX_STREAMS);
        // Do we need a OnLoadCompleteListener?
        soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
    }

    static CursorAudioManager getInstance(Context context) {
        if (instance == null) {
            instance = new CursorAudioManager(context);
        }
        return instance;
    }

    void loadTheme(CursorTheme theme) {
        // clear the map
        for (Integer id : assetSoundMap.values()) {
            soundPool.unload(id);
        }

        assetSoundMap.clear();

        for (CursorAsset cursorAsset : theme.getCursorAssets()) {
            if (cursorAsset.hasSound()) {
                int id = soundPool.load(cursorAsset.getSoundFd(), 0);
                assetSoundMap.put(cursorAsset.getAction(), id);
            }
        }
    }

    void play(CursorAsset cursorAsset) {
        Integer id = assetSoundMap.get(cursorAsset.getAction());

        if (id != null) {
            //TODO check if the volume can be adjusted.
            soundPool.play(id, DEFAULT_VOLUME, DEFAULT_VOLUME, STREAM_PRIORITY, NO_LOOP,
                    DEFAULT_PLAYBACK_RATE);
        }
    }

    void close() {
        soundPool.release();
    }
}
