/*
* Copyright (C) 2015 InfinitiveOS Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.android.mms.data.io;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;
import com.android.mms.ui.MessagingPreferenceActivity;

public class IoConversationSettings {
    private static final String TAG = "IoConversationSettings";

    private Context mContext;
    /* package */
    long mThreadId;
    int mNotificationEnabled;
    String mNotificationTone;
    int mVibrateEnabled;
    String mVibratePattern;

    private static final int DEFAULT_NOTIFICATION_ENABLED = IoMmsDatabaseHelper.DEFAULT;
    private static final String DEFAULT_NOTIFICATION_TONE = "";
    private static final int DEFAULT_VIBRATE_ENABLED = IoMmsDatabaseHelper.DEFAULT;
    private static final String DEFAULT_VIBRATE_PATTERN = "";

    private IoConversationSettings(Context context, long threadId, int notificationEnabled,
        String notificationTone, int vibrateEnabled, String vibratePattern) {
        mContext = context;
        mThreadId = threadId;
        mNotificationEnabled = notificationEnabled;
        mNotificationTone = notificationTone;
        mVibrateEnabled = vibrateEnabled;
        mVibratePattern = vibratePattern;
    }

    public static IoConversationSettings getOrNew(Context context, long threadId) {
        IoMmsDatabaseHelper helper = IoMmsDatabaseHelper.getInstance(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(IoMmsDatabaseHelper.CONVERSATIONS_TABLE,
            IoMmsDatabaseHelper.CONVERSATIONS_COLUMNS,
            " thread_id = ?",
            new String[] { String.valueOf(threadId) },
            null, null, null, null);

        // we should only have one result
        int count = cursor.getCount();
        IoConversationSettings convSetting;
        if (cursor != null && count == 1) {
            cursor.moveToFirst();
            convSetting = new IoConversationSettings(context,
                threadId,
                cursor.getInt(1),
                cursor.getString(2),
                cursor.getInt(3),
                cursor.getString(4)
            );
        } else if (count > 1) {
            Log.wtf(TAG, "More than one settings with the same thread id is returned!");
            return null;
        } else {
            convSetting = new IoConversationSettings(context, threadId,
                DEFAULT_NOTIFICATION_ENABLED, DEFAULT_NOTIFICATION_TONE,
                DEFAULT_VIBRATE_ENABLED, DEFAULT_VIBRATE_PATTERN);

            helper.insertIoConversationSettings(convSetting);
        }

        return convSetting;
    }

    public static void delete(Context context, long threadId) {
        IoMmsDatabaseHelper helper = IoMmsDatabaseHelper.getInstance(context);
        helper.deleteIoConversationSettings(threadId);
    }

    public static void deleteAll(Context context) {
        IoMmsDatabaseHelper helper = IoMmsDatabaseHelper.getInstance(context);
        helper.deleteAllIoConversationSettings();
    }

    public long getThreadId() {
        return mThreadId;
    }

    public boolean getNotificationEnabled() {
        if (mNotificationEnabled == IoMmsDatabaseHelper.DEFAULT) {
            SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(mContext);
            return sharedPreferences.getBoolean(MessagingPreferenceActivity.NOTIFICATION_ENABLED,
                DEFAULT_NOTIFICATION_ENABLED == IoMmsDatabaseHelper.TRUE);
        }
        return mNotificationEnabled == IoMmsDatabaseHelper.TRUE;
    }

    public void setNotificationEnabled(boolean enabled) {
        mNotificationEnabled = enabled ? IoMmsDatabaseHelper.TRUE : IoMmsDatabaseHelper.FALSE;
        setNotificationEnabled(mNotificationEnabled);
    }

    public void setNotificationEnabled(int enabled) {
        IoMmsDatabaseHelper helper = IoMmsDatabaseHelper.getInstance(mContext);
        helper.updateIoConversationSettingsField(mThreadId,
            IoMmsDatabaseHelper.CONVERSATIONS_NOTIFICATION_ENABLED, enabled);
    }

    public String getNotificationTone() {
        if (mNotificationTone.equals("")) {
            SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(mContext);
            return sharedPreferences.getString(MessagingPreferenceActivity.NOTIFICATION_RINGTONE,
                null);
        }
        return mNotificationTone;
    }

    public void setNotificationTone(String tone) {
        mNotificationTone = tone;
        IoMmsDatabaseHelper helper = IoMmsDatabaseHelper.getInstance(mContext);
        helper.updateIoConversationSettingsField(mThreadId,
            IoMmsDatabaseHelper.CONVERSATIONS_NOTIFICATION_TONE, tone);
    }

    public boolean getVibrateEnabled() {
        if (mVibrateEnabled == IoMmsDatabaseHelper.DEFAULT) {
            SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(mContext);
            return sharedPreferences.getBoolean(MessagingPreferenceActivity.NOTIFICATION_VIBRATE,
                DEFAULT_VIBRATE_ENABLED == IoMmsDatabaseHelper.TRUE);
        }
        return mVibrateEnabled == IoMmsDatabaseHelper.TRUE;
    }

    public void setVibrateEnabled(boolean enabled) {
        mVibrateEnabled = enabled ? IoMmsDatabaseHelper.TRUE : IoMmsDatabaseHelper.FALSE;
        setVibrateEnabled(mVibrateEnabled);
    }

    public void setVibrateEnabled(int enabled) {
        IoMmsDatabaseHelper helper = IoMmsDatabaseHelper.getInstance(mContext);
        helper.updateIoConversationSettingsField(mThreadId,
            IoMmsDatabaseHelper.CONVERSATIONS_VIBRATE_ENABLED, enabled);
    }

    public String getVibratePattern() {
        if (mVibratePattern.equals("")) {
            SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(mContext);
            return sharedPreferences.getString(MessagingPreferenceActivity.NOTIFICATION_VIBRATE_PATTERN,
                "0,1200");
        }
        return mVibratePattern;
    }

    public void setVibratePattern(String pattern) {
        mVibratePattern = pattern;
        IoMmsDatabaseHelper helper = IoMmsDatabaseHelper.getInstance(mContext);
        helper.updateIoConversationSettingsField(mThreadId,
            IoMmsDatabaseHelper.CONVERSATIONS_VIBRATE_PATTERN, pattern);
    }

    public void resetToDefault() {
        mNotificationEnabled = DEFAULT_NOTIFICATION_ENABLED;
        mNotificationTone = DEFAULT_NOTIFICATION_TONE;
        mVibrateEnabled = DEFAULT_VIBRATE_ENABLED;
        mVibratePattern = DEFAULT_VIBRATE_PATTERN;
        IoMmsDatabaseHelper helper = IoMmsDatabaseHelper.getInstance(mContext);
        helper.updateIoConversationSettings(this);
    }
}
