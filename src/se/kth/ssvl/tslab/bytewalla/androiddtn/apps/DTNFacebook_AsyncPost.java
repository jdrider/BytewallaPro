package se.kth.ssvl.tslab.bytewalla.androiddtn.apps;

import android.os.AsyncTask;

public class DTNFacebook_AsyncPost extends AsyncTask<Void, Void, Boolean> {

	
	private static final String Table_Create_Users = "create table IF NOT EXISTS  fbMsgs (id integer primary key autoincrement, " 
            + "msg text, userID integer);";
	
	
	@Override
	protected Boolean doInBackground(Void... arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
