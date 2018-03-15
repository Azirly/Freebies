package com.example.justin.freebies;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PostActivity extends AppCompatActivity {

    private Button takeImage;
    private Button uploadImage;
    private boolean tookImage;
    private boolean selectedImage;
    private ImageView image;
    private EditText mTitleBox;
    private EditText mDescriptionBox;

    private Button mSubmitPostButton;

    private Uri mImageUri = null;
    private String mCurrentPhotoPath;

    private static final int REQUEST_IMAGE_CAPTURE = 111;
    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int Gallery_Req = 1;

    private StorageReference store;
    private DatabaseReference database;
    private StorageReference filepath;
    private String imageEncoded;

    private ProgressDialog progress;

    private GPSTracker gps;
    private double currentLat;
    private double currentLong;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        store = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance().getReference().child("Blog");

        takeImage = (Button) findViewById(R.id.takeImage);
        uploadImage = (Button) findViewById(R.id.uploadImage);
        tookImage = false;
        selectedImage = false;
        image = (ImageView) findViewById(R.id.image);

        mTitleBox = (EditText) findViewById(R.id.TitleBox);
        mDescriptionBox = (EditText) findViewById(R.id.DescriptionBox);

        mSubmitPostButton = (Button) findViewById(R.id.SubmitPostButton);

        progress = new ProgressDialog(this);

        gps = new GPSTracker(this);

        takeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!selectedImage) {
                    currentLat = gps.getLatitude();
                    currentLong = gps.getLongitude();
                    dispatchTakePictureIntent();
                } else {
                    Toast.makeText(PostActivity.this, "Already selected an image.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!tookImage) {
                    currentLat = gps.getLatitude();
                    currentLong = gps.getLongitude();
                    Intent gallery = new Intent(Intent.ACTION_GET_CONTENT);
                    gallery.setType("image/*");
                    startActivityForResult(gallery, Gallery_Req);
                } else {
                    Toast.makeText(PostActivity.this, "Already took an image.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mSubmitPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPosting();
            }
        });
    }

    private void startPosting() {

        progress.setMessage("Posting to Blog ...");
        progress.show();

        final String title_val = mTitleBox.getText().toString().trim();
        final String desc_val = mDescriptionBox.getText().toString().trim();

        if (!TextUtils.isEmpty(title_val) && !TextUtils.isEmpty(desc_val) && (imageEncoded != null || mImageUri != null)) {
            if (selectedImage) {
                StorageReference filepath = store.child("Blog_Images").child(mImageUri.getLastPathSegment());

                filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();

                        DatabaseReference newPost = database.push(); //unique ids for posts

                        newPost.child("Title").setValue(title_val);
                        newPost.child("Description").setValue(desc_val);
                        newPost.child("Image").setValue(downloadUrl.toString());
                        //newPost.child("uid").setValue(FirebaseAuth.getInstance()); trying to get user id

                        startActivity(new Intent(PostActivity.this, EventsBlogPage.class));

                        progress.dismiss();
                    }
                });
            } else {

                DatabaseReference newPost = database.push(); //unique ids for posts

                String timeStamp = new SimpleDateFormat("MM-dd-yyyy K:mm a, z").format(new Date());

                newPost.child("Title").setValue(title_val);
                newPost.child("Location").setValue(currentLat + ", " + currentLong);
                newPost.child("Time").setValue(timeStamp);
                newPost.child("Description").setValue(desc_val);
                newPost.child("Image").setValue(imageEncoded);
                //newPost.child("uid").setValue(FirebaseAuth.getInstance()); trying to get user id

                startActivity(new Intent(PostActivity.this, EventsBlogPage.class));

                progress.dismiss();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            tookImage = true;
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            image.setImageBitmap(imageBitmap);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        }
        if (requestCode == Gallery_Req && resultCode == RESULT_OK) {
            selectedImage = true;
            mImageUri = data.getData();
            image.setImageURI(mImageUri);
        }

    }

}
