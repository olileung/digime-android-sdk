/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core;

@SuppressWarnings("SameParameterValue")
public class DigiMeException extends RuntimeException {

    DigiMeException() {
        super();
    }

    DigiMeException(String msg) {
        super(msg);
    }

    DigiMeException(String format, Object... args) {
        this(String.format(format, args));
    }

    DigiMeException(String message, Throwable throwable) {
        super(message, throwable);
    }

    DigiMeException(Throwable throwable) {
        super(throwable);
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
