package com.example.tifigosocialmedia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tifigosocialmedia.Adapters.AdapterPosts;
import com.example.tifigosocialmedia.Models.ModelPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ThereProfileActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;

    RecyclerView postsRecyclerView;

    ImageView avatarIv, coverIv;
    TextView nameTv, emailTv, phoneTv;

    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;
    String myUid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_profile);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Профиль");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        avatarIv = findViewById(R.id.avatarIv);
        coverIv = findViewById(R.id.coverIv);
        nameTv = findViewById(R.id.nameTv);
        emailTv = findViewById(R.id.emailTv);
        phoneTv = findViewById(R.id.phoneTv);

        postsRecyclerView = findViewById(R.id.recyclerview_posts);

        firebaseAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");

        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    String name = ""+ds.child("name").getValue();
                    String email = ""+ds.child("email").getValue();
                    String phone = ""+ds.child("phone").getValue();
                    String image = ""+ds.child("image").getValue();
                    String cover = ""+ds.child("cover").getValue();
                    String isAdmin = ""+ds.child("isAdmin").getValue();

                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);

                    if(isAdmin.equals("yes")){
                        nameTv.setTextColor(Color.BLUE);
                    }

                    try {
                        Picasso.get().load(image).into(avatarIv);

                    }catch (Exception e){
                        Picasso.get().load(R.drawable.ic_default_img_white).into(avatarIv);

                    }

                    try {
                        Picasso.get().load(cover).into(coverIv);

                    }catch (Exception e){

                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        postList = new ArrayList<>();


        checkUserStatus();

        loadHistPosts();


    }

    private void loadHistPosts() {

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        postsRecyclerView.setLayoutManager(layoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    postList.add(myPosts);

                    adapterPosts = new AdapterPosts(ThereProfileActivity.this, postList);

                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ThereProfileActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void  searchHistPosts(final String searchQuery){
        LinearLayoutManager layoutManager = new LinearLayoutManager(ThereProfileActivity.this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        postsRecyclerView.setLayoutManager(layoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    assert myPosts != null;
                    if (myPosts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            myPosts.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())){
                        postList.add(myPosts);
                    }

                    adapterPosts = new AdapterPosts(ThereProfileActivity.this, postList);

                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ThereProfileActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){

            myUid = user.getUid();

        } else{
            String timestamp = String.valueOf(System.currentTimeMillis());
            checkOnlineStatus(timestamp);
            Intent intent = new Intent(ThereProfileActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);

        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (!TextUtils.isEmpty(s)){
                    searchHistPosts(s);
                }else {
                    loadHistPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!TextUtils.isEmpty(s)){
                    searchHistPosts(s);
                }else {
                    loadHistPosts();
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkOnlineStatus(String status){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);


        dbRef.updateChildren(hashMap);
    }

    @Override
    public void onStart() {
        checkUserStatus();

        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();

        String timestamp = String.valueOf(System.currentTimeMillis());

        checkOnlineStatus(timestamp);

    }

    @Override
    public void onResume() {

        checkOnlineStatus("online");

        super.onResume();
    }
}