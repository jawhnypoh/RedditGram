package com.example.redditimages.redditgram;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.Random;

import com.example.redditimages.redditgram.Utils.DownloadImageTask;

import java.io.FileOutputStream;

/**
 * Created by Tam on 3/22/2018.
 */


public class DetailedImageActivity extends AppCompatActivity {

    private static final String TAG = DetailedImageActivity.class.getSimpleName();
    private String imageUrl;
    private String imageFileName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_image);
        getWindow().getDecorView().setBackgroundColor(Color.BLACK);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            imageUrl = bundle.getString("image");
            Log.d(TAG, "Image URL: " + imageUrl);
            if (imageUrl != null) {
                new DownloadImageTask((ImageView) findViewById(R.id.imageView))
                        .execute(imageUrl);
            }
        } else {
            Log.d(TAG, "Image URL not found");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_download, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_download:
                if (checkWriteExternalPermission()) {
                    downloadFile(imageUrl);

                    Toast.makeText(DetailedImageActivity.this, "Downloading image " + imageFileName,
                            Toast.LENGTH_LONG).show();
                    return true;
                }
                else {
                    Log.d(TAG, "Permissions not granted!");
                    requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 1);
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{permissionName}, permissionRequestCode);
    }
/*
    public void saveFileToStorage() {
        String filepath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String extraPath = "/image-"+System.currentTimeMillis()+".png";
        filepath += extraPath;

        FileOutputStream fos = null;
        fos = new FileOutputStream(filepath);

        bmImg.compress(Bitmap.CompressFormat.PNG, 75, fos);

//LOAD IMAGE FROM FILE
        Drawable d = Drawable.createFromPath(filepath);
        return d;
    }*/

    public void downloadFile(String uRl) {
        File direct = new File(Environment.getExternalStorageDirectory()
                + "/RedditGram");

        if (!direct.exists()) {
            direct.mkdirs();
        }

        DownloadManager mgr = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);

        Uri downloadUri = Uri.parse(uRl);
        DownloadManager.Request request = new DownloadManager.Request(
                downloadUri);

        imageFileName = "image-"+System.currentTimeMillis()+".png";
        request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI
                        | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false).setTitle("image-"+System.currentTimeMillis()+".png")
                .setDescription("")
                .setDestinationInExternalPublicDir("/RedditGram", imageFileName);
        mgr.enqueue(request);
    }

    private boolean checkWriteExternalPermission()
    {
        String permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }
}


