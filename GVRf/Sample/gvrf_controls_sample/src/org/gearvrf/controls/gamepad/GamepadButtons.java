package org.gearvrf.controls.gamepad;

import android.util.SparseArray;

import java.util.EnumSet;

public enum GamepadButtons {
    
    dpad(0), 
    play(1), 
    button1(2), 
    button2(3),
    button3(4), 
    button4(5), 
    analog_stick_l(6),
    analog_stick_r(7), 
    lt(8), 
    rt(9);
    
    private int index;
    private static SparseArray<GamepadButtons> lookup = new SparseArray<GamepadButtons>();
    
    static {

        for (GamepadButtons s : EnumSet.allOf(GamepadButtons.class)){
            lookup.put(s.getIndex(), s);
        }
    }
    
    GamepadButtons (int index){
        this.index = index;
    }

    public static GamepadButtons get(int index) {
        return lookup.get(index);
    }
    
    public int getIndex() {
        return index;
    }
}