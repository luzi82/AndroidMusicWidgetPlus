package com.luzi82.musicwidgetplus;

import java.lang.ref.WeakReference;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class MediaPlaybackService extends Service {

	static String LOG_TAG = "LZ_MediaPlaybackService";

	// public static final int NOW = 1;
	// public static final int NEXT = 2;
	// public static final int LAST = 3;
	// public static final int PLAYBACKSERVICE_STATUS = 1;

	// public static final int SHUFFLE_NONE = 0;
	// public static final int SHUFFLE_NORMAL = 1;
	// public static final int SHUFFLE_AUTO = 2;

	// public static final int REPEAT_NONE = 0;
	// public static final int REPEAT_CURRENT = 1;
	// public static final int REPEAT_ALL = 2;

	public static final String PLAYSTATE_CHANGED = "com.android.music.playstatechanged";
	public static final String META_CHANGED = "com.android.music.metachanged";
	// public static final String QUEUE_CHANGED =
	// "com.android.music.queuechanged";
	public static final String PLAYBACK_COMPLETE = "com.android.music.playbackcomplete";
	// public static final String ASYNC_OPEN_COMPLETE =
	// "com.android.music.asyncopencomplete";

	public static final String SERVICECMD = "com.android.music.musicservicecommand";
	public static final String CMDNAME = "command";
	public static final String CMDTOGGLEPAUSE = "togglepause";
	public static final String CMDSTOP = "stop";
	public static final String CMDPAUSE = "pause";
	public static final String CMDPREVIOUS = "previous";
	public static final String CMDNEXT = "next";

	public static final String TOGGLEPAUSE_ACTION = "com.android.music.musicservicecommand.togglepause";
	public static final String PAUSE_ACTION = "com.android.music.musicservicecommand.pause";
	public static final String PREVIOUS_ACTION = "com.android.music.musicservicecommand.previous";
	public static final String NEXT_ACTION = "com.android.music.musicservicecommand.next";

	public static final String SERVICECMDX = "com.luzi82.musicwidgetplus.musicservicecommand";
	public static final String SERVICE_CONNECTED = "com.luzi82.musicwidgetplus.serviceconnected";

	private MediaAppWidgetProvider mAppWidgetProvider = MediaAppWidgetProvider
			.getInstance();

	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			d("MediaPlaybackService mIntentReceiver onReceive");
			String action = intent.getAction();
			String cmd = intent.getStringExtra("command");
			if(SERVICECMDX.equals(action)){
				if (MediaAppWidgetProvider.CMDAPPWIDGETUPDATE.equals(cmd)) {
					// Someone asked us to refresh a set of specific widgets,
					// probably
					// because they were just added.
					int[] appWidgetIds = intent
							.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
					mAppWidgetProvider.performUpdate(MediaPlaybackService.this,
							appWidgetIds);
				}
			}
			else if(PLAYBACK_COMPLETE.equals(action)||META_CHANGED.equals(action)||PLAYSTATE_CHANGED.equals(action)){
				mAppWidgetProvider.notifyChange(MediaPlaybackService.this, action);
			}
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();

		d("onCreate");

		doBindService();

		IntentFilter commandFilter = new IntentFilter();
		commandFilter.addAction(SERVICECMDX);
		commandFilter.addAction(PLAYBACK_COMPLETE);
		commandFilter.addAction(META_CHANGED);
		commandFilter.addAction(PLAYSTATE_CHANGED);
		registerReceiver(mIntentReceiver, commandFilter);
	}

	@Override
	public void onDestroy() {
		d("onDestroy");

		unregisterReceiver(mIntentReceiver);

		doUnbindService();

		super.onDestroy();
	}

