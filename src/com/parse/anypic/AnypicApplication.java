package com.parse.anypic;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class AnypicApplication extends Application {

	static final String TAG = "Panagram";
	
	@Override
	public void onCreate() {
		super.onCreate();		
		
		/*
		 * In this tutorial, we'll subclass ParseObject for convenience to
		 * create and modify Picture objects
		 */
		ParseObject.registerSubclass(Picture.class);

		/*
		 * Fill in this section with your Parse credentials
		 */
		Parse.initialize(this, "cLXA3EhzyvWXitzfo8VNlqFEetDG9vKQgt87P85F", "O5EtsbdS59icxwKXqYm3WfLyYKUjHVX4pj5UOazj");
		
		// Set your Facebook App Id in strings.xml
		ParseFacebookUtils.initialize(getString(R.string.app_id));

		/*
		 * In this tutorial, we'll subclass ParseObject for convenience to
		 * create and modify Picture objects
		 */
		ParseObject.registerSubclass(Picture.class);
		
		/*
		 * This app lets an anonymous user create and save photos.
		 * An anonymous user is a user that can be created
		 * without a username and password but still has all of the same
		 * capabilities as any other ParseUser.
		 * 
		 * After logging out, an anonymous user is abandoned, and its data is no
		 * longer accessible. In your own app, you can convert anonymous users
		 * to regular users so that data persists.
		 * 
		 * Learn more about the ParseUser class:
		 * https://www.parse.com/docs/android_guide#users
		 */
		//ParseUser.enableAutomaticUser();

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
	}

}
