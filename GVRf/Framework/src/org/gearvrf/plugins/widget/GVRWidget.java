/* Copyright 2015 Samsung Electronics Co., LTD
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

package org.gearvrf.plugins.widget;

import org.gearvrf.GVRScript;

import com.badlogic.gdx.ApplicationAdapter;

public abstract class GVRWidget extends ApplicationAdapter {

    private boolean isInitialised = false;
    private int mTexid = 0;

    @Override
    public void create() {
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void render() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
    }

    public boolean isInitialised() {
        return isInitialised;
    }

    @Override
    public void notifyCreation(int id) {
        // TODO Auto-generated method stub
        isInitialised = true;
        mTexid = id;
    }

    public int getTexId() {
        return mTexid;
    }

}
