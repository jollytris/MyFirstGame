package com.jollytris.myfirstgame;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by zic325 on 2016. 11. 16..
 */

public class LifeContainer extends LinearLayout {

    private int count;
    private boolean isCreated;

    public LifeContainer(Context context) {
        super(context);
        setGravity(Gravity.CENTER);
        setOrientation(HORIZONTAL);
    }

    public LifeContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        setGravity(Gravity.CENTER);
        setOrientation(HORIZONTAL);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (b - t > 0 && !isCreated) {
            create();
        }
    }

    public void create(int count) {
        this.count = count;
        create();
    }

    public void reset() {

    }

    public void increase() {

    }

    public void decrease() {

    }

    private void create() {
        if (getHeight() <= 0) {
            return;
        }
        isCreated = true;

        int size = getHeight() * 2 / 3;
        for (int i = 0; i < count; i++) {
            addView(getLifeView(size));
        }
    }

    private ImageView getLifeView(int size) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(size, size);
        p.leftMargin = 10;
        p.rightMargin = 10;

        ImageView iv = new ImageView(getContext());
        iv.setBackgroundResource(R.drawable.ic_heart);
        iv.setLayoutParams(p);
        return iv;
    }
}
