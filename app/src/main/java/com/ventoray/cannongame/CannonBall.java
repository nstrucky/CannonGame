package com.ventoray.cannongame;

import android.graphics.Canvas;

/**
 * Created by nicks on 9/1/2017.
 */

public class CannonBall extends GameElement {

    private float velocityX;
    private boolean onScreen;


    public CannonBall(CannonView view, int color, int soundId, int x,
                      int y, int radius, float velocityX, float velocityY) {
        super(view, color, soundId, x, y,
                2 * radius, 2 * radius, velocityY);

        this.velocityX = velocityX;
        onScreen = true;
    }

    public int getRadius() {
        return (shape.right - shape.left) / 2;
    }

    public boolean isOnScreen() {
        return onScreen;
    }

    public void reverseVelocityX() {
        velocityX *= -1;
    }

    @Override
    public void update(double interval) {
        super.update(interval);

        // update horizontal position
        shape.offset((int) (velocityX * interval), 0);

        // if Cannonball goes off the screen
        if (shape.top < 0 || shape.left < 0 ||
                shape.bottom > view.getScreenHeight() ||
                shape.right > view.getScreenWidth()) {
            onScreen = false;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        canvas.drawCircle(shape.left + getRadius(),
                shape.top + getRadius(), getRadius(), paint);
    }
}
