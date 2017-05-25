/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.security;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.ASN1Primitive;
import org.spongycastle.asn1.pkcs.RSAPublicKey;
import org.spongycastle.crypto.BufferedBlockCipher;
import org.spongycastle.crypto.CipherParameters;
import org.spongycastle.crypto.Digest;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.PBEParametersGenerator;
import org.spongycastle.crypto.digests.SHA512Digest;
import org.spongycastle.crypto.engines.AESEngine;
import org.spongycastle.crypto.engines.AESFastEngine;
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.spongycastle.crypto.io.DigestInputStream;
import org.spongycastle.crypto.modes.CBCBlockCipher;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.util.Arrays;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.io.pem.PemObject;
import org.spongycastle.util.io.pem.PemWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import me.digi.security.domain.LocalVaultData;
import me.digi.security.domain.VaultData;
import me.digi.security.domain.VaultObject;
import me.digi.security.domain.VaultSecureData;

public class SecurityUtils {

    private static final Logger LOGGER = Logger.getLogger(SecurityUtils.class.getName());

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    private static final int DEFAULT_ITERATIONS = 10000;
    static final int KEY_LENGTH = 32;
    private static final int DSK_LENGTH = 32;

    private static final int RSA_KEY_SIZE = 1024;
    private static final String AES_KEY_NAME = "aeskey";
    private static final String RSA_PUBLIC_KEY_NAME = "rsapublickey";
    private static final String RSA_PRIVATE_KEY_NAME = "rsaprivatekey";
    private static final String VAULT_FILE_NAME = "vault";
    static final int VAULT_ENCRYPTED_WITH_AES256_AND_PBKDF2 = 7;
    static final int TYPE_BITFIELD_LENGTH_BITS = 128;
    public static final int VAULT_VERSION = 2;

    public static final int TYPE_LENGTH = 16;
    public static final int VERSION_LENGTH = 4;
    public static final int SALT_LENGTH = 16;
    public static final int ITERATIONS_LENGTH = 4;
    public static final int KIV_LENGTH = 16;
    public static final int ENCRYPTED_DSK_LENGTH = 112;
    public static final int DIV_LENGTH = 16;

    static {
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }

    private static byte[] salt;
    private static int iterations = DEFAULT_ITERATIONS;

    synchronized static byte[] generateSalt() {
        if (salt == null) {
            salt = getRandomUnsignedCharacters(SALT_LENGTH);
        }
        return salt;
    }

    synchronized static int getDefaultIterations() {
        return iterations;
    }

    public static VaultSecureData generateVaultSecureData(@NotNull String password) {
        byte[] salt = SecurityUtils.generateSalt();
        int iterations = SecurityUtils.getDefaultIterations();
        return new VaultSecureData(generateVaultKey(password, salt, iterations), salt, iterations);
    }

    /**
     * Method used to generate vault key
     *
     * @param password   which is provided by user
     * @param salt
     * @param iterations number - always bigger than 0
     * @return vaultKey
     */
    public static byte[] generateVaultKey(@NotNull String password, @NotNull byte[] salt, int iterations) {
        long startTime = System.currentTimeMillis();

        byte[] vaultKey = getPbkdf2Sha512Key(password, salt, iterations, KEY_LENGTH);

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        return vaultKey;
    }

    /**
     * Used to create complete vault object
     *
     * @param localVaultData, not null
     * @param vaultKey,       not null
     * @param salt            data, not null
     * @param iterations      number - always bigger than 0
     * @return {@link VaultObject}
     */
    public static VaultObject createVault(@NotNull LocalVaultData localVaultData, @NotNull byte[] vaultKey, @NotNull byte[] salt, int iterations) throws VaultFailureException {
        if (iterations <= 0) {
            throw new VaultFailureException(FailureCause.ITERATIONS_EMPTY);
        }

        // Randomly generated with unsigned characters.
        byte[] DSK = getRandomUnsignedCharacters(DSK_LENGTH);
        byte[] KIV = getRandomUnsignedCharacters(KIV_LENGTH);
        byte[] DIV = getRandomUnsignedCharacters(DIV_LENGTH);

        byte[] data = new Gson().toJson(localVaultData).getBytes();

        byte[] encryptedDataAndHash;
        try {
            encryptedDataAndHash = encryptAES(getDataAndHashBytes(data), DSK, DIV);
        } catch (VaultFailureException e) {
            throw e;
        }

        byte[] encryptedDskAndHash;
        try {
            encryptedDskAndHash = encryptAES(getDataAndHashBytes(DSK), vaultKey, KIV);
        } catch (VaultFailureException e) {
            throw e;
        }

        byte[] type = new byte[TYPE_BITFIELD_LENGTH_BITS / 8];
        setBit(VAULT_ENCRYPTED_WITH_AES256_AND_PBKDF2, type);
        byte[] version = intToBytes(VAULT_VERSION);

        VaultObject vaultObject = new VaultObject();
        vaultObject.type = type;
        vaultObject.version = version;
        vaultObject.iterations = iterations;
        vaultObject.salt = salt;
        vaultObject.kiv = KIV;
        vaultObject.div = DIV;
        vaultObject.dskAndHash = encryptedDskAndHash;
        vaultObject.dataAndHash = encryptedDataAndHash;
        return vaultObject;
    }

