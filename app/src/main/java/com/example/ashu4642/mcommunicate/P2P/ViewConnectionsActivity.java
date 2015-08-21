package com.example.ashu4642.mcommunicate.P2P;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.ashu4642.mcommunicate.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class ViewConnectionsActivity extends Activity {
	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private GetConnectionsTask mAuthTask = null;

	// UI references.
	private View mConnectionsView;
	private View mGetConnectionStatusView;
	private TextView mGetConnectionStatusMessageView;
	private ListView mListView;
	
	private ArrayList<NameValuePair> nameValuePairs;
	private String[] listIdentifiers = {"alias", "datetime", "location"};
	private int[] listColumns = {R.id.alias, R.id.datetime, R.id.location};
	
	public static List<HashMap<String, String>> connectionsList;
	private SimpleAdapter simpleAdapter;
	
	// broadcast listeners
	private IntentFilter intentFilter;
	private BroadcastReceiver broadcastReceiver;
	
	private static String TAG = "viewConnections activity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_view_connections);

		mConnectionsView = findViewById(R.id.login_form);
		mGetConnectionStatusView = findViewById(R.id.login_status);
		mGetConnectionStatusMessageView = (TextView) findViewById(R.id.login_status_message);
		
		mListView = (ListView) findViewById(R.id.connections_listview);		
		
		// inflate header view
		LayoutInflater inflater = getLayoutInflater();
		ViewGroup header = (ViewGroup)inflater.inflate(R.layout.header, mListView, false);
		ViewGroup searchHeader = (ViewGroup)inflater.inflate(R.layout.search_header, mListView, false);
		
		mListView.addHeaderView(searchHeader, null, true);
		mListView.addHeaderView(header, null, true);
		
		// initialize nameValuePairs
		nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("alias",
				LoginActivity.currentUser));

		// initialize empty list
		connectionsList = new ArrayList<HashMap<String, String>>();

		// register listeners
		registerClickCallBacks();
		registerBroadcastIntents();
		
		// get connection data
		refreshConnectionsListView();
		attemptConnectionsUpdate();
	}
	
	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		// update connections
		attemptConnectionsUpdate();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.view_connections, menu);
		return true;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(broadcastReceiver);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		
		// handle action bar item clicks
		switch(item.getItemId()) {
			case R.id.action_home:
				intent = new Intent(ViewConnectionsActivity.this, MenuActivity.class);
				startActivity(intent);
				
				return true;
			case R.id.action_settings:
				// TODO
				
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
    private void registerClickCallBacks() {
    	Log.d(ViewConnectionsActivity.TAG, "registerClickCallBack");
    	
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override 
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) 
            { 
            	HashMap<String, String> connectionData = (HashMap<String, String>) adapterView.getItemAtPosition(position);        	
            	nameValuePairs.add(new BasicNameValuePair("user", connectionData.get("alias")));
            	attemptConnectionsUpdate();
            }
		});
    }
    
    private void registerBroadcastIntents() {
    	Log.d(ViewConnectionsActivity.TAG, "registerBroadcastIntents");
    	
    	broadcastReceiver = new BroadcastReceiver() {
			@Override public void onReceive(Context context, Intent intent) {
				Log.d(ViewConnectionsActivity.TAG, "logout in progress");
				finish();
			}
		};
    	
		// listen for logout
		intentFilter = new IntentFilter();
		intentFilter.addAction("ACTION_LOGOUT");
		registerReceiver(broadcastReceiver, intentFilter);
    }
    
	public void refreshConnectionsListView() {
		simpleAdapter = new SimpleAdapter(getBaseContext(), connectionsList, R.layout.listitem, listIdentifiers, listColumns);
		mListView.setAdapter(simpleAdapter);
		simpleAdapter.notifyDataSetChanged();
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptConnectionsUpdate() {
		if (mAuthTask != null) {
			return;
		}
		
		// Show a progress spinner, and kick off a background task to
		// perform the user login attempt.
		mGetConnectionStatusMessageView.setText(R.string.view_connections_progress_update);
		showProgress(true);
		mAuthTask = new GetConnectionsTask();
		mAuthTask.execute((Void) null);
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mGetConnectionStatusView.setVisibility(View.VISIBLE);
			mGetConnectionStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mGetConnectionStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mConnectionsView.setVisibility(View.VISIBLE);
			mConnectionsView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mConnectionsView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mGetConnectionStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mConnectionsView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class GetConnectionsTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			
			try {
				// Simulate network access.
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				return false;
			}
			
			return Utils.getConnectionData(getString(R.string.service_connectionData), nameValuePairs);
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);

			if (success) {
				Log.d(ViewConnectionsActivity.TAG, "ViewConnections successful");
				refreshConnectionsListView();
			}
			else {
				Log.d(ViewConnectionsActivity.TAG, "ViewConnections not successful");
				finish(); // Should redirect to user main activity
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}
}
