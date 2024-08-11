package com.example.whatapp.activity;

import android.annotation.SuppressLint;
import android.content.*;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.whatapp.Common;
import com.example.whatapp.ICallback;
import com.example.whatapp.R;
import com.example.whatapp.adapter.ContactRecyclerAdapter;
import com.example.whatapp.model.*;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.*;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import static android.Manifest.permission.READ_CONTACTS;
import static android.content.ContentValues.TAG;

public class ContactsActivity extends AppCompatActivity implements ICallback {

    private int CONTACTS_REQUEST_CODE = 101;
    private RecyclerView recyclerView;
    private ContactRecyclerAdapter contactRecyclerAdapter;
    public ArrayList<User> dbUsers;
    private FirebaseFirestore db;
    public static boolean toContacts = false;
    public TextView permission;
    public LinearLayout broadcast;
    public LinearLayout channel;
    public LinearLayout group;
    private DatabaseReference mDatabase;
    String id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        initializeVariable();
        listeners();

        checkPermission();
    }

    public void initializeVariable() {
        recyclerView = findViewById(R.id.recyclerView);
        permission = findViewById(R.id.permission);
        broadcast = findViewById(R.id.broadcast);
        channel = findViewById(R.id.channel);
        group = findViewById(R.id.group);

        setTitle("Contacts ");
        mDatabase = FirebaseDatabase.getInstance().getReference();

        String value = getIntent().getStringExtra("toContacts");
//        Log.i("value", value);
        if (value.equals("true"));
        toContacts = true;
        Common.getInstance().setTheme(this);
    }

    public void listeners() {
        broadcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (contactRecyclerAdapter == null || contactRecyclerAdapter.getSelectedUsers() == null)
                    return;

                int size = contactRecyclerAdapter.getSelectedUsers().size();
                if (size == 0)
                    return;

                Common.getInstance().messageReceivers = contactRecyclerAdapter.getSelectedUsers();

//                SharedPreferences sp = getSharedPreferences("UserData", Context.MODE_PRIVATE);
//                String senderNumber = sp.getString("MobileNumber", "");
                String senderNumber = Common.getInstance().senderMobileNumber;

                ArrayList<Member> members = new ArrayList<>();
                for (User user: contactRecyclerAdapter.getSelectedUsers())
                    members.add(new Member(user.getNumber()));


                String users[] = new String[members.size()];
                for (int i = 0; i < members.size(); i++)
                    users[i] = members.get(i).getNumber();
                Arrays.sort(users);

                for (int i = 0; i < members.size(); i++)
                    id += users[i];
                id = String.valueOf(id.hashCode());


                mDatabase.child("broadcast").orderByChild("uid").equalTo(id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            Toast.makeText(ContactsActivity.this, "Broadcast Already Exists", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            mDatabase.child("broadcast").child(id);
                            BroadCast broadCast = new BroadCast(senderNumber, id, members);
//                broadCastId = id;
                            mDatabase.child("broadcast").child(id).setValue(broadCast);

                            Common.getInstance().broadCastModel = new BroadCast(senderNumber,id, members);

                            startActivity(new Intent(ContactsActivity.this, BroadcastChatActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

//        mDatabase.child("broadcast").child("-NEGbIXezlAG1ZvBeoyh").child("members").push().setValue(new Member("25299"));


//                Toast.makeText(ContactsActivity.this, "Selected item size is " + size, Toast.LENGTH_SHORT).show();
            }
        });

        group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (contactRecyclerAdapter == null || contactRecyclerAdapter.getSelectedUsers() == null)
                    return;

                int size = contactRecyclerAdapter.getSelectedUsers().size();
                if (size == 0)
                    return;

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ContactsActivity.this);
                alertDialog.setTitle("Enter Group Name");
                alertDialog.setMessage("Enter Group Message");
                EditText input = new EditText(ContactsActivity.this);
                alertDialog.setView(input);

                alertDialog.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (contactRecyclerAdapter == null || contactRecyclerAdapter.getSelectedUsers() == null
                        || input.getText().toString().length() == 0)
                            return;

                        int size = contactRecyclerAdapter.getSelectedUsers().size();
                        if (size == 0)
                            return;

                        Common.getInstance().messageReceivers = contactRecyclerAdapter.getSelectedUsers();

                        SharedPreferences sp = getSharedPreferences("UserData", Context.MODE_PRIVATE);
                        String senderNumber = sp.getString("MobileNumber", "");

                        ArrayList<Member> members = new ArrayList<>();
                        for (User user: contactRecyclerAdapter.getSelectedUsers())
                            members.add(new Member(user.getNumber()));
                        members.add(new Member(senderNumber));

                        String groupId = mDatabase.child("groups").push().getKey();
                        Group group = new Group(input.getText().toString(), members, groupId);
                        mDatabase.child("groups").child(groupId).setValue(group);

                        for (Member member: members)
                            mDatabase.child("groupsReferences").child(member.getNumber()).push().setValue(groupId);
                        mDatabase.child("groupsReferences").child(senderNumber).push().setValue(groupId);

                        Common.getInstance().groupModel = group;

                        startActivity(new Intent(ContactsActivity.this, GroupChatActivity.class));
                        Toast.makeText(ContactsActivity.this, "Group created", Toast.LENGTH_SHORT).show();
//                        Toast.makeText(ContactsActivity.this, input.getText().toString(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

                alertDialog.show();
            }
        });

        channel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (contactRecyclerAdapter == null || contactRecyclerAdapter.getSelectedUsers() == null)
                    return;

                int size = contactRecyclerAdapter.getSelectedUsers().size();
                if (size == 0)
                    return;

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ContactsActivity.this);
                alertDialog.setTitle("Enter Channel Name");
