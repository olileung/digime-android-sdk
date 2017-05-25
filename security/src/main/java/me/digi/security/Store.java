/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.security;

import java.io.IOException;

/**
 * Represents store where security data is stored
 */

public interface Store {
    void writeData(byte[] data, String filename);
    byte[] readData(String filename) throws IOException;
}
