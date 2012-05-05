package se.kth.ssvl.tslab.bytewalla.androiddtn.apps;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

import se.kth.ssvl.tslab.bytewalla.androiddtn.R;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.storage.SQLiteImplementation;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class DTNFacebook_Users extends Activity{
	
	
	private static final String Table_Create_Users = "create table IF NOT EXISTS  fbUsers (id integer primary key autoincrement, " 
			+ "name text, token text, expires text);";

	private static final String table = "fbUsers";

	private static final String[] idField = new String[]{"id"};

	private static final String fb_appID = "444494358901041";

	private static Facebook facebook;
	
	private final String[] permissions = {"publish_actions"};
	
	private SQLiteImplementation sqlDB;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.dtnapps_facebook_userchooser);
	}
	
	
	@Override
	public void onStart(){
		
		super.onStart();
		
		new DTNFacebook_AsyncPost(getApplicationContext()).execute(new Void[1]);
		
		sqlDB = new SQLiteImplementation(getApplicationContext(), Table_Create_Users);
		
		int numOfUsers = sqlDB.get_count(table, null, idField);
		
		final List<String> userNames = new ArrayList<String>();
		
		if(numOfUsers > 0){
			List<Integer> allUserIDs = sqlDB.get_records(table, null, "id");
			
			for(Integer userID : allUserIDs){
				
				String userName = sqlDB.get_record_as_string(table, "id=" + userID, "name", null, null);
				
				if(userName.length() > 0){
					userNames.add(userName);
				}
			}			
		}
		
		ListView userList = (ListView) findViewById(R.id.fbUsersListView);
		
		ArrayAdapter<String> userListAdapter = new ArrayAdapter<String>(this, R.layout.list_view_item, userNames);
		
		userList.setAdapter(userListAdapter);
		
		userList.setClickable(true);
		
		userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				
				Intent fbPostActivityIntent = new Intent();
				
				fbPostActivityIntent.putExtra("userName", userNames.get(arg2));
				
				fbPostActivityIntent.setClass(DTNFacebook_Users.this, DTNFacebook_Post.class);
				
				DTNFacebook_Users.this.startActivity(fbPostActivityIntent);
				
			}
		});
		
		userList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
				Toast.makeText(getApplicationContext(), "Remove User", Toast.LENGTH_SHORT).show();
				return false;
			}
		});	
	}
	
	public void addFBUser(View v){
		
		addNewUser();
	}
	
	private void addNewUser(){
		facebook = new Facebook(fb_appID);
		
		facebook.authorize(this, permissions, new DialogListener(){

			@Override
			public void onComplete(Bundle values) {
				addUserToDB(facebook.getAccessToken(), facebook.getAccessExpires());
				
			}

			@Override
			public void onFacebookError(FacebookError e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onError(DialogError e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onCancel() {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		
	}
	

	protected void addUserToDB(final String accessToken, final long accessExpires) {
		AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(facebook);
		
		
		asyncRunner.request("me", new RequestListener(){

			@Override
			public void onComplete(String response, Object state) {
				
				try {
					JSONObject responseJson = new JSONObject(response);

					ContentValues userInfo = new ContentValues();

					userInfo.put("name", responseJson.getString("name"));

					userInfo.put("token", accessToken);

					userInfo.put("expires", accessExpires + "");

					sqlDB.add(table, userInfo);

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			@Override
			public void onIOException(IOException e, Object state) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onFileNotFoundException(FileNotFoundException e,
					Object state) {
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
			
		});
		
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		
		facebook.authorizeCallback(requestCode, resultCode, data);
	}
	
	@Override
	public void onPause(){
		super.onPause();
		
		sqlDB.close();
	}
}
