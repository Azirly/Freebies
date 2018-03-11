package com.example.justin.freebies;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class PostActivity extends AppCompatActivity {

    private ImageButton selectImage;
    private EditText mTitleBox;
    private EditText mDescriptionBox;

    private Button mSubmitPostButton;

    private Uri mImageUri = null;

    private static final int Gallery_Req = 1;

    private StorageReference store;
    private DatabaseReference database;

    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        store = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance().getReference().child("Blog");

        selectImage = (ImageButton) findViewById(R.id.imageSelect);

        mTitleBox = (EditText) findViewById(R.id.TitleBox);
        mDescriptionBox = (EditText) findViewById(R.id.DescriptionBox);

        mSubmitPostButton = (Button) findViewById(R.id.SubmitPostButton);

        progress = new ProgressDialog(this);

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_GET_CONTENT);
                gallery.setType("image/*");
                startActivityForResult(gallery, Gallery_Req);
            }
        });

        mSubmitPostButton.setOnClickListener(new View.OnClickListener(){
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

        if(!TextUtils.isEmpty(title_val) && !TextUtils.isEmpty(desc_val) && mImageUri != null){

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
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Gallery_Req && resultCode == RESULT_OK){
            mImageUri = data.getData();
            selectImage.setImageURI(mImageUri);
        }

    }
}
