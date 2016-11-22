package com.jollytris.myfirstgame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class RacingCar {

    public static final int COLOR_ME = Colors.BROWN900;
    public static final int COLOR_OBSTACLE = Colors.BROWN500;

    public int blockSize = 0;
    public int x = 0;
    public int y = 0;
    public int width = 0;
    public int height = 0;
    private Paint paint = null;
    private Rect boundary = null;
    private RectF rectF;

    public RacingCar(int blockSize) {
        this(blockSize, 0, 0, COLOR_ME);
    }

    public RacingCar(int blockSize, int x, int y, int color) {
        this.blockSize = blockSize;
        this.x = x;
        this.y = y;
        this.width = this.blockSize * 3 + RacingView.SPACING * 2;
        this.height = this.blockSize * 4 + RacingView.SPACING * 3;

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);

        boundary = new Rect();
        rectF = new RectF();
    }

    public void setPosition(int x) {
        this.x = x;
    }

    public void moveDown(int speed) {
        y += speed;
    }

    public void moveLeft() {
        x = x - width - RacingView.SPACING;
    }

    public void moveRight() {
        x = x + width + RacingView.SPACING;
    }

    public Rect getBoundary() {
        boundary.left = x + blockSize;
        boundary.top = y + blockSize;
        boundary.right = x + width - blockSize;
        boundary.bottom = y + height - blockSize;
        return boundary;
    }

    public void draw(Canvas c) {
        drawBlock(c, 1, 0);

        drawBlock(c, 0, 1);
        drawBlock(c, 1, 1);
        drawBlock(c, 2, 1);

        drawBlock(c, 1, 2);

        drawBlock(c, 0, 3);
        drawBlock(c, 2, 3);
    }

    public void draw(Canvas c, boolean isCollision) {
        paint.setColor(isCollision ? Color.RED : COLOR_ME);

        drawBlock(c, 1, 0);

        drawBlock(c, 0, 1);
        drawBlock(c, 1, 1);
        drawBlock(c, 2, 1);

        drawBlock(c, 1, 2);

        drawBlock(c, 0, 3);
        drawBlock(c, 2, 3);
    }

    private void drawBlock(Canvas c, int px, int py) {
        rectF.left = px * blockSize + px * RacingView.SPACING + x;
        rectF.top = py * blockSize + py * RacingView.SPACING + y;
        rectF.right = rectF.left + blockSize;
        rectF.bottom = rectF.top + blockSize;
        c.drawRoundRect(rectF, 8f, 8f, paint);
    }
}
