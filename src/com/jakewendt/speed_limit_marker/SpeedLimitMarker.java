
package com.jakewendt.speed_limit_marker;

import com.jakewendt.speed_limit_marker.R;
import com.jakewendt.speed_limit_marker.Settings;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Button;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.NameValuePair;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import android.util.TypedValue;
public class SpeedLimitMarker extends Activity implements TextToSpeech.OnInitListener {

	private static final String TAG = "SpeedLimit";
	private TextToSpeech tts;
	static final int TTS_CHECK_CODE = 0;
 
	/* I need to learn the differences between public, private, final, static, etc. */
	private Location bearing_location;
	private float last_bearing = Float.NaN;
	private float distance;
	private LocationManager locationManager;
	public static final String PREFS_NAME = "MyPrefsFile";
	public boolean mSilentMode = false;
	static final private int BACK_ID = Menu.FIRST;
	static final private int SETTINGS_ID = Menu.FIRST + 1;
	public TextView latitudeField;
	public TextView longitudeField;
	public TextView cogField;
	public TextView distanceField;
	public Location last_location;

	/**
	 * void com.jakewendt.wherewasimarker.WhereWasIMarker.onInit(int initStatus)
	 * 
	 * @Override
	 * 
	 * Specified by: onInit(...) in OnInitListener
	 * public abstract void onInit (int status)
	 * Since: API Level 4
	 * Called to signal the completion of the TextToSpeech engine initialization.
	 * 
	 * Parameters
	 * status	SUCCESS or ERROR.
	 */
	@Override
	public void onInit(int initStatus) {
		Log.d(TAG,"onInit");
		if (initStatus == TextToSpeech.SUCCESS) {
			tts.speak( "Program in-ish-e-ated",TextToSpeech.QUEUE_FLUSH, null);
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG,"onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		latitudeField  = (TextView) findViewById(R.id.latitude);
		longitudeField = (TextView) findViewById(R.id.longitude);
		distanceField  = (TextView) findViewById(R.id.distance);
		cogField       = (TextView) findViewById(R.id.cog);

		// Initialize text-to-speech. This is an asynchronous operation.
		// The OnInitListener (second argument) is called after initialization completes.
		tts = new TextToSpeech( this, this );

		// get a hangle on the location manager
		locationManager =(LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// public void requestLocationUpdates (String provider, long minTime, float minDistance, PendingIntent intent)
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,
				0, new LocationUpdateHandler());

		((Button) findViewById(R.id.decrease)).setOnClickListener(mDecreaseListener);
		((Button) findViewById(R.id.increase)).setOnClickListener(mIncreaseListener);
		((Button) findViewById(R.id.mark)).setOnClickListener(mMarkListener);
//		((Button) findViewById(R.id.showlocation)).setOnClickListener(mShowListener);

		update_settings_view(this);
		refresh_speed();

/*
		TextView sl = (TextView) findViewById(R.id.speedlimit);
		Log.d(TAG,"Bottom " + Integer.toString(sl.getBottom()));
		Log.d(TAG,"PaddingBottom " + Integer.toString(sl.getPaddingBottom()));
		Log.d(TAG,"LineHeight " + Integer.toString(sl.getLineHeight()));
		sl.setTextSize(TypedValue.COMPLEX_UNIT_SP,192);
*/
	}

	public void update_settings_view(Context context) {
		Log.d(TAG,"update_settings_view");
		Settings.readSettings(context);
	}

	// this inner class is the intent reciever that recives notifcations
	// from the location provider about position updates, and then redraws
	// the MapView with the new location centered.
	public class LocationUpdateHandler implements LocationListener {
//		@Override
		public void onLocationChanged(Location location) {
			refresh_location(location);
		}
//		@Override
		public void onProviderDisabled(String provider) {}
//		@Override
		public void onProviderEnabled(String provider) {}
//		@Override
		public void onStatusChanged(String provider, int status,
				Bundle extras) {}
	}

	/**
	 * A call-back for when the user presses the show button.
	 */
	OnClickListener mShowListener = new OnClickListener() {
		public void onClick(View v) {
			Log.d(TAG,"mShowListener::onClick");
			LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			Location location = locationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (location != null) {
				refresh_location(location);
			} else {
				latitudeField.setText("GPS not available");
				longitudeField.setText("GPS not available");
				cogField.setText("GPS not available");
				distanceField.setText("GPS not available");
			}
		}
	};

	@Override
	protected void onStop(){
		Log.d(TAG,"onStop");
		tts.speak("Self destruct sequence in-ish-e-ated",TextToSpeech.QUEUE_FLUSH, null);
		super.onStop();
	}

	public void refresh_location(Location location){
		Log.d(TAG,"refresh_location");
		last_location = location;
		latitudeField.setText(Double.toString( location.getLatitude()));
		longitudeField.setText(Double.toString(location.getLongitude()));

//		perhaps save a bearing location every minute to compare to
/*
 * float 	distanceTo(Location dest)
 * Returns the approximate distance in meters between this location and the given location.
 * 
 * bull pucky. At walking speed, distanceTo and bearingTo are just wrong.  
 * 
 * float 	bearingTo(Location dest)
 * Returns the approximate initial bearing in degrees East of true North when traveling 
 * 	along the shortest path between this location and the given location.
 */
		
		if( bearing_location == null ){
			bearing_location = last_location;
		}
		// Returns the approximate distance in meters between this location and the given location.
		distance = bearing_location.distanceTo(last_location);
		distanceField.setText(Float.toString(distance));
//		if( distance > 0.1 ) {
		if( distance > Settings.distance ) {
			last_bearing = bearing_location.bearingTo(last_location);
			cogField.setText(Float.toString(last_bearing));
			bearing_location = last_location;	
//		} else {
//			cogField.setText("---");
		}
//		bearing_location = last_location;	
//		tts.speak("Set location",TextToSpeech.QUEUE_FLUSH, null);
	}

	public void refresh_speed() {
		Log.d(TAG,"refresh_speed");
		((TextView) findViewById(R.id.speedlimit)).setText(Integer.toString(Settings.speed_limit));
		tts.speak( Integer.toString(Settings.speed_limit),
				TextToSpeech.QUEUE_FLUSH, null);
	}

	public void increase_speed() {
		Log.d(TAG,"increase_speed");
		if( Settings.speed_limit < 85 ){
			Settings.speed_limit += 5;
		}
		refresh_speed();
	}

	public void decrease_speed() {
		Log.d(TAG,"decrease_speed");
		if( Settings.speed_limit > 5 ){
			Settings.speed_limit -= 5;
		}
		refresh_speed();
	}

	/**
	 * A call-back for when the user presses the increase button.
	 */
	OnClickListener mIncreaseListener = new OnClickListener() {
		public void onClick(View v) {
			Log.d(TAG,"mIncreaseListener::onClick");
			increase_speed();
		}
	};
	/**
	 * A call-back for when the user presses the decrease button.
	 */
	OnClickListener mDecreaseListener = new OnClickListener() {
		public void onClick(View v) {
			Log.d(TAG,"mDecreaseListener::onClick");
			decrease_speed();
		}
	};
	/**
	 * A call-back for when the user presses the mark button.
	 */
	OnClickListener mMarkListener = new OnClickListener() {
		public void onClick(View v) {
			Log.d(TAG,"mMarkListener::onClick");
			if (last_location == null) {
				tts.speak( "No Location",TextToSpeech.QUEUE_FLUSH, null);
			} else if ( Float.isNaN(last_bearing) ) {
				tts.speak( "No Bearing",TextToSpeech.QUEUE_FLUSH, null);
			} else {
				tts.speak( "Marking Speed " + Integer.toString(Settings.speed_limit),
					TextToSpeech.QUEUE_FLUSH, null);
				try {
					HttpClient httpclient = new DefaultHttpClient();
					List<NameValuePair> formparams = new ArrayList<NameValuePair>();
					formparams.add(new BasicNameValuePair("mlat", 
						Double.toString(last_location.getLatitude())));
					formparams.add(new BasicNameValuePair("mlon", 
						Double.toString(last_location.getLongitude())));
					formparams.add(new BasicNameValuePair("mmph", 
						Integer.toString(Settings.speed_limit)));
	
					formparams.add(new BasicNameValuePair("mtag", Settings.username));
					formparams.add(new BasicNameValuePair("mcog", 
						Double.toString(last_bearing)));
					formparams.add(new BasicNameValuePair("mhours", ""));
					formparams.add(new BasicNameValuePair("memail", Settings.email));
	
					UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
//					HttpPost httppost = new HttpPost("http://www.wikispeedia.org/a/process_submit_bb.php");
					HttpPost httppost = new HttpPost("http://wherewasi.jakewendt.com/markers");
					httppost.setEntity(entity);
					httpclient.execute(httppost);

				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	};

	/**
	 * Called when your activity's options menu needs to be created.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG,"onCreateOptionsMenu");
		super.onCreateOptionsMenu(menu);
		// We are going to create two menus. Note that we assign them
		// unique integer IDs, labels from our string resources, and
		// given them shortcuts.
		menu.add(0, BACK_ID, 0, R.string.back).setShortcut('0', 'b');
		menu.add(0, SETTINGS_ID, 0, R.string.settings).setShortcut('1', 's');
		return true;
	}

	/**
	 * Called right before your activity's option menu is displayed.
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Log.d(TAG,"onPrepareOptionsMenu");
		super.onPrepareOptionsMenu(menu);
		// Before showing the menu, we need to decide whether the clear
		// item is enabled depending on whether there is text to clear.
		//        menu.findItem(CLEAR_ID).setVisible(mEditor.getText().length() > 0);
		return true;
	}

	/**
	 * Called when a menu item is selected.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG,"onOptionsItemSelected");
		switch (item.getItemId()) {
		case BACK_ID:
			finish();
			return true;
		case SETTINGS_ID:
			Intent ssettings = new Intent(this,Settings.class);
			startActivity(ssettings);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}