/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import me.digi.sdk.core.CAContract;
import me.digi.sdk.core.CASession;

public class CASessionManager implements SessionManager<CASession> {

    private final ConcurrentHashMap<String, CASession> sessions;
    private final AtomicReference<CASession> currentSessionRef;
    private static final String MANAGER_NAME = "defaultCASessionManager";

    public final ListenerDispatch dispatch = new ListenerDispatch();

    public CASessionManager() {
        this.sessions = new ConcurrentHashMap<String, CASession>(1);
        this.currentSessionRef = new AtomicReference<>();
    }

    @Override
    public String getManagerName() {
        return MANAGER_NAME;
    }

    @Override
    public CASession getSession(String sessionId) {
        if (sessionId == null) {
            return null;
        }
        final CASession sess = sessions.get(sessionId);
        if (sess == null) {
            return null;
        } else {
            return sess;
        }
    }

    @Override
    public void addListener(SessionListener listener) {
        this.dispatch.addSessionListener(listener);
    }

    @Override
    public void removeListener(SessionListener listener) {
        this.dispatch.removeSessionListener(listener);
    }

    @Override
    public CASession getCurrentSession() {
        return currentSessionRef.get();
    }

    @Override
    public void setCurrentSession(CASession session) {
        if (session == null) {
            throw new IllegalArgumentException("Must set a non-null session!");
        }
        sessions.put(session.getId(), session);

        final CASession activeSession = currentSessionRef.get();
        synchronized (this) {
            currentSessionRef.compareAndSet(activeSession, session);
        }
    }

    @Override
    public void clearCurrentSession() {
        if (currentSessionRef.get() != null) {
            invalidateSession(currentSessionRef.get().getId());
        }
    }

    @Override
    public void setSession(String id, CASession session) {
        if (session == null) {
            throw new IllegalArgumentException("Must set a non-null session!");
        }
        sessions.put(session.getId(), session);

        final CASession activeSession = currentSessionRef.get();
        if (activeSession == null || activeSession.getId() == session.getId()) {
            synchronized (this) {
                currentSessionRef.compareAndSet(activeSession, session);
            }
        }
    }

    @Override
    public CASession invalidateSession(String id) {
        if (currentSessionRef.get() != null && currentSessionRef.get().getId() == id) {
            synchronized (this) {
                currentSessionRef.set(null);
            }
        }

        return sessions.remove(id);
    }

    @Override
    public Map<String, CASession> getSessions() {
        return Collections.unmodifiableMap(sessions);
    }
}
