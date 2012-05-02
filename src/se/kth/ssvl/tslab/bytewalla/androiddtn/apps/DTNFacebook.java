package se.kth.ssvl.tslab.bytewalla.androiddtn.apps;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

import se.kth.ssvl.tslab.bytewalla.androiddtn.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class DTNFacebook extends Activity {

	
	private static final String fb_appID = "444494358901041";
	
	private static Facebook facebook;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.dtnapps_dtnreceive);
		
		facebook = new Facebook(fb_appID);
		
		facebook.authorize(this, new DialogListener(){

			@Override
			public void onComplete(Bundle values) {
				// TODO Auto-generated method stub
				
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
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		
		facebook.authorizeCallback(requestCode, resultCode, data);
	}
}
