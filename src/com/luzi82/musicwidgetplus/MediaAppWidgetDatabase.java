package com.luzi82.musicwidgetplus;

import android.content.Context;
import android.content.SharedPreferences;

public class MediaAppWidgetDatabase {

	private static final String PREFS_NAME = "com.luzi82.musicwidgetplus.MediaAppWidgetDatabase";
	private static final String PREF_LAYOUTKEY_FORMAT = "widget_%d_layout";

	static synchronized void setLayoutKey(Context context, int appWidgetId,
			String text) {
		SharedPreferences.Editor prefs = context.getSharedPreferences(
				PREFS_NAME, 0).edit();
		prefs.putString(layoutKey(appWidgetId), text);
		prefs.commit();
	}

	static synchronized String getLayoutKey(Context context, int appWidgetId) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		return prefs.getString(layoutKey(appWidgetId), null);
	}

	static synchronized void delLayoutKey(Context context, int appWidgetId) {
		SharedPreferences.Editor prefs = context.getSharedPreferences(
				PREFS_NAME, 0).edit();
		prefs.remove(layoutKey(appWidgetId));
		prefs.commit();
	}

	static String layoutKey(int appWidgetId) {
		String out= String.format(PREF_LAYOUTKEY_FORMAT, appWidgetId);
//		System.err.println("layoutKey "+out);
		return out;
	}

}
