/* Copyright 2015 Samsung Electronicsimport android.content.res.Resources;

import org.gearvrf.controls.R;
import org.gearvrf.controls.focus.TouchAndGestureImpl;
"AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.controls.anim;

import android.content.res.Resources;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVRRotationByAxisAnimation;
import org.gearvrf.controls.R;
import org.gearvrf.controls.focus.TouchAndGestureImpl;

public class StarBoxSceneObject extends GVRSceneObject {

    private static final float CLEARBUTTON_OFFSET = 0.99f;
    private static final float PLAYBUTTON_OFFSET = 0.65f;
    private GVRSceneObject star;
    private AnimButtonPlay playButton;
    private AnimCleanButton cleanButton;
    private boolean playbuttonbIsHidden = true;
    private boolean cleanbuttonbIsHidden = true;

    private float evPositionX, evPositionY, evPositionZ, evRotationW;
    private GVRRotationByAxisAnimation rotationAnimation;

    public StarBoxSceneObject(GVRContext gvrContext) {
        super(gvrContext);

        Resources res = gvrContext.getContext().getResources();
        String clearButtonText = res.getString(R.string.clear_button);

        createStar();

        playButton = new AnimButtonPlay(gvrContext);
        cleanButton = new AnimCleanButton(gvrContext, clearButtonText);

        float starX = star.getTransform().getPositionX();
        float starY = star.getTransform().getPositionY();

        playButton.getTransform().setPosition(starX, starY - PLAYBUTTON_OFFSET, 0);
        playButton.getTransform().setRotationByAxis(180, 0, 1, 0);

        cleanButton.getTransform().setPosition(starX, starY - CLEARBUTTON_OFFSET, 0);
        cleanButton.getTransform().setRotationByAxis(180, 0, 1, 0);

        attachActionButtons();
    }

    private void attachActionButtons() {

        playButton.setTouchAndGesturelistener(new TouchAndGestureImpl() {

            @Override
            public void singleTap() {
                super.singleTap();

                rotate();
            }
        });

        cleanButton.setTouchAndGesturelistener(new TouchAndGestureImpl() {

            @Override
            public void singleTap() {
                super.singleTap();

                cleanAction();
            }
        });
    }

    protected void cleanAction() {

        removeChildObject(playButton);
        removeChildObject(cleanButton);

        StarPreviewInfo.restartRotation();

        star.getTransform().setRotation(evRotationW, evPositionX, evPositionY, evPositionZ);

        playbuttonbIsHidden = true;
        cleanbuttonbIsHidden = true;
    }

    public void showPlayButton() {

        if (playbuttonbIsHidden) {

            playbuttonbIsHidden = false;

            addChildObject(playButton);
        }
    }

    public void showCleanButton() {

        if (cleanbuttonbIsHidden) {

            cleanbuttonbIsHidden = false;

            addChildObject(cleanButton);
        }
    }

    private void createStar() {

        GVRAndroidResource starMeshRes = new GVRAndroidResource(getGVRContext(), R.raw.star);
        GVRAndroidResource starTextRes = new GVRAndroidResource(getGVRContext(),
                R.drawable.star_diffuse);

        star = new GVRSceneObject(getGVRContext(), starMeshRes, starTextRes);
        star.getTransform().setPositionY(0.2f);

        evPositionX = star.getTransform().getRotationX();
        evPositionY = star.getTransform().getRotationY();
        evPositionZ = star.getTransform().getRotationZ();
        evRotationW = star.getTransform().getRotationW();

        addChildObject(star);
    }

    public void rotate() {

        star.getTransform().setPosition(evPositionX, evPositionY, evPositionZ);
        star.getTransform().setRotation(evRotationW, evPositionX, evPositionY, evPositionZ);

        rotationAnimation = new GVRRotationByAxisAnimation(star, AnimationsTime.getRotationTime(),
                StarPreviewInfo.getRotation(), 0, 1, 0);

        rotationAnimation.setOnFinish(new GVROnFinish() {

            @Override
            public void finished(GVRAnimation arg0) {
                showCleanButton();
            }
        });

        rotationAnimation.start(getGVRContext().getAnimationEngine());
    }
}
