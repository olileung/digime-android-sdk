/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core;

public class DigiMeException extends RuntimeException {

    public DigiMeException() {

        super();
    }

    public DigiMeException(String msg) {

        super(msg);
    }

    public DigiMeException(String format, Object... args) {
        this(String.format(format, args));
    }

    public DigiMeException(String message, Throwable throwable) {

        super(message, throwable);
    }

    public DigiMeException(Throwable throwable) {

        super(throwable);
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
