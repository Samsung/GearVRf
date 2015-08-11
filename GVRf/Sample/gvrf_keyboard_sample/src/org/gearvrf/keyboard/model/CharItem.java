
package org.gearvrf.keyboard.model;

import org.gearvrf.keyboard.keyboard.numeric.Keyboard;

public class CharItem {

    private int mode;
    private int position;
    private String character;

    public CharItem(int mode, int position, String character) {
        super();
        this.mode = mode;
        this.position = position;
        this.character = character;
    }

    public int getMode() {
        return mode;
    }

    public int getPosition() {
        return position;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getCharacter() {

        switch (Keyboard.mode) {

            case Keyboard.SOFT_KEYBOARD_UPPERCASE:

                return character.toUpperCase();

            default:
                return character;
        }
    }

    public void setCharacter(String character) {
        this.character = character;
    }
}