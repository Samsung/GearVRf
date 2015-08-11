
package org.gearvrf.keyboard.spinner;

import org.gearvrf.GVRContext;
import org.gearvrf.keyboard.model.CharItem;
import org.gearvrf.keyboard.util.CircularList;

import android.util.Log;

public class SpinnerAdapter {

    private CircularList<CharItem> characterList;
    private CircularList<SpinnerItem> spinnerItems;
    float oldRotation = 0;
    private int centralPosition = 0;
    private int centralPositionCharacter = 0;

    public SpinnerAdapter(CircularList<SpinnerItem> spinnerItems, CircularList<CharItem> characterList) {
        this.characterList = characterList;
        this.spinnerItems = spinnerItems;
    }

    public void updateSpinnerCentral(float actualRotation, GVRContext context) {
        updateCentralValues(actualRotation, context);
        updateTextOnRig(actualRotation, context);
    }

    private void updateTextOnRig(float degree, GVRContext gvrContext) {

        if (degree < 0.0f) {
            int howFar = 1;
            int upSpinnerPosition = spinnerItems.getNextPosition(centralPosition + howFar + 1);
            int upCharacterPosition = characterList.getNextPosition(centralPositionCharacter + howFar + 1);
            spinnerItems.get(upSpinnerPosition).setText(gvrContext, characterList.get(upCharacterPosition));
            Log.d("SpinnerTest", "upSpinnerPosition" + upSpinnerPosition);
        } else {
            int howFar = 1;
            int downSpinnerPosition = spinnerItems.getPreviousPosition(centralPosition - howFar - 1);
            int downCharacterPosition = characterList.getPreviousPosition(centralPositionCharacter - howFar - 1);
            spinnerItems.get(downSpinnerPosition).setText(gvrContext, characterList.get(downCharacterPosition));
            Log.d("SpinnerTest", "downSpinnerPosition" + downSpinnerPosition);
        }
    }

    protected void updateCentralValues(float degree, GVRContext context) {

        if (degree < 0.0f) {

            centralPositionCharacter = characterList.getNextPosition(centralPositionCharacter);
            centralPosition = spinnerItems.getNextPosition(centralPosition);

        } else {

            centralPositionCharacter = characterList.getPreviousPosition(centralPositionCharacter);
            centralPosition =
                    spinnerItems.getPreviousPosition(centralPosition);

        }

    }

    public CharItem getCurrentCharItem() {
        return characterList.get(centralPositionCharacter);

    }

    public int getCentralPosition() {
        return centralPosition;
    }

    public CircularList<CharItem> getCharacterList() {
        return characterList;
    }

    public void setCharacterList(CircularList<CharItem> characterList) {
        this.characterList = characterList;
    }

    public CircularList<SpinnerItem> getSpinnerItems() {
        return spinnerItems;
    }

    public void setSpinnerItems(CircularList<SpinnerItem> spinnerItems) {
        this.spinnerItems = spinnerItems;
    }

    public void clean() {
        centralPosition = 0;
        centralPositionCharacter = 0;
        oldRotation = 0;

    }

    public void setInitialPosition(int initialPosition) {
        centralPositionCharacter = initialPosition;

    }
}
