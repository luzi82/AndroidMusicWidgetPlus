/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.luzi82.musicwidgetplus;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.luzi82.musicwidgetplus.Const.ThemeEntry;

/**
 * Simple widget to show currently playing album art along
 * with play/pause and next track buttons.  
 */
public class MediaAppWidgetProvider extends AppWidgetProvider {
    static final String TAG = "MusicAppWidgetProvider";
    
    public static final String CMDAPPWIDGETUPDATE = "appwidgetupdate";
    
    static final ComponentName THIS_APPWIDGET =
        new ComponentName("com.luzi82.musicwidgetplus",
                "com.luzi82.musicwidgetplus.MediaAppWidgetProvider");
    
    private static MediaAppWidgetProvider sInstance;

    static synchronized MediaAppWidgetProvider getInstance() {
        if (sInstance == null) {
            sInstance = new MediaAppWidgetProvider();
        }
        return sInstance;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    	MediaPlaybackService.d("MediaAppWidgetProvider.onUpdate");
        defaultAppWidget(context, appWidgetIds);
        
        Intent i = new Intent(context,MediaPlaybackService.class);
        context.startService(i);
        
        // Send broadcast intent to any running MediaPlaybackService so it can
        // wrap around with an immediate update.
        Intent updateIntent = new Intent(MediaPlaybackService.SERVICECMDX);
        updateIntent.putExtra(MediaPlaybackService.CMDNAME,
                MediaAppWidgetProvider.CMDAPPWIDGETUPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        updateIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        context.sendBroadcast(updateIntent);
    }
    
    /**
     * Initialize given widgets to default state, where we launch Music on default click
     * and hide actions if service not running.
     */
    private void defaultAppWidget(Context context, int[] appWidgetIds) {
    	MediaPlaybackService.d("MediaAppWidgetProvider.defaultAppWidget");
        final String contextPackageName=context.getPackageName();
        final Resources res = context.getResources();

        for(int appWidgetId:appWidgetIds){
//        System.err.println("appWidgetId "+appWidgetId);
        String key=MediaAppWidgetDatabase.getLayoutKey(context, appWidgetId);
        if(key!=null){
//        System.err.println("defaultAppWidget "+key);
        final RemoteViews views = new RemoteViews(contextPackageName, Const.mKeyToThemeEntry.get(key).layoutId);
        
        views.setViewVisibility(R.id.title, View.GONE);
        views.setTextViewText(R.id.artist, res.getText(R.string.emptyplaylist));

        linkButtons(context, views, false /* not playing */);
        pushUpdate(context, new int[]{appWidgetId}, views);
        }
    	}
    }
    
    private void pushUpdate(Context context, int[] appWidgetIds, RemoteViews views) {
    	MediaPlaybackService.d("MediaAppWidgetProvider.pushUpdate");
        // Update specific list of appWidgetIds if given, otherwise default to all
        final AppWidgetManager gm = AppWidgetManager.getInstance(context);
        if (appWidgetIds != null) {
            gm.updateAppWidget(appWidgetIds, views);
        } else {
            gm.updateAppWidget(THIS_APPWIDGET, views);
        }
    }
    
    /**
     * Check against {@link AppWidgetManager} if there are any instances of this widget.
     */
    private boolean hasInstances(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(THIS_APPWIDGET);
        return (appWidgetIds.length > 0);
    }

    /**
     * Handle a change notification coming over from {@link MediaPlaybackService}
     */
    void notifyChange(MediaPlaybackService service, String what) {
        if (hasInstances(service)) {
            if (MediaPlaybackService.PLAYBACK_COMPLETE.equals(what) ||
                    MediaPlaybackService.META_CHANGED.equals(what) ||
                    MediaPlaybackService.PLAYSTATE_CHANGED.equals(what)) {
                performUpdate(service, null);
            }
            else if (MediaPlaybackService.SERVICE_CONNECTED.equals(what)) {
                performUpdate(service, null);
            }
        }
    }
    
    /**
     * Update all active widget instances by pushing changes 
     */
    void performUpdate(MediaPlaybackService service, int[] appWidgetIds) {
        final Resources res = service.getResources();
        CharSequence titleName = service.getTrackName();
        CharSequence artistName = service.getArtistName();
        CharSequence errorState = null;
        String status = Environment.getExternalStorageState();
        final boolean playing = service.isPlaying();

        final String servicePackageName=service.getPackageName();

        final AppWidgetManager gm = AppWidgetManager.getInstance(service);
    	if(appWidgetIds==null){
    		appWidgetIds=gm.getAppWidgetIds(THIS_APPWIDGET);
    	}
        
        for(int appWidgetId:appWidgetIds){
//    	System.err.println("appWidgetId "+appWidgetId);
        String key=MediaAppWidgetDatabase.getLayoutKey(service, appWidgetId);
        if(key!=null){
//        System.err.println("performUpdate "+key);
        ThemeEntry themeEntry=Const.mKeyToThemeEntry.get(key);
        final RemoteViews views = new RemoteViews(servicePackageName, themeEntry.layoutId);
        
        // Format title string with track number, or show SD card message
        if (service.playerCompatible()==-1){
            errorState = res.getText(R.string.warning_nodefaultplayer);
        } else if (status.equals(Environment.MEDIA_SHARED) ||
                status.equals(Environment.MEDIA_UNMOUNTED)) {
            errorState = res.getText(R.string.sdcard_busy_title);
        } else if (status.equals(Environment.MEDIA_REMOVED)) {
            errorState = res.getText(R.string.sdcard_missing_title);
        } else if (titleName == null) {
            errorState = res.getText(R.string.emptyplaylist);
        }
        
        if (errorState != null) {
            // Show error state to user
            views.setViewVisibility(R.id.title, View.GONE);
            views.setTextViewText(R.id.artist, errorState);
            
        } else {
            // No error, so show normal titles
            views.setViewVisibility(R.id.title, View.VISIBLE);
            views.setTextViewText(R.id.title, titleName);
            views.setTextViewText(R.id.artist, artistName);
        }
        
        // Set correct drawable for pause state
        if (playing) {
            views.setImageViewResource(R.id.control_play, themeEntry.pauseButtonId);
        } else {
            views.setImageViewResource(R.id.control_play, themeEntry.playButtonId);
        }

        // Link actions buttons to intents
        linkButtons(service, views, playing);
        
        pushUpdate(service, new int[]{appWidgetId}, views);
        }
        }
    }

    /**
     * Link up various button actions using {@link PendingIntents}.
     * 
     * @param playerActive True if player is active in background, which means
     *            widget click will launch {@link MediaPlaybackActivityStarter},
     *            otherwise we launch {@link MusicBrowserActivity}.
     */
    private void linkButtons(Context context, RemoteViews views, boolean playerActive) {
    	MediaPlaybackService.d("linkButtons");
    	
        // Connect up various buttons and touch events
        Intent intent;
        PendingIntent pendingIntent;
        
        final ComponentName serviceName = new ComponentName("com.android.music", "com.android.music.MediaPlaybackService");
        
        if (playerActive) {
//            intent = new Intent(context, MediaPlaybackActivityStarter.class);
			intent = new Intent();
			intent.setClassName("com.android.music", Const.MEDIAPLAYBACKACTIVITY_CLASSNAME);
            pendingIntent = PendingIntent.getActivity(context,
                    0 /* no requestCode */, intent, 0 /* no flags */);
            views.setOnClickPendingIntent(R.id.album_appwidget, pendingIntent);
        } else {
//            intent = new Intent(context, MusicBrowserActivity.class);
			intent = new Intent();
			intent.setClassName("com.android.music", "com.android.music.MusicBrowserActivity");
            pendingIntent = PendingIntent.getActivity(context,
                    0 /* no requestCode */, intent, 0 /* no flags */);
            views.setOnClickPendingIntent(R.id.album_appwidget, pendingIntent);
        }
        
        intent = new Intent(MediaPlaybackService.TOGGLEPAUSE_ACTION);
        intent.setComponent(serviceName);
        pendingIntent = PendingIntent.getService(context,
                0 /* no requestCode */, intent, 0 /* no flags */);
        views.setOnClickPendingIntent(R.id.control_play, pendingIntent);
        
        intent = new Intent(MediaPlaybackService.NEXT_ACTION);
        intent.setComponent(serviceName);
        pendingIntent = PendingIntent.getService(context,
                0 /* no requestCode */, intent, 0 /* no flags */);
        views.setOnClickPendingIntent(R.id.control_next, pendingIntent);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.d(TAG, "onDeleted");
        // When the user deletes the widget, delete the preference associated with it.
        final int N = appWidgetIds.length;
        for (int i=0; i<N; i++) {
            MediaAppWidgetDatabase.delLayoutKey(context, appWidgetIds[i]);
        }
    }
    
//    private void delWidget(Context context,int appWidgetId){
//    	AppWidgetHost awh = new AppWidgetHost(context, 1);
//		awh.deleteAppWidgetId(appWidgetId);
//    }

}
