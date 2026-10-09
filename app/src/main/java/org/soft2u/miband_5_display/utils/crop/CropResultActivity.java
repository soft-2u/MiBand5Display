package org.soft2u.miband_5_display.utils.crop;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.soft2u.miband_5_display.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public final class CropResultActivity extends Activity {

    /** The image to show in the activity. */
    static Bitmap mImage;

    String crop_shape;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        crop_shape = getIntent().getStringExtra("EXTRA_CROP_SHAPE");

        setContentView(R.layout.activity_crop_result);

        ImageView imageView = findViewById(R.id.resultImageView);

        Intent intent = getIntent();
        if (mImage != null) {
            imageView.setImageBitmap(mImage);
            int sampleSize = intent.getIntExtra("SAMPLE_SIZE", 1);
            double ratio = ((int) (10 * mImage.getWidth() / (double) mImage.getHeight())) / 10d;
            int byteCount = 0;
            if (android.os.Build.VERSION.SDK_INT >= 19) {
                byteCount = mImage.getByteCount() / 1024;
            }
            String desc =
                    "("
                        + mImage.getWidth()
                        + ", "
                        + mImage.getHeight()
                        + "), Sample: "
                        + sampleSize
                        + ", Ratio: "
                        + ratio
                        + ", Bytes: "
                        + byteCount
                        + "K";
            ((TextView) findViewById(R.id.resultImageText)).setText(desc);
        } else {
            Uri imageUri = intent.getParcelableExtra("URI");
            if (imageUri != null) {
                imageView.setImageURI(imageUri);
            } else {
                Toast.makeText(this, "No image is set to show", Toast.LENGTH_LONG).show();
            }
        }
        //new
        releaseBitmap();
        finish();
//        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        releaseBitmap();
        super.onBackPressed();
    }

    public void onImageViewClicked(View view) {
        releaseBitmap();
        finish();
    }

    private void releaseBitmap() {
        if (mImage != null) {
            //mImage.recycle();
            storeImage(mImage, getBaseContext());
            //mImage = null;
        }
        storeImage(mImage, getBaseContext());
    }

    //new
    private void storeImage(Bitmap image, Context context) {
        File pictureFile = getOutputMediaFile(context);
        if (pictureFile == null) {
            Log.d("error",
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            int width, height;
            if(crop_shape.equals("crop_image_rectangle"))
            {
                width = 120;
                height = 240;
            }
            else
            {
                width = 300;
                height = 300;
            }
            Bitmap resized = Bitmap.createScaledBitmap(image, width, height, true);
            FileOutputStream fos = new FileOutputStream(pictureFile);
            resized.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d("error", "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d("error", "Error accessing file: " + e.getMessage());
        }
    }

    /** Create a File for saving an image or video */
    private File getOutputMediaFile(Context context){

        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(context.getExternalFilesDir(null), "");

//        File mediaStorageDir = new File(getApplicationContext().getFilesDir()
//                + "/DCIM/"
//                + getApplicatintext().getPackageName()
//                + "/Files");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        //String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName = "diy_background_1.png";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        //getWindow().getDecorView().findViewById(android.R.id.content).invalidate();
        return mediaFile;
    }
}