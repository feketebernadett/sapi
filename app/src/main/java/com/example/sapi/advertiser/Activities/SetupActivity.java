package com.example.sapi.advertiser.Activities;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.example.sapi.advertiser.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.List;

import static com.example.sapi.advertiser.Utils.Const.STORAGE_USR_IMAGES;
import static com.example.sapi.advertiser.Utils.Const.USERS_CHILD;
import static com.example.sapi.advertiser.Utils.Const.USER_IMG_FIELD;
import static com.example.sapi.advertiser.Utils.Const.USER_NAME_FIELD;

public class SetupActivity extends AppCompatActivity {

    private ImageButton mSetupImageButton;
    private EditText mName;
    private Button mSubmitButton;
    private ProgressBar mProgress;

    private Uri mImageUri = null;

    private static final int GALLERY_REQUEST = 1;

    private DatabaseReference mDatabaseUsers;
    private FirebaseAuth mAuth;
    private StorageReference mStorageImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child(USERS_CHILD);
        mAuth = FirebaseAuth.getInstance();
        mStorageImage = FirebaseStorage.getInstance().getReference().child(STORAGE_USR_IMAGES);

        mSetupImageButton = (ImageButton) findViewById(R.id.setupImageButton);
        mName = (EditText) findViewById(R.id.setupName);
        mSubmitButton=(Button) findViewById(R.id.setupButton);
        mProgress = (ProgressBar) findViewById(R.id.progressBar);

        mSetupImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);

            }
        });

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSetupAccount();
            }
        });
    }

    private void startSetupAccount() {

        final String name = mName.getText().toString().trim();
        final String userId= mAuth.getCurrentUser().getUid();
        if(!TextUtils.isEmpty(name) && mImageUri!=null){

            mProgress.setVisibility(View.VISIBLE);

            StorageReference filepath = mStorageImage.child(mImageUri.getLastPathSegment());
            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String downloadUri = taskSnapshot.getDownloadUrl().toString();

                    mDatabaseUsers.child(userId).child(USER_NAME_FIELD).setValue(name);
                    mDatabaseUsers.child(userId).child(USER_IMG_FIELD).setValue(downloadUri);

                    mProgress.setVisibility(View.INVISIBLE);

                    Intent listIntent = new Intent(SetupActivity.this, ListActivity.class);
                    listIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(listIntent);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GALLERY_REQUEST && resultCode==RESULT_OK){

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .setAspectRatio(1,1)
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mImageUri = result.getUri();

                mSetupImageButton.setImageURI(mImageUri);


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }
}
