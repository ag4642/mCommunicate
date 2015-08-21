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
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.example.ashu4642.mcommunicate.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class RegisterActivity extends Activity {

	/**
	 * The default email to populate the email field with.
	 */
	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";
	
	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserRegisterTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mAlias;
	private String mLastName;
	private String mFirstName;
	private String mEmail;
	private String mPassword;
	private String mConfirmPassword;

	// UI references.
	private EditText mAliasView;
	private EditText mLastNameView;
	private EditText mFirstNameView;
	private EditText mEmailView;
	private EditText mPasswordView;
	private EditText mConfirmPasswordView;
	private View mRegisterFormView;
	private View mRegisterStatusView;
	private TextView mRegisterStatusMessageView;
	
	// broadcast listeners
	private IntentFilter intentFilter;
	private BroadcastReceiver broadcastReceiver;
	
	private static String TAG = "register activity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_register);

		// Set up the login form.
		mAliasView = (EditText) findViewById(R.id.alias);
		mLastNameView = (EditText) findViewById(R.id.lastName);
		mFirstNameView = (EditText) findViewById(R.id.firstName);
		
		mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setText(mEmail);

		mPasswordView = (EditText) findViewById(R.id.password);
		
		mConfirmPasswordView = (EditText) findViewById(R.id.confirm_password);
		mConfirmPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptRegister();
							return true;
						}
						return false;
					}
				});

		mRegisterFormView = findViewById(R.id.login_form);
		mRegisterStatusView = findViewById(R.id.login_status);
		mRegisterStatusMessageView = (TextView) findViewById(R.id.login_status_message);
		
		// register listeners
		registerClickCallBacks();
		registerBroadcastIntents();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.register, menu);
		return true;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(broadcastReceiver);
	}
	
	private void registerClickCallBacks() {
		Log.d(RegisterActivity.TAG, "registerClickCallBacks");
		
		findViewById(R.id.register_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptRegister();
					}
		});
		
		findViewById(R.id.sign_in_text).setOnClickListener(
				new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						startActivity(new Intent(RegisterActivity.this, LoginActivity.class));						
					}
		});		
	}
	
	private void registerBroadcastIntents() {
		Log.d(RegisterActivity.TAG, "registerBroadcastIntents");
		
		broadcastReceiver = new BroadcastReceiver() {
			@Override public void onReceive(Context context, Intent intent) {
				Log.d(RegisterActivity.TAG, "login in progress");
				finish();
			}
		};
		
		// listen for login
		intentFilter = new IntentFilter();
		intentFilter.addAction("ACTION_LOGIN");
		registerReceiver(broadcastReceiver, intentFilter);
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptRegister() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		
		mAliasView.setError(null);
		mLastNameView.setError(null);
		mFirstNameView.setError(null);
		mEmailView.setError(null);
		mPasswordView.setError(null);
		mConfirmPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mAlias = mAliasView.getText().toString();
		mLastName = mLastNameView.getText().toString();
		mFirstName = mFirstNameView.getText().toString();
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();
		mConfirmPassword = mConfirmPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;
		
		// Check for a valid confirm password
		if(TextUtils.isEmpty(mConfirmPassword)) {
			mConfirmPasswordView.setError(getString(R.string.error_field_required));
			focusView = mConfirmPasswordView;
			cancel = true;
		}

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}
		
		// Check for matching passwords
		if(!TextUtils.isEmpty(mConfirmPassword) && !TextUtils.isEmpty(mPassword) &&
				!TextUtils.equals(mPassword, mConfirmPassword)) {
			mConfirmPasswordView.setError(getString(R.string.error_confirm_password));
			focusView = mConfirmPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		} else if (!mEmail.contains("@")) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}
		
		// Check for valid first name
		if(TextUtils.isEmpty(mFirstName)) {
			mFirstNameView.setError(getString(R.string.error_field_required));
			focusView = mFirstNameView;
			cancel = true;
		}
		
		// Check for valid last name
		if(TextUtils.isEmpty(mLastName)) {
			mLastNameView.setError(getString(R.string.error_field_required));
			focusView = mLastNameView;
			cancel = true;
		}
		
		// Check for valid alias
		if(TextUtils.isEmpty(mAlias)) {
			mAliasView.setError(getString(R.string.error_field_required));
			focusView = mAliasView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mRegisterStatusMessageView.setText(R.string.login_progress_register);
			showProgress(true);
			mAuthTask = new UserRegisterTask(this);
			mAuthTask.execute((Void) null);
		}
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

			mRegisterStatusView.setVisibility(View.VISIBLE);
			mRegisterStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mRegisterStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mRegisterFormView.setVisibility(View.VISIBLE);
			mRegisterFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mRegisterFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mRegisterStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {
		private Activity activity;
		
		public UserRegisterTask(Activity activ)
		{
			activity = activ;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO: attempt authentication against a network service.

			try {
				// Simulate network access.
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				return false;
			}
			
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("alias", mAlias));
			nameValuePairs.add(new BasicNameValuePair("lastName", mLastName));
			nameValuePairs.add(new BasicNameValuePair("firstName", mFirstName));
			nameValuePairs.add(new BasicNameValuePair("email", mEmail));
			nameValuePairs.add(new BasicNameValuePair("password", mPassword));
			
			return Utils.executeUserService(getString(R.string.service_register), nameValuePairs);
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);

			if (success) {
				finish();
			} else {
				try {
					JSONObject jsonObj = new JSONObject(Utils.userResponse);
					String emailError = jsonObj.getString("email");
					String aliasError = jsonObj.getString("alias");
					
					if(!TextUtils.isEmpty(emailError)) {
						mEmailView.setError(emailError);
						mEmailView.requestFocus();
					}
					if(!TextUtils.isEmpty(aliasError)) {
						mAliasView.setError(aliasError);
						mAliasView.requestFocus();
					}
				} catch (JSONException e) {
					String message = "Error parsing JSON object: " + e.toString();
					Log.e("register", message);
				}
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}
}
