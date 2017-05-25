/*
 * Copyright (c) 2017 digi.me. All rights reserved.
 */

package me.digi.security;

/**
 * List of failures which can occur when vault is being accessed
 */
public enum FailureCause {
    //TODO clean up messages
    ITERATIONS_EMPTY("Internal error, please contact support. (%s)"),
    AES_ENCRYPTION_FAILURE("Internal error, please contact support. (%s)"),
    AES_DECRYPTION_FAILURE("Internal error, please contact support. (%s)"),
    HASH_ALGORITHM_FAILURE("Internal error, please contact support. (%s)"),
    INVALID_TYPE_DESCRIPTOR("Invalid file type, please contact support. (%s)"),
    INVALID_VERSION("Invalid version, please contact support. (%s)"),
    READING_FILE_HEADER_FAILURE("Reading JFS file header failed, please contact support. (%s)"),
    READING_HASH_FAILURE("Reading data hash failed, please contact support. (%s)"),
    DATA_CORRUPTED_FAILURE("Data is corrupted - vault has been tampered with, please start again. (%s)"),
    INCORRECT_PASSWORD("Invalid password had been provided"),
    UNKNOWN_ERROR("Internal error, please try again");

    /**
     * ITERATIONS_EMPTY(501, "Internal error, please contact support. (%s)"),
     AES_ENCRYPTION_FAILURE(502, "Internal error, please contact support. (%s)"),
     AES_DECRYPTION_FAILURE(503, "Internal error, please contact support. (%s)"),
     HASH_ALGORITHM_FAILURE(504, "Internal error, please contact support. (%s)"),
     INCORRECT_HASH(404, "Vault has been tampered with, please start again. (%s)"),
     INCORRECT_PASSWORD(405, null),
     EXPECTED_FLAG_NOT_SET(406, "Vault has been tampered with, please start again. (%s");
     */

    private final String message;

    FailureCause(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
