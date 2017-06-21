/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core.internal.network;

import java.io.IOException;

public class BackOffTimer {

    static final long STOP = -100L;

    public static final int DEFAULT_MIN_INTERVAL = 500;
    public static final double DEFAULT_RANDOMIZATION_FACTOR = 0.5;
    public static final double DEFAULT_MULTIPLIER = 1.5;
    public static final int DEFAULT_MAX_INTERVAL = 60000;
    public static final int DEFAULT_MAX_ELAPSED_TIME = 800000;

    private int currentDelay;

    private final int initialInterval;
    private final double randomizationFactor;
    private final double multiplier;
    private final int maxInterval;
    //Nanosecond precision
    private long timerStart;

    private final int maxElapsedTime;

    public BackOffTimer() {
        initialInterval = DEFAULT_MIN_INTERVAL;
        randomizationFactor = DEFAULT_RANDOMIZATION_FACTOR;
        multiplier = DEFAULT_MULTIPLIER;
        maxInterval = DEFAULT_MAX_INTERVAL;
        maxElapsedTime = DEFAULT_MAX_ELAPSED_TIME;
        rewind();
    }

    public BackOffTimer(int minInterval) {
        initialInterval = minInterval;
        randomizationFactor = DEFAULT_RANDOMIZATION_FACTOR;
        multiplier = DEFAULT_MULTIPLIER;
        maxInterval = DEFAULT_MAX_INTERVAL;
        maxElapsedTime = DEFAULT_MAX_ELAPSED_TIME;
        rewind();
    }

    public final long getElapsedTime() {
        return (System.nanoTime() - timerStart) / 1000000;
    }

    public final void rewind() {
        currentDelay = initialInterval;
        timerStart = System.nanoTime();
    }

    public long calculateNextBackOffMillis() {
        if (getElapsedTime() > maxElapsedTime) {
            return STOP;
        }
        int randomizedInterval = randomizeInterval(randomizationFactor, Math.random(), currentDelay);
        incrementDelay();
        return randomizedInterval;
    }

    private void incrementDelay() {
        if (currentDelay >= maxInterval / multiplier) {
            currentDelay = maxInterval;
        } else {
            currentDelay *= multiplier;
        }
    }

    private static int randomizeInterval(
            double randomizationFactor, double random, int currentIntervalMillis) {
        double delta = randomizationFactor * currentIntervalMillis;
        double minInterval = currentIntervalMillis - delta;
        double maxInterval = currentIntervalMillis + delta;

        return (int) (minInterval + (random * (maxInterval - minInterval + 1)));
    }

}
