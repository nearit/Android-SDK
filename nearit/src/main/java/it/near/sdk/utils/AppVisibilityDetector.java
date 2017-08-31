/*
 * Copyright (C) zyc945@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.near.sdk.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;


import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import it.near.sdk.logging.NearLog;

/**
 * Author: zyc945@gmail.com
 * Date  : 05/02/2017.
 */

public final class AppVisibilityDetector {
    private static boolean DEBUG = false;
    private static final String TAG = "AppVisibilityDetector";
    private static CopyOnWriteArrayList<AppVisibilityCallback> sAppVisibilityCallbackList = new CopyOnWriteArrayList<>();
    public static boolean sIsForeground = false;
    private static Handler sHandler;
    private static final int MSG_GOTO_FOREGROUND = 1;
    private static final int MSG_GOTO_BACKGROUND = 2;

    public static void init(final Context app, AppVisibilityCallback appVisibilityCallback) {
        checkIsMainProcess(app);
        sAppVisibilityCallbackList.add(appVisibilityCallback);
        ((Application)app.getApplicationContext()).registerActivityLifecycleCallbacks(new AppActivityLifecycleCallbacks());

        sHandler = new Handler(app.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_GOTO_FOREGROUND:
                        if (DEBUG) {
                            NearLog.d(TAG, "handleMessage(MSG_GOTO_FOREGROUND)");
                        }
                        performAppGotoForeground();
                        break;
                    case MSG_GOTO_BACKGROUND:
                        if (DEBUG) {
                            NearLog.d(TAG, "handleMessage(MSG_GOTO_BACKGROUND)");
                        }
                        performAppGotoBackground();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    public static void addVisibilityCallback(AppVisibilityCallback callback) {
        sAppVisibilityCallbackList.add(callback);
    }

    public static void removeVisibilityCallback(AppVisibilityCallback callback) {
        sAppVisibilityCallbackList.remove(callback);
    }

    private static void checkIsMainProcess(Context app) {
        ActivityManager activityManager = (ActivityManager) app.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> runningAppProcessInfoList = activityManager.getRunningAppProcesses();
        String currProcessName = null;
        int currPid = android.os.Process.myPid();
        //find the process name
        for (RunningAppProcessInfo processInfo : runningAppProcessInfoList) {
            if (processInfo.pid == currPid) {
                currProcessName = processInfo.processName;
            }
        }

        //is current process the main process
        if (!TextUtils.equals(currProcessName, app.getPackageName())) {
            throw new IllegalStateException("make sure BgDetector.init(...) called in main process");
        }
    }

    private static void performAppGotoForeground() {
        if (!sIsForeground && null != sAppVisibilityCallbackList) {
            sIsForeground = true;
            for (AppVisibilityCallback callback : sAppVisibilityCallbackList) {
                callback.onAppGotoForeground();
            }
        }
    }

    private static void performAppGotoBackground() {
        if (sIsForeground && null != sAppVisibilityCallbackList) {
            sIsForeground = false;
            for (AppVisibilityCallback callback : sAppVisibilityCallbackList) {
                callback.onAppGotoBackground();
            }
        }
    }

    public interface AppVisibilityCallback {
        void onAppGotoForeground();

        void onAppGotoBackground();
    }

    private static class AppActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {
        int activityDisplayCount = 0;

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            if (DEBUG) {
                NearLog.d(TAG, activity.getClass().getName() + " onActivityCreated");
            }
        }

        @Override
        public void onActivityStarted(Activity activity) {
            sHandler.removeMessages(MSG_GOTO_FOREGROUND);
            sHandler.removeMessages(MSG_GOTO_BACKGROUND);
            if (activityDisplayCount == 0) {
                sHandler.sendEmptyMessage(MSG_GOTO_FOREGROUND);
            }
            activityDisplayCount++;

            if (DEBUG) {
                NearLog.d(TAG, activity.getClass().getName() + " onActivityStarted "
                        + " activityDisplayCount: " + activityDisplayCount);
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {
            if (DEBUG) {
                NearLog.d(TAG, activity.getClass().getName() + " onActivityResumed");
            }
        }

        @Override
        public void onActivityPaused(Activity activity) {
            if (DEBUG) {
                NearLog.d(TAG, activity.getClass().getName() + " onActivityPaused");
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            if (DEBUG) {
                NearLog.d(TAG, activity.getClass().getName() + " onActivitySaveInstanceState");
            }
        }

        @Override
        public void onActivityStopped(Activity activity) {
            sHandler.removeMessages(MSG_GOTO_FOREGROUND);
            sHandler.removeMessages(MSG_GOTO_BACKGROUND);
            if (activityDisplayCount > 0) {
                activityDisplayCount--;
            }

            if (activityDisplayCount == 0) {
                sHandler.sendEmptyMessage(MSG_GOTO_BACKGROUND);
            }

            if (DEBUG) {
                NearLog.d(TAG, activity.getClass().getName() + " onActivityStopped "
                        + " activityDisplayCount: " + activityDisplayCount);
            }
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            if (DEBUG) {
                NearLog.d(TAG, activity.getClass().getName() + " onActivityDestroyed");
            }
        }
    }
}

