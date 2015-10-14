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

package org.gearvrf.immersivepedia.model;

import java.io.IOException;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRRotationByAxisAnimation;
import org.gearvrf.immersivepedia.R;
import org.gearvrf.immersivepedia.dinosaur.Dinosaur;
import org.gearvrf.immersivepedia.dinosaur.DinosaurFactory;
import org.gearvrf.immersivepedia.focus.FocusableSceneObject;
import org.gearvrf.immersivepedia.focus.OnGestureListener;
import org.gearvrf.immersivepedia.gallery.Gallery;
import org.gearvrf.immersivepedia.props.Totem;
import org.gearvrf.immersivepedia.util.AudioClip;

public class GalleryDinosaurGroup extends GVRSceneObject {
    private int photos[] = new int[] {
            R.drawable.photo01, R.drawable.photo02, R.drawable.photo03,
            R.drawable.photo04,
            R.drawable.photo05, R.drawable.photo06, R.drawable.photo07,
            R.drawable.photo08,
            R.drawable.photo09, R.drawable.photo10, R.drawable.photo03,
            R.drawable.photo02
    };

    private GVRSceneObject galleryGroup;
    private Gallery gallery;
    private GVRScene scene;
    private GVRContext gvrContext;
    private FocusableSceneObject focus;
    private Dinosaur dinosaur;

    public GalleryDinosaurGroup(GVRContext gvrContext, GVRScene scene) throws IOException {
        super(gvrContext);
        this.gvrContext = gvrContext;
        this.scene = scene;

        createGallery();
        createGalleryGroup();
        createTotem();
        createDinosaur();
        createFocus();
    }

    private void createFocus() {

        focus = new FocusableSceneObject(gvrContext, gvrContext.createQuad(15f, 9f),
                gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.empty)));
        focus.getTransform().setPosition(0f, 3.5f, DinosaurFactory.APATOSAURUS_DISTANCE - 0.01f);
        focus.getTransform().rotateByAxis(-180.0f, 0f, 1f, 0f);
        focus.attachEyePointeeHolder();
        focus.setOnGestureListener(new OnGestureListener() {

            @Override
            public void onSwipeUp() {
            }

            @Override
            public void onSwipeIgnore() {
            }

            @Override
            public void onSwipeForward() {
                AudioClip.getInstance(getGVRContext().getContext()).playSound(AudioClip.getUIRotateSoundID(), 1.0f, 1.0f);
                new GVRRotationByAxisAnimation(dinosaur, 4f, 45, 0, 1, 0).start(gvrContext.getAnimationEngine());
            }

            @Override
            public void onSwipeDown() {
            }

            @Override
            public void onSwipeBack() {
                AudioClip.getInstance(getGVRContext().getContext()).playSound(AudioClip.getUIRotateSoundID(), 1.0f, 1.0f);
                new GVRRotationByAxisAnimation(dinosaur, 4f, -45, 0, 1, 0).start(gvrContext.getAnimationEngine());
            }
        });
        scene.addSceneObject(focus);
    }

    private void createGallery() {
        gallery = new Gallery(getGVRContext(), photos);
        gallery.getTransform().setPosition(Gallery.GALLERY_POSITION_X, Gallery.GALLERY_POSITION_Y, Gallery.GALLERY_POSITION_Z);
        gallery.getTransform().setRotationByAxis(180.0f, 0f, 1f, 0f);
        scene.addSceneObject(gallery);
    }

    private void createGalleryGroup() {
        galleryGroup = new GVRSceneObject(getGVRContext());
        galleryGroup.getTransform().setPosition(0f, 0f, -8.0f);
        galleryGroup.getTransform().rotateByAxis(180.0f, 0f, 1f, 0f);
        galleryGroup.getTransform().rotateByAxisWithPivot(
                DinosaurFactory.APATOSAURUS_ANGLE_AROUND_CAMERA - 35.0f, 0f, 1f, 0f, 0f, 0f, 0f);
    }

    private void createDinosaur() throws IOException {
        dinosaur = DinosaurFactory.getInstance(getGVRContext()).getApatosaurus();
        dinosaur.getTransform().rotateByAxisWithPivot(-90.0f, 1f, 0f, 0f, 0f, 0f, 0f);
        dinosaur.getTransform().rotateByAxisWithPivot(180.0f, 0f, 1f, 0f, 0f, 0f, 0f);
        dinosaur.getTransform().setPosition(0f, 0f, DinosaurFactory.APATOSAURUS_DISTANCE);
        dinosaur.getTransform().rotateByAxis(-70.0f, 0f, 1f, 0f);
        scene.addSceneObject(dinosaur);
    }

    private void createTotem() {

        Totem totem = new Totem(getGVRContext(),
                getGVRContext().loadTexture(new GVRAndroidResource(getGVRContext(),
                        R.drawable.totem_tex_diffuse)));

        totem.getTransform().setPosition(-1f, 0f, -3f);
        totem.setTotemEventListener(gallery);
        totem.setText(gvrContext.getActivity().getResources().getString(R.string.gallery_totem));
        galleryGroup.addChildObject(totem);
        scene.addSceneObject(galleryGroup);
    }

    public boolean isOpen() {
        return gallery.isOpen();
    }

    public void closeThis() {
        gallery.closeThis();
    }

}
