package com.example.sapi.advertiser.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.text.TextUtilsCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.sapi.advertiser.R;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import static com.example.sapi.advertiser.Utils.Const.ADS_CHILD;
import static com.example.sapi.advertiser.Utils.Const.AD_DESC_FIELD;
import static com.example.sapi.advertiser.Utils.Const.AD_IMG_FIELD;
import static com.example.sapi.advertiser.Utils.Const.AD_LOCATION_FIELD;
import static com.example.sapi.advertiser.Utils.Const.AD_TITLE_FIELD;
import static com.example.sapi.advertiser.Utils.Const.AD_UID_FIELD;
import static com.example.sapi.advertiser.Utils.Const.AD_USERIMG_FIELD;
import static com.example.sapi.advertiser.Utils.Const.RC_GALLERY;
import static com.example.sapi.advertiser.Utils.Const.RC_PLACE_PICKER;
import static com.example.sapi.advertiser.Utils.Const.STORAGE_AD_IMAGES;
import static com.example.sapi.advertiser.Utils.Const.USERS_CHILD;
import static com.example.sapi.advertiser.Utils.Const.USER_IMG_FIELD;

public class AddAdvertisementActivity extends AppCompatActivity {

    private ImageButton mSelectImage;
    private EditText mAdTitle;
    private EditText mAdDescription;
    private Uri mImageUri=null;


    private Button mSubmitButton;
    private Button mLocationButton;

    private StorageReference mStorage;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseUsers;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    private ProgressBar mProgressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_advertisement);

        mAuth=FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child(ADS_CHILD);
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child(USERS_CHILD).child(mCurrentUser.getUid());

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mSelectImage = (ImageButton) findViewById(R.id.imageSelect);
        mAdTitle = (EditText) findViewById(R.id.AdTitle);
        mAdDescription = (EditText) findViewById(R.id.AdDescription);
        mSubmitButton = (Button) findViewById(R.id.SubmitButton);
        mLocationButton = (Button) findViewById(R.id.AdLocation);

        mSelectImage.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, RC_GALLERY);
            }
        });
        mLocationButton.setOnClickListener(new View.OnClickListener(){

            @Override

            public void onClick(View view) {
                startPlacePicker();

            }
        });
        mSubmitButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                startAdding();
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==RC_GALLERY && resultCode==RESULT_OK){
            mImageUri = data.getData();
            mSelectImage.setImageURI(mImageUri);
        }

        if (requestCode == RC_PLACE_PICKER
                && resultCode == Activity.RESULT_OK) {

            final Place place = PlacePicker.getPlace(this, data);

            mLocationButton.setText(place.getAddress());
        }
    }

    private void startAdding() {

        final String title = mAdTitle.getText().toString().trim();
        final String description = mAdDescription.getText().toString().trim();
        final String location = mLocationButton.getText().toString().trim();

        if(!TextUtils.isEmpty(title) && !TextUtils.isEmpty(description) && mImageUri!=null && !location.equals(R.string.location)){
            mProgressBar.setVisibility(View.VISIBLE);
            StorageReference filepath = mStorage.child(STORAGE_AD_IMAGES).child(mImageUri.getLastPathSegment());
            filepath.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            final Uri downloadUrl = taskSnapshot.getDownloadUrl();

                            final DatabaseReference newAd = mDatabase.push();

                            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    newAd.child(AD_TITLE_FIELD).setValue(title);
                                    newAd.child(AD_DESC_FIELD).setValue(description);
                                    newAd.child(AD_IMG_FIELD).setValue(downloadUrl.toString());
                                    newAd.child(AD_LOCATION_FIELD).setValue(location);

                                    newAd.child(AD_UID_FIELD).setValue(mCurrentUser.getUid());
                                    newAd.child(AD_USERIMG_FIELD).setValue(dataSnapshot.child(USER_IMG_FIELD).getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                startActivity(new Intent(AddAdvertisementActivity.this, ListActivity.class));
                                            }
                                        }
                                    });
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            mProgressBar.setVisibility(View.INVISIBLE);
                        }
                    });
        }
    }

    private void startPlacePicker() {
        try {
            PlacePicker.IntentBuilder intentBuilder =
                    new PlacePicker.IntentBuilder();

            Intent intent = intentBuilder.build(AddAdvertisementActivity.this);
            //TODO: If you want to change the place picker's default behavior, you can use the builder to set the initial latitude and longitude bounds of the map displayed by the place picker. Call setLatLngBounds() on the builder, passing in a LatLngBounds to set the initial latitude and longitude bounds. These bounds define an area called the 'viewport'. By default, the viewport is centered on the device's location, with the zoom at city-block level.
            startActivityForResult(intent, RC_PLACE_PICKER);

        } catch (GooglePlayServicesRepairableException
                | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

}
