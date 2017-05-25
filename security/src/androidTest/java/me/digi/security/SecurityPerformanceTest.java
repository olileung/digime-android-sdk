/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.security;

import android.support.test.filters.RequiresDevice;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.digi.security.domain.LocalVaultData;
import me.digi.security.domain.VaultObject;
import me.digi.security.domain.VaultSecureData;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class SecurityPerformanceTest {

    private static final Logger LOGGER = Logger.getLogger(SecurityUtils.class.getName());

    Store store = new Store() {

        private byte[] data;

        @Override
        public void writeData(byte[] data, String filename) {
            this.data = data;
        }

        @Override
        public byte[] readData(String filename) throws IOException {
            return data;
        }
    };

    @After
    public void cleanUpAfterTest() {
        store.writeData(null, "");
    }

    @Test
    @RequiresDevice
    public void examplePerformanceOnDeviceTest() throws Exception {
        String testPassword = "password";
        String testSalt = "salt";
        int iterations = 1000;

        LOGGER.log(Level.INFO, "PBKD Test at " + String.valueOf(iterations) + " iterations");
        long startTime = System.currentTimeMillis();
        String bytea = SecurityUtils.bytesToHex(SecurityUtils.getPbkdf2Sha512Key(testPassword, testSalt.getBytes(), iterations, 64));
        LOGGER.log(Level.INFO, "PBKD: Test took %s milliseconds", System.currentTimeMillis() - startTime);

        //assertEquals(bytea, "867f70cf1ade02cff3752599a3a53dc4af34c7a669815ae5d513554e1c8cf252c02d470a285a0501bad999bfe943c08f050235d7d68b1da55e63f73b60a57fce");
    }

    @Test
    @RequiresDevice
    public void createAndOpenVault() throws Exception {
        LocalVaultData localVaultData = new LocalVaultData("123", "a", "/a");

        byte[] salt = SecurityUtils.generateSalt();
        int iterations = SecurityUtils.getDefaultIterations();
        byte[] VSK = SecurityUtils.generateVaultKey("vinny", salt, iterations);
        VaultObject vaultObject = SecurityUtils.createVault(localVaultData, VSK, salt, iterations);

        Thread.sleep(1000);
        SecurityUtils.saveVault(store, vaultObject);
        vaultObject = SecurityUtils.readVault(store);
        VaultSecureData key = new VaultSecureData("vinny", vaultObject);
        SecurityUtils.openVault(vaultObject, key, LocalVaultData.class);
    }
}
