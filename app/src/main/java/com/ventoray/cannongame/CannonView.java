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

    private SoundPool soundPool;

    public CannonView(Context context, AttributeSet attrs) {
        super(context, attrs);



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
