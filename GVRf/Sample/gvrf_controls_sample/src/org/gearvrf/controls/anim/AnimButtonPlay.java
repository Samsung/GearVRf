/* Copyright 2015 Samsung Electronicimport org.gearvrf.controls.R;
import org.gearvrf.controls.focus.ControlSceneObject;
import org.gearvrf.controls.shaders.ButtonShader;
import org.gearvrf.controls.util.RenderingOrder;
" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.controls.anim;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRTexture;
import org.gearvrf.controls.R;
import org.gearvrf.controls.focus.ControlSceneObject;
import org.gearvrf.controls.shaders.ButtonShader;
import org.gearvrf.controls.util.RenderingOrder;

public class AnimButtonPlay extends ControlSceneObject {

    private final int IDLE_STATE = 0;
    private final int HOVER_STATE = 1;
    private final int SELECTED_STATE = 2;

    public AnimButtonPlay(GVRContext gvrContext) {
        super(gvrContext);

        GVRMesh sMesh = getGVRContext().createQuad(0.3f, 0.3f);

        attachRenderData(new GVRRenderData(gvrContext));
        getRenderData().setMaterial(
                new GVRMaterial(gvrContext, new ButtonShader(gvrContext).getShaderId()));
        getRenderData().setMesh(sMesh);
        createTextures(gvrContext);

        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, IDLE_STATE);
        getRenderData().setRenderingOrder(RenderingOrder.MOVE_BUTON);

        attachEyePointeeHolder();
    }

    private void createTextures(GVRContext gvrContext) {

        GVRTexture empty = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.raw.empty));
        GVRTexture idle = gvrContext.loadTexture(new GVRAndroidResource(gvrContext,
                R.drawable.bt_play_idle));
        GVRTexture hover = gvrContext.loadTexture(new GVRAndroidResource(gvrContext,
                R.drawable.bt_play_hover));
        GVRTexture selected = gvrContext.loadTexture(new GVRAndroidResource(gvrContext,
                R.drawable.bt_play_pressed));

        getRenderData().getMaterial().setTexture(ButtonShader.STATE1_BACKGROUND_TEXTURE, empty);
        getRenderData().getMaterial().setTexture(ButtonShader.STATE1_TEXT_TEXTURE, idle);

        getRenderData().getMaterial().setTexture(ButtonShader.STATE2_BACKGROUND_TEXTURE, empty);
        getRenderData().getMaterial().setTexture(ButtonShader.STATE2_TEXT_TEXTURE, hover);

        getRenderData().getMaterial().setTexture(ButtonShader.STATE3_BACKGROUND_TEXTURE, empty);
        getRenderData().getMaterial().setTexture(ButtonShader.STATE3_TEXT_TEXTURE, selected);
    }

    @Override
    protected void gainedFocus() {
        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, HOVER_STATE);
    }

    @Override
    protected void lostFocus() {
        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, IDLE_STATE);
    }

    @Override
    protected void singleTap() {
        super.singleTap();
        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, SELECTED_STATE);
    }
}
