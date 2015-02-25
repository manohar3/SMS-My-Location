package com.sharelocation.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;

import com.sharelocation.android.R.drawable;

@SuppressLint("NewApi")
public class InitActivity extends Activity implements INetworkAvailable{

	LocationActivity locationActivity;
	boolean gpsAvailable;
	boolean networkAvailable;

	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.init_activity);
		View view = getWindow().getDecorView().getRootView();
		view.setBackgroundResource(R.drawable.animate);
		AnimationDrawable drawable = (AnimationDrawable)view.getBackground();
		drawable.start();
		locationActivity = new LocationActivity(this);
		locationActivity.registerForUpdates();
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		locationActivity.unregiserForUpdates();
	}

	@Override
	public void onGpsAvailable(boolean status) {
		gpsAvailable = status;
		locationActivity.logError(status, true);
	}

	@Override
	public void onNetworkAvailable(boolean status) {
		networkAvailable = status;
		if(gpsAvailable){
			locationActivity.logError(true, status);
		}
	}

	@Override
	public void onStatusReceived(boolean status, double latitude,
			double longitude) {

		Intent i = new Intent(getApplicationContext(), LocationActivity.class);
		i.putExtra("latitude", latitude);
		i.putExtra("longitude", longitude);
		i.putExtra("gpsstatus", gpsAvailable);
		i.putExtra("networkstatus",networkAvailable);
		startActivity(i);
		finish();
	}
}
