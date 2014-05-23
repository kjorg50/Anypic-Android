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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/*
 * This fragment manages the data entry for a
 * new Photo object. It lets the user input a 
 * picture name, give it a rating, and take a 
 * photo. If there is already a photo associated
 * with this object, it will be displayed in the 
 * preview at the bottom, which is a standalone
 * ParseImageView.
 */
public class NewPhotoFragment extends Fragment {

	private ImageButton photoButton;
	private Button saveButton;
	private Button cancelButton;
	private TextView photoName;
	private Spinner photoRating;
	private ParseImageView photoPreview;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle SavedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_new_photo, parent, false);

		photoName = ((EditText) v.findViewById(R.id.photo_name));

		// The photoRating spinner lets people assign favorites of pics they've
		// taken.
		// Pictures with 4 or 5 ratings will appear in the Favorites view.
		photoRating = ((Spinner) v.findViewById(R.id.rating_spinner));
		ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter
				.createFromResource(getActivity(), R.array.ratings_array,
						android.R.layout.simple_spinner_dropdown_item);
		photoRating.setAdapter(spinnerAdapter);

		photoButton = ((ImageButton) v.findViewById(R.id.camera_button));
		photoButton.setOnClickListener(new View.OnClickListener() {
			// The photoButton can be used to re-open the camera if the 
			// user is not satisfied with the current preview. I guess 
			// this is not the best solution, but I think it will work. 
			
			@Override
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager) getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(photoName.getWindowToken(), 0);
				// Open the camera using an Intent
				((NewPhotoActivity) getActivity()).startCamera();
			}
		});

		saveButton = ((Button) v.findViewById(R.id.save_button));
		saveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Photo photo = ((NewPhotoActivity) getActivity()).getCurrentPhoto();

				// When the user clicks "Save," upload the picture to Parse
				// Add data to the picture object:
				//photo.setTitle(photoName.getText().toString());

				// Associate the picture with the current user
				photo.setUser(ParseUser.getCurrentUser());

				// Add the rating
				//picture.setRating(picRating.getSelectedItem().toString());

				// If the user added a photo, that data will be
				// added in the CameraFragment

				// Save the picture and return
				photo.saveInBackground(new SaveCallback() {

					@Override
					public void done(ParseException e) {
						if (e == null) {
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
				// Todo (extra) - delete the files of any picturs that were
				// saved onto the local device, but not Parse
				
				getActivity().setResult(Activity.RESULT_CANCELED);
				getActivity().finish();
			}
		});

		// Until the user has taken a photo, hide the preview
		photoPreview = (ParseImageView) v.findViewById(R.id.photo_preview);
		photoPreview.setVisibility(View.INVISIBLE);

		return v;
	}

	/*
	 * Old function to start Camera fragment.... probably don't need anymore
	 */
//	public void startCamera() {
//		Fragment cameraFragment = new CameraFragment();
//		FragmentTransaction transaction = getActivity().getFragmentManager()
//				.beginTransaction();
//		transaction.replace(R.id.fragmentContainer, cameraFragment);
//		transaction.addToBackStack("NewPhotoFragment");
//		transaction.commit();
//	}

	/*
	 * On resume, check and see if a photo has been set from the
	 * CameraFragment. If it has, load the image in this fragment and make the
	 * preview image visible.
	 */
	@Override
	public void onResume() {
		super.onResume();
		ParseFile photoFile = ((NewPhotoActivity) getActivity())
				.getCurrentPhoto().getImage();
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
