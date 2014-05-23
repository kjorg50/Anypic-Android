package com.parse.anypic;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.parse.ParseFile;

/*
 * NewPhotoActivity contains two fragments that handle
 * data entry and capturing a photo.
 * The Activity manages the overall photo data.
 */
public class NewPhotoActivity extends Activity {

	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	public static final int MEDIA_TYPE_IMAGE = 1;
	private Photo photo;
	private Uri fileUri;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		photo = new Photo();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_photo);
		
		// Open the camera using an Intent
		startCamera();
		
		// After taking a picture, open the NewPhotoFragment
		FragmentManager manager = getFragmentManager();
		Fragment fragment = manager.findFragmentById(R.id.fragmentContainer);

		if (fragment == null) {
			fragment = new NewPhotoFragment();
			// add id of the FrameLayout to fill, and the fragment that the layout will hold
			manager.beginTransaction().add(R.id.fragmentContainer, fragment)
					.commit();
		}
	}

	/** Create the Intent which opens the Camera */
	private void startCamera(){
		// create Intent to take a picture and return control to the calling application
	    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

	    fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
	    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

	    // start the image capture Intent
	    startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
	        if (resultCode == RESULT_OK) {
	            // Image captured and saved to fileUri specified in the Intent
	            if(fileUri != null){
	            	Toast.makeText(this, "Image saved to:\n" +
	            			fileUri.toString(), Toast.LENGTH_LONG).show();
	            } else {
	            	Toast.makeText(this, "Error: image not saved to device", 
	            			Toast.LENGTH_LONG).show();
	            }
	        } else if (resultCode == RESULT_CANCELED) {
	            // User cancelled the image capture
	        } else {
	            // Image capture failed, advise user
	        	Toast.makeText(this, "Error: image not saved to device", 
            			Toast.LENGTH_LONG).show();
	        }
	    }
	}
	
	/** Create a file Uri for saving an image */
	private static Uri getOutputMediaFileUri(int type){
		File output = getOutputMediaFile(type);
	    if(output!=null){
	    	return Uri.fromFile(output);
	    } else {
	    	return null;
	    }
	}
	
	/** Create a File for saving an image */
	private static File getOutputMediaFile(int type){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.
		Log.i(AnypicApplication.TAG, "entering getOutputMediaFile");

	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), "Anypic");
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    // Make sure you have the permission to write to the SD Card enabled 
	    // in order to do this!!
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.i(AnypicApplication.TAG, "getOutputMediaFile failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "IMG_"+ timeStamp + ".jpg");
	    } else {
	        return null;
	    }

	    return mediaFile;
	}
	
	public Uri getPhotoFileUri(){
		return fileUri;
	}
	
	public Photo getCurrentPhoto() {
		return photo;
	}

}
