package com.ventoray.cannongame;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.ventoray.cannongame.elements.Blocker;
import com.ventoray.cannongame.elements.Target;

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

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

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
}
