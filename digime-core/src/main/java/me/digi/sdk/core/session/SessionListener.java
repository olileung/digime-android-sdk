/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core.session;

public interface SessionListener {

    void sessionCreated(final Session session);

    void sessionDestroyed(final Session session, DestroyedReason reason);

    void currentSessionChanged(final Session oldSession, final Session newSession);

    enum DestroyedReason {
        TIMEOUT,
        INVALIDATED
    }
}