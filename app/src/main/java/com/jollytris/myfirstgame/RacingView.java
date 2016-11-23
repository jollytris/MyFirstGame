package com.jollytris.myfirstgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by zic325 on 2016. 11. 16..
 */

public class RacingView extends View {

    //---------------------------------------------------------------------------------------------
    // fields
    //---------------------------------------------------------------------------------------------
    public static final int SPACING = 2;
    public static final int MAX_COL_COUNT = 2;
    public static final int VERTICAL_COUNT = 20;
    public static final int HORIZONTAL_COUNT = MAX_COL_COUNT * 3 + 2 + 2;

    public static final int MSG_SCORE = 1000;
    public static final int MSG_COLLISION = 2000;
    public static final int MSG_COMPLETE = 3000;

    public enum PlayState {
        Ready, Playing, Pause, LevelUp, Collision
    }

    private Handler handler = null;
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
    public RacingView(Context context) {
        super(context);
    }

    public RacingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RacingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    //---------------------------------------------------------------------------------------------
    // override
    //---------------------------------------------------------------------------------------------
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int height = bottom - top;
        if (bottom - top > 0) {
            int blockSize = (height - (SPACING * (VERTICAL_COUNT + 1))) / VERTICAL_COUNT;
            int w = blockSize * HORIZONTAL_COUNT + SPACING * (HORIZONTAL_COUNT + 1);
            int h = blockSize * VERTICAL_COUNT + SPACING * (VERTICAL_COUNT + 1);
            initialize(w, h, blockSize);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Colors.BROWN50);

        drawWall(canvas);

        if (state != PlayState.Ready) {
            drawObastacles(canvas);
        }

        if (myself != null) {
            myself.draw(canvas, state == PlayState.Collision);
        }

        if (handler != null && state == PlayState.Playing) {
            handler.sendEmptyMessage(MSG_SCORE);
        }

        invalidate();
    }

    //---------------------------------------------------------------------------------------------
    // public
    //---------------------------------------------------------------------------------------------
    public void setPlayState(PlayState state) {
        this.state = state;
    }

    public PlayState getPlayState() {
        return state;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getSpeed() {
        return speed;
    }

    public void play(Handler handler) {
        this.handler = handler;
        state = PlayState.Playing;
    }

    public void resume() {
        state = PlayState.Playing;
    }

    public void pause() {
        state = PlayState.Pause;
    }

    public void reset() {
        state = PlayState.Ready;

        createObstacles();
    }

    public void moveLeft() {
        if (state != PlayState.Playing) {
            return;
        }

        if (myself != null) {
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
            return;
        }

        if (myself != null) {
            if (myself.x + myself.width < viewWidth - RacingView.SPACING * 2 - blockSize) {
                myself.moveRight();
            }

            if (myself.x + myself.width >= viewWidth - RacingView.SPACING * 2 - blockSize) {
                myself.setPosition(viewWidth - RacingView.SPACING * 2 - blockSize - myself.width
                        - (RacingView.SPACING + blockSize));
            }
        }
    }

    //---------------------------------------------------------------------------------------------
    // private
    //---------------------------------------------------------------------------------------------
    private void initialize(int width, int height, int blockSize) {
        if (myself != null) {
            return;
        }
        state = PlayState.Ready;

        this.viewWidth = width;
        this.viewHeight = height;
        this.blockSize = blockSize;

        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = width;
        params.height = height;
        setLayoutParams(params);

        setProperties();

        createWall();
        createObstacles();

        myself = new RacingCar(blockSize);
        myself.x = getLeftPositionX(random.nextInt(2));
        myself.y = viewHeight - myself.height - RacingView.SPACING;
    }

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
        if (obstacles == null) {
            obstacles = new ArrayList<RacingCar>();
        } else {
            obstacles.clear();
        }
        int carHeight = blockSize * 4 + RacingView.SPACING * 3;
        int startOffset = -carHeight;
        for (int i = 0; i < speed * 3; i++) {
            RacingCar obstacle = new RacingCar(
                    blockSize,
                    getLeftPositionX(random.nextInt(RacingView.MAX_COL_COUNT)), startOffset,
                    RacingCar.COLOR_OBSTACLE);
            obstacles.add(obstacle);
            startOffset = startOffset - ((carHeight + RacingView.SPACING) * 2);
        }
        obstacles.get(obstacles.size()-1).setLast(true);
    }

    private void drawObastacles(Canvas c) {
        if (obstacles != null) {
            boolean isComplete = false;
            int size = obstacles.size();
            for (int i = 0; i < size; i++) {
                RacingCar obstacle = obstacles.get(i);
                if (state == PlayState.Playing) {
                    obstacle.moveDown(speed);
                }
                obstacle.draw(c);


                if (state == PlayState.Playing) {
                    if (isCollision(obstacle)) {
                        state = PlayState.Collision;
                        if (handler != null) {
                            handler.sendEmptyMessage(MSG_COLLISION);
                        }
                    }

                    if (obstacle.y >= viewHeight + (obstacle.height * 2) && obstacle.isLast()) {
                        isComplete = true;
                    }
                }
            }

            if (isComplete) {
                state = PlayState.LevelUp;
                createObstacles();

                if (handler != null) {
                    handler.sendEmptyMessage(MSG_COMPLETE);
                }
            }
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

