package me.digi.security.domain;

import java.util.Arrays;

import me.digi.security.SecurityUtils;

/**
 * VaultSecureData is used to pass around key generated from user's password
 */

public final class VaultSecureData {
    public final byte[] vaultKey;
    public final byte[] salt;
    public final int iterations;

    public VaultSecureData(final byte[] vaultKey, byte[] salt, int iterations) {
        this.vaultKey = vaultKey;
        this.salt = salt;
        this.iterations = iterations;
    }

    public VaultSecureData(String password, byte[] salt, int iterations) {
        this.vaultKey = SecurityUtils.generateVaultKey(password, salt, iterations);
        this.salt = salt;
        this.iterations = iterations;
    }

    public VaultSecureData(String password, VaultObject vaultObject) {
        this(password, vaultObject.salt, vaultObject.iterations);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VaultSecureData that = (VaultSecureData) o;

        if (iterations != that.iterations) return false;
        if (!Arrays.equals(vaultKey, that.vaultKey)) return false;
        return Arrays.equals(salt, that.salt);

    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(vaultKey);
        result = 31 * result + Arrays.hashCode(salt);
        result = 31 * result + iterations;
        return result;
    }
}
