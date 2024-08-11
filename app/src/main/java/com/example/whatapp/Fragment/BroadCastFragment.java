package com.example.whatapp.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.whatapp.R;
import com.example.whatapp.adapter.BroadCastRecyclerAdapter;
import com.example.whatapp.model.BroadCast;
import com.google.firebase.database.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class BroadCastFragment extends Fragment {

    View view;
    private DatabaseReference mDatabase;
    private ArrayList<BroadCast> broadCasts;
    private BroadCastRecyclerAdapter adapter;
    private RecyclerView recyclerView;
    ProgressBar progressBar;
    private String senderMobileNumber;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_broad_cast, container, false);

        initializeVariable();

        return view;
    }

    public void initializeVariable() {
        progressBar = view.findViewById(R.id.progress_bar_broadcast_fragment);
        SharedPreferences sp = getContext().getSharedPreferences("UserData", MODE_PRIVATE);
        senderMobileNumber = sp.getString("MobileNumber", "");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        broadCasts = new ArrayList<>();
        mDatabase.child("broadcast").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                broadCasts.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    BroadCast broadCast = snap.getValue(BroadCast.class);
                    Log.i("senderMobileNumber ", senderMobileNumber);
                    if (broadCast.getSenderMobileNumber().equals(senderMobileNumber))
                        broadCasts.add(broadCast);
//                    Log.d("broadCast ", broadCast.getMembers());
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        adapter = new BroadCastRecyclerAdapter(getContext(), broadCasts);

        recyclerView = view.findViewById(R.id.recyclerViewBroadcast);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);
    }

    public void listners() {
    }
}