package com.example.whatapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.example.whatapp.Common;
import com.example.whatapp.R;
import com.example.whatapp.model.Channel;
import com.example.whatapp.model.Member;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ChannelSettingRecyclerAdapter extends RecyclerView.Adapter<ChannelSettingRecyclerAdapter.ViewHolder> {

    private ArrayList<Member> members;
    private Set<String> editAccessMembers;
    private Context context;
    private DatabaseReference db;
    private Channel channelMode;

    public ChannelSettingRecyclerAdapter(Context context, Channel channelModel) {
        this.context = context;
        this.channelMode = channelModel;
        this.members = channelMode.getMembers();
//        this.editAccessMembers = editAccessMembers;
        this.editAccessMembers = new HashSet<>();

        ArrayList<String> members = channelModel.getMessageAccess();
        for (int i = 0; members != null && i < members.size(); i++)
            this.editAccessMembers.add(members.get(i));
        db = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.layout_channel_setting_recycler, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        String name = getUserName(members.get(position).getNumber());
        holder.name.setText(name);


        holder.number.setText("");
        if(editAccessMembers.contains(members.get(position).getNumber()))
            holder.number.setText("Admin");

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!editAccessMembers.contains(Common.getInstance().senderMobileNumber))
                    return;

                int index = holder.getAdapterPosition();
                String number = members.get(index).getNumber();

                if (number.equals(Common.getInstance().senderMobileNumber))
                    return;

                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                if (editAccessMembers.contains(number))
                    dialog.setTitle("Remove from Admin");
                else
                    dialog.setTitle("Make Admin");

                dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (editAccessMembers.contains(number)) {
                            ArrayList<String> members = channelMode.getMessageAccess();

                            int ind = 0;
                            for (int i = 0; i < members.size(); i++)
                                if (members.get(i).equals(number))
                                    ind = i;

                            if (members.size() != 0)
                                members.remove(ind);
                            editAccessMembers.remove(number);

                            holder.number.setText("");
                            db.child("channels").child(channelMode.getUid())
                                    .child("messageAccess")
                                    .setValue(channelMode.getMessageAccess());
                        }
                        else {
                            editAccessMembers.add(number);
                            holder.number.setText("Admin");
                            channelMode.getMessageAccess().add(number);
                            editAccessMembers.add(number);
                            db.child("channels").child(channelMode.getUid())
                                    .child("messageAccess")
                                    .setValue(channelMode.getMessageAccess());
                        }
//                        Toast.makeText(context, "admin added", Toast.LENGTH_SHORT).show();
                    }
                });

                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        Toast.makeText(context, "cancel", Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.show();

            }
        });
    }
    public String getUserName(String string) {
        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            @SuppressLint("Range") String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            @SuppressLint("Range") String contactNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            if (contactNumber.contains((CharSequence)"-")) {
                contactNumber = contactNumber.replaceAll("-", "");
            }
            if (contactNumber.contains((CharSequence)" ")) {
                contactNumber = contactNumber.replace((CharSequence)" ", (CharSequence)"");
            }
            if (contactNumber.length() > 10) {
                contactNumber = contactNumber.substring(3);
            }
            if (!contactNumber.equals((Object)string)) continue;
            return contactName;
        }
        return string;
    }

    @Override
    public int getItemCount() {
        return members.size();
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
