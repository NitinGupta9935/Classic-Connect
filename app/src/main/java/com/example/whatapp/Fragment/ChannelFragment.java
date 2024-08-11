package com.example.whatapp.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.whatapp.R;
import com.example.whatapp.adapter.ChannelRecyclerAdapter;
import com.example.whatapp.adapter.GroupRecyclerAdapter;
import com.example.whatapp.model.Channel;
import com.example.whatapp.model.Group;
import com.example.whatapp.model.Member;
import com.google.firebase.database.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;

import static android.content.Context.MODE_PRIVATE;

public class ChannelFragment extends Fragment {
    View view;
    private DatabaseReference mDatabase;
    private ArrayList<Channel> channels;
    private ChannelRecyclerAdapter adapter;
    private RecyclerView recyclerView;
    ProgressBar progressBar;
    private String senderMobileNumber;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_channel, container, false);

        initializeVariable();

        return view;
    }

    private void initializeVariable() {
        progressBar = view.findViewById(R.id.progress_bar_channel_fragment);
        SharedPreferences sp = getContext().getSharedPreferences("UserData", MODE_PRIVATE);
        senderMobileNumber = sp.getString("MobileNumber", "");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        channels = new ArrayList<>();
        HashSet<String> channelName = new HashSet<>();


        mDatabase.child("channelsReferences").child(senderMobileNumber).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
//                channels.clear();
                for (DataSnapshot data: snapshot.getChildren()) {
                    String uid = data.getValue(String.class);
                    mDatabase.child("channels").child(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            Channel channel = snapshot.getValue(Channel.class);
                            if (channel == null)
                                return;

                            if (!channelName.contains(channel.getUid()))
                                    channels.add(channel);

                                channelName.add(channel.getUid());
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        adapter = new ChannelRecyclerAdapter(getContext(), channels);
        recyclerView = view.findViewById(R.id.recyclerViewChannel);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);
    }
}