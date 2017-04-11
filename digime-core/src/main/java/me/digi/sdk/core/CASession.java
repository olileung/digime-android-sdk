package me.digi.sdk.core;

import com.google.gson.annotations.SerializedName;

public class CASession {

    public static final long DEFAULT_EXPIRY = 60000;

    @SerializedName("sessionKey")
    public String sessionKey;

    @SerializedName("expiry")
    public long expiry;

    public CASession(String sessionKey) {
        this(sessionKey, System.currentTimeMillis() + DEFAULT_EXPIRY);
    }

    public CASession(String sessionKey, long expiry) {
        if (sessionKey == null) {
            throw new IllegalArgumentException("Valid session key must be provided.");
        }
        this.sessionKey = sessionKey;
        this.expiry = expiry;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= expiry;
    }

    public String getSessionKey() { return sessionKey; }
    public long getExpiry() { return expiry; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        final CASession session = (CASession) obj;
        return sessionKey != null ? sessionKey.equals(session.sessionKey) : session.sessionKey == null;
    }

    @Override
    public int hashCode() {
        return sessionKey != null ? sessionKey.hashCode() : 0;
    }
}
