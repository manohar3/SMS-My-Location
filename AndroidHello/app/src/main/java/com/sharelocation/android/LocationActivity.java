package com.sharelocation.android;

import java.io.IOException;
import java.lang.System;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.internal.is;

import static android.provider.Settings.*;
import static android.provider.Settings.Secure.*;

@SuppressLint("NewApi")
public class LocationActivity extends Activity implements LocationListener, IContactSelected,  com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks{

	EditText txtPhoneNumber;
	Button btnSend;
    ToggleButton aSwitch;
    RelativeLayout contactLayout;
	
	double mLatitude;
	double mLongitude;
	GoogleApiClient mGoogleApiClient;
	int error_code = LocationProvider.AVAILABLE;
	LocationManager  locationManager = null;
	ConnectivityManager connectivityManager = null;
	String locationProvider = LocationManager.GPS_PROVIDER;
	AlertDialog alertDialog = null;
	Map<String,String> nameToNumberMap = new HashMap<String,String>();
	InitActivity initActivity;
	TextView errorView;
    public List<String> names = new ArrayList<String>();
    public List<String> numbers = new ArrayList<String>();

	public LocationActivity()
	{
		
	}

    RelativeLayout mainLayout;
    RelativeLayout bottomlayout;
	public LocationActivity(InitActivity initActivity) {
		this.initActivity = initActivity;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location_activity);
		readcontacts();
		btnSend = (Button) findViewById(R.id.button_send);
        txtPhoneNumber = (EditText) findViewById(R.id.phone_number);
        contactLayout = (RelativeLayout)findViewById(R.id.contact_layout);
        aSwitch = (ToggleButton) findViewById(R.id.select_switch);
        mainLayout = (RelativeLayout)findViewById(R.id.main_layout);
        bottomlayout = (RelativeLayout)findViewById(R.id.bottom_layout);

