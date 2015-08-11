
package org.gearvrf.keyboard.keyboard.model;

import org.gearvrf.keyboard.model.KeyboardCharItem;

public interface KeyboardEventListener {

    void onKeyDelete();

    void onKeyConfirm();

    void onKeyPressedWhitItem(KeyboardCharItem charItem);
}
