package com.luzi82.musicwidgetplus;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;

public class AddWidgetActivity extends Activity {

	private int mAppWidgetId;
	private FinishType mFinishType = FinishType.NO_ID;
	private AppWidgetHost awh = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// setContentView(R.layout.empty);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras == null) {
			finish();
		} else {
			// mFinishType = FinishType.BAD;
			updateResult(FinishType.BAD);
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
			System.err.println("onCreate " + mAppWidgetId);

			showDialog(0);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		ArrayList<String> widgetTypeList = new ArrayList<String>();
		for (String key : Const.mKeyList) {
			widgetTypeList
					.add(getString(Const.mKeyToThemeEntry.get(key).nameId));
		}

		AlertDialog.Builder ab = new Builder(this);
		ab.setTitle(R.string.mwp_app_name);
		ab.setItems(widgetTypeList.toArray(new String[0]),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String key = Const.mKeyList.get(which);
						System.err.println("user select " + key);
						MediaAppWidgetDatabase.saveTitlePref(
								AddWidgetActivity.this, mAppWidgetId, key);

						Intent updateIntent = new Intent(
								MediaPlaybackService.SERVICECMDX);
						updateIntent.putExtra(MediaPlaybackService.CMDNAME,
								MediaAppWidgetProvider.CMDAPPWIDGETUPDATE);
						updateIntent.putExtra(
								AppWidgetManager.EXTRA_APPWIDGET_IDS,
								new int[] { mAppWidgetId });
						updateIntent
								.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
						AddWidgetActivity.this.sendBroadcast(updateIntent);

						// Intent resultValue = new Intent();
						// resultValue.putExtra(
						// AppWidgetManager.EXTRA_APPWIDGET_ID,
						// mAppWidgetId);
						// AddWidgetActivity.this
						// .setResult(RESULT_OK, resultValue);
						// mFinishType = FinishType.GOOD;
						updateResult(FinishType.GOOD);
						AddWidgetActivity.this.finish();
					}
				});
		ab.setCancelable(true);
		ab.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				System.err.println("dialog onCancel");
				AddWidgetActivity.this.finish();
				// Intent resultValue = new Intent();
				// resultValue.putExtra(
				// AppWidgetManager.EXTRA_APPWIDGET_ID,
				// mAppWidgetId);
				// AddWidgetActivity.this.setResult(RESULT_CANCELED,
				// resultValue);
				// AddWidgetActivity.this.finish();
			}
		});
		return ab.create();
	}

	@Override
	protected void onPause() {
		System.err.println("AddWidgetActivity onPause");
		// if(!mIntentSent){
		// System.err.println("AddWidgetActivity set result");
		// mIntentSent=true;
		// Intent resultValue = new Intent();
		// resultValue.putExtra(
		// AppWidgetManager.EXTRA_APPWIDGET_ID,
		// mAppWidgetId);
		// setResult(RESULT_CANCELED, resultValue);
		// }
		// if(!isFinishing()){
		// System.err.println("finish");
		// finish();
		// }
		// System.err.println()
		// finish();

		if (mFinishType == FinishType.BAD) {
			if (awh == null)
				awh = new AppWidgetHost(this, 0);
			awh.deleteAppWidgetId(mAppWidgetId);
		}

		System.err.println("is fin " + isFinishing());
		if (!isFinishing()) {
			finish();
		}

		super.onPause();
	}

	public void updateResult(FinishType finishType) {
		mFinishType = finishType;
		switch (mFinishType) {
		case NO_ID: {
			setResult(RESULT_CANCELED);
			break;
		}
		case BAD: {
			Intent resultValue = new Intent();
			resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					mAppWidgetId);
			setResult(RESULT_CANCELED, resultValue);
			break;
		}
		case GOOD: {
			Intent resultValue = new Intent();
			resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					mAppWidgetId);
			setResult(RESULT_OK, resultValue);
			break;
		}
		}
	}

	// @Override
	// public void finish() {
	// System.err.println("AddWidgetActivity.finish "+mFinishType);
	// switch (mFinishType) {
	// case NO_ID: {
	// setResult(RESULT_CANCELED);
	// break;
	// }
	// case BAD: {
	// if(awh==null)
	// awh=new AppWidgetHost(this, 0);
	// awh.deleteAppWidgetId(mAppWidgetId);
	// Intent resultValue = new Intent();
	// resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
	// mAppWidgetId);
	// setResult(RESULT_CANCELED, resultValue);
	// break;
	// }
	// case GOOD: {
	// Intent resultValue = new Intent();
	// resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
	// mAppWidgetId);
	// setResult(RESULT_OK, resultValue);
	// break;
	// }
	// }
	// super.finish();
	// }

	enum FinishType {
		NO_ID, GOOD, BAD
	}

}