    public static byte[] collapseLocalVaultObject(VaultObject vaultObject) {
        byte[] T = vaultObject.type;
        byte[] V = vaultObject.version;
        byte[] S = vaultObject.salt;
        byte[] I = SecurityUtils.intToBytes(vaultObject.iterations);
        byte[] KIV = vaultObject.kiv;
        byte[] encryptedDSK = vaultObject.dskAndHash;
        byte[] DIV = vaultObject.div;
        byte[] encryptedData = vaultObject.dataAndHash;

        byte[] collapsedBytes = concatenateByteArrays(T, V);
        collapsedBytes = concatenateByteArrays(collapsedBytes, S);
        collapsedBytes = concatenateByteArrays(collapsedBytes, I);
        collapsedBytes = concatenateByteArrays(collapsedBytes, KIV);
        collapsedBytes = concatenateByteArrays(collapsedBytes, encryptedDSK);
        collapsedBytes = concatenateByteArrays(collapsedBytes, DIV);
        collapsedBytes = concatenateByteArrays(collapsedBytes, encryptedData);
        return collapsedBytes;
    }

    public static VaultObject unpackLocalVaultObject(byte[] collapsedBytes) {
        VaultObject vaultObject = new VaultObject();
        vaultObject.type = Arrays.copyOfRange(collapsedBytes, 0, TYPE_LENGTH);
        vaultObject.version = Arrays.copyOfRange(collapsedBytes, TYPE_LENGTH, TYPE_LENGTH + VERSION_LENGTH);
        vaultObject.salt = Arrays.copyOfRange(collapsedBytes, TYPE_LENGTH + VERSION_LENGTH, TYPE_LENGTH + VERSION_LENGTH + SALT_LENGTH);
        vaultObject.iterations = SecurityUtils.bytesToInt(Arrays.copyOfRange(collapsedBytes, TYPE_LENGTH + VERSION_LENGTH + SALT_LENGTH, TYPE_LENGTH + VERSION_LENGTH + SALT_LENGTH + ITERATIONS_LENGTH));
        vaultObject.kiv = Arrays.copyOfRange(collapsedBytes, TYPE_LENGTH + VERSION_LENGTH + SALT_LENGTH + ITERATIONS_LENGTH, TYPE_LENGTH + VERSION_LENGTH + SALT_LENGTH + ITERATIONS_LENGTH + KIV_LENGTH);
        vaultObject.dskAndHash = Arrays.copyOfRange(collapsedBytes, TYPE_LENGTH + VERSION_LENGTH + SALT_LENGTH + ITERATIONS_LENGTH + KIV_LENGTH, TYPE_LENGTH + VERSION_LENGTH + SALT_LENGTH + ITERATIONS_LENGTH + KIV_LENGTH + ENCRYPTED_DSK_LENGTH);
        vaultObject.div = Arrays.copyOfRange(collapsedBytes, TYPE_LENGTH + VERSION_LENGTH + SALT_LENGTH + ITERATIONS_LENGTH + KIV_LENGTH + ENCRYPTED_DSK_LENGTH, TYPE_LENGTH + VERSION_LENGTH + SALT_LENGTH + ITERATIONS_LENGTH + KIV_LENGTH + ENCRYPTED_DSK_LENGTH + DIV_LENGTH);
        vaultObject.dataAndHash = Arrays.copyOfRange(collapsedBytes, TYPE_LENGTH + VERSION_LENGTH + SALT_LENGTH + ITERATIONS_LENGTH + KIV_LENGTH + ENCRYPTED_DSK_LENGTH + DIV_LENGTH, collapsedBytes.length);
        return vaultObject;
    }

