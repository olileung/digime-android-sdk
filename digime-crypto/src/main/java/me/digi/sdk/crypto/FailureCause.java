/*
 * Copyright (c) 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.crypto;

/**
 * List of failures which can occur when vault is being accessed
 */
public enum FailureCause {
    AES_DECRYPTION_FAILURE("Internal error, please contact support. (%s)");

    private final String message;

    FailureCause(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
