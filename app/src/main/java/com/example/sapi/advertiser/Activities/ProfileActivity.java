package com.example.sapi.advertiser.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.sapi.advertiser.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.example.sapi.advertiser.Utils.Const.AD_UID_FIELD;
import static com.example.sapi.advertiser.Utils.Const.USERS_CHILD;
import static com.example.sapi.advertiser.Utils.Const.USER_IMG_FIELD;
import static com.example.sapi.advertiser.Utils.Const.USER_NAME_FIELD;

public class ProfileActivity extends AppCompatActivity {
    private DatabaseReference mDatabaseUsers;
    private FirebaseAuth mAuth;
    private String mUid;
    private TextView mUserName;
    private ImageView mUserImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child(USERS_CHILD);

        mAuth = FirebaseAuth.getInstance();

        mUid = getIntent().getExtras().getString(AD_UID_FIELD);


        mUserName = (TextView) findViewById(R.id.user_profile_name);
        mUserImage = (ImageView) findViewById(R.id.user_profile_image);


        mDatabaseUsers.child(mUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String user_name = (String) dataSnapshot.child(USER_NAME_FIELD).getValue();
                String user_image = (String) dataSnapshot.child(USER_IMG_FIELD).getValue();

                mUserName.setText(user_name);
                Glide.with(ProfileActivity.this).load(user_image).into(mUserImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }
}
