package com.jakewendt.speed_limit_marker;

import com.jakewendt.speed_limit_marker.R;
import java.util.ArrayList;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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

public class Settings extends Activity {
	public static final String PREFS_NAME = "MyPrefsFile";
	private static final String TAG = "WhereWasI::Settings";

	private Spinner unitsSpinner;
	private TextWatcher emailWatcher;
	private TextWatcher usernameWatcher;
	private OnItemSelectedListener unitsListener;
	private EditText usernameText;
	private EditText emailText;

	public static String username;
	public static String email;
	public static Integer speed_limit;
	public static Boolean mph_kph;
	private static Boolean settings_read = false;

	public static void readSettings(Context context) {
		if( !settings_read ){
			/*
			 * Restore preferences
			 * SharedPreferences android.content.ContextWrapper.getSharedPreferences(String name, int mode)
			 * 
			 * Parameters
			 * name	Desired preferences file. If a preferences file by this name does not exist, it will be created 
			 * 		you retrieve an editor (SharedPreferences.edit()) and then commit changes (Editor.commit()).
			 * mode	Operating mode. Use 0 or MODE_PRIVATE for the default operation, MODE_WORLD_READABLE and 
			 * 		MODE_WORLD_WRITEABLE to control permissions.
			 * 
			 * Returns
			 * Returns the single SharedPreferences instance that can be used to retrieve and modify the preference values.
			 */
			SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
			username = settings.getString("username", "your name");
			email = settings.getString("email", "your@email.com");
			mph_kph = settings.getBoolean("mph_kph", true);
			speed_limit = settings.getInt("speed_limit", 25);
			settings_read = true;
		}
	}
	
	public static void writeSettings(Context context) {
		// Save user preferences as persistent. 
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("mph_kph", mph_kph);
		editor.putString("username", username);
		editor.putString("email", email);
		editor.putInt("speed_limit", speed_limit);

		// Don't forget to commit your edits!!!
		//to see your stuff go into DDMS->File Explorer->data->data->org.wikispeedia.backseatdriverV->shared_prefs
		editor.commit();      		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {         
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		readSettings(this);

		//initThreading();
		findViews(); 
		setAdapters(); 
		setListeners(); 

		//initialize username and email EditText boxes
		usernameText.setText(username);
		emailText.setText(email);
	}

	@Override
	protected void onStop(){
		super.onStop();
		writeSettings(this);
	}

	/** Get a handle to all user interface elements */
	private void findViews() {
		unitsSpinner = (Spinner)  findViewById(R.id.mph_kph_language);
		usernameText = (EditText) findViewById(R.id.username_field);
		emailText    = (EditText) findViewById(R.id.email_field);
	}

	/** Define data source for the spinners */
	private void setAdapters() {
		// Spinner list comes from a resource,
		// Spinner user interface uses standard layouts
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
			this, R.array.units,
			android.R.layout.simple_spinner_item);

		adapter.setDropDownViewResource(
			android.R.layout.simple_spinner_dropdown_item);

		unitsSpinner.setAdapter(adapter);

//		if(mph_kph == true){
		if( mph_kph ){
			unitsSpinner.setSelection(0);  //0=Mph  
		} else {
			unitsSpinner.setSelection(1);  //1=Kph
		}
	}

	private void setListeners() {
		// Define event listeners
		emailWatcher = new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start,
					int count, int after) {
			}
			public void onTextChanged(CharSequence s, int start,
					int before, int count) {
				//get email address text
				email = s.toString();             
			}
			public void afterTextChanged(Editable s) {
			}
		};

		usernameWatcher = new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start,
					int count, int after) {
			}
			public void onTextChanged(CharSequence s, int start,
					int before, int count) {
				//get username text
				username = s.toString();                  
			}
			public void afterTextChanged(Editable s) {
			}
		};

		unitsListener = new OnItemSelectedListener() {
			public void onItemSelected(AdapterView parent, View v,
					int position, long id) {
				//turns out, id and position are identical, both indicate 0=mph, 1=kph
				if(id==0) {
					mph_kph = true;		//mph
				} else {
					mph_kph = false;		//kph
				}
			}
			public void onNothingSelected(AdapterView parent) {
			}
		};

		// Set listeners on graphical user interface widgets
		usernameText.addTextChangedListener(usernameWatcher);
		emailText.addTextChangedListener(emailWatcher);
		unitsSpinner.setOnItemSelectedListener(unitsListener);
	}
}