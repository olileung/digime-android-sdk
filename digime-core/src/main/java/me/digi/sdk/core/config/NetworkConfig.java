/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core.config;

import android.support.annotation.Nullable;

public class NetworkConfig {
    private int[] alwaysOnCodes;
    private int maxRetries;
    private long minDelay;
    private boolean doExponentialBackoff;

    public NetworkConfig(int maxRetries, long minDelay) {
        this(maxRetries, minDelay, null, true);
    }

    public NetworkConfig(int maxRetries, long minDelay, @Nullable int[] alwaysOnCodes, boolean doExponentialBackoff) {
        this.maxRetries = maxRetries;
        this.minDelay = minDelay;
        this.alwaysOnCodes = alwaysOnCodes;
        this.doExponentialBackoff = doExponentialBackoff;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public long getMinDelay() {
        return minDelay;
    }

    public int[] getAlwaysOnCodes() {
        return alwaysOnCodes;
    }

    public boolean shouldPerformExponentialBackoff() {
        return doExponentialBackoff;
    }

}
