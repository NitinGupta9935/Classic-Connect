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
import com.example.whatapp.adapter.GroupRecyclerAdapter;
import com.example.whatapp.model.Group;
import com.example.whatapp.model.Member;
import com.google.firebase.database.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;

import static android.content.Context.MODE_PRIVATE;

public class GroupFragment extends Fragment {
    View view;
    private DatabaseReference mDatabase;
    private ArrayList<Group> groups;
    private GroupRecyclerAdapter adapter;
    private RecyclerView recyclerView;
    ProgressBar progressBar;
    private String senderMobileNumber;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_group, container, false);

        initializeVariable();

        return view;
    }

    public void initializeVariable() {
        progressBar = view.findViewById(R.id.progress_bar_group_fragment);
        SharedPreferences sp = getContext().getSharedPreferences("UserData", MODE_PRIVATE);
        senderMobileNumber = sp.getString("MobileNumber", "");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        groups = new ArrayList<>();

        mDatabase.child("groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                groups.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Group group = snap.getValue(Group.class);
                    HashSet<String> set = new HashSet<>();
                    for (Member member: group.getMembers())
                        set.add(member.getNumber());
                    if (set.contains(senderMobileNumber))
                        groups.add(group);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        adapter = new GroupRecyclerAdapter(getContext(), groups);
        recyclerView = view.findViewById(R.id.recyclerViewGroup);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);
    }


}
