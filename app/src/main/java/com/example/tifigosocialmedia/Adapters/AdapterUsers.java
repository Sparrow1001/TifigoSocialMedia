package com.example.tifigosocialmedia.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tifigosocialmedia.ChatActivity;
import com.example.tifigosocialmedia.Models.ModelUsers;
import com.example.tifigosocialmedia.R;
import com.example.tifigosocialmedia.ThereProfileActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder> {

    Context context;
    List<ModelUsers> usersList;

    FirebaseAuth firebaseAuth;
    String myUid;
    boolean isAdmin = false;

    public AdapterUsers(Context context, List<ModelUsers> usersList) {
        this.context = context;
        this.usersList = usersList;

        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_users, viewGroup, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, final int i) {

        final String hisUID = usersList.get(i).getUid();
        FirebaseUser userC = FirebaseAuth.getInstance().getCurrentUser();
        String userCheck = userC.getUid();
        String userImage = usersList.get(i).getImage();
        String userName = usersList.get(i).getName();
        final String userEmail = usersList.get(i).getEmail();

        myHolder.mNameTv.setText(userName);
        myHolder.mEmailTv.setText(userEmail);

        try{
            Picasso.get().load(userImage)
                    .placeholder(R.drawable.ic_default_img)
                    .into(myHolder.mAvatarIv);
        }catch (Exception e){

        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Users").child(userCheck);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ModelUsers user = dataSnapshot.getValue(ModelUsers.class);
                assert user != null;
                isAdmin = user.getIsAdmin();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        myHolder.blockIv.setImageResource(R.drawable.ic_unblock_green);
        checkIsBlocked(hisUID, myHolder, i);


        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Profile", "Chat"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0){
                            //profile
                            Intent intent = new Intent(context, ThereProfileActivity.class);
                            intent.putExtra("uid", hisUID);
                            context.startActivity(intent);
                        }
                        if (which == 1){
                            //chat
                            imBlockedOrNot(hisUID);
                        }
                    }
                });
                builder.create().show();
            }
        });

        myHolder.blockIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (usersList.get(i).isBlocked()){
                    unBlockUser(hisUID);
                }else {
                    blockUser(hisUID);
                }
            }
        });

    }

    private void imBlockedOrNot(String hisUID){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUID).child("BlockedUsers").orderByChild("uid").equalTo(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            if(ds.exists()){
                                Toast.makeText(context, "Этот пользователь заблокировал Вас, невозможно отправить сообщение", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("hisUid", hisUID);
                        context.startActivity(intent);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkIsBlocked(String hisUID, MyHolder myHolder, final int i) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds: dataSnapshot.getChildren()){
                                if(ds.exists()){
                                    myHolder.blockIv.setImageResource(R.drawable.ic_block_red);
                                    usersList.get(i).setBlocked(true);
                                }
                            }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void blockUser(String hisUID) {

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", hisUID);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").child(hisUID).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Blocked Successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failed"+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unBlockUser(String hisUID) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            if(ds.exists()){
                                ds.getRef().removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(context, "Unblocked Successfully", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(context, "Failed"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        ImageView mAvatarIv, blockIv;
        TextView mNameTv, mEmailTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            mAvatarIv = itemView.findViewById(R.id.avatarIv);
            mNameTv = itemView.findViewById(R.id.nameTv);
            mEmailTv = itemView.findViewById(R.id.emailTv);
            blockIv = itemView.findViewById(R.id.blockIv);

        }
    }


}
