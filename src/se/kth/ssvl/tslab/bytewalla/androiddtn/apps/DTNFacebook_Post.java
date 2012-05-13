package se.kth.ssvl.tslab.bytewalla.androiddtn.apps;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.Facebook.ServiceListener;
import com.facebook.android.FacebookError;

import se.kth.ssvl.tslab.bytewalla.androiddtn.R;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.storage.SQLiteImplementation;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class DTNFacebook_Post extends Activity {


	private static final String Table_Create_Messages = "create table IF NOT EXISTS  fbMessages (id integer primary key autoincrement, " 
			+ "message text, userID integer);";

	private static final String msgTable = "fbMessages";
	
	private static final String userTable = "fbUsers";

	private static final String fb_appID = "444494358901041";

	private static Facebook facebook;

	private final static Object state = new Object();

	private final String[] permissions = {"publish_actions"};

	private SQLiteImplementation sqlDB;
	
	private static int currentUserID;


	@Override
	public void onCreate(Bundle savedInstanceState){

		super.onCreate(savedInstanceState);

		setContentView(R.layout.dtnapps_dtnfacebook);		
	}

	@Override
	public void onStart(){
		super.onStart();
		
		sqlDB = new SQLiteImplementation(getApplicationContext(), Table_Create_Messages);

		facebook = new Facebook(fb_appID);

		Bundle extras = this.getIntent().getExtras();

		String userName = extras.getString("userName");
		
		currentUserID = getIDByName(userName);

		String accessToken = getAccessTokenForUser(currentUserID);

		long expires = getExpirationForUser(currentUserID);
		
		

		if(accessToken != null){
			facebook.setAccessToken(accessToken);

			if(expires != 0){
				facebook.setAccessExpires(expires);
			}

			facebook.extendAccessTokenIfNeeded(getApplicationContext(), new ServiceListener(){

				@Override
				public void onComplete(Bundle values) {
					ContentValues updateValues = new ContentValues();

					updateValues.put("token", facebook.getAccessToken());

					updateValues.put("expires", facebook.getAccessExpires() + "");

					boolean success = sqlDB.update(userTable, updateValues, "id=" + currentUserID, null);

					if(!success){
						Log.w("FB", "User update did not occur");
					}

				}

				@Override
				public void onFacebookError(FacebookError e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onError(Error e) {
					// TODO Auto-generated method stub

				}

			});
		}

		if(!facebook.isSessionValid()){

			facebook.authorize(this, permissions, new DialogListener(){

				@Override
				public void onComplete(Bundle values) {
					
					ContentValues updateValues = new ContentValues();
					
					updateValues.put("token", facebook.getAccessToken());
					
					updateValues.put("expires", facebook.getAccessExpires() + "");
					
					boolean success = sqlDB.update(userTable, updateValues, "id=" + currentUserID, null);
					
					if(!success){
						Log.w("FB", "User update did not occur");
					}

				}

				@Override
				public void onFacebookError(FacebookError e) {
					Log.i("DTN_FB", "facebook error occurred in auth");

				}

				@Override
				public void onError(DialogError e) {
					Log.i("DTN_FB", "dialog error occurred in auth");

				}

				@Override
				public void onCancel() {
					Log.i("DTN_FB", "auth cancelled");

				}

			});
		}
	}

	private int getIDByName(String userName) {
		int id = sqlDB.get_record(userTable, "name="+"\"" + userName + "\"", "id", null, null);
		
		return id;
	}

	private long getExpirationForUser(int userID) {
		String expireString = sqlDB.get_record_as_string(userTable, "id=" + userID, "expires", null, null);
		
		long expiresLong = Long.parseLong(expireString);
		
		return expiresLong;
	}

	private String getAccessTokenForUser(int userID) {
		
		String userAccessToken = sqlDB.get_record_as_string(userTable, "id="+userID, "token", null, null);
		
		return userAccessToken;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);

		facebook.authorizeCallback(requestCode, resultCode, data);
	}

	public void postToFacebook(View v){

		EditText msgBox = (EditText) findViewById(R.id.DTNApps_DTNFacebook_EditText);

		final String msgToPost = msgBox.getText().toString();

		if(hasWifi()){

			postMessage(msgToPost);
		}
		else{
			saveMessage(msgToPost);
			
			Toast.makeText(getApplicationContext(), "Message saved", Toast.LENGTH_SHORT).show();
		}
		
		finish();
	}

	private void saveMessage(String msgToPost) {
		
		ContentValues msgRow = new ContentValues();
		
		msgRow.put("message", "\"" + msgToPost + "\"");
		
		msgRow.put("userID", currentUserID);
		
		if(sqlDB.add(msgTable, msgRow) < 0){
			Log.w("FB", "Message was not saved to DB");
		}

	}

	private boolean hasWifi() {
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		
		return mWifi.isConnected();
	}

	private void postMessage(String msgToPost) {
		if (msgToPost != null && msgToPost.length() > 0) {

			Bundle params = new Bundle();

			params.putString("message", msgToPost);

			AsyncFacebookRunner poster = new AsyncFacebookRunner(facebook);

			poster.request("me/feed", params, "POST", new RequestListener(){

				@Override
				public void onComplete(String response, Object state) {
					Log.i("DTN_FB", response);					
				}

				@Override
				public void onIOException(IOException e, Object state) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onFileNotFoundException(FileNotFoundException e, Object state) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onMalformedURLException(MalformedURLException e,
						Object state) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onFacebookError(FacebookError e, Object state) {
					// TODO Auto-generated method stub

				}

			}, state);

		}
	}
	
	@Override
	public void onPause(){
		super.onPause();
		
		sqlDB.close();
	}
}
