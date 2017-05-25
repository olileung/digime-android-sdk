/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.security;

import org.json.JSONObject;
import org.spongycastle.crypto.BufferedBlockCipher;
import org.spongycastle.crypto.CipherParameters;
import org.spongycastle.crypto.Digest;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.digests.SHA512Digest;
import org.spongycastle.crypto.engines.AESEngine;
import org.spongycastle.crypto.io.DigestInputStream;
import org.spongycastle.crypto.modes.CBCBlockCipher;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;
import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Security;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;

public class SecurityUtils {

    private static final Logger LOGGER = Logger.getLogger(SecurityUtils.class.getName());
    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    static {
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }

    /**
     * RSA decrypt the given data using the default application key
     *
     * @param data       The data to decryptRSA
     * @param privateKey The PrivateKey to use for decryption
     * @return The decrypted bytes
     */
    static byte[] decryptRSA(byte[] data, PrivateKey privateKey) {
        try {
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding", "SC");
            rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
            return rsaCipher.doFinal(data);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "RSA", "Error while decrypting data: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * AES decrypt the given data with the given key
     *
     * @param data The data to decryptAES
     * @param key  The key to decryptAES with
     * @return The decrypted bytes
     */
    public static byte[] decryptAES(byte[] data, byte[] key, byte[] ivBytes) throws SecurityFailureException {
        try {
            return cipherData(blockCipher(key, ivBytes), data);
        } catch (InvalidCipherTextException e) {
            throw new SecurityFailureException(FailureCause.AES_DECRYPTION_FAILURE, e);
        }
    }

    static BufferedBlockCipher blockCipher(byte[] key, byte[] ivBytes) {
        PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(new CBCBlockCipher(
            new AESEngine()));
        CipherParameters ivAndKey = new ParametersWithIV(new KeyParameter(key), ivBytes);
        aes.init(false, ivAndKey);
        return aes;
    }

    private static byte[] cipherData(BufferedBlockCipher cipher, byte[] data) throws InvalidCipherTextException {
        int minSize = cipher.getOutputSize(data.length);
        byte[] outBuf = new byte[minSize];
        int length1 = cipher.processBytes(data, 0, data.length, outBuf, 0);
        int length2 = cipher.doFinal(outBuf, length1);
        int actualLength = length1 + length2;
        byte[] result = new byte[actualLength];
        System.arraycopy(outBuf, 0, result, 0, result.length);
        return result;
    }

    /**
     * Hashing
     */
    static String hashSha512(String input) {
        byte[] hash = hashSha512(input.getBytes(UTF_8));

        StringBuilder sb = new StringBuilder();
        for (byte aByte : hash) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    static byte[] hashSha512(byte[] data) {
        final byte[] dataHashBytes = new byte[64];
        try (DigestInputStream in = new DigestInputStream(new ByteArrayInputStream(data),
            new SHA512Digest())) {
            // Read the stream and do nothing with it
            while (in.read() != -1) {
            }

            final Digest md = in.getDigest();
            md.doFinal(dataHashBytes, 0);
        } catch (IOException e) {
            //TODO report that calculating hash failed
            e.printStackTrace();
        }
        return dataHashBytes;
    }

    /**
     * Conversions
     */
    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for (byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    public static byte[] hexToBytes(String in) {
        int len = in.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(in.charAt(i), 16) << 4) + Character.digit(in.charAt(i + 1), 16));
        }

        return data;
    }

    static String hexToUTF(String in) {
        return bytesToString(hexToBytes(in));
    }

    public static String bytesToString(byte[] in) {
        return new String(in, UTF_8);
    }

    static String getJSONStringFromMap(HashMap<String, String> map) {
        return new JSONObject(map).toString();
    }

    static String HexToBinary(String hex, int size) {
        String bin = new BigInteger(hex, size).toString(2);
        int inb = Integer.parseInt(bin);
        bin = String.format("%08d", inb);
        return bin;
    }

    static String bytesToBinary(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * Byte.SIZE);
        for (int i = 0; i < Byte.SIZE * bytes.length; i++)
            sb.append((bytes[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
        return sb.toString();
    }

    static byte[] binaryToBytes(String binary) {
        short a = Short.parseShort(binary, 2);
        ByteBuffer bytes = ByteBuffer.allocate(2).putShort(a);
        return bytes.array();
    }

    static byte[] intToBytes(int input) {
        return new byte[]{
            (byte) (input >>> 24),
            (byte) (input >>> 16),
            (byte) (input >>> 8),
            (byte) input};
    }

    public static int bytesToInt(byte[] b) {
        int value = 0;
        for (int i = 0; i < b.length; i++)
            value = (value << 8) | b[i];
        return value;
    }

    public static byte[] readBytesFromStream(InputStream stream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16];

        while ((nRead = stream.read(data)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();

        return buffer.toByteArray();
    }

    static byte[] getDataAndHashBytes(byte[] data) {
        byte[] hash = SecurityUtils.hashSha512(data);
        return SecurityUtils.concatenateByteArrays(hash, data);
    }

    static byte[] concatenateByteArrays(byte[] b1, byte[] b2) {
        byte[] concatenated = new byte[b2.length + b1.length];
        System.arraycopy(b1, 0, concatenated, 0, b1.length);
        System.arraycopy(b2, 0, concatenated, b1.length, b2.length);
        return concatenated;
    }
}
