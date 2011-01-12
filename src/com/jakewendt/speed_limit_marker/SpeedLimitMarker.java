
package com.jakewendt.speed_limit_marker;

import com.jakewendt.speed_limit_marker.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Button;
import android.content.SharedPreferences;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

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

public class SpeedLimitMarker extends Activity {

	private LocationManager locationManager;

	public static final String PREFS_NAME = "MyPrefsFile";

    public int speed;
    public String name;
    public String email;

    public boolean mSilentMode = false;

    static final private int BACK_ID = Menu.FIRST;
    static final private int CLEAR_ID = Menu.FIRST + 1;
	public TextView latitudeField;
	public TextView longitudeField;
	public TextView cogField;
	public Location last_location;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		latitudeField  = (TextView) findViewById(R.id.latitude);
		longitudeField = (TextView) findViewById(R.id.longitude);
		cogField       = (TextView) findViewById(R.id.cog);

		// get a hangle on the location manager
		locationManager =(LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// public void requestLocationUpdates (String provider, long minTime, float minDistance, PendingIntent intent)
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, new LocationUpdateHandler());
/*
 *         locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000,
        		1, new LocationUpdateHandler());
*/
		((Button) findViewById(R.id.decrease)).setOnClickListener(mDecreaseListener);
		((Button) findViewById(R.id.increase)).setOnClickListener(mIncreaseListener);
		((Button) findViewById(R.id.mark)).setOnClickListener(mMarkListener);

		// Restore preferences
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		speed = settings.getInt("speed", 15);
		name  = settings.getString("name", "Jake's Speed Limit Marker");
		email = settings.getString("email", "jake@example.com");

		refresh_speed();
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

	public void showLocation(View view) {
		switch (view.getId()) {
		case R.id.showlocation:
			LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			Location location = locationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (location != null) {
				refresh_location(location);
			} else {
				latitudeField.setText("GPS not available");
				longitudeField.setText("GPS not available");
			}
			break;
		}

	}

	@Override
	protected void onStop(){
		super.onStop();

		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("speed", speed);
		editor.putString("name", name);
		editor.putString("email", email);

		// Commit the edits!
		editor.commit();
	}


	public void refresh_location(Location location){
		last_location = location;
/*    	
		double lat = location.getLatitude();
		double lng = location.getLongitude();
		float  cog = location.getBearing();
*/
		latitudeField.setText(Double.toString( location.getLatitude()));
		longitudeField.setText(Double.toString(location.getLongitude()));
		cogField.setText(Float.toString(location.getBearing()));
	}

	public void refresh_speed() {
		((TextView) findViewById(R.id.speedlimit)).setText(Integer.toString(speed));
	}

	public void increase_speed() {
		speed += 5;
		refresh_speed();
	}

	public void decrease_speed() {
		speed -= 5;
		refresh_speed();
	}

	/**
	 * A call-back for when the user presses the increase button.
	 */
	OnClickListener mIncreaseListener = new OnClickListener() {
		public void onClick(View v) {
			increase_speed();
		}
	};
	/**
	 * A call-back for when the user presses the decrease button.
	 */
	OnClickListener mDecreaseListener = new OnClickListener() {
		public void onClick(View v) {
				decrease_speed();
		}
	};
	/**
	 * A call-back for when the user presses the mark button.
	 */
	OnClickListener mMarkListener = new OnClickListener() {
		public void onClick(View v) {
/*
        		
        	final int  myMph=  (int) (70);
        	final double myLatitude= loc.getLatitude();
        	final double myLongitude= loc.getLongitude();
        	final float  myCog= loc.getBearing();
        	
            String name=       "Mr. Android";
        	String direction=  Float.toString(myCog);
        	String hours=      "";
        	String email=      "";
*/
			try {
				HttpClient httpclient = new DefaultHttpClient();
				List<NameValuePair> formparams = new ArrayList<NameValuePair>();
				formparams.add(new BasicNameValuePair("mlat", 
					Double.toString(last_location.getLatitude())));
				formparams.add(new BasicNameValuePair("mlon", 
					Double.toString(last_location.getLongitude())));
				formparams.add(new BasicNameValuePair("mmph", 
					Integer.toString(speed)));

/*
formparams.add(new BasicNameValuePair("mtag", name));
formparams.add(new BasicNameValuePair("mcog", direction));
formparams.add(new BasicNameValuePair("mhours", hours));
formparams.add(new BasicNameValuePair("memail", email));
*/
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
				HttpPost httppost = new HttpPost("http://www.wikispeedia.org/a/process_submit_bb.php");
				httppost.setEntity(entity);
//				httpclient.execute(httppost);
			}catch (Exception e) {
				e.printStackTrace();
			}

			finish();
		}
	};

	/**
	 * Called when the activity is about to start interacting with the user.
	 */
	@Override
	protected void onResume() {
		super.onResume();
	}

	/**
	 * Called when your activity's options menu needs to be created.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		// We are going to create two menus. Note that we assign them
		// unique integer IDs, labels from our string resources, and
		// given them shortcuts.
		menu.add(0, BACK_ID, 0, R.string.back).setShortcut('0', 'b');
		menu.add(0, CLEAR_ID, 0, R.string.clear).setShortcut('1', 'c');

		return true;
	}

	/**
	 * Called right before your activity's option menu is displayed.
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
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
		switch (item.getItemId()) {
		case BACK_ID:
			finish();
			return true;
		case CLEAR_ID:
//			mEditor.setText("");
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

}