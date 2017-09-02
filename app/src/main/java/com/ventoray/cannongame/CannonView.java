package com.ventoray.cannongame;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by nicks on 8/31/2017.
 */

public class CannonView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String LOG_TAG = "CannonView";

    // constants for game play
    public static final int MISS_PENALTY = 2;
    public static final int HIT_REWARD = 3;

    // constants for the Cannon
    public static final double CANNON_BASE_RADIUS_PERCENT = 3.0 / 40;
    public static final double CANNON_BARREL_WIDTH_PERCENT = 3.0 / 40;
    public static final double CANNON_BARREL_LENGTH_PERCENT = 1.0 / 10;

    // constants for the CannonBall
    public static final double CANNONBALL_RADIUS_PERCENT = 3.0 / 80;
    public static final double CANNONBALL_SPEED_PERCENT = 3.0 / 2;

    // constants for the Targets
    public static final double TARGET_WIDTH_PERCENT = 1.0 / 40;
    public static final double TARGET_LENGTH_PERCENT = 3.0 / 20;
    public static final double TARGET_FIRST_X_PERCENT = 3.0 / 5;
    public static final double TARGET_SPACING_PERCENT = 1.0 / 60;
    public static final double TARGET_PIECES = 9;
    public static final double TARGET_MIN_SPEED_PERCENT = 3.0 / 4;
    public static final double TARGET_MAX_SPEED_PERCENT = 6.0 / 4;

    // constants for the Blocker
    public static final double BLOCKER_WIDTH_PERCENT = 1.0 / 40;
    public static final double BLOCKER_LENGTH_PERCENT = 1.0 / 4;
    public static final double BLOCKER_X_PERCENT = 1.0 / 2;
    public static final double BLOCKER_SPEED_PERCENT = 1.0;

    // text size 1/18 of screen width
    public static final double TEXT_SIZE_PERCENT = 1.0 / 10;

    private CannonBallThread cannonThread; // controls the game loop

    private Activity activity; //to display the Game Over dialog in GUI thread
     private boolean dialogIsDisplayed = false;

    // game objects
    private Cannon cannon;
    private Blocker blocker;
    private ArrayList<Target> targets;

    // dimension variables
    private int screenWidth;
    private int screenHeight;

    // variables for the game loop and tracking statistics
    private boolean gameOver;
    private double timeLeft;
    private int shotsFired;
    private double totalElapsedtime;

    // constants and variables for managing sounds
    public static final int TARGET_SOUND_ID = 0;
    public static final int CANNON_SOUND_ID = 1;
    public static final int BLOCKER_SOUND_ID = 2;
    private SoundPool soundPool;
    private SparseIntArray soundMap;


    // Paint variables used when drawing each item on the screen
    private Paint textPaint;
    private Paint backgroundPaint;


    public CannonView(Context context, AttributeSet attrs) {
        super(context, attrs);

        activity = (Activity) context;

        getHolder().addCallback(this);

        initializeSoundPool();
        // TODO: 9/1/2017 add raw files
//        soundMap = new SparseIntArray(3);
//        soundMap.put(TARGET_SOUND_ID,
//                soundPool.load(context, R.raw.target_hit, 1));
//
//        soundMap.put(CANNON_SOUND_ID,
//                soundPool.load(context, R.raw.cannon_fire, 1));
//
//        soundMap.put(BLOCKER_SOUND_ID,
//                soundPool.load(context, R.raw.blocker_hit, 1));
//

        textPaint = new Paint();
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.WHITE);

    }


    /**
     *  Initializes soundPool
     */
    private void initializeSoundPool() {

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
            attrBuilder.setUsage(AudioAttributes.USAGE_GAME);

            SoundPool.Builder soundPoolBuilder = new SoundPool.Builder();
            soundPoolBuilder.setMaxStreams(1);
            soundPoolBuilder.setAudioAttributes(attrBuilder.build());
            soundPool = soundPoolBuilder.build();
        } else {
            // initializes SoundPool to play the app's three sound effects
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        }
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public void playSound(int soundId) {
        soundPool.play(soundMap.get(soundId), 1, 1, 1, 0, 1f);
    }


    /**
     *  called when the size of the SurfaceView changes,
     *  such as when it's first added to the View hierarchy
     * @param w - assigned to screenWidth
     * @param h - assigned to screenHeight
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        screenWidth = w;
        screenHeight = h;

        //configure text properties
        textPaint.setTextSize((int) (TEXT_SIZE_PERCENT * screenHeight));
        textPaint.setAntiAlias(true);
    }





    /**************************************************************
     *  newGame()
     **************************************************************/
    public void newGame() {
        cannon = new Cannon(this,
                (int) (CANNON_BASE_RADIUS_PERCENT * screenHeight),
                (int) (CANNON_BARREL_LENGTH_PERCENT * screenWidth),
                (int) (CANNON_BARREL_WIDTH_PERCENT * screenHeight));


        Random random = new Random(); // for determining random velocities

        targets = new ArrayList<>();

        // initialize targetX for the first Target from the left
        int targetX = (int) (TARGET_FIRST_X_PERCENT * screenWidth);

        // calculate Y coordinate of Targets
        int targetY = (int) ((0.5 - TARGET_LENGTH_PERCENT / 2) *
                screenHeight);

        // add TARGET_PIECES Targets to the Target list
        for (int n = 0; n < TARGET_PIECES; n++) {

            //determine a random velocity between min and max values
            // for Target n
            double velocity = screenHeight * (random.nextDouble() *
                    (TARGET_MAX_SPEED_PERCENT - TARGET_MIN_SPEED_PERCENT) +
                    TARGET_MIN_SPEED_PERCENT);

            int color = (n % 2 == 0) ?
                    getResources().getColor(R.color.dark,
                            getContext().getTheme()) :
                    getResources().getColor(R.color.light,
                            getContext().getTheme());

            velocity *= -1; // reverse the initial velocity for next Target

            //create and add new Target to the Target list
            targets.add(new Target(this, color, HIT_REWARD, targetX,
                    targetY,
                    (int) (TARGET_WIDTH_PERCENT * screenWidth),
                    (int) (TARGET_LENGTH_PERCENT * screenHeight),
                    (int) velocity));


            // increase the x coordinate to position the next Target
            // more to the right
            targetX += (TARGET_WIDTH_PERCENT + TARGET_SPACING_PERCENT) *
                    screenWidth;
        }

        // create a new Blocker
        blocker = new Blocker(this, Color.BLACK, MISS_PENALTY,
                (int) (BLOCKER_X_PERCENT * screenWidth),
                (int) ((0.5 - BLOCKER_LENGTH_PERCENT / 2) * screenHeight),
                (int) (BLOCKER_WIDTH_PERCENT * screenWidth),
                (int) (BLOCKER_LENGTH_PERCENT * screenHeight),
                (float) (BLOCKER_SPEED_PERCENT * screenHeight));

        timeLeft = 10;

        shotsFired = 0;
        totalElapsedtime = 0.0;

        if (gameOver) {
            gameOver = false;
            cannonThread = new CannonThread(getHolder());
            cannonThread.start();
        }

        hideSystemBars();

    }


    private void updatePositions(double elapsedTimeMS) {
        double interval  = elapsedTimeMS / 1000.0; //converted to seconds

        //update cannonball's position if it is on the screen
        if (cannon.getCannonBall() != null) {
            cannon.getCannonBall().update(interval);
        }

        blocker.update(interval); //update the blocker's position

        for (GameElement target : targets) {
            target.update(interval); // update the target's position
        }

        timeLeft -= interval; //subtract from time left

        // if the timer reached zero
        if (timeLeft <= 0) {
            timeLeft = 0.0;
            gameOver = true;
            cannonThread.setRunning(false); // terminate thread
            showGameOverDialog(R.string.lose);
        }

        // if all pieces have been hit
        if (targets.isEmpty()) {
            cannonThread.setRunning(false); //terminate thread
            showGameOverDialog(R.string.win);
            gameOver = true;
        }


    }


    public void alignAndFireCannonball(MotionEvent event) {
        Point touchPoint = new Point((int) event.getX(), (int) event.getY());

        // compute the touch's distance from the center of the screen
        //on the y-axis
        double centerMinusY = (screenHeight / 2 - touchPoint.y);
        double angle = 0; //initialize angle to 0

        // calculate the angle the barrel makes with the horizontal
        angle = Math.atan2(touchPoint.x, centerMinusY);

        // point the barrel at the point where the screen was touched
        cannon.align(angle);

        // fire Cannonball if there is not already a Cannonball on screen
        if (cannon.getCannonBall() == null ||
                !cannon.getCannonBall().isOnScreen()) {
            cannon.fireCannonball();;
            ++shotsFired;
        }
    }



    private void showGameOverDialog(final int messageId) {
        final DialogFragment gameResult = new DialogFragment() {
            // create an AlertDialog and return it

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(getActivity());
                builder.setTitle(getResources().getString(messageId));
                builder.setMessage(getResources().getString(R.string.results_format,
                        shotsFired, totalElapsedtime));
                builder.setPositiveButton(R.string.reset_game, new DialogInterface.OnClickListener() {
                    // called when "Reset Game" Button is pressed
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialogIsDisplayed = false;
                        newGame();

                    }
                });
                return builder.create();
            }
        };

        //I think we have to do it this way because it is a SurfaceView, not activity
        // in GUI thread, use FragmentManager to display the DialogFragment
        activity.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        showSystemBars(); //exit immersive mode
                        dialogIsDisplayed = true;
                        gameResult.setCancelable(false); // modal dialog
                        gameResult.show(activity.getFragmentManager(), "results");
                    }
                }
        );
    }

    /**
     * draws the game to the given Canvas
     * @param canvas
     */
    public void drawGameElements(Canvas canvas) {
        // clear the background
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(),
                backgroundPaint);

        // display time remaining
        canvas.drawText(getResources().getString(
                R.string.time_remaining_format, timeLeft), 50, 100,
                textPaint);


        // draw the GameElements
        if (cannon.getCannonBall() != null &&
                cannon.getCannonBall().isOnScreen()) {
            cannon.getCannonBall().draw(canvas);
        }

        blocker.draw(canvas);

        for (GameElement target : targets) {
            target.draw(canvas);
        }
    }

    /**
     *  checks if the ball collides with the Blocker or any of the Targets
     *  and handles the collisions
     */
    public void testForCollisions() {
        //remove any of the targets that the Cannonball
        // collides with
        if (cannon.getCannonBall() !=  null &&
                cannon.getCannonBall().isOnScreen()) {
            for (int n = 0; n < targets.size(); n++) {
                if (cannon.getCannonBall().collidesWith(targets.get(n))) {
                    targets.get(n).playSound();
                    // add hit rewards time to remaining time
                    timeLeft += targets.get(n).getHitReward();
                    cannon.removeCannonball(); // remove Cannonball from game
                    targets.remove(n); // remove the Target that was hit
                    --n; // ensures that we don't skip testing new target n
                    break;
                }
            }


        } else { // remove the Cannonball if it should not be on the screen
            cannon.removeCannonBall();
        }

        // check if ball collides with blocker
        if (cannon.getCannonBall() != null &&
                cannon.getCannonBall().collidesWith(blocker)) {
            blocker.playSound();
            cannon.getCannonBall().reverseVelocityX();
            timeLeft -= blocker.getMissPenalty();
        }
    }

    /**
     * stops the game: called by CannonGameFragment's onPause method
     */
    public void stopGame() {
        if (cannonThread != null)
            cannonThread.setRunning(false);
    }

    /**
     * release resources: called by CannonGame's onDestroy method
     */
    public void releaseResources() {
        soundPool.release();
        soundPool = null;
    }


    /**
     * called when surface is first created
     * @param holder
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!dialogIsDisplayed) {
            newGame(); // set up and start a new game ... also called in dialog button
            cannonThread = new CannonThread(holder);
            cannonThread.setRunning(true); // start game running
            cannonthread.start(); // start the game loop thread
        }
    }

    /**
     * called when surface changes size
     * @param holder
     * @param format
     * @param width
     * @param height
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    /**
     * called when the surface is destroyed
     * @param holder
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // ensure that the thread terminates properly
        boolean retry = true;
        cannonThread.setRunning(false);

        while (retry) {
            try {
                cannonThread.join(); // wait for cannonThread to finish
                retry = false;
            } catch (InterruptedException e) {
                Log.e(LOG_TAG, "Thread interrupted", e);
            }
        }
    }


    /**
     * called when the user touches the screen in this activity
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // get int representing the type of action which caused this event
        int action = event.getAction();

        //the user touched the screen or dragged along the screen
        if (action == MotionEvent.ACTION_DOWN ||
                action == MotionEvent.ACTION_MOVE) {
            // fire the cannonball toward the touch point
            alignAndFireCannonball(event);
        }
        return true;
    }
}
