package com.example.ashu4642.mcommunicate.P2P;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.ashu4642.mcommunicate.P2P.Peer2PeerService.Peer2PeerBinder;
import com.example.ashu4642.mcommunicate.R;

public class MenuActivity extends Activity {
	
	// maintains service binding
	private Peer2PeerService mService;
	private boolean mBound = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_menu);
		
		setOnClickListeners();	
		
		//Intent intent = new Intent(MenuActivity.this, Peer2PeerService.class);
		//bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		Intent intent = new Intent(MenuActivity.this, Peer2PeerService.class);
		startService(intent);
	}
	
	@Override 
	protected void onStart() {
		super.onStart();
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if(mBound) {
			Intent serviceIntent = new Intent(MenuActivity.this, Peer2PeerService.class);
			stopService(serviceIntent);
			//unbindService(mConnection);
			mBound = false;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// handle action bar item clicks
		switch(item.getItemId()) {
			case R.id.action_home:
				return true;
			case R.id.action_settings:
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void setOnClickListeners() {
		findViewById(R.id.view_connections_button).setOnClickListener(
				new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						startActivity(new Intent(MenuActivity.this, ViewConnectionsActivity.class));
					}
		});
		
		findViewById(R.id.settings_button).setOnClickListener(
				new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						//startActivity(new Intent(MenuActivity.this, ViewConnectionsActivity.class));
					}
		});
		
		findViewById(R.id.logout_button).setOnClickListener(
				new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						logout();
					}
		});	
	}
	
	private void logout() {
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction("ACTION_LOGOUT");
		sendBroadcast(broadcastIntent);
		
		Intent intent = new Intent(MenuActivity.this, LoginActivity.class);
		startActivity(intent);
		
		if(mBound) {
			Intent serviceIntent = new Intent(MenuActivity.this, Peer2PeerService.class);
			stopService(serviceIntent);
			//unbindService(mConnection);
			mBound = false;
		}
		
		finish();
	}
	
	// Defines callbacks for service binding, passed to bindService()
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            Peer2PeerBinder binder = (Peer2PeerBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

}