//	@Override
//	public int onStartCommand(Intent intent, int flags, int startId) {
//		d("onStartCommand");
//
//		// mServiceStartId = startId;
//		// mDelayedStopHandler.removeCallbacksAndMessages(null);
//
//		if (intent != null) {
//			String action = intent.getAction();
//			String cmd = intent.getStringExtra("command");
//
//			if (CMDNEXT.equals(cmd) || NEXT_ACTION.equals(action)) {
//				next(true);
//			} else if (CMDPREVIOUS.equals(cmd)
//					|| PREVIOUS_ACTION.equals(action)) {
//				if (position() < 2000) {
//					prev();
//				} else {
//					seek(0);
//					play();
//				}
//			} else if (CMDTOGGLEPAUSE.equals(cmd)
//					|| TOGGLEPAUSE_ACTION.equals(action)) {
//				if (isPlaying()) {
//					pause();
//				} else {
//					play();
//				}
//			} else if (CMDPAUSE.equals(cmd) || PAUSE_ACTION.equals(action)) {
//				pause();
//			} else if (CMDSTOP.equals(cmd)) {
//				pause();
//				seek(0);
//			}
//		}
//		// // make sure the service will shut down on its own if it was
//		// // just started but not bound to and nothing is playing
//		// mDelayedStopHandler.removeCallbacksAndMessages(null);
//		// Message msg = mDelayedStopHandler.obtainMessage();
//		// mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
//		return START_STICKY;
//	}

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	public String getTrackName() {
		try {
			if (mIsBound && (mBoundService != null)) {
				return mBoundService.getTrackName();
			}
		} catch (RemoteException e) {
		}
		return null;
	}

	public String getArtistName() {
		try {
			if (mIsBound && (mBoundService != null)) {
				return mBoundService.getArtistName();
			}
		} catch (RemoteException e) {
		}
		return null;
	}

	public boolean isPlaying() {
		try {
			if (mIsBound && (mBoundService != null)) {
				return mBoundService.isPlaying();
			}
		} catch (RemoteException e) {
		}
		return false;
	}

//	public void prev() {
//		try {
//			if (mIsBound && (mBoundService != null)) {
//				mBoundService.prev();
//			}
//		} catch (RemoteException e) {
//		}
//	}
//
//	public void next(boolean force) {
//		d("next");
//		// force should be true
//		try {
//			if (mIsBound && (mBoundService != null)) {
//				mBoundService.next();
//			}
//		} catch (RemoteException e) {
//		}
//	}
//
//	public void pause() {
//		d("pause");
//		try {
//			if (mIsBound && (mBoundService != null)) {
//				mBoundService.pause();
//			}
//		} catch (RemoteException e) {
//		}
//	}
//
//	public void play() {
//		d("play");
//		try {
//			if (mIsBound && (mBoundService != null)) {
//				mBoundService.play();
//			}
//		} catch (RemoteException e) {
//		}
//	}
//
//	public long seek(long pos) {
//		try {
//			if (mIsBound && (mBoundService != null)) {
//				return mBoundService.seek(pos);
//			}
//		} catch (RemoteException e) {
//		}
//		return -1;
//	}
//
//	public long position() {
//		try {
//			if (mIsBound && (mBoundService != null)) {
//				return mBoundService.position();
//			}
//		} catch (RemoteException e) {
//		}
//		return -1;
//	}

	boolean mIsBound = false;
	private com.android.music.IMediaPlaybackService mBoundService = null;

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			d("onServiceConnected");
			mBoundService = com.android.music.IMediaPlaybackService.Stub
					.asInterface(service);
			mAppWidgetProvider.notifyChange(MediaPlaybackService.this, SERVICE_CONNECTED);
		}

		public void onServiceDisconnected(ComponentName className) {
			d("onServiceDisconnected");
			mBoundService = null;
		}
	};

	private void doBindService() {
		Intent i = new Intent();
		i.setClassName("com.android.music",
				"com.android.music.MediaPlaybackService");
//		startService(i);
		bindService(i, mConnection, BIND_AUTO_CREATE);
		mIsBound = true;
	}

	private void doUnbindService() {
		if (mIsBound) {
			unbindService(mConnection);
			mIsBound = false;
		}
	}

	static class ServiceStub extends IMediaPlaybackService.Stub {
		WeakReference<MediaPlaybackService> mService;

		ServiceStub(MediaPlaybackService service) {
			mService = new WeakReference<MediaPlaybackService>(service);
		}
	}

	private final IBinder mBinder = new ServiceStub(this);

	static int d(String msg) {
		return Log.d(LOG_TAG, msg);
	}

}
