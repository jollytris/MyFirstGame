package com.jollytris.myfirstgame;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class MainActivity extends AppCompatActivity {

    private static final int MSG_FINISH_LEVEL = 10000;
    private static final int DURATION_LEVEL = 15 * 1000;
    private static final int INIT_SPEED = 12;
    private static final int SPEED_INTERVAL = 4;
    private static final int MAX_SPEED = 40;

    private AdView adBanner;
    private View contLabel;
    private TextView tvLabelLevel;
    private TextView tvScore, tvLevel, tvBest;
    private ImageView ivCenter;
    private RacingView racingView;
    private int score, level, bestScore;
    private long startLevelTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adBanner = (AdView) findViewById(R.id.banner);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("E22D097DA71BEAB0F0D3BBD3DD1A6700")
                .addTestDevice("D3BD70725AB672E8AF899E55C5485CEE")
                .build();
        adBanner.loadAd(adRequest);

        contLabel = findViewById(R.id.contLabel);
        tvLabelLevel = (TextView) findViewById(R.id.labelLevel);

        tvScore = (TextView) findViewById(R.id.score);
        tvLevel = (TextView) findViewById(R.id.level);
        tvBest = (TextView) findViewById(R.id.best);

        ivCenter = (ImageView) findViewById(R.id.imgCenter);

        findViewById(R.id.contLeft).setOnTouchListener(directionTouchListener);
        findViewById(R.id.contRight).setOnTouchListener(directionTouchListener);
        findViewById(R.id.contCenter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play();
            }
        });

        racingView = (RacingView) findViewById(R.id.racingView);

        initialize();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adBanner != null) {
            adBanner.resume();
        }
    }

    @Override
    protected void onPause() {
        if (adBanner != null) {
            adBanner.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (adBanner != null) {
            adBanner.destroy();
        }

        racingView.reset();
        racingHandler.removeMessages(MSG_FINISH_LEVEL);
        super.onDestroy();
    }

    private View.OnTouchListener directionTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int action = motionEvent.getAction();
            if (action == MotionEvent.ACTION_DOWN
                    || action == MotionEvent.ACTION_MOVE) {
                view.setBackgroundResource(R.drawable.rect_brown100);
            } else {
                view.setBackgroundResource(R.drawable.rect_brown200);
            }

            int vId = view.getId();
            if (action == MotionEvent.ACTION_DOWN) {
                if (vId == R.id.contLeft) {
                    racingView.moveLeft();
                } else if (vId == R.id.contRight) {
                    racingView.moveRight();
                }
            }
            return true;
        }
    };

    private Handler racingHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RacingView.MSG_SCORE:
                    score = score + (level);
                    tvScore.setText(String.valueOf(score));
                    break;
                case RacingView.MSG_COLLISION:
                    boolean achieveBest = false;
                    if (bestScore < score) {
                        tvBest.setText(String.valueOf(score));
                        bestScore = score;
                        saveBestScore(bestScore);
                        achieveBest = true;
                    }
                    collision(achieveBest);
                    break;
                case MSG_FINISH_LEVEL:
                    level++;
                    racingView.setPlayState(RacingView.PlayState.LevelUp);

                    if (racingView.getSpeed() < MAX_SPEED) {
                        racingView.setSpeed(racingView.getSpeed() + SPEED_INTERVAL);
                    }

                    prepare();
                    break;
                default:
                    break;
            }
        }
    };

    private void initialize() {
        reset();
        prepare();
    }

    private int loadBestScore() {
        SharedPreferences preferences = getSharedPreferences("MyFirstGame", Context.MODE_PRIVATE);
        if (preferences.contains("BestScore")) {
            return preferences.getInt("BestScore", 0);
        } else {
            return 0;
        }
    }

    private void saveBestScore(int bestScore) {
        SharedPreferences preferences = getSharedPreferences("MyFirstGame", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("BestScore", bestScore);
        editor.commit();
    }

    private void reset() {
        score = 0;
        level = 1;
        bestScore = loadBestScore();

        racingView.setSpeed(INIT_SPEED);
        racingView.setPlayState(RacingView.PlayState.Ready);

        tvScore.setText(String.valueOf(score));
        tvLevel.setText(String.valueOf(level));
        tvBest.setText(String.valueOf(bestScore));
    }

    private void prepare() {
        tvLevel.setText(String.valueOf(level));
        tvLabelLevel.setText("LEVEL " + level);
        showLabelContainer();

        ivCenter.setBackgroundResource(R.drawable.ic_play);
    }

    private void play() {
        if (racingView.getPlayState() == RacingView.PlayState.Collision) {
            initialize();

            racingView.reset();
            return;
        }

        // Click on playing
        if (racingView.getPlayState() == RacingView.PlayState.Playing) {
            ivCenter.setBackgroundResource(R.drawable.ic_play);
            racingView.pause();
            startLevelTime = System.currentTimeMillis() - startLevelTime;
            racingHandler.removeMessages(MSG_FINISH_LEVEL);
        } else {
            ivCenter.setBackgroundResource(R.drawable.ic_pause);

            // Click on pause
            if (racingView.getPlayState() == RacingView.PlayState.Pause) {
                racingView.resume();
                racingHandler.sendEmptyMessageDelayed(MSG_FINISH_LEVEL, DURATION_LEVEL - startLevelTime);
            } else if (racingView.getPlayState() == RacingView.PlayState.LevelUp) {
                racingView.resume();
                hideLabelContainer();
                racingHandler.sendEmptyMessageDelayed(MSG_FINISH_LEVEL, DURATION_LEVEL);
            } else {
                // Click on stop
                hideLabelContainer();
                racingView.play(racingHandler);
                racingHandler.sendEmptyMessageDelayed(MSG_FINISH_LEVEL, DURATION_LEVEL);
            }
            startLevelTime = System.currentTimeMillis();
        }
    }

    private void collision(boolean achieveBest) {
        racingHandler.removeMessages(MSG_FINISH_LEVEL);

        if (achieveBest) {
            tvLabelLevel.setText("Congratulation!\nYou are the Best!");
        } else {
            tvLabelLevel.setText("Try again!");
        }

        contLabel.setVisibility(View.VISIBLE);
        contLabel.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left));

        ivCenter.setBackgroundResource(R.drawable.ic_retry);
    }

    private void showLabelContainer() {
        contLabel.setVisibility(View.VISIBLE);
        contLabel.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left));
    }

    private void hideLabelContainer() {
        Animation anim = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                contLabel.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        contLabel.startAnimation(anim);
    }
}