    static <T extends VaultData> T getData(String data, Class<T> klass) {
        return new Gson().fromJson(data, klass);
    }

    public static <T extends VaultData> T openVault(@NotNull VaultObject vaultObject, @NotNull VaultSecureData key, Class<T> klass) throws VaultFailureException {
        return getData(openVault(vaultObject, key), klass);
    }

    /**
     * Used to load complete vault object and vault data
     *
     * @param vaultObject VaultObject.
     * @param key    Vault Symmetrical Key generated from user input password.
     * @return {@link String} Unencrypted Vault data.
     * @throws VaultFailureException
     */
    private static String openVault(@NotNull VaultObject vaultObject, @NotNull VaultSecureData key) throws VaultFailureException {
        byte[] VSK = key.vaultKey;

        long dskDecryptionStartTime = System.currentTimeMillis();

        byte[] DSK = decrypt(vaultObject.dskAndHash, VSK, vaultObject.kiv);

        long dskDecryptionEndTime = System.currentTimeMillis();
        long dskDecryptionElapsedTime = dskDecryptionEndTime - dskDecryptionStartTime;

        byte[] data = decrypt(vaultObject.dataAndHash, DSK, vaultObject.div);
        return bytesToString(data);
    }

    private static byte[] decrypt(byte[] encryptedDataAndHash, byte[] key, byte[] IV) throws VaultFailureException {
        byte[] dataAndHash = decryptAES(encryptedDataAndHash, key, IV);

        try {
            return readAndVerify(new ByteArrayInputStream(dataAndHash));
        } catch (IOException e) {
            throw new VaultFailureException(FailureCause.UNKNOWN_ERROR, e);
        }
    }

    public static byte[] readAndVerify(InputStream dataAndHash) throws VaultFailureException, IOException {
        byte[] hash = new byte[64];

        if (dataAndHash.read(hash) != hash.length) {
            throw new IOException();
        }

        byte[] data = readBytesFromStream(dataAndHash);
        SecurityUtils.verifyHashForData(data, hash);

        return data;
    }

    static void verifyHashForData(byte[] data, byte[] hash) throws VaultFailureException {
        byte[] newHash = hashSha512(data);

        if (!Arrays.areEqual(newHash, hash)) {
            throw new VaultFailureException(FailureCause.DATA_CORRUPTED_FAILURE);
        }
    }

    public static void saveVault(Store store, VaultObject vaultObject) {
        byte[] collapsedBytes = SecurityUtils.collapseLocalVaultObject(vaultObject);
        store.writeData(collapsedBytes, VAULT_FILE_NAME);
    }