//                alertDialog.setMessage("Enter Group Message");
                EditText input = new EditText(ContactsActivity.this);
                alertDialog.setView(input);

                alertDialog.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (contactRecyclerAdapter == null || contactRecyclerAdapter.getSelectedUsers() == null
                                || input.getText().toString().length() == 0)
                            return;

                        int size = contactRecyclerAdapter.getSelectedUsers().size();
                        if (size == 0)
                            return;

                        Common.getInstance().messageReceivers = contactRecyclerAdapter.getSelectedUsers();

                        SharedPreferences sp = getSharedPreferences("UserData", Context.MODE_PRIVATE);
                        String senderNumber = sp.getString("MobileNumber", "");

                        ArrayList<Member> members = new ArrayList<>();
                        for (User user: contactRecyclerAdapter.getSelectedUsers())
                            members.add(new Member(user.getNumber()));
                        members.add(new Member(senderNumber));

                        String channelId = mDatabase.child("channels").push().getKey();
                        Channel channel = new Channel(input.getText().toString(), members, channelId);
                        mDatabase.child("channels").child(channelId).setValue(channel);

                        for (Member member: members)
                            mDatabase.child("channelsReferences").child(member.getNumber()).child(channelId).setValue(channelId);
//                        mDatabase.child("channelsReferences").child(senderNumber).push().setValue(channelId);

                        Common.getInstance().channelModel = channel;

                        startActivity(new Intent(ContactsActivity.this, ChannelChatActivity.class));
//                        startActivity(new Intent(ContactsActivity.this, MainActivity.class));
                        Toast.makeText(ContactsActivity.this, "Channel created", Toast.LENGTH_SHORT).show();
//                        Toast.makeText(ContactsActivity.this, input.getText().toString(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

                alertDialog.show();
            }
        });
    }

    public void getContacts() {
        permission.setVisibility(View.GONE);
        ContentResolver contentResolver = getContentResolver();
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, null);

        HashSet<String> dbUsersNumbersSet = new HashSet<>();
        for (int i = 0; i < dbUsers.size(); i++) {
            dbUsersNumbersSet.add(dbUsers.get(i).getNumber());
            Log.d("dbUsers", dbUsers.get(i).getNumber());
        }

        ArrayList<User> showUsers = new ArrayList<>();
        HashSet<String> removeDuplicate = new HashSet<>();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                @SuppressLint("Range") String contactNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                if (contactNumber.contains("-"))
                    contactNumber = contactNumber.replaceAll("-", "");
                if (contactNumber.contains(" "))
                    contactNumber = contactNumber.replace(" ", "");

                if (contactNumber.length() > 10) {
                    contactNumber = contactNumber.substring(3);
                }

                User user = new User();
                user.setNumber(contactNumber);
                user.setName(contactName);

                if (removeDuplicate.contains(contactNumber))
                    continue;
                else
                    removeDuplicate.add(contactNumber);

                if (dbUsersNumbersSet.contains(contactNumber)) {
                    showUsers.add(user);
                    Log.i("Contact ", "Name " + contactName + "  Number " + contactNumber);
                }
            }
        }

        contactRecyclerAdapter = new ContactRecyclerAdapter(this, showUsers, this);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(contactRecyclerAdapter);

    }

    public void getUsers() {
        db = FirebaseFirestore.getInstance();
        dbUsers = new ArrayList<>();
        SharedPreferences sp = getSharedPreferences("UserData", MODE_PRIVATE);
        String userMobileNumber = sp.getString("MobileNumber", "");

        db.collection("users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                dbUsers.clear();
                for (DocumentSnapshot i : queryDocumentSnapshots.getDocuments()) {
                    if (userMobileNumber.equals(i.getId()))
                        continue;
                    User user =  i.toObject(User.class);
                    user.setNumber(i.getId());
                    dbUsers.add(user);
                }
                getContacts();
            }
        });

    }

    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {READ_CONTACTS}, CONTACTS_REQUEST_CODE);
        }
        else {
//            getContacts();
            getUsers();
            permission.setVisibility(View.GONE);
//            Toast.makeText(ContactsActivity.this, "Permission Already Granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CONTACTS_REQUEST_CODE) {
            Log.d("TAG", "onRequestPermissionsResult: ");

            // Checking whether user granted the permission or not.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

//                getContacts();
                getUsers();
                // Showing the toast message
                permission.setVisibility(View.GONE);
//                Toast.makeText(MainActivity.th=[;is, "Contacts Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else {
//                finish();
                permission.setVisibility(View.VISIBLE);
                Toast.makeText(ContactsActivity.this, "Contacts Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void isSelected() {
        Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
    }
}