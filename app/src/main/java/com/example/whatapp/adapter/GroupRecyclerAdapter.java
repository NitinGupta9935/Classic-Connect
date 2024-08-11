package com.example.whatapp.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.example.whatapp.activity.BroadcastChatActivity;
import com.example.whatapp.activity.GroupChatActivity;
import com.example.whatapp.model.Group;
import com.example.whatapp.model.Member;
import com.example.whatapp.model.UserChatData;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class GroupRecyclerAdapter extends RecyclerView.Adapter<GroupRecyclerAdapter.ViewHolder> {
    Context context;
    DatabaseReference db;
    ArrayList<Group> groups;

    public GroupRecyclerAdapter(Context context, ArrayList<Group> arrayList) {
        this.groups = arrayList;
        this.context = context;
        this.db = FirebaseDatabase.getInstance().getReference();
    }

    public int getItemCount() {
        return groups.size();
    }

    public void onBindViewHolder(final ViewHolder viewHolder, int n) {
        viewHolder.name.setText((groups.get(n)).getGroupName());
        String numbers = "";
        for (int i = 0; i < groups.get(n).getMembers().size(); ++i) {
            numbers = numbers + groups.get(n).getMembers().get(i).getNumber() + ", ";
        }
        String string3 = numbers.substring(0, -2 + numbers.length());
        if (string3.length() > 22)
            string3 = string3.substring(0, 23) + " ...";

        viewHolder.number.setText(string3);
        viewHolder.image.setImageDrawable(ContextCompat.getDrawable(context, (int)2131165318));
        viewHolder.itemView.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view) {
                Common.getInstance().groupModel = GroupRecyclerAdapter.this.groups.get(viewHolder.getAdapterPosition());
                context.startActivity(new Intent(context.getApplicationContext(), GroupChatActivity.class));
            }
        });
        String string4 = groups.get(n).getUid();
        String string5 = Common.getInstance().senderMobileNumber;
        db.child("groups").child(string4).child("membersData")
                .child(string5).addValueEventListener(new ValueEventListener(){

            public void onCancelled(DatabaseError databaseError) {
            }

            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    viewHolder.unseenMessageVisibility.setVisibility(View.GONE);
                    return;
                }
                UserChatData userChatData = dataSnapshot.getValue(UserChatData.class);
                int n = userChatData.getUnseenChat();
                String string2 = Common.getInstance().formatTime(userChatData.getTime(), userChatData.getDate());
                if (n == 0) {
                    viewHolder.unseenMessageVisibility.setVisibility(View.GONE);
                    viewHolder.time.setText(string2);
                    return;
                }
                viewHolder.unseenMessageVisibility.setVisibility(View.VISIBLE);
                viewHolder.unseenMessage.setText(String.valueOf(n));
                viewHolder.time.setText(string2);
            }
        });
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.layout_user_recycler_item, parent, false);
        return new ViewHolder(view);
    }
    public class ViewHolder
            extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name;
        TextView number;
        TextView time;
        TextView unseenMessage;
        MaterialCardView unseenMessageVisibility;

        public ViewHolder(View view) {
            super(view);
            number = itemView.findViewById(R.id.users_number);
            name = itemView.findViewById(R.id.users_name);
            image = itemView.findViewById(R.id.user_image);
            time= itemView.findViewById(R.id.last_message_time);
            unseenMessage = itemView.findViewById(R.id.unseenMessage);
            unseenMessageVisibility = itemView.findViewById(R.id.unseenMessageVisibility);
        }
    }

}

/*

    ArrayList<Group> groups;
    Context context;

    public GroupRecyclerAdapter(Context context, ArrayList<Group> groups) {
        this.groups = groups;
        this.context = context;
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
        holder.name.setText(groups.get(position).getGroupName());

        String numbers = "";
        for (int i = 0; i < groups.get(position).getMembers().size(); i++)
            numbers += groups.get(position).getMembers().get(i).getNumber() + ", ";

        numbers = numbers.substring(0, numbers.length() - 2);

        if (numbers.length() > 22)
            numbers = numbers.substring(0, 23) + " ...";
        holder.number.setText(numbers);

        holder.image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.groups));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.getInstance().groupModel = groups.get(holder.getAdapterPosition());
                context.startActivity(new Intent(context.getApplicationContext(), GroupChatActivity.class));
            }
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView number;
        ImageView image;
        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.user_image);
            name = itemView.findViewById(R.id.users_name);
            number = itemView.findViewById(R.id.users_number);
        }
    }
}
*/
