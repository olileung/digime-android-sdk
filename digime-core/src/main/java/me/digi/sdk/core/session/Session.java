/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core.session;

public interface Session {

    String getId();

    boolean isValid();

    void requestCompleted();

    long getCreatedTime();

    long getLastAccessedTime();

    void invalidate();

    SessionManager getSessionManager();

    String changeSessionId(final String id);
}