        if(isAppInstalled("com.whatsapp")) {
            contactLayout.setVisibility(View.INVISIBLE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)bottomlayout.getLayoutParams();
            params.addRule(RelativeLayout.BELOW, R.id.select_switch);
            bottomlayout.setLayoutParams(params); //causes layout update
            btnSend.setEnabled(true);

            aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (!buttonView.isChecked()) {
                        contactLayout.setVisibility(View.INVISIBLE);
                        btnSend.setEnabled(true);

                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)bottomlayout.getLayoutParams();
                        params.addRule(RelativeLayout.BELOW, R.id.select_switch);
                        bottomlayout.setLayoutParams(params); //causes layout update
                    } else {
                        contactLayout.setVisibility(View.VISIBLE);
                        if(txtPhoneNumber.getText().length() == 0) {
                            btnSend.setEnabled(false);
                        }
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)bottomlayout.getLayoutParams();
                        params.addRule(RelativeLayout.BELOW, R.id.contact_layout);
                        bottomlayout.setLayoutParams(params); //causes layout update
                    }
                }
            });
        }
        else
        {
            contactLayout.setVisibility(View.VISIBLE);
            btnSend.setEnabled(false);
            aSwitch.setVisibility(View.INVISIBLE);
        }

	/*	txtPhoneNumber.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(txtPhoneNumber.getText().length() > 0)
				{
					btnSend.setEnabled(true);
				}
				else
				{
					btnSend.setEnabled(false);
				}
				return false;
			}
		});
    */
    txtPhoneNumber.addTextChangedListener(new TextWatcher()
    {
        public void afterTextChanged(Editable s) {
            if(txtPhoneNumber.getText().length() > 0)
            {
                btnSend.setEnabled(true);
            }
            else
            {
                btnSend.setEnabled(false);
            }
         }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if(txtPhoneNumber.getText().length() > 0)
            {
                btnSend.setEnabled(true);
            }
            else
            {
                btnSend.setEnabled(false);
            }
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(txtPhoneNumber.getText().length() > 0)
            {
                btnSend.setEnabled(true);
            }
            else
            {
                btnSend.setEnabled(false);
            }
            }
        });

		ImageView contactSearch = (ImageView) findViewById(R.id.contact_search);
		contactSearch.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				
				AlphaAnimation animation = new AlphaAnimation(0.5f, 1.0f);
				animation.setDuration(1);
				animation.setStartOffset(0);
				animation.setFillAfter(false);
				v.startAnimation(animation);
				
				FragmentTransaction transaction = getFragmentManager().beginTransaction();

				// Replace whatever is in the fragment_container view with this fragment,
				// and add the transaction to the back stack so the user can navigate back
                ContactReader cr = new ContactReader();
                cr.setNames(names);
                cr.setNumbers(numbers);
				transaction.replace(android.R.id.content, cr);
				transaction.addToBackStack(null);

				// Commit the transaction
				transaction.commit();
			}
		});

		btnSend.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
                String mylocation = constuctMessage();
                if(contactLayout.getVisibility() == View.VISIBLE) {
                    sendSMSMessage();
                }
                else {
                   Intent waIntent = new Intent(Intent.ACTION_SEND);
                    waIntent.setType("text/plain");
                    waIntent.setPackage("com.whatsapp");
                    if (waIntent != null) {
                        waIntent.putExtra(Intent.EXTRA_TEXT, mylocation);//
                        startActivity(waIntent);
                    } else {
                        Toast.makeText(initActivity.getApplicationContext(), "WhatsApp not found", Toast.LENGTH_SHORT)
                                .show();
                    }
                }
			}
		});
		
	
		Intent intent = getIntent();
		mLatitude = intent.getDoubleExtra("latitude", 0);
		mLongitude = intent.getDoubleExtra("longitude", 0);
		
		if(intent.getBooleanExtra("gpsstatus",	false) == false){
			errorView = (TextView) findViewById(R.id.error_message);
			errorView.setText("Error: Wifi/Mobile data not enabled..enable it and try again");
			errorView.setBackgroundColor(Color.RED);
			errorView.setTextColor(Color.WHITE);
		}
		else if(intent.getBooleanExtra("networkstatus",	false) == false){
			errorView = (TextView) findViewById(R.id.error_message);
			errorView.setText("Error: Location access not enabled..enable location access and try again");
			errorView.setBackgroundColor(Color.RED);
			errorView.setTextColor(Color.WHITE);
		}
		else
		{
			registerForUpdates();
		}
	}

    private boolean isAppInstalled(String packageName) {
        PackageManager pm = getPackageManager();
        boolean installed = false;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed  ;
    }
	

	public void logError(boolean gpsEnabled, boolean networkEnable )
	{
		if(!gpsEnabled){
			Toast.makeText(initActivity.getApplicationContext(), "Wifi/Mobile data not enabled",
					Toast.LENGTH_LONG).show();
		}
		else if(!networkEnable){
			Toast.makeText(initActivity.getApplicationContext(), "Location access not enabled",
					Toast.LENGTH_LONG).show();
		}
	}

    protected String constuctMessage()
    {
        StringBuffer mylocation = new StringBuffer();
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        try {
            List<Address> addressList = geocoder.getFromLocation(
                    mLatitude, mLongitude, 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i)).append("\n");
                }
                sb.append(address.getLocality()).append("\n");
                sb.append(address.getPostalCode()).append("\n");
                sb.append(address.getCountryName());
                mylocation.append(sb.toString());
            }
        } catch (IOException e) {
        } catch(Throwable e)
        {
            System.out.println(e);
        }

        EditText landMark = (EditText) findViewById(R.id.land_mark);
        String lMark = landMark.getText().toString();
        if(lMark != null && lMark.isEmpty() == false)
        {
            mylocation.append("\n\n").append("LandMark:").append(lMark);
        }

        return mylocation.toString();
    }

	protected void sendSMSMessage() {
		
        String mylocation = constuctMessage();

		try {
			SmsManager smsManager = SmsManager.getDefault();
			
			for(String name:txtPhoneNumber.getText().toString().split(";"))
			{
				String teleNo = nameToNumberMap.get(name);
				if(teleNo == null)
				{
					teleNo = name;
				}
				smsManager.sendTextMessage(teleNo, null, "I am at \n" + mylocation, null, null);
			}
			
			Toast.makeText(initActivity != null ? initActivity.getApplicationContext() : getApplicationContext(), "SMS sent.",
					Toast.LENGTH_LONG).show();
		} catch (Throwable e) {
			Toast.makeText(initActivity.getApplicationContext(),
					"SMS faild, please try again.",
					Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		
	}

	@Override
	public void onConnectionSuspended(int cause) {

	}
	
	private LocationManager getLocationManager()
	{
		if(locationManager == null){
			if(initActivity != null){
				locationManager = (LocationManager )initActivity.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
			}
			else
			{
				locationManager = (LocationManager )getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
			}
		}
		return locationManager;
	}
	
	private boolean hasLocationAccessEnabled()
	{
		return getLocationManager().isProviderEnabled(locationProvider);
	}
	
	private boolean haveNetworkConnection() {
	    boolean haveConnectedWifi = false;
	    boolean haveConnectedMobile = false;

	    ConnectivityManager cm = (ConnectivityManager) initActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo[] netInfo = cm.getAllNetworkInfo();
	    for (NetworkInfo ni : netInfo) {
	        if (ni.getTypeName().equalsIgnoreCase("WIFI"))
	            if (ni.isConnected())
	                haveConnectedWifi = true;
	        if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
	            if (ni.isConnected())
	                haveConnectedMobile = true;
	    }
	    return haveConnectedWifi || haveConnectedMobile;
	}
	
	@Override
	public void onLocationChanged(Location location) {
		mLatitude = location.getLatitude();
		mLongitude = location.getLongitude();
		if(initActivity != null){
		initActivity.onStatusReceived(true, mLatitude, mLongitude);
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onProviderEnabled(String provider) {
	
	}

	@Override
	public void onProviderDisabled(String provider) {

	}
	
	public void registerForUpdates()
	{
		if(initActivity != null){
			initActivity.onGpsAvailable(haveNetworkConnection());
			initActivity.onNetworkAvailable(hasLocationAccessEnabled());
		}
		
		LocationManager  locationManager = getLocationManager();
		locationManager.requestLocationUpdates(locationProvider, 60000, 5, this);
	}
	
	public void unregiserForUpdates()
	{
		locationManager.removeUpdates(this);
	}
	
		
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if(locationManager != null)
		{
			locationManager.removeUpdates(this);
		}
		if(initActivity != null){
			initActivity.finish();
		}
	}

	@Override
	public void onContactSelected(String name, String number) {
		StringBuilder contactNames = new StringBuilder();
		
		if(txtPhoneNumber.getText().toString().length() > 0 )
		{
			if(txtPhoneNumber.getText().toString().charAt(txtPhoneNumber.getText().toString().length() - 1) != ';')
			{
				txtPhoneNumber.setText(txtPhoneNumber.getText().toString() + ";");
			}
		}
		
		contactNames.append(txtPhoneNumber.getText().toString()).append(name).append(";");
		nameToNumberMap.put(name, number);
		txtPhoneNumber.setText(contactNames.toString());
		if(txtPhoneNumber.getText().length() > 0)
		{
			btnSend.setEnabled(true);
		}
	}
	
	public void readcontacts() {
		Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
	
	      String phoneNumber = null;

	      String _ID = ContactsContract.Contacts._ID;
	      String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
	      String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;


	      Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
	      String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
	      String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

	      ContentResolver contentResolver =  getApplicationContext().getContentResolver();
	      Cursor cursor = contentResolver.query(CONTENT_URI, null,null, null, null); 

	      // Loop for every contact in the phone
	      if (cursor.getCount() > 0) {
	          while (cursor.moveToNext()) {
	              String contact_id = cursor.getString(cursor.getColumnIndex( _ID ));
	              String name = cursor.getString(cursor.getColumnIndex( DISPLAY_NAME ));
	              int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex( HAS_PHONE_NUMBER )));
	              if (hasPhoneNumber > 0) {
	                  // Query and loop for every phone number of the contact
	                  Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[] { contact_id }, null);
	                  while (phoneCursor.moveToNext()) {
	                      phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
	                  }
	                  phoneCursor.close();
	                  names.add(name);
	                  numbers.add(phoneNumber);
	              }
	          }	
	      }
	}

}
