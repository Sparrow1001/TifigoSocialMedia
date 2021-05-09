package com.example.tifigosocialmedia.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tifigosocialmedia.Adapters.AdapterNotification;
import com.example.tifigosocialmedia.Models.ModelNotification;
import com.example.tifigosocialmedia.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class NotificationsFragment extends Fragment {

    RecyclerView notificationRv;

    private ArrayList<ModelNotification> notificationList;
    private FirebaseAuth firebaseAuth;
    private AdapterNotification adapterNotification;

    public NotificationsFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        notificationRv = view.findViewById(R.id.notificationRv);

        firebaseAuth = FirebaseAuth.getInstance();

        getAllNotifications();

        return view;
    }

    private void getAllNotifications() {
        notificationList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Notifications")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        notificationList.clear();
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            ModelNotification model = ds.getValue(ModelNotification.class);

                            notificationList.add(model);
                        }

                        adapterNotification = new AdapterNotification(getActivity(), notificationList);

                        notificationRv.setAdapter(adapterNotification);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}