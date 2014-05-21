package com.parse.anypic;


import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.parse.ParseFacebookUtils;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;


public class HomeListActivity extends ListActivity {

	private ParseQueryAdapter<Photo> mainAdapter;
	private FavoriteMealAdapter favoritesAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getListView().setClickable(false);

		mainAdapter = new ParseQueryAdapter<Photo>(this, Photo.class);
		mainAdapter.setTextKey("title");
		mainAdapter.setImageKey("photo");

		// Subclass of ParseQueryAdapter
		favoritesAdapter = new FavoriteMealAdapter(this);

		// Default view is all posts
		setListAdapter(mainAdapter);
		
		// Fetch Facebook user info if the session is active
		Session session = ParseFacebookUtils.getSession();
		if (session != null && session.isOpened()) {
			makeMeRequest();
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();

		//Log.i(AnypicApplication.TAG, "Entered HomeListActivity onResume()");
		
		ParseUser currentUser = ParseUser.getCurrentUser();
		if (currentUser != null) {
			// Check if the user is currently logged
			// and show any cached content
			
		} else {
			// If the user is not logged in, go to the
			// activity showing the login view.
			startLoginActivity();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_home_list, menu);
		return true;
	}

	/*
	 * Posting pictures and refreshing the list will be controlled from the Action
	 * Bar.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.action_refresh: {
			updateHomeList();
			break;
		}

		case R.id.action_favorites: {
			showFavorites();
			break;
		}

		case R.id.action_new: {
			newPicture();
			break;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	private void updateHomeList() {
		mainAdapter.loadObjects();
		setListAdapter(mainAdapter);
	}

	private void showFavorites() {
		favoritesAdapter.loadObjects();
		setListAdapter(favoritesAdapter);
	}

	private void newPicture() {
		Intent i = new Intent(this, NewPicActivity.class);
		startActivityForResult(i, 0);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			// If a new post has been added, update
			// the list of posts
			updateHomeList();
		}
	}
	
	/**
	 * Requesting and setting user data. Essentially, this is the User constructor
	 */
	private void makeMeRequest() {
		Request request = Request.newMeRequest(ParseFacebookUtils.getSession(),
				new Request.GraphUserCallback() {
					@Override
					public void onCompleted(GraphUser user, Response response) {
						if (user != null) {
							// get the relevant data using the GraphAPI
							// and store them as fields in our ParseUser
							
							/*
							 * User Model
							 * 
							 * displayName : String
							 * email : string
							 * profilePictureMedium : File
							 * profilePictureSmall : File
							 * facebookId : String
							 * facebookFriends : Array
							 * channel : String
							 */
							ParseUser currentUser = ParseUser
									.getCurrentUser();
							currentUser.put("facebookId", user.getId());
							currentUser.put("displayName", user.getName());
							currentUser.saveInBackground();

							// handle errors accessing data from facebook
						} else if (response.getError() != null) {
							if ((response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_RETRY)
									|| (response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION)) {
								Log.i(AnypicApplication.TAG,
										"The facebook session was invalidated.");
								onLogoutButtonClicked();
							} else {
								Log.i(AnypicApplication.TAG,
										"Some other error: "
												+ response.getError()
														.getErrorMessage());
							}
						}
					}
				});
		request.executeAsync();

	}
	
	private void onLogoutButtonClicked() {
		// Log the user out
		ParseUser.logOut();

		// Go to the login view
		startLoginActivity();
	}
	
	private void startLoginActivity() {
		Intent intent = new Intent(this, LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

}
