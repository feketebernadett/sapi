package com.example.sapi.advertiser.Activities;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.example.sapi.advertiser.Models.Advertisment;
import com.example.sapi.advertiser.Models.User;
import com.example.sapi.advertiser.R;
import com.glide.slider.library.Animations.DescriptionAnimation;
import com.glide.slider.library.SliderLayout;
import com.glide.slider.library.SliderTypes.DefaultSliderView;
import com.glide.slider.library.SliderTypes.TextSliderView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.sapi.advertiser.Utils.Const.ADS_CHILD;
import static com.example.sapi.advertiser.Utils.Const.EXTRA_AD_ID;
import static com.example.sapi.advertiser.Utils.Const.EXTRA_AD_UID;
import static com.example.sapi.advertiser.Utils.Const.USERS_CHILD;

// TODO: rewrite this with fragments
public class AdvertismentDetailActivity extends FragmentActivity implements OnMapReadyCallback {

    private String mAd_key = null;
    private String mUser_key = null;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    //@BindView(R.id.ad_detail_image) private ImageView mAdDetailImage;
    @BindView(R.id.ad_detail_title)  TextView mAdDetailTitle;
    @BindView(R.id.ad_detail_description)  TextView mAdDetailDescription;

    @BindView(R.id.ad_detail_remove)  Button mRemoveAdButton;
    private GoogleMap mGoogleMap;
    @BindView(R.id.imageSlider)
    SliderLayout mSlider;
    @BindView(R.id.ad_detail_user_image) ImageView mUserImage;
    @BindView(R.id.ad_detail_user_name) TextView mUserName;
    @BindView(R.id.call) Button mCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertisment_detail);

        ButterKnife.bind(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();

        mAd_key = getIntent().getExtras().getString(EXTRA_AD_ID);
        mUser_key = getIntent().getExtras().getString(EXTRA_AD_UID);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        DatabaseReference adRef = mDatabase.child(ADS_CHILD).child(mAd_key);
        adRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Advertisment ad = dataSnapshot.getValue(Advertisment.class);

                    mAdDetailTitle.setText(ad.title);
                    mAdDetailDescription.setText(ad.description);

                    mGoogleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(ad.locationLat, ad.locationLng))
                            .title(ad.location)) ;
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(ad.locationLat, ad.locationLng),150));

                    initSlider(ad.adImages);
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null && user.getUid().equals(ad.uid)) {
                        mRemoveAdButton.setVisibility(View.VISIBLE);
                    }
                }
                else{
                    onBackPressed();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        DatabaseReference userRef = mDatabase.child(USERS_CHILD).child(mUser_key);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);

                    mUserName.setText(user.firstName);
                    mCall.setTag(user.phonenumber);

                    Glide.with(getApplicationContext()).load(user.image).into(mUserImage);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mRemoveAdButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
               mDatabase.child(mAd_key).removeValue();
                Intent listIntent = new Intent(AdvertismentDetailActivity.this, ListActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(listIntent);

            }
        });

        mCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = (String)mCall.getTag();
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                startActivity(intent);

            }
        });

    }

    private void initSlider(Map<String,String> images) {
        mSlider.removeAllSliders();
        for (String image : images.values()) {
            TextSliderView textSliderView = new TextSliderView(this);

            textSliderView
                    .description("")
                    .image(image)
                    .setBitmapTransformation(new CenterCrop())
                ;

            mSlider.addSlider(textSliderView);
        }
        mSlider.setPresetTransformer(SliderLayout.Transformer.Default);
        mSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mSlider.setCustomAnimation(new DescriptionAnimation());
        mSlider.setDuration(4000);
        mSlider.stopAutoCycle();

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

    }
}
