package com.parse.anypic;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
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

	//private ImageButton cameraButton; // not used right now
	private Button saveButton;
	private Button cancelButton;
	private ParseImageView photoPreview;
	private ProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle SavedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_new_photo, parent, false);

		photoPreview = (ParseImageView) v.findViewById(R.id.photo_preview);

		saveButton = ((Button) v.findViewById(R.id.save_button));
		saveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				setProgressDialog( ProgressDialog.show(getActivity(), "",
								"Publishing photo...", true, true) );
				
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
						getProgressDialog().dismiss();
						if (e == null) {
							Log.i(AnypicApplication.TAG, "Saved new Photo to Parse!");
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
			Log.i(AnypicApplication.TAG, "The photo WAS taken");
			photoPreview.setParseFile(photoFile);
			photoPreview.loadInBackground(new GetDataCallback() {
				@Override
				public void done(byte[] data, ParseException e) {
					photoPreview.setVisibility(View.VISIBLE);
				}
			});
		} else{
			photoPreview.setVisibility(View.INVISIBLE);
		}
	}
	
	public ProgressDialog getProgressDialog(){
		return progressDialog;
	}
	
	public void setProgressDialog(ProgressDialog pd){
		progressDialog = pd;
	}

}
