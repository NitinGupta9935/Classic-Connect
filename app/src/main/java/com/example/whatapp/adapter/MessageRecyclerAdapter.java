package com.example.whatapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.whatapp.Common;
import com.example.whatapp.MessageSecurity;
import com.example.whatapp.activity.ImageViewActivity;
import com.example.whatapp.R;
import com.example.whatapp.activity.ChatActivity;
import com.example.whatapp.model.Message;
import com.squareup.picasso.Picasso;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class MessageRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    int RECEIVE = 2;
    int RECEIVE_IMAGE = 4;
    int SENT = 1;
    int SENT_IMAGE = 3;
    Context context;
    ArrayList<Message> list = new ArrayList();
    private String senderMobileNumber;
    MessageSecurity ms;

    public MessageRecyclerAdapter(Context context, ArrayList<Message> arrayList) {
        this.context = context;
        this.list = arrayList;
        this.senderMobileNumber = context.getSharedPreferences("UserData", 0).getString("MobileNumber", "");
        ms = new MessageSecurity();
    }

    public int getItemCount() {
        return this.list.size();
    }

    public int getItemViewType(int n) {
        if (list.get(n).isImage() && (this.list.get(n)).getSender().equals(Common.getInstance().senderMobileNumber)) {
            return this.SENT_IMAGE;
        }
        if (list.get(n).isImage()) {
            return this.RECEIVE_IMAGE;
        }
        if ((this.list.get(n)).getSender().equals(this.senderMobileNumber)) {
            return this.SENT;
        }
        return this.RECEIVE;
    }

    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int n) {
        String temp = this.list.get(n).getMessage();
        final String receivedMessage = ms.decrypt(temp);
//        string2 = ms.decrypt(string2);
        Message message = this.list.get(n);
        String string3 = Common.getInstance().formatTime((this.list.get(n)).getTime(), (this.list.get(n)).getDate());
        if (viewHolder.getClass() == SentViewHolder.class) {
            SentViewHolder sentViewHolder = (SentViewHolder)viewHolder;
            sentViewHolder.sentMessage.setText(receivedMessage);
            sentViewHolder.time.setText(string3);
            if (message.isMessageSeen()) {
                sentViewHolder.singeTick.setVisibility(View.GONE);
                sentViewHolder.doubleTick.setVisibility(View.VISIBLE);
            } else {
                sentViewHolder.singeTick.setVisibility(View.VISIBLE);
                sentViewHolder.doubleTick.setVisibility(View.GONE);
            }
            return;
        }
        if (viewHolder.getClass() == ReceiveViewHolder.class) {
            ReceiveViewHolder receiveViewHolder = (ReceiveViewHolder)viewHolder;
            receiveViewHolder.receiveMessage.setText(receivedMessage);
            receiveViewHolder.time.setText(string3);
            return;
        }
        if (viewHolder.getClass() == SentImageViewHolder.class) {
            SentImageViewHolder sentImageViewHolder = (SentImageViewHolder)viewHolder;
            Picasso.get().load(receivedMessage).into(sentImageViewHolder.sendImage);
            sentImageViewHolder.time.setText(string3);
            if (message.isMessageSeen()) {
                sentImageViewHolder.singeTick.setVisibility(View.GONE);
                sentImageViewHolder.doubleTick.setVisibility(View.VISIBLE);
            } else {
                sentImageViewHolder.singeTick.setVisibility(View.VISIBLE);
                sentImageViewHolder.doubleTick.setVisibility(View.GONE);
            }

            sentImageViewHolder.sendImage.setOnClickListener(new View.OnClickListener(){

                public void onClick(View view) {
                    MessageRecyclerAdapter.this.viewImage(receivedMessage);
                }
            });
            return;
        }
        if (viewHolder.getClass() == ReceiveImageViewHolder.class) {
            ReceiveImageViewHolder receiveImageViewHolder = (ReceiveImageViewHolder)viewHolder;
            Picasso.get().load(receivedMessage).into(receiveImageViewHolder.receiveImage);
            receiveImageViewHolder.time.setText(string3);
            receiveImageViewHolder.receiveImage.setOnClickListener(new View.OnClickListener(){

                public void onClick(View view) {
                    MessageRecyclerAdapter.this.viewImage(receivedMessage);
                }
            });
        }
    }

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View view;

        if (viewType == this.SENT_IMAGE) {
            view = layoutInflater.inflate(R.layout.layout_send_image_recycler_item, viewGroup, false);
            return new SentImageViewHolder(view);
        }
        if (viewType == this.RECEIVE_IMAGE) {
            view = layoutInflater.inflate(R.layout.layout_receive_image_recycler_item, viewGroup, false);
            return new ReceiveImageViewHolder(view);
        }
        if (viewType == this.SENT) {
            view = layoutInflater.inflate(R.layout.layout_sent_message_recycler_item, viewGroup, false);
            return new SentViewHolder(view);
        }

        view = layoutInflater.inflate(R.layout.layout_receive_message_recycler_item, viewGroup, false);
        return new ReceiveViewHolder(view);
    }

    public void viewImage(String string2) {
        SharedPreferences.Editor editor = this.context.getSharedPreferences("image", MODE_PRIVATE).edit();
        editor.putString("link", string2);
        editor.apply();
        this.context.startActivity(new Intent(this.context, ImageViewActivity.class));
    }

    public class ReceiveImageViewHolder
            extends RecyclerView.ViewHolder {
        ImageView receiveImage;
        TextView time;

        public ReceiveImageViewHolder(View view) {
            super(view);
            this.receiveImage = view.findViewById(R.id.receive_image);
            this.time = view.findViewById(R.id.receive_image_time);
        }
    }

    public class ReceiveViewHolder
            extends RecyclerView.ViewHolder {
        TextView receiveMessage;
        TextView time;

        public ReceiveViewHolder(View view) {
            super(view);
            this.receiveMessage = view.findViewById(R.id.receiveMessage);
            this.time = view.findViewById(R.id.receive_message_time);
        }
    }

    public class SentImageViewHolder
            extends RecyclerView.ViewHolder {
        ImageView doubleTick;
        ImageView sendImage;
        ImageView singeTick;
        TextView time;

        public SentImageViewHolder(View view) {
            super(view);
            this.sendImage = view.findViewById(R.id.send_image);
            this.time = view.findViewById(R.id.send_image_time);
            this.singeTick = view.findViewById(R.id.send_image_singleCheck);
            this.doubleTick = view.findViewById(R.id.send_image_doubleCheck);
        }
    }

    public class SentViewHolder
            extends RecyclerView.ViewHolder {
        ImageView doubleTick;
        TextView sentMessage;
        ImageView singeTick;
        TextView time;

        public SentViewHolder(View view) {
            super(view);
            this.sentMessage = view.findViewById(R.id.sentMessage);
            this.time = view.findViewById(R.id.send_message_time);
            this.singeTick = view.findViewById(R.id.send_message_singe_check);
            this.doubleTick = view.findViewById(R.id.send_message_double_check);
        }
    }
}
