/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.crypto;

import android.util.Base64;
import android.util.Base64InputStream;

import org.jetbrains.annotations.NotNull;
import org.spongycastle.util.Arrays;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.PrivateKey;

import static me.digi.sdk.crypto.CryptoUtils.*;

public class CACryptoProvider {
    private static final int HASH_LENGTH = 64;
    private static final int ENCRYPTED_DSK_LENGTH = 256;
    private static final int DIV_LENGTH = 16;

    private KeyPair providerKeys;

    public CACryptoProvider(@NotNull KeyPair kp) {
        this.providerKeys = kp;
    }

    public CACryptoProvider(@NotNull PrivateKey privateKey) {
        this.providerKeys = new KeyPair(null, privateKey);
    }

    public CACryptoProvider(String hexCodedPrivateKey) throws DGMCryptoFailureException {
        PrivateKey priv;
        try {
            priv = CryptoUtils.getPrivateKey(ByteUtils.hexToBytes(hexCodedPrivateKey));
        } catch (Exception ex) {
            throw new DGMCryptoFailureException(FailureCause.INVALID_KEY_FAILURE, ex);
        }
        if (priv == null) {
            throw new DGMCryptoFailureException(FailureCause.INVALID_KEY_FAILURE);
        }
        this.providerKeys = new KeyPair(null, priv);
    }

    public String decryptStream(@NotNull InputStream fileInputStream) throws IOException, DGMCryptoFailureException {
        return decryptStream(fileInputStream, true);
    }

    public String decryptStream(@NotNull InputStream fileInputStream, boolean streamBase64Encoded) throws IOException, DGMCryptoFailureException {
        byte[] encryptedDSK = new byte[ENCRYPTED_DSK_LENGTH];
        byte[] DIV = new byte[DIV_LENGTH];

        if (providerKeys.getPrivate() == null) {
            throw new DGMCryptoFailureException(FailureCause.INVALID_KEY_FAILURE);
        }
        InputStream dataStream = fileInputStream;
        if (streamBase64Encoded) {
            dataStream = new Base64InputStream(fileInputStream, Base64.DEFAULT);
        }

        if ( dataStream.read(encryptedDSK) != encryptedDSK.length //read DSK
             || dataStream.read(DIV) != DIV.length //read DIV header
                ) {
            throw new DGMCryptoFailureException(FailureCause.FILE_READING_FAILURE);
        }

        InputStream dataAndHash;
        try {
            byte[] DSK = decryptRSA(encryptedDSK, providerKeys.getPrivate());
            byte[] content = ByteUtils.readBytesFromStream(dataStream);
            int totalLength = content.length + ENCRYPTED_DSK_LENGTH + DIV_LENGTH;

            if (totalLength < 352 || totalLength % 16 != 0) {
                throw new DGMCryptoFailureException(FailureCause.CHECKSUM_CORRUPTED_FAILURE);
            }

            dataAndHash = new ByteArrayInputStream(decryptAES(content, DSK, DIV));
        } catch (Exception e) {
            throw new DGMCryptoFailureException(FailureCause.DATA_CORRUPTED_FAILURE);
        }

        return ByteUtils.bytesToString(readAndVerify(dataAndHash));
    }

    private byte[] readAndVerify(InputStream dataAndHash) throws DGMCryptoFailureException, IOException {
        byte[] hash = new byte[HASH_LENGTH];

        if (dataAndHash.read(hash) != hash.length) {
            throw new DGMCryptoFailureException(FailureCause.CHECKSUM_CORRUPTED_FAILURE);
        }

        byte[] data = ByteUtils.readBytesFromStream(dataAndHash);
        verifyHashForData(data, hash);

        return data;
    }

    private void verifyHashForData(byte[] data, byte[] hash) throws DGMCryptoFailureException {
        byte[] newHash = hashSha512(data);

        if (!Arrays.areEqual(newHash, hash)) {
            throw new DGMCryptoFailureException(FailureCause.DATA_CORRUPTED_FAILURE);
        }
    }

}