    public static VaultObject readVault(Store store) {
        try {
            VaultObject vaultObject = SecurityUtils.unpackLocalVaultObject(store.readData(VAULT_FILE_NAME));
            return vaultObject;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static void setBit(int bit, byte[] bitfield) {
        if (bit < 0) {
            throw new RuntimeException("Bit cannot be smaller than 0.");
        } else if (bit > (bitfield.length * 8)) {
            throw new RuntimeException("Bit cannot be larger than bitfield.");
        }

        int index = (bitfield.length) - (bit / 8) - 1;
        bitfield[index] |= (1 << (bit % 8));
    }

    static boolean isBitSet(int bit, byte[] bitfield) {
        if (bit < 0) {
            throw new RuntimeException("Bit cannot be smaller than 0.");
        } else if (bit > (bitfield.length * 8)) {
            throw new RuntimeException("Bit cannot be larger than bitfield.");
        }

        int index = (bitfield.length) - (bit / 8) - 1;
        return (bitfield[index] & (1 << (bit % 8))) == (1 << (bit % 8));
    }

    /**
     * RSA encrypt the given plaintext using the default application key
     *
     * @param data The plaintext to encryptRSA
     * @return The encrypted bytes
     */
    static byte[] encryptRSA(Store store, byte[] data) {
        PublicKey publicKey = createOrRetrieveRSAKeyPair(store).getPublic();
        if (publicKey == null)
            return null;

        return encryptRSA(data, publicKey);
    }

    /**
     * RSA encrypt the given plaintext bytes using the given key
     *
     * @param data      The plaintext to encryptRSA
     * @param publicKey The PublicKey to use for encryption
     * @return The encrypted bytes
     */
    static byte[] encryptRSA(byte[] data, PublicKey publicKey) {
        try {
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding", "SC"); // RSA 2048
            rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return rsaCipher.doFinal(data);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "RSA", "Error while encrypting data: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * RSA decrypt the given data using the default application key
     *
     * @param data The data to decryptRSA
     * @return The decrypted bytes
     */
    static byte[] decryptRSA(Store store, byte[] data) {
        PrivateKey privateKey = createOrRetrieveRSAKeyPair(store).getPrivate();
        if (privateKey == null)
            return null;

        return decryptRSA(data, privateKey);
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
     * AES encrypt the given plaintext bytes using the given key
     *
     * @param data     The plaintext to encryptAES
     * @param keyBytes The key to use for encryption
     * @param ivBytes
     * @return The encrypted bytes
     */
    static byte[] encryptAES(byte[] data, byte[] keyBytes, byte[] ivBytes) throws VaultFailureException {
        try {
            // 16 bytes is the IV size for AES256
            PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()));

            cipher.init(true, new ParametersWithIV(new KeyParameter(keyBytes), ivBytes));
            byte[] outBuf = new byte[cipher.getOutputSize(data.length)];

            int processed = cipher.processBytes(data, 0, data.length, outBuf, 0);
            cipher.doFinal(outBuf, processed);
            return outBuf;
        } catch (InvalidCipherTextException e) {
            throw new VaultFailureException(FailureCause.AES_ENCRYPTION_FAILURE, e);
        }
    }

    /**
     * AES decrypt the given data using the default application key
     *
     * @param data The data to decryptAES
     * @return The decrypted bytes
     */
//    static byte[] decryptAES(byte[] data) throws NoSuchAlgorithmException, BadPaddingException, NoSuchPaddingException, IllegalBlockSizeException, InvalidKeyException {
//        SecretKey key = createOrRetrieveAESSecretKey();
//        if (key == null)
//            return null;
//
//        return decryptAES(data, key.getEncoded());
//    }

    /**
     * AES decrypt the given data with the given key
     *
     * @param data The data to decryptAES
     * @param key  The key to decryptAES with
     * @return The decrypted bytes
     */
    public static byte[] decryptAES(byte[] data, byte[] key, byte[] ivBytes) throws VaultFailureException {
        try {
            return cipherData(blockCipher(key, ivBytes), data);
        } catch (InvalidCipherTextException e) {
            throw new VaultFailureException(FailureCause.AES_DECRYPTION_FAILURE, e);
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

    static byte[] getPbkdf2Sha512Key(String password, byte[] salt, int iterations, int keyLength) {
        keyLength = keyLength * 8; // to change it into bits
        PKCS5S2ParametersGenerator generator = new PKCS5S2ParametersGenerator(new SHA512Digest());
        generator.init(PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(password.toCharArray()), salt, iterations);
        KeyParameter key = (KeyParameter) generator.generateDerivedParameters(keyLength);
        return key.getKey();
    }

    static KeyPair createOrRetrieveRSAKeyPair(Store store) {
        PublicKey publicKey = readRSAPublicKey(store, RSA_PUBLIC_KEY_NAME);
        PrivateKey privateKey = readRSAPrivateKey(store, RSA_PRIVATE_KEY_NAME);
        KeyPair keyPair;
        if (publicKey == null || privateKey == null) {
            keyPair = generateRSAKeyPair();
            writeRSAKeyPair(store, keyPair);
        } else {
            keyPair = new KeyPair(publicKey, privateKey);
        }
        return keyPair;
    }

    static KeyPair generateRSAKeyPair() {
        try {
            SecureRandom random = new SecureRandom();
            RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(RSA_KEY_SIZE, RSAKeyGenParameterSpec.F4); // 65537
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "SC");
            generator.initialize(spec, random);
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Check for a currently saved key and if not present, create a new one
     *
     * @return The newly or previously created key
     */
    private static SecretKey createOrRetrieveAESSecretKey(Store store) {
        try {
            byte[] keyBytes = readKey(store, AES_KEY_NAME);
            SecretKey key;
            if (keyBytes == null) {
                key = generateAESKey();
                writeKey(store, key.getEncoded(), AES_KEY_NAME);
            } else {
                key = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
            }
            return key;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Generate a key suitable for AES256 encryption
     *
     * @return The generated key
     * @throws NoSuchAlgorithmException
     */
    private static SecretKey generateAESKey() throws NoSuchAlgorithmException {
        // Generate a 256-bit key
        final int outputKeyLength = 256;

        // EDIT - do not need to create SecureRandom, this is done automatically by init() if one is not provided
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(outputKeyLength);
        SecretKey key = keyGenerator.generateKey();
        return key;
    }

    /**
     * Write the given encryption key to private storage using the hard-coded filename
     *
     * @param key The key to write
     */
    private static void writeKey(Store store, byte[] key, String fileName) {
        store.writeData(key, fileName);
    }

    private static void writeRSAKeyPair(Store store, KeyPair keyPair) {
        String publicKey = writePemObject("PUBLIC KEY", keyPair.getPublic().getEncoded());
        String privateKey = writePemObject("PRIVATE KEY", keyPair.getPrivate().getEncoded());

        if (publicKey == null || privateKey == null) {
            return;
        }

        store.writeData(publicKey.getBytes(), RSA_PUBLIC_KEY_NAME);
        store.writeData(privateKey.getBytes(), RSA_PRIVATE_KEY_NAME);
    }

    private static String writePemObject(String pemName, byte[] key) {
        StringWriter publicStringWriter = new StringWriter();
        try {
            PemWriter pemWriter = new PemWriter(publicStringWriter);
            pemWriter.writeObject(new PemObject(pemName, key));
            pemWriter.flush();
            pemWriter.close();
            return publicStringWriter.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Read the encryption key from private storage
     *
     * @return
     */
    private static byte[] readKey(Store store, String fileName) {
        try {
            return store.readData(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static PublicKey readRSAPublicKey(Store store, String fileName) {
        try {
            byte[] data = store.readData(fileName);
            if (data == null) {
                return null;
            }

            return getRSAPublicKeyFromString(bytesToString(data));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static PrivateKey readRSAPrivateKey(Store store, String fileName) {
        try {
            byte[] data = store.readData(fileName);
            if (data == null) {
                return null;
            }

            return getRSAPrivateKeyFromString(bytesToString(data));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static PublicKey getRSAPublicKeyFromString(String publicKeyPEM) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
        publicKeyPEM = stripPublicKeyHeaders(publicKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA", "SC");
        byte[] publicKeyBytes = Base64.decode(publicKeyPEM.getBytes(UTF_8));
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKeyBytes);
        return keyFactory.generatePublic(x509KeySpec);
    }

//    KeyFactory keyFactory = KeyFactory.getInstance("RSA", "SC");
//    PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(bytes);
//    return keyFactory.generatePublic(pkcs8EncodedKeySpec);

    static PublicKey getRSAPublicKeyFromBytes(byte[] bytes) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        ASN1Primitive asn1Prime = new ASN1InputStream(bytes).readObject();
        RSAPublicKey rsaPub = RSAPublicKey.getInstance(asn1Prime);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(new RSAPublicKeySpec(rsaPub.getModulus(), rsaPub.getPublicExponent()));
    }

    static PrivateKey getRSAPrivateKeyFromString(String privateKeyPEM) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
        privateKeyPEM = stripPrivateKeyHeaders(privateKeyPEM);
        KeyFactory fact = KeyFactory.getInstance("RSA", "SC");
        byte[] clear = Base64.decode(privateKeyPEM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(clear);
        PrivateKey priv = fact.generatePrivate(keySpec);
        Arrays.fill(clear, (byte) 0);
        return priv;
    }

    static String stripPublicKeyHeaders(String publicKeyPEM) {
        return publicKeyPEM.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");
    }

    static String stripPrivateKeyHeaders(String privateKeyPEM) {
        return privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "");
    }

    /**
     * Random character generation
     */
    private static String getRandom(char[] characters, int size) {
        StringBuilder stringBuilder = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < size; i++) {
            stringBuilder.append(characters[random.nextInt(characters.length)]);
        }
        return stringBuilder.toString();
    }

    static byte[] getRandomUnsignedCharacters(int length) {
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            int x = getRandomUnsignedCharacter();
            bytes[i] = (byte) x;
        }
        return bytes;
    }

    static int getRandomUnsignedCharacter() {
        SecureRandom random = new SecureRandom();
        return random.nextInt(256);
    }

    static String getRandomAlpha(int length) {
        return getRandom("abcdefghijklmnopqrstuvwxyz".toCharArray(), length);
    }

    static String getRandomHex(int length) {
        return getRandom("0123456789abcdefghijklmnopqrstuvwxyz".toCharArray(), length);
    }

    public static String getRandomUpperLowerNumeric(int length) {
        return getRandom("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYP".toCharArray(), length);
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
