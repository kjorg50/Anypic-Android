package com.parse.anypic;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.PushService;

public class AnypicApplication extends Application {

	static final String TAG = "Panagram";
	
	@Override
	public void onCreate() {
		super.onCreate();		
		
		/*
		 * In this tutorial, we'll subclass ParseObjects for convenience to
		 * create and modify Photo objects.
		 * 
		 * Also, we'll use an Activity class to keep track of the relationships
		 * of ParseUsers with each other and Photos. Every time a user follows, likes 
		 * or comments, a new activity is created to represent the relationship.
		 */
		ParseObject.registerSubclass(Photo.class);
		ParseObject.registerSubclass(Activity.class);
		
		/*
		 * Fill in this section with your Parse credentials
		 */
		Parse.initialize(this, "YOUR_APPLICATION_KEY", "YOUR_CLIENT_KEY");
		
		// Set your Facebook App Id in strings.xml
		ParseFacebookUtils.initialize(getString(R.string.app_id));
		

		/*
		 * For more information on app security and Parse ACL:
		 * https://www.parse.com/docs/android_guide#security-recommendations
		 */
		ParseACL defaultACL = new ParseACL();

		/*
		 * If you would like all objects to be private by default, remove this
		 * line
		 */
		defaultACL.setPublicReadAccess(true);

		/*
		 * Default ACL is public read access, and user read/write access
		 */
		ParseACL.setDefaultACL(defaultACL, true);
		
		/*
		 *  Register for push notifications.
		 */
		PushService.setDefaultPushCallback(this, LoginActivity.class);
		ParseInstallation.getCurrentInstallation().saveInBackground();
	}

}
