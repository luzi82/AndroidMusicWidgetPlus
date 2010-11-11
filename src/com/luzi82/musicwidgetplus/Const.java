package com.luzi82.musicwidgetplus;

import java.util.ArrayList;
import java.util.TreeMap;

import android.os.Build;

public class Const {

	public static final String MEDIAPLAYBACKACTIVITY_CLASSNAME = (Build.VERSION.SDK_INT == 7) ? "com.android.music.MediaPlaybackActivityStarter"
			: "com.android.music.MediaPlaybackActivity";

	public static final ArrayList<String> mKeyList = new ArrayList<String>();
	public static final TreeMap<String, ThemeEntry> mKeyToThemeEntry = new TreeMap<String, ThemeEntry>();
	static {
		addThemeEntry("ECLAIR", R.string.mwp_widgetname_eclair,
				R.layout.eclair_album_appwidget,
				R.drawable.eclair_ic_appwidget_music_play,
				R.drawable.eclair_ic_appwidget_music_pause);
		addThemeEntry("ECLAIR_50", R.string.mwp_widgetname_eclair_50,
				R.layout.eclair_50_album_appwidget,
				R.drawable.eclair_ic_appwidget_music_play,
				R.drawable.eclair_ic_appwidget_music_pause);
		addThemeEntry("ECLAIR_00", R.string.mwp_widgetname_eclair_00,
				R.layout.eclair_00_album_appwidget,
				R.drawable.eclair_ic_appwidget_music_play,
				R.drawable.eclair_ic_appwidget_music_pause);
		addThemeEntry("DONUT", R.string.mwp_widgetname_donut,
				R.layout.donut_album_appwidget,
				R.drawable.donut_appwidget_play,
				R.drawable.donut_appwidget_pause);
		addThemeEntry("DONUT_50", R.string.mwp_widgetname_donut_50,
				R.layout.donut_50_album_appwidget,
				R.drawable.donut_appwidget_play,
				R.drawable.donut_appwidget_pause);
		addThemeEntry("DONUT_00", R.string.mwp_widgetname_donut_00,
				R.layout.donut_00_album_appwidget,
				R.drawable.donut_appwidget_play,
				R.drawable.donut_appwidget_pause);
	}

//	public static final String DEFAULT_KEY = "DONUT";

	private static void addThemeEntry(String key, int nameId, int layoutId,
			int playButtonId, int pauseButtonId) {
		mKeyToThemeEntry.put(key, new ThemeEntry(key, nameId, layoutId,
				playButtonId, pauseButtonId));
		mKeyList.add(key);
	}

	public static class ThemeEntry {
		final String key;
		final int nameId;
		final int layoutId;
		final int playButtonId;
		final int pauseButtonId;

		ThemeEntry(String key, int nameId, int layoutId, int playButtonId,
				int pauseButtonId) {
			this.key = key;
			this.nameId = nameId;
			this.layoutId = layoutId;
			this.playButtonId = playButtonId;
			this.pauseButtonId = pauseButtonId;
		}
	}

}
