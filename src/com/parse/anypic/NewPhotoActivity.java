package com.parse.anypic;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.IOUtils;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.SaveCallback;
	
/*
 * NewPhotoActivity contains two fragments that handle
 * data entry and capturing a photo.
 * The Activity manages the overall photo data.
 */
public class NewPhotoActivity extends Activity {

	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private static final int MEDIA_TYPE_IMAGE = 1;
	private static final int REQ_WIDTH = 560;
	private static final int REQ_HEIGHT = 560;
	
	private Photo photo;
	private Uri fileUri;
	private ParseFile image;
	private ParseFile thumbnail;

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
	public void startCamera(){
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
	            	
	            	// TODO (future) - save photo files in background with Async task
	            	// Convert the image into ParseFiles
	            	if(fileUri.getPath() != null){
	            		savePhotoFiles(fileUri.getPath());
	            	} else { 
	            		Log.i(AnypicApplication.TAG, "Error finding file path");
	            		cancelActivity(); 
	            	}
	            } else {
	            	// fileUri was null
	            	Toast.makeText(this, "Error: image not saved to device", 
	            			Toast.LENGTH_LONG).show();
	            	// return to previous activity after failure
	            	cancelActivity();
	            }
	        } else if (resultCode == RESULT_CANCELED) {
	            // User cancelled the image capture, return to previous activity
	        	cancelActivity();
	        } else {
	            // Image capture failed, advise user
	        	Toast.makeText(this, "Error: image not saved to device", 
            			Toast.LENGTH_LONG).show();
	        	cancelActivity();
	        }
	    }
	}
	
	public void cancelActivity(){
		setResult(Activity.RESULT_CANCELED);
		finish();
	}
	
	/**
	 * Takes the photo captured by the user, and saves the image and it's 
	 * scaled-down thumbnail as ParseFiles. This occurs after the user captures
	 * a photo, but before the user chooses to publish to Anypic. Thus, these
	 * ParseFiles can later be associated with the Photo object itself. 
	 * 
	 * @param data The byte array containing the image data
	 */
	private void savePhotoFiles(String pathToFile) {

		// Convert to Bitmap to assist with resizing
		Bitmap anypicImage = decodeSampledBitmapFromFile(pathToFile, REQ_WIDTH, REQ_HEIGHT);
		//Bitmap anypicImage = BitmapFactory.decodeByteArray(data, 0, data.length);
		
		// Override Android default landscape orientation and save portrait
		Matrix matrix = new Matrix();
		matrix.postRotate(90);
		Bitmap rotatedImage = Bitmap.createBitmap(anypicImage, 0,
				0, anypicImage.getWidth(), anypicImage.getHeight(),
				matrix, true);
		
		// make thumbnail with size of 86 pixels
		Bitmap anypicThumbnail = Bitmap.createScaledBitmap(rotatedImage, 86, 86, false);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		rotatedImage.compress(Bitmap.CompressFormat.JPEG, 100, bos);
		byte[] rotatedData = bos.toByteArray();
		
		bos.reset(); // reset the stream to prepare for the thumbnail
		anypicThumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bos);
		byte[] thumbnailData = bos.toByteArray();

		try {
			// close the byte array output stream
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
		// Create the ParseFiles and save them in the background
		image = new ParseFile("photo.jpg", rotatedData);
		thumbnail = new ParseFile("photo_thumbnail.jpg", thumbnailData);
		image.saveInBackground( new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e != null) {
					Toast.makeText(getApplicationContext(),
							"Error saving image file: " + e.getMessage(),
							Toast.LENGTH_LONG).show();
				} else {
					// saved image to Parse
				}
			}
		});
		thumbnail.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e != null) {
					Toast.makeText(getApplicationContext(),
							"Error saving thumbnail file: " + e.getMessage(),
							Toast.LENGTH_LONG).show();
				} else {
					// saved image to Parse
				}				
			}
		});
		
		Log.i(AnypicApplication.TAG, "Finished saving the photos to ParseFiles!");
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
	
	/** 
	 * Create a File for saving an image. Uses the environment external
	 * storage directory. Creates each file using unique timestamp. 
	 * 
	 * Returns the File object for the new image, or null if there was
	 * some error creating the file.
	 */
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
	
	/**
	 * This function takes the path to our file from which we want to create
	 * a Bitmap, and decodes it using a smaller sample size in an effort to 
	 * avoid an OutOfMemoryError.
	 * 
	 * See: http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
	 * 
	 * @param path The path to the file resource 
	 * @param reqWidth The required width that the image should be prepared for
	 * @param reqHeight The required height that the image should be prepared for
	 * @return A Bitmap resource that has been scaled down to an appropriate size, so that it can conserve memory
	 * 
	 */
	public static Bitmap decodeSampledBitmapFromFile(String path,
									int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(path, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeFile(path, options);
	}
	
	/**
	 * This is a helper function used to calculate the appropriate sampleSize 
	 * for use in the decodeSampledBitmapFromFile() function.
	 * 
	 * See: http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
	 * 
	 * @param options The BitmapOptions for the Bitmap resource that we want to scale down
	 * @param reqWidth The target width of the UI element in which we want to fit the Bitmap
	 * @param reqHeight The target height of the UI element in which we want to fit the Bitmap
	 * @return A sample size value that is a power of two based on a target width and height
	 */
	public static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	/*** Getters ***/
	public Uri getPhotoFileUri(){
		return fileUri;
	}
	
	public ParseFile getImageFile(){
		return image;
	}
	
	public ParseFile getThumbnailFile(){
		return thumbnail;
	}
	
	public Photo getCurrentPhoto() {
		return photo;
	}
	
}
