/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.security.domain;

/**
 * VaultObject which contains unencrypted data.
 */

public class VaultObject {
    public byte[] type;

    public byte[] version;

    public int iterations;

    public byte[] salt;

    public byte[] kiv;

    public byte[] div;

    public byte[] dskAndHash;

    public byte[] dataAndHash;
}
