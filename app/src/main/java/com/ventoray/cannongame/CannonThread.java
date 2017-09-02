package com.ventoray.cannongame;

import android.view.SurfaceHolder;

/**
 * Created by Nick on 9/2/2017.
 *
 * Thread class to control the game loop
 */

public class CannonThread extends Thread {

    private SurfaceHolder surfaceHolder; //for manipulating canvas
    private boolean threadIsRunning = true; // running by default

}
