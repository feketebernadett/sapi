package com.example.sapi.advertiser.Activities;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.sapi.advertiser.R;
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

import static com.example.sapi.advertiser.Utils.Const.ADS_CHILD;
import static com.example.sapi.advertiser.Utils.Const.AD_DESC_FIELD;
import static com.example.sapi.advertiser.Utils.Const.AD_IMG_FIELD;
import static com.example.sapi.advertiser.Utils.Const.AD_TITLE_FIELD;
import static com.example.sapi.advertiser.Utils.Const.AD_UID_FIELD;
import static com.example.sapi.advertiser.Utils.Const.EXTRA_AD_ID;

// TODO: rewrite this with fragments
public class AdvertismentDetailActivity extends FragmentActivity implements OnMapReadyCallback {

    private String mAd_key = null;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private ImageView mAdDetailImage;
    private TextView mAdDetailTitle;
    private TextView mAdDetailDescription;

    private Button mRemoveAdButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertisment_detail);

        mDatabase = FirebaseDatabase.getInstance().getReference().child(ADS_CHILD);

        mAuth = FirebaseAuth.getInstance();

        mAd_key = getIntent().getExtras().getString(EXTRA_AD_ID);

        mAdDetailDescription = (TextView) findViewById(R.id.ad_detail_description);
        mAdDetailTitle = (TextView) findViewById(R.id.ad_detail_title);
        mAdDetailImage = (ImageView) findViewById(R.id.ad_detail_image);
        mRemoveAdButton = (Button) findViewById(R.id.ad_detail_remove);
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mDatabase.child(mAd_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String ad_title = (String) dataSnapshot.child(AD_TITLE_FIELD).getValue();
                    String ad_description = (String) dataSnapshot.child(AD_DESC_FIELD).getValue();
                    String ad_image = (String) dataSnapshot.child(AD_IMG_FIELD).getValue();
                    String ad_uid = (String) dataSnapshot.child(AD_UID_FIELD).getValue();

                    mAdDetailTitle.setText(ad_title);
                    mAdDetailDescription.setText(ad_description);

                    Glide.with(getApplicationContext()).load(ad_image).into(mAdDetailImage);
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null && user.getUid().equals(ad_uid)) {
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

        mRemoveAdButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
               mDatabase.child(mAd_key).removeValue();
                Intent listIntent = new Intent(AdvertismentDetailActivity.this, ListActivity.class);
                startActivity(listIntent);

            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Marker"));
    }
}
