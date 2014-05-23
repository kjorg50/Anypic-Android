package com.parse.anypic;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/*
 * This fragment is the preview page for publishing
 * a new photo to Anypic. It lets the user see a 
 * preview of the image they just captured in a ParseImageView,
 * and then choose whether they would like to publish it or 
 * cancel.
 * 
 * If they choose "Publish", then a new Photo object 
 * gets created on Parse with the associated ParseFile 
 * containing the image. 
 */
public class NewPhotoFragment extends Fragment {

	private ImageButton cameraButton; // not used right now
	private Button saveButton;
	private Button cancelButton;
	private ParseImageView photoPreview;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle SavedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_new_photo, parent, false);

		photoPreview = (ParseImageView) v.findViewById(R.id.photo_preview);

		// Taking this out for now, since it causes a java.lang.OutOfMemoryError
		// when on the preview page and you open the Camera Intent again and save
		// a new picture. I believe it's because of the Bitmap operations in the 
		// savePhotoFiles() function. Fix this by finding a way to run that Bitmap
		// scaling and copying in the background, rather than the main thread.
		
//		cameraButton = ((ImageButton) v.findViewById(R.id.camera_button));
//		cameraButton.setOnClickListener(new View.OnClickListener() {
//			// The cameraButton can be used to re-open the camera if the 
//			// user is not satisfied with the current preview. I guess 
//			// this is not the best solution, but I think it will work. 
//			
//			@Override
//			public void onClick(View v) {
//				InputMethodManager imm = (InputMethodManager) getActivity()
//						.getSystemService(Context.INPUT_METHOD_SERVICE);
//				imm.hideSoftInputFromWindow(cameraButton.getWindowToken(), 0);
//				// Open the camera using an Intent
//				((NewPhotoActivity) getActivity()).startCamera();
//			}
//		});

		saveButton = ((Button) v.findViewById(R.id.save_button));
		saveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Photo photo = ((NewPhotoActivity) getActivity()).getCurrentPhoto();

				// When the user clicks "Save," upload the picture to Parse
				// Associate the picture with the current user
				photo.setUser(ParseUser.getCurrentUser());

				// Add the image
				photo.setImage( ((NewPhotoActivity) getActivity()).getImageFile() );

				// Add the thumbnail
				photo.setThumbnail( ((NewPhotoActivity) getActivity()).getThumbnailFile() );

				// Save the picture and return
				photo.saveInBackground(new SaveCallback() {

					@Override
					public void done(ParseException e) {
						if (e == null) {
							Log.i(AnypicApplication.TAG, "Saved new Photo to Parse!!!");
							getActivity().setResult(Activity.RESULT_OK);
							getActivity().finish();
						} else {
							Toast.makeText(
									getActivity().getApplicationContext(),
									"Error saving: " + e.getMessage(),
									Toast.LENGTH_SHORT).show();
						}
					}

				});

			}
		});

		cancelButton = ((Button) v.findViewById(R.id.cancel_button));
		cancelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO (extra) - delete the files of any picturs that were
				// saved onto the local device, but not Parse
				
				getActivity().setResult(Activity.RESULT_CANCELED);
				getActivity().finish();
			}
		});


		return v;
	}

	/*
	 * On resume, check and see if a photo has been set from the
	 * CameraFragment. If it has, load the (full) image in this fragment and 
	 * make the preview image visible.
	 */
	@Override
	public void onResume() {
		super.onResume();
		ParseFile photoFile = ((NewPhotoActivity) getActivity()).getImageFile();
		if (photoFile != null) {
			photoPreview.setParseFile(photoFile);
			photoPreview.loadInBackground(new GetDataCallback() {
				@Override
				public void done(byte[] data, ParseException e) {
					photoPreview.setVisibility(View.VISIBLE);
				}
			});
		}
	}

}
