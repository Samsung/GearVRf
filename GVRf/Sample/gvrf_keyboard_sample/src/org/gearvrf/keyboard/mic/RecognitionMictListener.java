
package org.gearvrf.keyboard.mic;

import java.util.ArrayList;

public interface RecognitionMictListener {

    void onResults(ArrayList<String> resultList);

    void onError(String text, int error);

    void onReadyForSpeech();

}
