package com.example.whatapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.whatapp.Common;
import com.example.whatapp.R;
import com.example.whatapp.activity.ChatActivity;
import com.example.whatapp.activity.ContactsActivity;
import com.example.whatapp.activity.ImageViewActivity;
import com.example.whatapp.model.User;
import com.example.whatapp.model.UserChatData;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.*;
import com.squareup.picasso.Picasso;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class UsersRecyclerAdapter extends RecyclerView.Adapter<UsersRecyclerAdapter.ViewHolder> {

    Context context;
    DatabaseReference db;
    ArrayList<User> list;


    public UsersRecyclerAdapter(Context context, ArrayList<User> arrayList) {
        this.context = context;
        this.list = arrayList;
        this.db = FirebaseDatabase.getInstance().getReference();
    }

    public String getHash(String string2) {
        String string3;
        String string4;
        String string5 = Common.getInstance().senderMobileNumber;
        if (string5.compareTo(string2) > 0) {
            string3 = string5;
            string4 = string2;
        } else {
            string3 = string2;
            string4 = string5;
        }
        return string3 + "&" + string4;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.layout_user_recycler_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {

        holder.number.setText(list.get(position).getNumber());
        holder.name.setText(list.get(position).getName());
        holder.itemView.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view) {
                String primaryNumber = UsersRecyclerAdapter.this.context.getSharedPreferences("UserData", Context.MODE_PRIVATE).getString("MobileNumber", "");
                int n = holder.getAdapterPosition();
                String secondaryNumber = list.get(n).getNumber();
                String secondaryName = list.get(n).getName();
                SharedPreferences.Editor chatEdit = UsersRecyclerAdapter.this.context.getSharedPreferences("ChatData", Context.MODE_PRIVATE).edit();
                chatEdit.putString("senderNumber", primaryNumber);
                chatEdit.putString("receiverNumber", secondaryNumber);
                chatEdit.putString("receiverName", secondaryName);
                chatEdit.apply();
                context.startActivity(new Intent(context.getApplicationContext(), ChatActivity.class));
            }
        });
        String string2 = list.get(position).getNumber();
        db.child("messages").child(getHash(string2)).child(Common.getInstance().senderMobileNumber).addValueEventListener(new ValueEventListener(){

            public void onCancelled(DatabaseError databaseError) {
            }

            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    holder.unseenMessageVisibility.setVisibility(View.GONE);
                    return;
                }
                UserChatData userChatData = (UserChatData)dataSnapshot.getValue(UserChatData.class);
                int n = userChatData.getUnseenChat();
                String string2 = Common.getInstance().formatTime(userChatData.getTime(), userChatData.getDate());
                if (n == 0) {
                    holder.unseenMessageVisibility.setVisibility(View.GONE);
                    holder.time.setText((CharSequence)string2);
                    return;
                }
                holder.unseenMessageVisibility.setVisibility(View.VISIBLE);
                holder.unseenMessage.setText((CharSequence)String.valueOf((int)n));
                holder.time.setText((CharSequence)string2);
            }
        });
        db.child("profiles").child(string2).child("profilePic").addValueEventListener(new ValueEventListener(){

            public void onCancelled(DatabaseError databaseError) {
            }

            public void onDataChange(DataSnapshot dataSnapshot) {
                String link = dataSnapshot.getValue(String.class);
                if (link != null && !link.equals("")) {
                    Picasso.get().load(link).into(holder.image);
                    list.get(holder.getAdapterPosition()).setImageLink(link);
                }
            }
        });

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp = context.getSharedPreferences("image", Context.MODE_PRIVATE);
                SharedPreferences.Editor ed = sp.edit();
                ed.putString("link", list.get(holder.getAdapterPosition()).getImageLink());
                ed.apply();
                Intent intent = new Intent(context, ImageViewActivity.class);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        TextView name;
        TextView number;
        TextView time;
        TextView unseenMessage;
        MaterialCardView unseenMessageVisibility;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
                number = itemView.findViewById(R.id.users_number);
                name = itemView.findViewById(R.id.users_name);
                image = itemView.findViewById(R.id.user_image);
                time= itemView.findViewById(R.id.last_message_time);
                unseenMessage = itemView.findViewById(R.id.unseenMessage);
                unseenMessageVisibility = itemView.findViewById(R.id.unseenMessageVisibility);
        }
    }

}
