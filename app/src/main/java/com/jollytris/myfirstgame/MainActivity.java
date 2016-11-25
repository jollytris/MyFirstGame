package com.jollytris.myfirstgame;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

public class MainActivity extends AppCompatActivity {

    private static final int INIT_SPEED = 12;
    private static final int SPEED_INTERVAL = 2;
    private static final int MAX_SPEED = 40;

    private AdView adBanner;
    private InterstitialAd adInterstitial;

    private View contLabel;
    private TextView tvNotify;
    private TextView tvScore, tvLevel, tvBest;
    private ImageView ivCenter;
    private RacingView racingView;
    private int score, level, bestScore;
    private int playCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adBanner = (AdView) findViewById(R.id.banner);
        AdRequest.Builder builder = new AdRequest.Builder();
        if (BuildConfig.DEBUG) {
            builder.addTestDevice("E22D097DA71BEAB0F0D3BBD3DD1A6700");
            builder.addTestDevice("D3BD70725AB672E8AF899E55C5485CEE");
        }
        adBanner.loadAd(builder.build());

        adInterstitial = new InterstitialAd(this);
        adInterstitial.setAdUnitId("ca-app-pub-8613452669109382/2469624951");
        adInterstitial.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });
        requestNewInterstitial();

        contLabel = findViewById(R.id.contNotify);
        tvNotify = (TextView) findViewById(R.id.notify);

        tvScore = (TextView) findViewById(R.id.score);
        tvLevel = (TextView) findViewById(R.id.level);
        tvBest = (TextView) findViewById(R.id.best);

        ivCenter = (ImageView) findViewById(R.id.imgCenter);
        ivCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play();
            }
        });

        racingView = (RacingView) findViewById(R.id.racingView);

        playCount = 0;
        initialize();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adBanner != null) {
            adBanner.resume();
        }
        if (racingView != null && racingView.getPlayState() == RacingView.PlayState.Pause) {
            racingView.resume();
        }
    }

    @Override
    protected void onPause() {
        if (adBanner != null) {
            adBanner.pause();
        }
        if (racingView != null && racingView.getPlayState() == RacingView.PlayState.Playing) {
            pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (adBanner != null) {
            adBanner.destroy();
        }

        racingView.reset();
        super.onDestroy();
    }

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
                case RacingView.MSG_COMPLETE:
                    level++;

                    if (racingView.getSpeed() < MAX_SPEED) {
                        racingView.setSpeed(racingView.getSpeed() + SPEED_INTERVAL);
                    }

                    tvLevel.setText(String.valueOf(level));
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
        tvNotify.setText("LEVEL " + level);
        showLabelContainer();

        ivCenter.setImageResource(R.drawable.ic_play);
    }

    private void play() {
        if (racingView.getPlayState() == RacingView.PlayState.Collision) {
            initialize();

            racingView.reset();
            return;
        }

        // Click on playing
        if (racingView.getPlayState() == RacingView.PlayState.Playing) {
            pause();
        } else {
            ivCenter.setImageResource(R.drawable.ic_pause);

            showArrowToast();

            // Click on pause
            if (racingView.getPlayState() == RacingView.PlayState.Pause) {
                racingView.resume();
            } else if (racingView.getPlayState() == RacingView.PlayState.LevelUp) {
                racingView.resume();
                hideLabelContainer();
            } else {
                // Click on stop
                playCount++;
                if (playCount > 5) {
                    playCount = 0;
                    if (adInterstitial != null && adInterstitial.isLoaded()) {
                        adInterstitial.show();
                        ivCenter.setImageResource(R.drawable.ic_play);
                        return;
                    }
                }
                hideLabelContainer();
                racingView.play(racingHandler);
            }
        }
    }

    private void pause() {
        ivCenter.setImageResource(R.drawable.ic_play);
        racingView.pause();
    }

    private void collision(boolean achieveBest) {
        if (achieveBest) {
            tvNotify.setText("Congratulation!\nYou are the Best!");
        } else {
            tvNotify.setText("Try again!");
        }

        contLabel.setVisibility(View.VISIBLE);
        contLabel.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left));

        ivCenter.setImageResource(R.drawable.ic_retry);
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

    private void showArrowToast() {
        final View v = findViewById(R.id.toast);
        Animation anim = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hideArrowToast();
                    }
                }, 1000);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(anim);
    }

    private void hideArrowToast() {
        final View v = findViewById(R.id.toast);
        Animation anim = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(anim);
    }

    private void requestNewInterstitial() {
        AdRequest.Builder builder = new AdRequest.Builder();
        if (BuildConfig.DEBUG) {
            builder.addTestDevice("E22D097DA71BEAB0F0D3BBD3DD1A6700");
            builder.addTestDevice("D3BD70725AB672E8AF899E55C5485CEE");
        }
        adInterstitial.loadAd(builder.build());
    }
}
