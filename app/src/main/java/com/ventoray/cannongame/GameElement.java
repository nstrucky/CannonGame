package com.ventoray.cannongame;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.ventoray.cannongame.CannonView;

/**
 * Created by Nick on 8/31/2017.
 */

public class GameElement {

    protected CannonView view; //the view that contains this element
    protected Paint paint = new Paint(); // Paint to draw this game element
    protected Rect shape; // the GameElement's rectangular bounds
    private float velocityY; // vertical velocity of this game element;
    private int soundId;


    public GameElement(CannonView view, int color, int soundId, int x, int y,
                       int width, int length, float velocityY) {

        this.view = view;
        paint.setColor(color);
        shape = new Rect(x, y, x + width, y + length);
        this.soundId = soundId;
        this.velocityY = velocityY;
    }


    // update GameElement position and check for wall collisions
    public void update(double interval) {
        //update vertical position
        shape.offset(0, (int) (velocityY * interval));

        //if this GameElement collides with the wall, reverse direction
        if (shape.top < 0 && velocityY < 0 || shape.bottom > view.getScreenHeight() &&
                velocityY > 0) {
            velocityY *= -1;
        }
    }

    public void draw(Canvas canvas) {
        canvas.drawRect(shape, paint);
    }

    public void playSound() {
        view.playSoundEffect(soundId);
    }


}
