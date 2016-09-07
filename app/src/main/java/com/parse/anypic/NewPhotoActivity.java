package com.parse.anypic;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.DocumentsContract;
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
		
		// Open the camera or gallery using an Intent
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

	/** Create the Intent which opens the Camera or some image gallery */
	public void startCamera(){
		fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
		
		// Camera intents
	    final List<Intent> cameraIntents = new ArrayList<Intent>();
	    final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    final PackageManager packageManager = getPackageManager();
	    final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
	    for(ResolveInfo res : listCam) {
	        final String packageName = res.activityInfo.packageName;
	        final Intent intent = new Intent(captureIntent);
	        intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
	        intent.setPackage(packageName);
	        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
	        cameraIntents.add(intent);
	    }
	    
	    // Filesystem intents (to upload from Gallery, Dropbox, etc.)
	    final Intent galleryIntent = new Intent();
	    galleryIntent.setType("image/*");
	    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
		
	    // Chooser of filesystem options.
	    final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");
	    
	    // Add the camera options.
	    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));

	    // start the image capture Intent
	    startActivityForResult(chooserIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
	        if (resultCode == RESULT_OK) {
	        	final boolean isCamera;
	        	
	        	// first try and determine if the picture was taken by the camera
	        	if(data == null){
	        		Log.i(AnypicApplication.TAG, "intent data was null");
	        		isCamera = true;
	        	}
	        	else {
	        		final String action = data.getAction();
	                if(action == null)
	                {
	                	Log.i(AnypicApplication.TAG, "Intent data.getAction() was null");
	                    isCamera = false;
	                }
	                else
	                {
	                    isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
	                    Log.i(AnypicApplication.TAG, "Intent data.getAction was equal to camera capture? " + isCamera);
	                }
	        	}
	        	
	        	// Now, determine the file URI of where the image is, either from
	        	// the camera, or another file URI on the device storage
	        	Uri selectedImageUri;
	            if(isCamera)
	            {
	                selectedImageUri = fileUri;
	            }
	            else // the image did not come from the camera
	            {
	            	// if data is null, then selectImageUri = null, else selectImageUri = data.getData()
	                selectedImageUri = data == null ? null : data.getData();
	                
	                Log.i(AnypicApplication.TAG, "selectedImageUri is "+ selectedImageUri);
	                if(selectedImageUri != null){
	                	// we need to decode the file from its URI
	                	String realPath = getPathFromUri(getApplicationContext(), selectedImageUri);
	                	Log.i(AnypicApplication.TAG, "selectedImageUri *real path* is "+ realPath);
	                	savePhotoFiles(realPath);
	                	return;
	                }
	            }
	        	
	            // Image captured and saved to fileUri specified in the Intent
	            if(selectedImageUri != null){
//	            	Toast.makeText(this, "Image saved to:\n" +
//	            			selectedImageUri.toString(), Toast.LENGTH_LONG).show();	            	
	            	
	            	// Convert the image into ParseFiles
	            	if(selectedImageUri.getPath() != null){
	            		savePhotoFiles(selectedImageUri.getPath());
	            	} else { 
	            		Log.i(AnypicApplication.TAG, "Error finding file path");
	            		cancelActivity(); 
	            	}
	            } else {
	            	// selectedImageUri was null
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
	
	/* ***************************************************************** 
	 * Section taken from http://stackoverflow.com/a/20559175/1092403
	 * *****************************************************************
	 */
	
	/**
	 * Get a file path from a Uri. This will get the the path for Storage Access
	 * Framework Documents, as well as the _data field for the MediaStore and
	 * other file-based ContentProviders.
	 *
	 * @param context The context.
	 * @param uri The Uri to query.
	 * @author paulburke
	 */
	@SuppressLint("NewApi")
	public static String getPathFromUri(final Context context, final Uri uri) {

	    final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

	    // DocumentProvider
	    if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
	        // ExternalStorageProvider
	        if (isExternalStorageDocument(uri)) {
	            final String docId = DocumentsContract.getDocumentId(uri);
	            final String[] split = docId.split(":");
	            final String type = split[0];

	            if ("primary".equalsIgnoreCase(type)) {
	                return Environment.getExternalStorageDirectory() + "/" + split[1];
	            }

	            // TODO handle non-primary volumes
	        }
	        // DownloadsProvider
	        else if (isDownloadsDocument(uri)) {

	            final String id = DocumentsContract.getDocumentId(uri);
	            final Uri contentUri = ContentUris.withAppendedId(
	                    Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

	            return getDataColumn(context, contentUri, null, null);
	        }
	        // MediaProvider
	        else if (isMediaDocument(uri)) {
	            final String docId = DocumentsContract.getDocumentId(uri);
	            final String[] split = docId.split(":");
	            final String type = split[0];

	            Uri contentUri = null;
	            if ("image".equals(type)) {
	                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	            } else if ("video".equals(type)) {
	                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
	            } else if ("audio".equals(type)) {
	                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	            }

	            final String selection = "_id=?";
	            final String[] selectionArgs = new String[] {
	                    split[1]
	            };

	            return getDataColumn(context, contentUri, selection, selectionArgs);
	        }
	    }
	    // MediaStore (and general)
	    else if ("content".equalsIgnoreCase(uri.getScheme())) {
	        return getDataColumn(context, uri, null, null);
	    }
	    // File
	    else if ("file".equalsIgnoreCase(uri.getScheme())) {
	        return uri.getPath();
	    }

	    return null;
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 *
	 * @param context The context.
	 * @param uri The Uri to query.
	 * @param selection (Optional) Filter used in the query.
	 * @param selectionArgs (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri, String selection,
	        String[] selectionArgs) {

	    Cursor cursor = null;
	    final String column = "_data";
	    final String[] projection = {
	            column
	    };

	    try {
	        cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
	                null);
	        if (cursor != null && cursor.moveToFirst()) {
	            final int column_index = cursor.getColumnIndexOrThrow(column);
	            return cursor.getString(column_index);
	        }
	    } finally {
	        if (cursor != null)
	            cursor.close();
	    }
	    return null;
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
	    return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
	    return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
	    return "com.android.providers.media.documents".equals(uri.getAuthority());
	}
	
	/* *************************************************************************** 
	 * 									END SECTION
	 * ***************************************************************************
	 */
	
	/**
	 * Takes the photo captured by the user, and saves the image and it's 
	 * scaled-down thumbnail as ParseFiles. This occurs after the user captures
	 * a photo, but before the user chooses to publish to Anypic. Thus, these
	 * ParseFiles can later be associated with the Photo object itself. 
	 * 
	 * @param data The byte array containing the image data
	 * TODO (future) - do this in background AsyncTask
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
