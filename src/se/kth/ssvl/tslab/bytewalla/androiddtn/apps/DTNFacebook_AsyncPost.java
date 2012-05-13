package se.kth.ssvl.tslab.bytewalla.androiddtn.apps;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import com.facebook.android.Facebook;

import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.storage.SQLiteImplementation;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class DTNFacebook_AsyncPost extends AsyncTask<Void, Void, Boolean> {

	
	private static final String Table_Create_Messages = "create table IF NOT EXISTS  fbMessages (id integer primary key autoincrement, " 
            + "message text, userID integer);";
	
	private static final String fb_appID = "444494358901041";
	
	private Context context;
	
	private static SQLiteImplementation sqlDB;
	
	public DTNFacebook_AsyncPost(Context context){
		this.context = context;
	}
	
	
	@Override
	protected Boolean doInBackground(Void... arg0) {
		
		boolean msgsPosted = false;
		
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		
		if(mWifi.isConnected()){
			
			sqlDB = new SQLiteImplementation(context, Table_Create_Messages);
			
			int msgsToPost = sqlDB.get_count("fbMessages", null, new String[]{"id"});
			
			if(msgsToPost > 0){
				
				List<Integer> msgIDs = sqlDB.get_records("fbMessages", null, "id");
				
				for(Integer msgID : msgIDs){

					String message = sqlDB.get_record_as_string("fbMessages", "id=" + msgID, "message", null, null);

					if(message.length() > 0){

						int userID = sqlDB.get_record("fbMessages", "id=" + msgID, "userID", null, null);

						Facebook fb = createFacebookObjectForUser(userID);

						if(fb.isSessionValid()){

							Bundle msgParams = new Bundle();

							msgParams.putString("message", message);

							try {
								fb.request("me/feed", msgParams, "POST");
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (MalformedURLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}

						else{
							Log.w("FB", "Session expired for user: " + userID);
						}
					}
					
					Log.i("FB", "All saved messages posted.");
				}
				
			}
			
			sqlDB.drop_table("fbMessages");
			
			sqlDB.close();
		}
		
		
		
		return msgsPosted;
	}


	private Facebook createFacebookObjectForUser(int userID) {

		Facebook fbForUser = new Facebook(fb_appID);

		String expireString = sqlDB.get_record_as_string("fbUsers", "id=" + userID, "expires", null, null);

		long expiresLong = Long.parseLong(expireString);

		String userAccessToken = sqlDB.get_record_as_string("fbUsers", "id="+userID, "token", null, null);
		
		fbForUser.setAccessExpires(expiresLong);
		
		fbForUser.setAccessToken(userAccessToken);

		return fbForUser;
	}

}
