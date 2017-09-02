package com.ventoray.cannongame;

/**
 *
 * Created by nicks on 9/1/2017.
 *  Subclass of GameElement customized for the Blocker
 *
 */

public class Blocker extends GameElement {

    private int missPenalty;

    public Blocker(CannonView view, int color, int missPenalty, int x,
                   int y, int width, int length, float velocityY) {
        super(view, color, CannonView.BLOCKER_SOUND_ID, x, y, width, length,
                velocityY);

        this.missPenalty = missPenalty;

    }


    //returns the miss penalty for this Blocker
    public int getMissPenalty() {
        return missPenalty;
    }





}
