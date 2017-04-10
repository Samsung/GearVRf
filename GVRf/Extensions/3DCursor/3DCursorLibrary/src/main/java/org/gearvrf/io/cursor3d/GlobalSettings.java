/*
 * Copyright (c) 2016. Samsung Electronics Co., LTD
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.io.cursor3d;

class GlobalSettings {
    private boolean soundEnabled;
    private boolean preview;
    private boolean onScreen;

    private static GlobalSettings globalSettings;

    private GlobalSettings() {

    }

    static GlobalSettings getInstance() {
        if (globalSettings == null) {
            globalSettings = new GlobalSettings();
        }
        return globalSettings;
    }

    void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }

    boolean isSoundEnabled() {
        return soundEnabled;
    }

    void setOnScreen(boolean onScreen) {
        this.onScreen = onScreen;
    }

    boolean isOnScreen() {
        return onScreen;
    }

    void setPreview(boolean preview) {
        this.preview = preview;
    }

    boolean getPreview() {
        return preview;
    }
}