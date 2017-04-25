/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core.session;

import android.util.Log;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import me.digi.sdk.core.CASession;
import me.digi.sdk.core.DigiMeClient;


public class CASessionDeserializer implements JsonDeserializer<CASession> {
    public CASession deserialize(JsonElement json, Type typeOfT,
                             JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonObject()) {
            return new CASession();
        }

        final JsonObject obj = json.getAsJsonObject();
        String sessionKey = null;
        long expiry = 0;
        try {
            sessionKey = context.deserialize(obj.get("sessionKey"), String.class);
            expiry = context.deserialize(obj.get("expiry"), long.class);
        } catch (Exception ex) {
            throw new JsonParseException("Wrong format retrieved from session object");
        }

        return new CASession(sessionKey, expiry, sessionKey,  (CASessionManager) DigiMeClient.getInstance().getSessionManager());
    }
}
