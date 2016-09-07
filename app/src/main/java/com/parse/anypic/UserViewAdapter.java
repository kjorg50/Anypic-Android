package com.parse.anypic;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

/*
 * The UserViewAdapter is an extension of ParseQueryAdapter
 * that has a custom layout for Anypic photos for the current user
 */

public class UserViewAdapter extends ParseQueryAdapter<Photo> {
	
	public UserViewAdapter(Context context) {
		super(context, new ParseQueryAdapter.QueryFactory<Photo>() {
			public ParseQuery<Photo> create() {
				
				// Get the current user's photos
				ParseQuery<Photo> photosFromCurrentUserQuery = new ParseQuery<Photo>("Photo");
				photosFromCurrentUserQuery.whereEqualTo("user", ParseUser.getCurrentUser());
				photosFromCurrentUserQuery.whereExists("thumbnail");
				
				photosFromCurrentUserQuery.include("user");
				photosFromCurrentUserQuery.orderByDescending("createdAt");
				
				return photosFromCurrentUserQuery;
			}
		});
	}
	
	/**
	 * This class is overridden to provide a custom view for each item in the 
	 * User's List View. It sets the user's profile picture, the user name,
	 * and then displays the actual photo.
	 * 
	 * See user_list_item.xml for the layout file
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
				}
			});
		} else { // Clear ParseImageView if an object doesn't have a photo
	        anypicPhotoView.setImageResource(android.R.color.transparent);
	    }
		
		return v;
	}
	
}