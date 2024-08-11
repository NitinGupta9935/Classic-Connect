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
import com.example.whatapp.model.BroadCast;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class BroadCastRecyclerAdapter extends RecyclerView.Adapter<BroadCastRecyclerAdapter.ViewHolder> {
    ArrayList<BroadCast> broadCasts;
    Context context;

    public BroadCastRecyclerAdapter(Context context, ArrayList<BroadCast> broadCasts) {
        this.broadCasts = broadCasts;
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
        holder.name.setText("Broadcast");

        String name = "";
        for (int i = 0; i < broadCasts.get(position).getMembers().size(); i++)
            name += broadCasts.get(position).getMembers().get(i).getNumber() + ", ";

        name = name.substring(0, name.length() - 2);
        holder.number.setText(name);

        holder.image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.broadcast));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.getInstance().broadCastModel = broadCasts.get(holder.getAdapterPosition());
                context.startActivity(new Intent(context.getApplicationContext(), BroadcastChatActivity.class));
            }
        });
    }

    @Override
    public int getItemCount() {
        return broadCasts.size();
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
