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

package org.gearvrf.modelviewer2;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

import org.gearvrf.plugins.widget.GVRWidget;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.graphics.Bitmap.Config;
import android.opengl.GLException;
import android.widget.RadioButton;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.android.AndroidGraphics;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;

import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;

public class MyGdxWidget extends GVRWidget {

    private Stage stage;
    private Table container;
    public Bitmap b = null;
    public boolean resetSlider = false;
    Actor xs;
    Actor ys;
    Actor zs;
    Actor colorbutton;
    Actor lookinsidebutton;
    public float x, y, z;
    public ViewerScript mScript;
    Button nextbutton;
    Button previousbutton;
    Button mcolorbutton;
    Button resetbutton;
    Button mlookinsidebutton;
    public CheckBox mcheckbox;
    public EGLContext meglContext = null;

    @SuppressWarnings("unchecked")
    public void create() {
        stage = new Stage();
        Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));
        Gdx.input.setInputProcessor(stage);

        // Gdx.graphics.setVSync(false);

        container = new Table();
        stage.addActor(container);
        container.setFillParent(true);

        Table table = new Table();
        // table.debug();

        final ScrollPane scroll = new ScrollPane(table, skin);

        InputListener stopTouchDown = new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y,
                    int pointer, int button) {
                event.stop();
                return false;
            }
        };

        table.pad(0).defaults().expandX().space(10);
        for (int i = 0; i < 4; i++) {
            table.row();
            table.add(new Label("", skin)).expandX().fillX();
            TextButton button = null;
            if (i == 0) {
                button = new TextButton("  Next  ", skin);
                nextbutton = button;
                button.addListener(new ClickListener() {
                    public void clicked(InputEvent event, float x, float y) {
                        System.out.println("click " + x + ", " + y);

                        mScript.ThumbnailSelected = (mScript.ThumbnailSelected + 1) % 5;
                        /*
                         * if(mScript.ThumbnailSelected == 2){
                         * mScript.ThumbnailSelected = 1; }
                         * if(mScript.ThumbnailSelected == 1){
                         * mScript.ThumbnailSelected = 2; }
                         */

                        boolean checked = nextbutton.isChecked();
                        nextbutton.setChecked(false);

                    }
                });
                // button.setScale(10.0f);
            } else if (i == 1) {
                button = new TextButton("Previous", skin);
                previousbutton = button;
                button.addListener(new ClickListener() {
                    public void clicked(InputEvent event, float x, float y) {
                        System.out.println("click " + x + ", " + y);
                        mScript.ThumbnailSelected = (mScript.ThumbnailSelected + 4) % 5;
                        /*
                         * if(mScript.ThumbnailSelected == 2){
                         * mScript.ThumbnailSelected = 1; }
                         * if(mScript.ThumbnailSelected == 1){
                         * mScript.ThumbnailSelected = 2; }
                         */
                        // event.stop();
                        previousbutton.setChecked(false);
                    }
                });
                // button.setSize(200.0f, 200.0f);
                // button.setScale(10.0f);
            } else if (i == 2) { /*
                                  * button = new TextButton("Reset", skin);
                                  * button.setVisible(false);
                                  * 
                                  * button.addListener(new ClickListener() {
                                  * public void clicked (InputEvent event, float
                                  * x, float y) { resetSlider = true; } });
                                  */
                // button = new TextButton("Color", skin);
                final SelectBox selectBox = new SelectBox(skin);
                // mcolorbutton = button;
                // button.setPosition(300, 300);
                /*
                 * button.addListener(new ClickListener() { public void clicked
                 * (InputEvent event, float x, float y) {
                 * 
                 * mScript.color = (mScript.color)%5+1;
                 * //mcolorbutton.setChecked(false); //event.stop(); } });
                 * button.setVisible(false); button.setName("colorbutton");
                 */
                selectBox.addListener(new ChangeListener() {
                    public void changed(ChangeEvent event, Actor actor) {
                        mScript.color = selectBox.getSelectedIndex() + 1;
                    }
                });
                selectBox
                        .setItems("Maroon", "Black", "Blue", "Green", "Silver");
                selectBox.setSelected("Maroon");
                selectBox.setVisible(false);
                selectBox.setName("colorbutton");
                table.add(selectBox);

            } else {
                final CheckBox box = new CheckBox("Reset", skin);
                mcheckbox = box;
                box.setChecked(true);
                // button = new TextButton("Reset", skin);
                // resetbutton = button;
                // button.setPosition(300, 300);
                /*
                 * button.addListener(new ClickListener() { public void clicked
                 * (InputEvent event, float x, float y) { resetSlider = true;
                 * //resetbutton.setChecked(false); //event.stop(); } });
                 */

                box.addListener(new ChangeListener() {
                    public void changed(ChangeEvent event, Actor actor) {
                        ((Slider) xs).setValue(0.0f);
                        resetSlider = box.isChecked();
                    }
                });
                table.add(box);
            }

            // if(i != 1){
            // if(i < 3)
            table.add(button).height(50).width(140);

            // if(i < 2)

            // }
            Slider slider = null;
            if (i < 3) {
                slider = new Slider(0, 100, 1, false, skin);
                if (i == 0) {
                    slider.setName("X");
                    slider.setVisible(false);
                }
                if (i == 1) {
                    slider.setName("Y");
                    slider.setVisible(false);
                }
                if (i == 2) {
                    slider.setName("Z");
                    slider.setVisible(false);
                }
                ;
                slider.addListener(stopTouchDown); // Stops touchDown events
                                                   // from propagating to the
                                                   // FlickScrollPane.
                if (i == 0) {
                    Label l = new Label("Rotate X", skin);
                    table.add(l);
                    l.setVisible(false);
                }
                if (i == 1) {
                    Label l = new Label("Rotate Y", skin);
                    l.setVisible(false);
                    table.add(l);
                }
                if (i == 2) {
                    Label l2 = new Label("Rotate Z", skin);
                    table.add(l2);
                    l2.setVisible(false);
                }
                table.add(slider).height(60).width(160);
            }

        }

        table.row();
        table.add(new Label("", skin)).expandX().fillX();
        Button button = new TextButton("Look Inside", skin);
        mlookinsidebutton = button;
        // button.setPosition(300, 300);
        button.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {

                mScript.mLookInside = true;
                mlookinsidebutton.setChecked(false);
                mlookinsidebutton.toggle();
                // event.stop();
            }
        });
        button.setVisible(false);
        button.setName("lookinsidebutton");
        table.add(button).height(50).width(140);
        table.row();

        Slider slider = null;

        slider = new Slider(0, 100, 1, false, skin);
        slider.setName("Zoom");
        slider.addListener(stopTouchDown);
        table.add(new Label("Zoom", skin));
        table.add(slider).height(60).width(160);
        final TextButton flickButton = new TextButton("Flick Scroll", skin.get(
                "toggle", TextButtonStyle.class));
        flickButton.setChecked(true);
        flickButton.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                scroll.setFlickScroll(flickButton.isChecked());
            }
        });

        final TextButton fadeButton = new TextButton("Fade Scrollbars",
                skin.get("toggle", TextButtonStyle.class));
        fadeButton.setChecked(true);
        fadeButton.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                scroll.setFadeScrollBars(fadeButton.isChecked());
            }
        });

        final TextButton smoothButton = new TextButton("Smooth Scrolling",
                skin.get("toggle", TextButtonStyle.class));
        smoothButton.setChecked(true);
        smoothButton.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                scroll.setSmoothScrolling(smoothButton.isChecked());
            }
        });

        final TextButton onTopButton = new TextButton("Scrollbars On Top",
                skin.get("toggle", TextButtonStyle.class));
        onTopButton.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                scroll.setScrollbarsOnTop(onTopButton.isChecked());
            }
        });

        container.add(scroll).expand().fill().colspan(4);
        container.row().space(10).padBottom(10);
        // container.add(flickButton).right().expandX();
        // container.add(onTopButton);
        // container.add(smoothButton);
        // container.add(fadeButton).left().expandX();

    }

    private static int drawnum = 0;

    public void render() {
        drawnum++;

        // if(mScript.libGDxTexture == 0 && drawnum > 5){
        // mScript.libGDxTexture =
        // ((AndroidGraphics)Gdx.graphics).mTargetTexture;

        // mScript.mSharedcontext = ((AndroidGraphics)Gdx.graphics).eglContext;

        /*
         * int[] configAttribs = {EGL10.EGL_RED_SIZE, 8, EGL10.EGL_GREEN_SIZE,
         * 8, EGL10.EGL_BLUE_SIZE, 8, EGL10.EGL_ALPHA_SIZE,
         * 8,EGL10.EGL_DEPTH_SIZE,16, EGL10.EGL_STENCIL_SIZE,0,EGL10.EGL_NONE};
         * 
         * 
         * int auxNumConfigs; EGLConfig auxConfig;
         * 
         * EGLConfig[] configs = new EGLConfig[10]; int[] num_config = new
         * int[1]; int[] attrib_list = { 0x3098, 2, EGL10.EGL_NONE }; EGLDisplay
         * d = ((EGL10)EGLContext.getEGL()).eglGetCurrentDisplay(); int[]
         * num_conf = new int[1];
         * 
         * ((EGL10)EGLContext.getEGL()).eglChooseConfig(((EGL10)EGLContext.getEGL
         * ()).eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY), configAttribs, configs,
         * 1, num_config);
         * 
         * ((EGL10)EGLContext.getEGL()).eglGetConfigs(((EGL10)EGLContext.getEGL()
         * ).eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY), null, 0, num_conf); int
         * configurations = num_conf[0];
         * 
         * //Querying actual configurations EGLConfig[] conf = new
         * EGLConfig[configurations];
         * 
         * ((EGL10)EGLContext.getEGL()).eglGetConfigs(((EGL10)EGLContext.getEGL()
         * ).eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY), conf, configurations,
         * num_conf); EGLContext c =
         * ((EGL10)EGLContext.getEGL()).eglCreateContext
         * (((EGL10)EGLContext.getEGL
         * ()).eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
         * ,configs[0],mScript.mSharedcontext, attrib_list); // EGLSurface s =
         * ((EGL10)EGLContext.getEGL()).eglGetCurrentSurface(EGL10.EGL_DRAW);
         * SurfaceTexture mSurfaceTexture = new
         * SurfaceTexture(mScript.libGDxTexture);
         * 
         * EGLSurface EglSurface =
         * ((EGL10)EGLContext.getEGL()).eglCreateWindowSurface
         * (((EGL10)EGLContext
         * .getEGL()).eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY), configs[0],
         * mSurfaceTexture, attrib_list);
         * ((EGL10)EGLContext.getEGL()).eglMakeCurrent(d, EglSurface,
         * EglSurface,c);
         */
        // float[] a = mScript.button_mesh.getVertices();

        // float[] c = mScript.button_mesh.getTexCoords();
        // char[] b = mScript.button_mesh.getIndices();
        // }
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // Gdx.gl.glEnable(GL10.GL_BLEND);
        // Gdx.gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);
        // RGdx.gl.glClearColor(0, 1, 0, 0);
        stage.act(Gdx.graphics.getDeltaTime());
        if (xs == null) {
            colorbutton = stage.getRoot().findActor("colorbutton");
            lookinsidebutton = stage.getRoot().findActor("lookinsidebutton");
            xs = stage.getRoot().findActor("Zoom");
            ys = stage.getRoot().findActor("Y");
            zs = stage.getRoot().findActor("Z");
        }
        Array<Actor> stageActors = stage.getActors();
        if (xs != null) {
            x = ((Slider) xs).getValue();
            y = ((Slider) ys).getValue();
            z = ((Slider) zs).getValue();
            mScript.zoomlevel = (x / 100.0f * 2.0f) - 2.0f;
            // if(!mScript.objectpointed)
            // mScript.rotatey = (360.0f/100.0f) * y;
            mScript.rotatez = (360.0f / 100.0f) * z;
        }
        if (mcheckbox.isChecked() && x != 0)
            mcheckbox.setChecked(false);
        if (resetSlider) {

            /*
             * mScript.rotatex = 0.0f; mScript.rotatey = 0.0f; mScript.rotatez =
             * 0.0f; mScript.lastrotatex = 0.0f; mScript.lastrotatey = 0.0f;
             * mScript.lastrotatez = 0.0f;
             */
            ((Slider) xs).setValue(0);
            ((Slider) ys).setValue(0);
            ((Slider) zs).setValue(0);
            mScript.resetrotate = true;
            for (int i = 0; i < 5; i++)
                mScript.Objects[i].getTransform().setRotationByAxis(0.0f, 0.0f,
                        0.0f, 0.0f);

            // int len = stageActors.size;
            /*
             * for(int i=0; i<len; i++){ Actor a = stageActors.get(i);
             * if(a.getName() != null && (a.getName().equals("X") ||
             * a.getName().equals("Y") || a.getName().equals("Z"))){ //a is your
             * Actor! ((Slider) a).setValue(0); break; } }
             */
            resetSlider = false;

        }
        if (mScript.ThumbnailSelected == 1 || mScript.ThumbnailSelected == 3) {
            ((SelectBox) colorbutton).setVisible(true);

        } else
            ((SelectBox) colorbutton).setVisible(false);
        if (mScript.ThumbnailSelected == 3)
            ((Button) lookinsidebutton).setVisible(true);
        else
            ((Button) lookinsidebutton).setVisible(false);

        stage.draw();

        SpriteBatch batch = (SpriteBatch) stage.getBatch();

        // Pixmap p = ScreenUtils.getFrameBufferTexturepixmap(0 ,0, 512,512);
        /*
         * IntBuffer intBuf = p.getPixels().asIntBuffer();
         * 
         * int[] array = new int[intBuf.remaining()]; intBuf.get(array);
         */
        // b = Bitmap.createBitmap(array, 512, 512, Bitmap.Config.ARGB_8888);
        // b = Bitmap.createScaledBitmap(src, 400, 400, true);
        // b = createBitmapFromGLSurface(0,0,100,100);
        // b = createBitmapFromGLSurface(0,0,512,512);
        // Gdx.gl.glClearColor(0, 0, 0, 0);

    }

    public void resize(int width, int height) {
        super.resize(width, height);
        stage.getViewport().update(width, height, true);
        // mScript.libGDxTexture =
        // ((AndroidGraphics)Gdx.graphics).mTargetTexture;
        // Gdx.gl.glViewport(100, 100, width - 200, height - 200);
        // stage.setViewport(800, 600, false, 100, 100, width - 200, height -
        // 200);
    }

    public void dispose() {
        stage.dispose();
    }

    public boolean needsGL20() {
        return false;
    }

    private Bitmap createBitmapFromGLSurface(int x, int y, int w, int h)
            throws OutOfMemoryError {
        long a = 4278190080l;
        int bitmapSource[] = new int[w * h];
        ByteBuffer pixelBuffer;
        pixelBuffer = ByteBuffer.allocateDirect(w * h * 4).order(
                ByteOrder.nativeOrder());
        try {
            Gdx.gl.glReadPixels(x, y, w, h, GL10.GL_RGBA,
                    GL10.GL_UNSIGNED_BYTE, pixelBuffer);
            int pixelsBuffer[] = new int[w * h];
            pixelBuffer.asIntBuffer().get(pixelsBuffer);
            int offset1, offset2;
            for (int i = 0; i < h; i++) {
                offset1 = i * w;
                offset2 = (h - i - 1) * w;
                for (int j = 0; j < w; j++) {
                    int texturePixel = pixelsBuffer[offset1 + j];
                    int blue = (texturePixel >> 16) & 0xff;
                    int red = (texturePixel << 16) & 0x00ff0000;
                    int pixel = (texturePixel & 0xff00ff00) | red | blue;
                    pixel |= a;
                    bitmapSource[offset2 + j] = pixel;

                }
            }
        } catch (GLException e) {
            return null;
        }

        return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
    }

}
