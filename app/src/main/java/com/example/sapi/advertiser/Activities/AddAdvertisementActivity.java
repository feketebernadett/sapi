package com.example.sapi.advertiser.Activities;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.example.sapi.advertiser.R;
import com.glide.slider.library.Animations.DescriptionAnimation;
import com.glide.slider.library.SliderLayout;
import com.glide.slider.library.SliderTypes.BaseSliderView;
import com.glide.slider.library.SliderTypes.DefaultSliderView;
import com.glide.slider.library.SliderTypes.TextSliderView;
import com.glide.slider.library.Tricks.ViewPagerEx;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
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

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.sapi.advertiser.Utils.Const.ADS_CHILD;
import static com.example.sapi.advertiser.Utils.Const.AD_DESC_FIELD;
import static com.example.sapi.advertiser.Utils.Const.AD_IMAGES_CHILD;
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


    @BindView(R.id.AdTitle)
    EditText mAdTitle;
    @BindView(R.id.AdDescription)
    EditText mAdDescription;
    @BindView(R.id.AdLocation)
    Button mLocationButton;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;
    @BindView(R.id.SubmitButton)
    Button mSubmitButton;

    @BindView(R.id.imagegallery)
    LinearLayout imageGallery;
    @BindView(R.id.imageSelect)
    ImageButton mImageSelect;
    @BindView(R.id.horizontal_scroll)
    HorizontalScrollView horizontal_scroll;

    private ArrayList<Uri> mImageUriList = new ArrayList<Uri>();;

    private StorageReference mStorage;
    private DatabaseReference mDatabaseAds;
    private DatabaseReference mDatabaseUsers;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_advertisement);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabaseAds = FirebaseDatabase.getInstance().getReference().child(ADS_CHILD);
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child(USERS_CHILD).child(mCurrentUser.getUid());

        mLocationButton.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View view) {
                startPlacePicker();

            }
        });
        mSubmitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                startAdding();
            }
        });
        mImageSelect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                startGallery();
            }
        });
    }

    private void startGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(galleryIntent, RC_GALLERY);
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GALLERY && resultCode == RESULT_OK) {
            // Get the Image from data
            mImageUriList = new ArrayList<>();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            ArrayList<String> imagesEncodedList = new ArrayList<String>();
            String imageEncoded;
            if (data.getData() != null) {

                Uri mImageUri = data.getData();
                mImageUriList.add(mImageUri);

                imageGallery.setVisibility(View.VISIBLE);
                mImageSelect.setVisibility(View.GONE);

            } else {
                if (data.getClipData() != null) {
                    ClipData mClipData = data.getClipData();
                    for (int i = 0; i < mClipData.getItemCount(); i++) {

                        ClipData.Item item = mClipData.getItemAt(i);
                        Uri uri = item.getUri();
                        mImageUriList.add(uri);

                    }
                }
            }
            imageGallery.removeAllViews();
            for (int i = 0; i < (mImageUriList.size()<10?mImageUriList.size():10); i++) {
                ImageView imageView = new ImageView(this);
                imageView.setId(i);
                imageView.setPadding(20, 20, 20, 20);
                imageView.setLayoutParams(new LinearLayout.LayoutParams(300,300));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Glide.with(getApplicationContext()).load(mImageUriList.get(i)).into(imageView);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) { startGallery();}});
                imageGallery.addView(imageView);
            }
            horizontal_scroll.setVisibility(View.VISIBLE);
            mImageSelect.setVisibility(View.GONE);


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

        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(description) && !mImageUriList.isEmpty() && !location.equals(R.string.location)) {
            mProgressBar.setVisibility(View.VISIBLE);
            final DatabaseReference newAd = mDatabaseAds.push();
            final DatabaseReference newAdImages = newAd.child(AD_IMAGES_CHILD);
            newAd.child(AD_TITLE_FIELD).setValue(title);
            newAd.child(AD_DESC_FIELD).setValue(description);
            newAd.child(AD_LOCATION_FIELD).setValue(location);
            newAd.child(AD_UID_FIELD).setValue(mCurrentUser.getUid());

            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    newAd.child(AD_USERIMG_FIELD).setValue(dataSnapshot.child(USER_IMG_FIELD).getValue());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            for (Uri mImageUri : mImageUriList) {
                StorageReference filepath = mStorage.child(STORAGE_AD_IMAGES).child(mImageUri.getLastPathSegment());
                filepath.putFile(mImageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                DatabaseReference newAdImage = newAdImages.push();
                                newAdImage.child(AD_IMG_FIELD).setValue(downloadUrl.toString());
                                newAd.child(AD_IMG_FIELD).setValue(downloadUrl.toString());
                            }
                        });
            }

            startActivity(new Intent(AddAdvertisementActivity.this, ListActivity.class));


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
