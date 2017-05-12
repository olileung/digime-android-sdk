/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core;

public class DigiMeClientException extends DigiMeException {
    public DigiMeClientException() {
        super();
    }

    public DigiMeClientException(String message) {
        super(message);
    }

    public DigiMeClientException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public DigiMeClientException(Throwable throwable) {
        super(throwable);
    }
}
