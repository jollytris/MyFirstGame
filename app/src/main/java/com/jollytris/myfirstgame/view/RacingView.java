package com.jollytris.myfirstgame.view;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

/**
 * Created by zic325 on 2016. 11. 16..
 */

public class RacingView extends SurfaceView implements SurfaceHolder.Callback {

    //---------------------------------------------------------------------------------------------
    // fields
    //---------------------------------------------------------------------------------------------
    public static final int SPACING = 2;
    public static final int VERTICAL_COUNT = 20;
    public static final int HORIZONTAL_COUNT = 10;
    public static final int MAX_COL_COUNT = 2;
    public static final int MAX_OBSTACLE = 10;

    private DrawThread drawThread = null;

    //---------------------------------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------------------------------
    public RacingView(Context context) {
        this(context, null);
    }

    public RacingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RacingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
    }

    //---------------------------------------------------------------------------------------------
    // implements SurfaceHolder.Callback
    //---------------------------------------------------------------------------------------------
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        createThread();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        destroyThread();
    }


    //---------------------------------------------------------------------------------------------
    // public
    //---------------------------------------------------------------------------------------------
    public void setSpeed(int speed) {
        drawThread.setSpeed(speed);
    }

    public DrawThread.PlayState getPlayState() {
        return drawThread.getPlayState();
    }

    public void play(Handler handler, int speed) {
        if (!drawThread.isRunning()) {
            createThread();
        }
        drawThread.play(handler, speed);
        drawThread.setPlayState(DrawThread.PlayState.Playing);
    }

    public void stop() {
        drawThread.setRunning(false);
    }

    public void resume() {
        drawThread.setPlayState(DrawThread.PlayState.Playing);
    }

    public void pause() {
        drawThread.setPlayState(DrawThread.PlayState.Pause);
    }

    public void reset() {
        drawThread.setPlayState(DrawThread.PlayState.Stop);
        destroyThread();
    }

    public void clear() {
        if (!drawThread.isRunning()) {
            createThread();
        }
    }

    public void destroyThread() {
        if (drawThread == null) {
            return;
        }
        drawThread.setRunning(false);

        boolean retry = true;

        while (retry) {
            try {
                drawThread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //---------------------------------------------------------------------------------------------
    // private
    //---------------------------------------------------------------------------------------------
    private void createThread() {
        int blockSize = (getHeight() - (SPACING * (VERTICAL_COUNT + 1))) / VERTICAL_COUNT;
        int width = blockSize * HORIZONTAL_COUNT + SPACING * (HORIZONTAL_COUNT + 1);
        int height = blockSize * VERTICAL_COUNT + SPACING * (VERTICAL_COUNT + 1);

        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = width;
        params.height = height;
        setLayoutParams(params);

        drawThread = new DrawThread(getContext(), getHolder());
        drawThread.initialize(width, height, blockSize);
        drawThread.start();
    }

    public void moveLeft() {
        drawThread.moveLeft();
    }

    public void moveRight() {
        drawThread.moveRight();
    }
}

