package com.example.tifigosocialmedia.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.tifigosocialmedia.Adapters.AdapterChatlist;
import com.example.tifigosocialmedia.GroupCreateActivity;
import com.example.tifigosocialmedia.MainActivity;
import com.example.tifigosocialmedia.Models.ModelChat;
import com.example.tifigosocialmedia.Models.ModelChatlist;
import com.example.tifigosocialmedia.Models.ModelUsers;
import com.example.tifigosocialmedia.R;
import com.example.tifigosocialmedia.SettingsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ChatListFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;
    List<ModelChatlist> chatlistList;
    List<ModelUsers> usersList;
    DatabaseReference reference;
    FirebaseUser currentUser;
    AdapterChatlist adapterChatlist;

    String myUid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        recyclerView = view.findViewById(R.id.recyclerView);

        chatlistList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatlistList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelChatlist chatlist = ds.getValue(ModelChatlist.class);
                    chatlistList.add(chatlist);
                }
                loadChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }

    private void loadChats() {
        usersList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelUsers user = ds.getValue(ModelUsers.class);
                    for(ModelChatlist chatlist: chatlistList){
                        if (user.getUid() != null && user.getUid().equals(chatlist.getId())){
                            usersList.add(user);
                            break;
                        }
                    }

                    adapterChatlist = new AdapterChatlist(getContext(), usersList);

                    recyclerView.setAdapter(adapterChatlist);

                    for(int i=0; i<usersList.size(); i++){
                        lastMessage(usersList.get(i).getUid());
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void lastMessage(String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String theLastMessage = "default";
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat == null){
                        continue;
                    }
                    String sender = chat.getSender();
                    String receiver = chat.getReceiver();
                    if (sender == null || receiver == null){
                        continue;
                    }
                    if (chat.getReceiver().equals(currentUser.getUid()) && chat.getSender().equals(userId) || chat.getReceiver().equals(userId) && chat.getSender().equals(currentUser.getUid())){

                        if (chat.getType().equals("image")){
                            theLastMessage = "Sent a photo";
                        }else {
                            theLastMessage = chat.getMessage();
                        }

                    }
                }
                adapterChatlist.setLastMessageMap(userId, theLastMessage);
                adapterChatlist.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater ) {
        inflater.inflate(R.menu.menu_main, menu);

        //hide addpost
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);

        super.onCreateOptionsMenu(menu, inflater);
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        else if(id == R.id.action_settings){
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        }
        else if(id == R.id.action_create_group){
            startActivity(new Intent(getActivity(), GroupCreateActivity.class));
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

