package com.decideme.recruit.attributes;

import android.os.CountDownTimer;

/**
 * Created by vivek_shah on 7/11/17.
 */
public abstract class CountUpTimer extends CountDownTimer {
    private static final long INTERVAL_MS = 1000;
    private final long duration;

    protected CountUpTimer(long durationMs) {
        super(durationMs, INTERVAL_MS);
        this.duration = durationMs;
    }

    public abstract void onTick(int hour, int min, int second);

    @Override
    public void onTick(long msUntilFinished) {
        int second = (int) ((duration - msUntilFinished) / 1000);
        int min = (int) ((duration - msUntilFinished) / 1000 / 60 % 60);
        int hours =
                (int) ((duration - msUntilFinished) / 1000 / 60 / 60);
        onTick(hours, min, second);
    }

    @Override
    public void onFinish() {
        onTick(duration / 1000);
    }
}

