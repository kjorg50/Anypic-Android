package com.parse.anypic;

import java.util.Arrays;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

/*
 * The HomeViewAdapter is an extension of ParseQueryAdapter
 * that has a custom layout for Anypic photos in the home
 * list view.
 */

public class HomeViewAdapter extends ParseQueryAdapter<Photo> {

	public HomeViewAdapter(Context context) {
		super(context, new ParseQueryAdapter.QueryFactory<Photo>() {
			public ParseQuery<Photo> create() {
				
				// First, query for the friends whom the current user follows
				ParseQuery<com.parse.anypic.Activity> followingActivitiesQuery = new ParseQuery<com.parse.anypic.Activity>("Activity");
				followingActivitiesQuery.whereMatches("type", "follow");
				followingActivitiesQuery.whereEqualTo("fromUser", ParseUser.getCurrentUser());
				
				// Get the photos from the Users returned in the previous query
				ParseQuery<Photo> photosFromFollowedUsersQuery = new ParseQuery<Photo>("Photo");
				photosFromFollowedUsersQuery.whereMatchesKeyInQuery("user", "toUser", followingActivitiesQuery);
				photosFromFollowedUsersQuery.whereExists("image");
				
				// Get the current user's photos
				ParseQuery<Photo> photosFromCurrentUserQuery = new ParseQuery<Photo>("Photo");
				photosFromCurrentUserQuery.whereEqualTo("user", ParseUser.getCurrentUser());
				photosFromCurrentUserQuery.whereExists("image");
				
				// We create a final compound query that will find all of the photos that were
			    // taken by the user's friends or by the user
				ParseQuery<Photo> query = ParseQuery.or(Arrays.asList( photosFromFollowedUsersQuery, photosFromCurrentUserQuery ));
				query.include("user");
				query.orderByDescending("createdAt");
				
				return query;
			}
		});
	}

	/**
	 * This class is overridden to provide a custom view for each item in the 
	 * Home List View. It sets the user's profile picture, their user name,
	 * and then displays the actual photo.
	 * 
	 * See home_list_item.xml for the layout file
	 * 
	 * @see com.parse.ParseQueryAdapter#getItemView(com.parse.ParseObject, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getItemView(Photo photo, View v, ViewGroup parent) {

		if (v == null) {
			v = View.inflate(getContext(), R.layout.home_list_item, null);
		}

		super.getItemView(photo, v, parent);

		// Set up the user's profile picture
		ParseImageView fbPhotoView = (ParseImageView) v.findViewById(R.id.user_thumbnail);
		ParseUser user = photo.getUser();
		ParseFile thumbnailFile = user.getParseFile("profilePictureSmall");
		if (thumbnailFile != null) {
			fbPhotoView.setParseFile(thumbnailFile);
			fbPhotoView.loadInBackground(new GetDataCallback() {
				@Override
				public void done(byte[] data, ParseException e) {
					// nothing to do
					//Log.i(AnypicApplication.TAG, "7. Thumbnail view loaded");
				}
			});
		} else { // Clear ParseImageView if an object doesn't have a photo
	        fbPhotoView.setImageResource(android.R.color.transparent);
	    }

		// Set up the username
		TextView usernameView = (TextView) v.findViewById(R.id.user_name);
		usernameView.setText((String) user.get("displayName"));
		
		// Set up the actual photo
		ParseImageView anypicPhotoView = (ParseImageView) v.findViewById(R.id.photo);
		ParseFile photoFile = photo.getImage();
		
		// TODO (future) - get image bitmap, then set the image view with setImageBitmap()
		// we can use the decodeBitmap tricks to reduce the size to save memory
		
		if (photoFile != null) {
			anypicPhotoView.setParseFile(photoFile);
			anypicPhotoView.loadInBackground(new GetDataCallback() {
				@Override
				public void done(byte[] data, ParseException e) {
					// nothing to do
					//Log.i(AnypicApplication.TAG, "8. Image view loaded");
				}
			});
		} else { // Clear ParseImageView if an object doesn't have a photo
	        anypicPhotoView.setImageResource(android.R.color.transparent);
	    }
		

//		final ImageView iv=anypicPhotoView;
//		ViewTreeObserver vto = iv.getViewTreeObserver();
//		vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
//			public boolean onPreDraw() {
//				Log.i(AnypicApplication.TAG, "*** Photo height: " + iv.getMeasuredHeight() + " width: " + iv.getMeasuredWidth());
//				return true;
//			}
//		});
		return v;
	}

}
