package com.ventoray.cannongame;

import android.app.Activity;
import android.content.Context;
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

/**
 * Created by nicks on 8/31/2017.
 */

public class CannonView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String LOG_TAG = "CannonView";

    private CannonThread cannonThread; //controls the game loop
    private Activity activity;
    private boolean dialogIsDisplayed = false;

    // constants for gameplay
    public static final int TARGET_PIECES = 7;
    public static final int MISS_PENALTY = 2;
    public static final int HIT_REWARD = 3;

    // variables for the game loop and tracking statistics
    private boolean gameOver;
    private double timeLeft;
    private int shotsFired;
    private double totalElapsedTime;

    // variables for the blocker and target
    private Line blocker;
    private int blockerDistance;
    private int blockerBeginning;
    private int blockerEnd;
    private int intialBlockerVelocity;
    private float blockerVelocity;

    private Line target;
    private int targetDistance;
    private int targetBeginning;
    private double pieceLength;
    private int targetEnd;
    private int initialTargetVelocity;
    private float targetVelocity;

    private int lindWidth;
    private boolean[] hitStates;
    private int targetPiecesHit;

    //variables for the cannon and cannonball
    private Point cannonBall;
    private int cannonballVelocityX;
    private int cannonballVelocityY;
    private boolean cannonballOnscreen;
    private int cannonballRadius;
    private int cannonballSpeed;
    private int cannonBaseRadius;
    private int cannonLength;
    private Point barrelEnd;
    private int screenWidth;
    private int screenHeight;

    //constants and variables for managing sounds
    private static final int TARGET_SOUND_ID = 0;
    private static final int CANNON_SOUND_ID = 1;
    private static final int BLOCKER_SOUND_ID = 2;
    private SoundPool soundPool; //plays sound effects
    private SparseIntArray soundMap; // maps Ids to SoundPool

    // Paint variables used when drawing each item on the screen
    private Paint textPaint;
    private Paint cannonballPaint;
    private Paint cannonPaint;
    private Paint blockerPaint;
    private Paint targetPaint;
    private Paint backgroundPaint;

    public CannonView(Context context, AttributeSet attrs) {
        super(context, attrs);

        activity = (Activity) context;

        //register SurfaceHolder.Callback listener
        getHolder().addCallback(this);

        // initialize Lines and Point representing game items;
        blocker = new Line();
        target = new Line();
        cannonBall = new Point();

        // initialize hitStates as a boolean array
        hitStates = new boolean[TARGET_PIECES];

        initializeSoundPool();


    }


    /**
     *  Initializes soundPool
     */
    private void initializeSoundPool() {

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setFlags(AudioManager.STREAM_MUSIC).build();

            SoundPool.Builder soundPoolBuilder = new SoundPool.Builder();
            soundPoolBuilder.setMaxStreams(1);
            soundPoolBuilder.setAudioAttributes(audioAttributes);
            soundPool = soundPoolBuilder.build();
        } else {
            // initializes SoundPool to play the app's three sound effects
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        }


        // TODO: 8/31/2017 load sounds 

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
}
