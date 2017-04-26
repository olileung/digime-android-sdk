/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core.internal;

import java.lang.ref.WeakReference;

import me.digi.sdk.core.CASession;
import me.digi.sdk.core.SDKException;

public class AuthorizationException extends SDKException {
    private Reason throwReason;
    private WeakReference<CASession> failedForSession;

    public AuthorizationException(String message) {
        this(message, null, Reason.IN_PROGRESS);
    }

    public AuthorizationException(String message, CASession failedForSession, Reason failReason) {
        super(message);
        this.throwReason = failReason;
        this.failedForSession = new WeakReference<CASession>(failedForSession);
    }

    public AuthorizationException(String message, Throwable throwable) { super(message, throwable); }

    public CASession getFailedForSession() {
        if (failedForSession != null) {
            return failedForSession.get();
        }
        return null;
    }

    public Reason getThrowReason() {
        return throwReason;
    }

    public enum Reason {
        ACCESS_DENIED,
        IN_PROGRESS,
        WRONG_CODE
    }

}