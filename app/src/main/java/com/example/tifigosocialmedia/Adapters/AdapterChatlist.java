package com.example.tifigosocialmedia.Adapters;

import android.content.Context;
import android.content.Intent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tifigosocialmedia.ChatActivity;
import com.example.tifigosocialmedia.Models.ModelUsers;
import com.example.tifigosocialmedia.R;
import com.squareup.picasso.Picasso;


import java.util.HashMap;
import java.util.List;

public class AdapterChatlist extends RecyclerView.Adapter<AdapterChatlist.MyHolder> {

    Context context;
    List<ModelUsers> userList;

    private HashMap<String, String> lastMessageMap;

    public AdapterChatlist(Context context, List<ModelUsers> userList) {
        this.context = context;
        this.userList = userList;
        lastMessageMap = new HashMap<>();
    }



    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.row_chatlist, viewGroup, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {
        String hisUid = userList.get(i).getUid();
        String userImage = userList.get(i).getImage();
        String userName = userList.get(i).getName();
        String lastMessage = lastMessageMap.get(hisUid);

        myHolder.nameTv.setText(userName);
        if (lastMessage==null || lastMessage.equals("default")){
            myHolder.lastMessageTv.setVisibility(View.GONE);
        }
        else{
            myHolder.lastMessageTv.setVisibility(View.VISIBLE);
            myHolder.lastMessageTv.setText(lastMessage);
        }
        try{
            Picasso.get().load(userImage)
                    .placeholder(R.drawable.ic_default_img)
                    .into(myHolder.profileIv);
        }catch (Exception e){
            Picasso.get().load(R.drawable.ic_default_img).into(myHolder.profileIv);
        }

        if (userList.get(i).getOnlineStatus().equals("online")){
            myHolder.onlineStatusIv.setImageResource(R.drawable.circle_online);
        }else{
            myHolder.onlineStatusIv.setImageResource(R.drawable.circle_offline);
        }

        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("hisUid", hisUid);
                context.startActivity(intent);
            }
        });



    }

    public void setLastMessageMap(String userId, String lastMessage){
        lastMessageMap.put(userId, lastMessage);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        ImageView profileIv, onlineStatusIv;
        TextView nameTv, lastMessageTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            profileIv = itemView.findViewById(R.id.profileIv);
            onlineStatusIv = itemView.findViewById(R.id.onlineStatusIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            lastMessageTv = itemView.findViewById(R.id.lastMessageTv);
        }
    }

}
