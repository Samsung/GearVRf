
package org.gearvrf.keyboard.model;

import org.gearvrf.keyboard.keyboard.numeric.Keyboard;

public class KeyboardCharItem {

    private String character;
    private String specialCharacter;
    
    public KeyboardCharItem(String character, String specialCharacter) {
        super();
        this.character = character;
        this.specialCharacter = specialCharacter;
    }

    public String getCharacter() {

        switch (Keyboard.mode) {

            case Keyboard.SOFT_KEYBOARD_UPPERCASE:

                return character.toUpperCase();
                
            case Keyboard.SOFT_KEYBOARD_SPECIAL:

                return specialCharacter;

            default:
                return character;
        }
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    public String getSpecialCharacter() {
        return specialCharacter;
    }

    public void setSpecialCharacter(String specialCharacter) {
        this.specialCharacter = specialCharacter;
    }
}