package com.example.sapi.advertiser.Activities;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.sapi.advertiser.Models.Advertisment;
import com.example.sapi.advertiser.R;
import com.example.sapi.advertiser.Utils.Const;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.sapi.advertiser.Utils.Const.ADS_CHILD;
import static com.example.sapi.advertiser.Utils.Const.EXTRA_AD_ID;
import static com.example.sapi.advertiser.Utils.Const.EXTRA_AD_UID;
import static com.example.sapi.advertiser.Utils.Const.USERS_CHILD;


public class ListActivity extends AppCompatActivity {
    private DatabaseReference mDatabaseAds;
    private DatabaseReference mDatabaseUsers;
    MyFirebaseRecyclerAdapter firebaseRecyclerAdapter;
    MyFirebaseRecyclerAdapter firebaseSearchRecyclerAdapter;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    @BindView(R.id.search)
    EditText mSearch;
    @BindView(R.id.ad_list)
    RecyclerView mAdList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        mAuth=FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()==null){
                    Intent loginIntent = new Intent(ListActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
                else{
                    checkUserExists();
                }
            }
        };
        ButterKnife.bind(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        mAdList.setHasFixedSize(true);
        mAdList.setLayoutManager(layoutManager);



        mDatabaseAds = FirebaseDatabase.getInstance().getReference().child(ADS_CHILD);
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child(USERS_CHILD);
        mDatabaseUsers.keepSynced(true);
        mDatabaseAds.keepSynced(true);

    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);

        firebaseRecyclerAdapter = new MyFirebaseRecyclerAdapter(
                        Advertisment.class,
                        R.layout.ad_row,
                        AdvertisementViewHolder.class,
                        mDatabaseAds);
        firebaseSearchRecyclerAdapter = new MyFirebaseRecyclerAdapter(
                Advertisment.class,
                R.layout.ad_row,
                AdvertisementViewHolder.class,
                mDatabaseAds);
        mAdList.setAdapter(firebaseRecyclerAdapter);

        mSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String mInput = mSearch.getText().toString().toLowerCase();


                if (mInput.equals("")){
                    mAdList.setAdapter(firebaseRecyclerAdapter);
                    return;
                }
                else {

                    firebaseSearchRecyclerAdapter = new MyFirebaseRecyclerAdapter(Advertisment.class,
                            R.layout.ad_row,
                            AdvertisementViewHolder.class,
                            mDatabaseAds.orderByChild(Const.AD_TITLE_FIELD)
                            .startAt(mInput)
                            .endAt(mInput + "\uf8ff"));

                    mAdList.setAdapter(firebaseSearchRecyclerAdapter);
                }
            }
        });


    }

    public static class AdvertisementViewHolder extends RecyclerView.ViewHolder{
        public View mView;
        public ImageView mUserImage;

        public AdvertisementViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            mUserImage=(ImageView) mView.findViewById(R.id.ad_user_image);

        }

        public void setUserImage(Context ctx, String image, String uid){
            mUserImage=(ImageView) mView.findViewById(R.id.ad_user_image);
            Glide.with(ctx).load(image).into(mUserImage);
        }
        public void setTitle(String title){
            TextView ad_title= (TextView) mView.findViewById(R.id.ad_title);
            ad_title.setText(title);
        }

        public void setDescription(String description){
            TextView ad_description= (TextView) mView.findViewById(R.id.ad_description);
            if(description.length()>30) {
                ad_description.setText(description.replace("\n"," ").substring(0, 30) + "...");
            }
            else {
                ad_description.setText(description.replace("\n"," "));
            }
        }

        public void setImage(Context ctx, String image){
            ImageView ad_image=(ImageView) mView.findViewById(R.id.ad_image);
            Glide.with(ctx).load(image).into(ad_image);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.action_add){
            startActivity(new Intent(ListActivity.this, AddAdvertisementActivity.class));
        }
        if(item.getItemId()==R.id.action_logout){
            logout();
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mAuth.signOut();
    }

    private void checkUserExists() {

        if(mAuth.getCurrentUser()!=null) {
            final String userId = mAuth.getCurrentUser().getUid();

            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(userId)) {
                        Intent setupIntent = new Intent(ListActivity.this, SetupActivity.class);
                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupIntent);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }


    public class MyFirebaseRecyclerAdapter  extends  FirebaseRecyclerAdapter<com.example.sapi.advertiser.Models.Advertisment, ListActivity.AdvertisementViewHolder>{


        public MyFirebaseRecyclerAdapter(Class<Advertisment> modelClass, int modelLayout, Class<ListActivity.AdvertisementViewHolder> viewHolderClass, Query ref) {
            super(modelClass, modelLayout, viewHolderClass, ref);
        }

        @Override
        protected void populateViewHolder(ListActivity.AdvertisementViewHolder viewHolder, Advertisment model, int position) {
            final String ad_key = getRef(position).getKey();
            final String ad_uid = model.getUid();

            viewHolder.setTitle(model.getTitle());
            viewHolder.setDescription(model.getDescription());
            viewHolder.setImage(getApplicationContext(), model.getImage());
            viewHolder.setUserImage(getApplicationContext(), model.getUserImage(), model.getUid());

            viewHolder.mView.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View view) {
                    Intent adDetailIntent = new Intent(ListActivity.this, AdvertismentDetailActivity.class);
                    adDetailIntent.putExtra(EXTRA_AD_ID, ad_key);
                    startActivity(adDetailIntent);
                }
            });

            viewHolder.mUserImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent userProfileIntent = new Intent(ListActivity.this, ProfileActivity.class);
                    userProfileIntent.putExtra(EXTRA_AD_UID, ad_uid);
                    startActivity(userProfileIntent);
                }
            });
        }
    };




}
