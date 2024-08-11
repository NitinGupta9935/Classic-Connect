package com.example.whatapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.whatapp.Common;
import com.example.whatapp.R;
import com.example.whatapp.activity.ChannelChatActivity;
import com.example.whatapp.activity.GroupChatActivity;
import com.example.whatapp.activity.ImageViewActivity;
import com.example.whatapp.model.Channel;
import com.example.whatapp.model.UserChatData;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.*;
import com.squareup.picasso.Picasso;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ChannelRecyclerAdapter extends RecyclerView.Adapter<ChannelRecyclerAdapter.ViewHolder> {
    Context context;
    DatabaseReference db;
    ArrayList<Channel> channels;

    public ChannelRecyclerAdapter(Context context, ArrayList<Channel> channels) {
        this.context = context;
        this.db = FirebaseDatabase.getInstance().getReference();
        this.channels = channels;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.layout_user_recycler_item, parent, false);
        return new ChannelRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        holder.name.setText(channels.get(position).getChannelName());
        String numbers = "";
        for (int i = 0; i < channels.get(position).getMembers().size(); ++i) {
            numbers = numbers + channels.get(position).getMembers().get(i).getNumber() + ", ";
        }
        String string3 = numbers.substring(0, -2 + numbers.length());
        if (string3.length() > 22)
            string3 = string3.substring(0, 23) + " ...";

        holder.number.setText(string3);
        holder.image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.channel));
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp = context.getSharedPreferences("image", Context.MODE_PRIVATE);
                SharedPreferences.Editor ed = sp.edit();
                int pos = holder.getAdapterPosition();
                ed.putString("link", channels.get(pos).getImageLink());
                ed.apply();

                Intent intent = new Intent(context, ImageViewActivity.class);
                context.startActivity(intent);
            }
        });

        String imageLink = channels.get(position).getImageLink();
        if (imageLink != null)
            Picasso.get().load(imageLink).into(holder.image);
        holder.itemView.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view) {
                Common.getInstance().channelModel = channels.get(holder.getAdapterPosition());
                context.startActivity(new Intent(context.getApplicationContext(), ChannelChatActivity.class));
            }
        });

        String string4 = channels.get(position).getUid();
        String string5 = Common.getInstance().senderMobileNumber;

        db.child("channels").child(string4).child("membersData")
                .child(string5).addValueEventListener(new ValueEventListener(){

                    public void onCancelled(DatabaseError databaseError) {
                    }

                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() == null) {
                            holder.unseenMessageVisibility.setVisibility(View.GONE);
                            return;
                        }
                        UserChatData userChatData = dataSnapshot.getValue(UserChatData.class);
                        int n = userChatData.getUnseenChat();
                        String string2 = Common.getInstance().formatTime(userChatData.getTime(), userChatData.getDate());
                        if (n == 0) {
                            holder.unseenMessageVisibility.setVisibility(View.GONE);
                            holder.time.setText(string2);
                            return;
                        }
                        holder.unseenMessageVisibility.setVisibility(View.VISIBLE);
                        holder.unseenMessage.setText(String.valueOf(n));
                        holder.time.setText(string2);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return channels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
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
