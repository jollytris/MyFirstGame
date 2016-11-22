package com.jollytris.myfirstgame.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.view.SurfaceHolder;

import com.jollytris.myfirstgame.common.Colors;
import com.jollytris.myfirstgame.model.RacingCar;

import java.util.ArrayList;
import java.util.Random;

public class DrawThread extends Thread {

    //---------------------------------------------------------------------------------------------
    // fields
    //---------------------------------------------------------------------------------------------
    public static final int MSG_SCORE = 1000;
    public static final int MSG_COLLISION = 2000;
    public enum PlayState {
        Playing, Pause, Stop
    }

    private SurfaceHolder holder = null;
    private Handler handler = null;
    private boolean isRunning;
    private boolean isCollision;
    private PlayState state;
    private int viewWidth, viewHeight, blockSize;
    private int speed;

    private Paint paint;
    private Random random;

    private ArrayList<RectF> walls;
    private ArrayList<RacingCar> obstacles = null;
    private RacingCar myself = null;

    //---------------------------------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------------------------------
    public DrawThread(Context context, SurfaceHolder holder) {
        this.holder = holder;
        isRunning = true;
        state = PlayState.Stop;
        isCollision = false;
        speed = 0;
    }

    //---------------------------------------------------------------------------------------------
    // override
    //---------------------------------------------------------------------------------------------
    @Override
    public void run() {
        super.run();

        while (isRunning) {
            Canvas canvas = null;

            try {
                canvas = holder.lockCanvas(null);

                synchronized (holder) {
                    paint.setColor(Colors.BROWN50);
                    canvas.drawRect(0, 0, viewWidth, viewHeight, paint);

                    drawWall(canvas);
                    drawObastacles(canvas);

                    if (myself != null) {
                        myself.draw(canvas);
                    }

                    if (isCollision) {
                        myself.draw(canvas, true);
                    } else {
                        if (handler != null && state == PlayState.Playing) {
                            handler.sendEmptyMessage(MSG_SCORE);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    //---------------------------------------------------------------------------------------------
    // public method
    //---------------------------------------------------------------------------------------------
    public void initialize(int width, int height, int blockSize) {
        this.viewWidth = width;
        this.viewHeight = height;
        this.blockSize = blockSize;

        setProperties();

        createWall();
        createObstacles();

        myself = new RacingCar(blockSize);
        myself.x = getLeftPositionX(1);
        myself.y = viewHeight - myself.height - RacingView.SPACING;
    }

    public void play(Handler handler, int speed) {
        this.handler = handler;
        this.speed = speed;
    }

    public void moveLeft() {
        if (state != PlayState.Playing) {
            return ;
        }

        if (myself != null && !isCollision) {
            if (myself.x > blockSize + RacingView.SPACING * 2) {
                myself.moveLeft();
            }
            if (myself.x <= blockSize + RacingView.SPACING * 2) {
                myself.setPosition(blockSize + RacingView.SPACING * 2 + (RacingView.SPACING + blockSize));
            }
        }
    }

    public void moveRight() {
        if (state != PlayState.Playing) {
            return ;
        }

        if (myself != null && !isCollision) {
            if (myself.x + myself.width < viewWidth - RacingView.SPACING * 2 - blockSize) {
                myself.moveRight();
            }

            if (myself.x + myself.width >= viewWidth - RacingView.SPACING * 2 - blockSize) {
                myself.setPosition(viewWidth - RacingView.SPACING * 2 - blockSize - myself.width
                        - (RacingView.SPACING + blockSize));
            }
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setPlayState(PlayState state) {
        this.state = state;
    }

    public PlayState getPlayState() {
        return state;
    }

    //-------------------------------------------------------------------------------------------
    // private method
    //-------------------------------------------------------------------------------------------
    private void setProperties() {
        paint = new Paint();
        paint.setAntiAlias(true);

        random = new Random();
    }

    private void createWall() {
        walls = new ArrayList<>();
        for (int i = 0; i < RacingView.VERTICAL_COUNT; i++) {
            if (i != 0 && i != RacingView.VERTICAL_COUNT
                    && i % 4 == 0) {
                continue;
            }

            RectF left = new RectF(0, 0, 0, 0);
            left.top = i * blockSize + RacingView.SPACING * (i + 1);
            left.bottom = left.top + blockSize;
            left.left = 0;
            left.right = blockSize;
            walls.add(left);

            RectF right = new RectF(0, 0, 0, 0);
            right.top = i * blockSize + RacingView.SPACING * (i + 1);
            right.bottom = left.top + blockSize;
            right.left = viewWidth - blockSize - RacingView.SPACING;
            right.right = right.left + blockSize;
            walls.add(right);
        }
    }

    private void drawWall(Canvas c) {
        if (walls != null) {
            paint.setColor(Colors.BROWN300);
            for (RectF r : walls) {
                c.drawRoundRect(r, 8f, 8f, paint);
            }
        }
    }

    private void createObstacles() {
        obstacles = new ArrayList<RacingCar>();
        int carHeight = blockSize * 4 + RacingView.SPACING * 3;
        int startOffset = -carHeight;
        for (int i = 0; i < RacingView.MAX_OBSTACLE; i++) {
            RacingCar obstacle = new RacingCar(
                    blockSize,
                    getLeftPositionX(random.nextInt(RacingView.MAX_COL_COUNT)), startOffset,
                    RacingCar.COLOR_OBSTACLE);
            obstacles.add(obstacle);
            startOffset = startOffset - ((carHeight + RacingView.SPACING) * 2);
        }
    }

    private void drawObastacles(Canvas c) {
        if (obstacles != null) {
            int size = obstacles.size();
            for (int i = 0; i < size; i++) {
                RacingCar obstacle = obstacles.get(i);
                if (state == PlayState.Playing) {
                    obstacle.moveDown(speed);
                }
                obstacle.draw(c);

                if (state == PlayState.Playing) {
                    if (isCollision(obstacle)) {
                        isCollision = true;
                        if (handler != null) {
                            handler.sendEmptyMessage(MSG_COLLISION);
                        }
                    }

                    passingCheck(obstacle);
                }
            }
        }
    }

    private void passingCheck(RacingCar obstacle) {
        if (obstacle.y >= viewHeight + (obstacle.height * 2)) {
            obstacle.x = getLeftPositionX(random.nextInt(RacingView.MAX_COL_COUNT));
            obstacle.y = obstacle.y - ((obstacle.height * 2) * RacingView.MAX_OBSTACLE);
        }
    }

    private boolean isCollision(RacingCar obstacle) {
        if (myself == null) {
            return false;
        }
        return myself.getBoundary().intersect(obstacle.getBoundary());
    }

    private int getLeftPositionX(int r) {
        return (RacingView.SPACING + blockSize)
                + (RacingView.SPACING + blockSize)
                + RacingView.SPACING * (r + 1)
                + (blockSize * 3 + RacingView.SPACING * 2) * r;
    }
}
