package com.sharelocation.android;

public interface INetworkAvailable {
	public void onGpsAvailable(boolean status);
	public void onNetworkAvailable(boolean status);
	public void onStatusReceived(boolean status, double latidue, double longitude);
}
