/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.examples.ca_no_sdk.service;

import android.os.AsyncTask;
import android.support.annotation.Nullable;

import me.digi.examples.ca_no_sdk.service.models.DataGetResponse;

import retrofit2.Response;

public class GetUserDataTask extends AsyncTask<GetUserDataTask.GetUserDataTaskParams, Void, Response<DataGetResponse>> {
    private GetUserDataTask.Listener listener;

    @Override
    protected Response<DataGetResponse> doInBackground(GetUserDataTask.GetUserDataTaskParams... getUserDataTaskParams) {
        GetUserDataTask.GetUserDataTaskParams params = getUserDataTaskParams[0];
        this.listener = params.getListener();
        try {
            if (params.fileName == null) {
                return params.getPermissionService().listDataFiles(params.sessionKey).execute();
            } else {
                return params.getPermissionService().getDataFile(params.sessionKey, params.fileName).execute();
            }
        } catch (Exception e) {
            if (listener != null) {
                listener.userDataTaskFailed(e);
            }
            cancel(true);
            return null;
        }
    }

    @Override
    protected void onPostExecute(Response<DataGetResponse> response) {
        if (listener != null) {
            listener.userDataTaskComplete(response);
        }
    }

    public interface Listener {
        void userDataTaskComplete(Response<DataGetResponse> response);
        void userDataTaskFailed(Exception e);
    }

    public static class GetUserDataTaskParams {
        private PermissionService permissionService;
        private String sessionKey;
        private String fileName;
        private GetUserDataTask.Listener listener;

        public GetUserDataTaskParams(PermissionService permissionService, String sessionKey, @Nullable String fileName, GetUserDataTask.Listener listener) {
            this.permissionService = permissionService;
            this.sessionKey = sessionKey;
            this.fileName = fileName;
            this.listener = listener;
        }

        public PermissionService getPermissionService() {
            return permissionService;
        }

        public GetUserDataTask.Listener getListener() {
            return listener;
        }
    }
}
