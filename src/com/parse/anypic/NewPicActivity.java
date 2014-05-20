package com.parse.anypic;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.parse.ParseFile;

/*
 * NewPicActivity contains two fragments that handle
 * data entry and capturing a photo of a given picture.
 * The Activity manages the overall picture data.
 */
public class NewPicActivity extends Activity {

	private Picture picture;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		picture = new Picture();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);

		// Begin with main data entry view,
		// NewPicFragment
		setContentView(R.layout.activity_new_meal);
		FragmentManager manager = getFragmentManager();
		Fragment fragment = manager.findFragmentById(R.id.fragmentContainer);

		if (fragment == null) {
			fragment = new NewPicFragment();
			manager.beginTransaction().add(R.id.fragmentContainer, fragment)
					.commit();
		}
	}

	public Picture getCurrentMeal() {
		return picture;
	}

}
