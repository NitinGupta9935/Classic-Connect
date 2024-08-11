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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.whatapp.Common;
import com.example.whatapp.ICallback;
import com.example.whatapp.R;
import com.example.whatapp.activity.ChatActivity;
import com.example.whatapp.activity.ContactsActivity;
import com.example.whatapp.model.User;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ContactRecyclerAdapter extends RecyclerView.Adapter<ContactRecyclerAdapter.ViewHolder> {
    Context context;
    ArrayList<User> list;
    ArrayList<User> selectedUsers;
    private Boolean selectMultipleUser = false;

    ICallback iCallback;

    public ContactRecyclerAdapter(Context context, ArrayList<User> users, ICallback iCallback) {
        this.context = context;
        this.list = users;
        selectedUsers = new ArrayList<>();
        this.iCallback = iCallback;
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
//        holder.name.setText("Hi");
        holder.name.setText(list.get(position).getName());

//        holder.image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_check_circle_24));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getSelectedUsers().size() == 0)
                    selectMultipleUser = false;
                if (selectMultipleUser) {
                    int position = holder.getAdapterPosition();
                    if (list.get(position).isSelected()) {
                        list.get(position).select(false);
                        selectedUsers.remove(list.get(position));
                        holder.image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.user));
                        return;
                    }

                    list.get(position).select(true);
                    holder.image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_check_circle_24));
                    selectedUsers.add(list.get(position));
                    return;
                }

                SharedPreferences spUser = context.getSharedPreferences("UserData", Context.MODE_PRIVATE);
                String primaryNumber = spUser.getString("MobileNumber", "");
                int pos = holder.getAdapterPosition();
                String secondaryNumber = list.get(pos).getNumber();
                String secondaryName = list.get(pos).getName();

                SharedPreferences chat = context.getSharedPreferences("ChatData", Context.MODE_PRIVATE);
                SharedPreferences.Editor chatEdit = chat.edit();
                chatEdit.putString("senderNumber", primaryNumber);
                chatEdit.putString("receiverNumber", secondaryNumber);
                chatEdit.putString("receiverName", secondaryName);
                chatEdit.apply();

                context.startActivity(new Intent(context.getApplicationContext(), ChatActivity.class));
                ((Activity) context).finish();
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int position = holder.getAdapterPosition();
//                Toast.makeText(view.getContext(), "Long Click", Toast.LENGTH_SHORT).show();
                boolean isSelected = list.get(position).isSelected();
                selectMultipleUser = !selectMultipleUser;
                iCallback.isSelected();

                if (selectMultipleUser) {
                    list.get(position).select(true);
                    selectedUsers.add(list.get(position));
//                    Toast.makeText(view.getContext(), " selectMultipleUser ", Toast.LENGTH_SHORT).show();
                    holder.image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_check_circle_24));
                }
                else
                    selectedUsers.clear();

                return true;
            }
        });
    }


    public ArrayList<User> getSelectedUsers() {
        return selectedUsers;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView number;
        TextView name;
        ImageView image;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            number = itemView.findViewById(R.id.users_number);
            name = itemView.findViewById(R.id.users_name);
            image = itemView.findViewById(R.id.user_image);
        }
    }
}
