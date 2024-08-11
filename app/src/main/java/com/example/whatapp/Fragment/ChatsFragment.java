package com.example.whatapp.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import com.example.whatapp.R;
import com.example.whatapp.activity.ContactsActivity;
import com.example.whatapp.activity.MainActivity;
import com.example.whatapp.adapter.UsersRecyclerAdapter;
import com.example.whatapp.adapter.VPAdapter;
import com.example.whatapp.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.*;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static android.Manifest.permission.READ_CONTACTS;
import static android.content.Context.MODE_PRIVATE;

public class ChatsFragment extends Fragment {
    private int CONTACTS_REQUEST_CODE = 101;
    public FloatingActionButton contacts_button;
    private FirebaseFirestore db;
    private TextView email;
    private ImageView logoutButton;
    private DatabaseReference myRef;
    private TextView name;
    public ProgressBar progressBar;
    private RecyclerView recyclerView;
    private ImageView refressButton;
    private HashMap<String, String> userContacts;
    private UsersRecyclerAdapter usersRecyclerAdapter;
    View view;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        view = layoutInflater.inflate(R.layout.fragment_chats, viewGroup, false);
        initializeVariable();
        setData();
        checkPermission();
        getUsers();
        return view;
    }


    public void checkPermission() {
        this.userContacts = new HashMap();
        if (ContextCompat.checkSelfPermission(getContext(), "android.permission.READ_CONTACTS") != 0) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{"android.permission.READ_CONTACTS"}, (int)this.CONTACTS_REQUEST_CODE);
            return;
        }
        this.getContacts();
    }

    public void displayOnlyChatMembers(final ArrayList<User> users) {
        final String myNumber = view.getContext().getSharedPreferences("UserData", MODE_PRIVATE).getString("MobileNumber", "");
        Log.i("S1 S2 ", String.valueOf(users.size()));
        final HashSet hashSet = new HashSet();
        final ArrayList chatMembers = new ArrayList();
        DatabaseReference databaseReference = this.myRef.child("messages");
        ValueEventListener valueEventListener = new ValueEventListener(){

            public void onCancelled(DatabaseError databaseError) {
            }

            public void onDataChange(DataSnapshot dataSnapshot) {
                chatMembers.clear();
                for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                    hashSet.add(dataSnapshot2.getKey());
                }
                for (int i = 0; i < users.size(); ++i) {
                    String string4 = myNumber;
                    String string2 = users.get(i).getNumber();
                    String string3 = string2;
                    if (string4.compareTo(string3) < 0) {
                        string4 = string2;
                        string3 = myNumber;
                    }
                    if (!hashSet.contains(string4 + "&" + string3)) continue;
                    if (ChatsFragment.this.userContacts.containsKey(users.get(i).getNumber())) {
                        users.get(i).setName(userContacts.get(users.get(i).getNumber()));
                    } else {
                        users.get(i).setName(users.get(i).getNumber());
                    }
                    chatMembers.add(users.get(i));
                }
                usersRecyclerAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }
        };
        databaseReference.addValueEventListener(valueEventListener);
        usersRecyclerAdapter = new UsersRecyclerAdapter(getContext(), chatMembers);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), 1, false));
        recyclerView.setAdapter(usersRecyclerAdapter);
    }

    public void getContacts() {
        Cursor cursor = getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String string = cursor.getString(cursor.getColumnIndex("display_name"));
                String string2 = cursor.getString(cursor.getColumnIndex("data1"));
                if (string2.contains("-")) {
                    string2 = string2.replaceAll("-", "");
                }
                if (string2.contains(" ")) {
                    string2 = string2.replace(" ", "");
                }
                if (string2.length() > 10) {
                    string2 = string2.substring(3);
                }
                this.userContacts.put(string2, string);
            }
        }
    }

    public void getUsers() {
        db = FirebaseFirestore.getInstance();
        final ArrayList users = new ArrayList();
        final String string = this.getContext().getSharedPreferences("UserData", MODE_PRIVATE).getString("MobileNumber", "");
        this.db.collection("users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>(){

            public void onSuccess(QuerySnapshot querySnapshot) {
                for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                    if (string.equals(documentSnapshot.getId())) continue;
                    User user = documentSnapshot.toObject(User.class);
                    user.setNumber(documentSnapshot.getId());
                    users.add(user);
                }
                displayOnlyChatMembers(users);
            }
        });
    }

    public void initializeVariable() {
        contacts_button = view.findViewById(com.example.whatapp.R.id.contacts);
        progressBar = view.findViewById(com.example.whatapp.R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        this.myRef = FirebaseDatabase.getInstance().getReference();
    }
    public void onRequestPermissionsResult(int n, String[] arrstring, int[] arrn) {
        super.onRequestPermissionsResult(n, arrstring, arrn);
        if (n == this.CONTACTS_REQUEST_CODE) {
            Log.d("TAG", "onRequestPermissionsResult: ");
            if (arrn.length > 0 && arrn[0] == 0) {
                this.getContacts();
                return;
            }
            Toast.makeText(this.getContext(), "Contacts Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    public void setData() {
        SharedPreferences sharedPreferences = this.getContext().getSharedPreferences("UserData", MODE_PRIVATE);
        sharedPreferences.getString("name", "  ");
        sharedPreferences.getString("email", "  ");
        sharedPreferences.getString("about", "  ");
    }

    public void testing() {
        RecyclerView recyclerView;
        ArrayList arrayList = new ArrayList();
        User user = new User();
        user.setNumber("1111111");
        arrayList.add(user);
        User user2 = new User();
        user2.setNumber("22222222222");
        arrayList.add(user2);
        User user3 = new User();
        user3.setNumber("66666");
        arrayList.add(user3);
        User user4 = new User();
        user4.setNumber("45135123");
        arrayList.add(user4);
        User user5 = new User();
        user5.setNumber("65145121");
        arrayList.add(user5);
        User user6 = new User();
        user6.setNumber("65131");
        arrayList.add(user6);
        User user7 = new User();
        user7.setNumber("4653546565");
        arrayList.add(user7);
        this.usersRecyclerAdapter = new UsersRecyclerAdapter(this.getContext(), (ArrayList<User>)arrayList);
        this.recyclerView = recyclerView = this.view.findViewById(2131231132);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext(), 1, false));
        this.recyclerView.setAdapter(this.usersRecyclerAdapter);
    }
}

/*

    View view;

    private TextView name;
    private TextView email;
    //    private TextView profile;
    private ImageView refressButton;
    private ImageView logoutButton;
    private UsersRecyclerAdapter usersRecyclerAdapter;
    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private HashMap<String, String> userContacts;
    private DatabaseReference myRef;
    public FloatingActionButton contacts_button;
    public ProgressBar progressBar;

    private int CONTACTS_REQUEST_CODE = 101;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_chats, container, false);

        initializeVariable();
        setData();
//        testing();

        checkPermission();
        getUsers();

//        listners();
//        displayOnlyChatMembers();
        return view;
    }

    */
/*@Override
    protected void onRestart() {
        super.onRestart();
        getContacts();
        getUsers();
    }*//*


    public void getUsers() {
        db = FirebaseFirestore.getInstance();
        ArrayList<User> users = new ArrayList<>();
        SharedPreferences sp = getContext().getSharedPreferences("UserData", MODE_PRIVATE);
        String userMobileNumber = sp.getString("MobileNumber", "");

        db.collection("users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot i : queryDocumentSnapshots.getDocuments()) {
                    if (userMobileNumber.equals(i.getId()))
                        continue;
//                    i.getId();
//                    Log.d("Query", i.getId());
//                    User user = new User();
                    User user =  i.toObject(User.class);
                    user.setNumber(i.getId());
//                    Log.d("User", user.getName());
//                    user.setName("Hello");
                    users.add(user);
                }
//                usersRecyclerAdapter.notifyDataSetChanged();
                displayOnlyChatMembers(users);
            }
        });
    }


    public void testing() {
        ArrayList<User> users = new ArrayList<>();
        User user = new User();
        user.setNumber("1111111");
        users.add(user);

        user = new User();
        user.setNumber("22222222222");
        users.add(user);

        user = new User();
        user.setNumber("66666");
        users.add(user);

        user = new User();
        user.setNumber("45135123");
        users.add(user);

        user = new User();
        user.setNumber("65145121");
        users.add(user);

        user = new User();
        user.setNumber("65131");
        users.add(user);

        user = new User();
        user.setNumber("4653546565");
        users.add(user);

        usersRecyclerAdapter = new UsersRecyclerAdapter(getContext(), users);
        recyclerView = view.findViewById(com.example.whatapp.R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(usersRecyclerAdapter);
    }

    public void initializeVariable() {
//        name = findViewById(R.id.show_name);
//        email = findViewById(R.id.show_email);
//        profile = findViewById(R.id.profile);

//        refressButton = findViewById(R.id.refress_button);
//        logoutButton = findViewById(R.id.logout_button);
        contacts_button = view.findViewById(com.example.whatapp.R.id.contacts);
        progressBar = view.findViewById(com.example.whatapp.R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void setData() {
        SharedPreferences sp = getContext().getSharedPreferences("UserData", MODE_PRIVATE);
        String getName = sp.getString("name", "  ");
        String getEmail = sp.getString("email", "  ");
        String getAbout = sp.getString("about", "  ");

//        name.setText(getName.substring(0, 1).toUpperCase() + getName.substring(1));
//        email.setText("Welcome " + getEmail.substring(0, 1).toUpperCase() + getEmail.substring(1));
//        profile.setText(getName.substring(0, 1).toUpperCase());

    }

    public void checkPermission() {
        userContacts = new HashMap<>();
        if (ContextCompat.checkSelfPermission(getContext(), READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[] {READ_CONTACTS}, CONTACTS_REQUEST_CODE);
        }
        else {
            getContacts();
//            Toast.makeText(MainActivity.this, "Permission Already Granted", Toast.LENGTH_SHORT).show();
        }
    }

    public void getContacts() {
        ContentResolver contentResolver = getContext().getContentResolver();
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, null);

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
                userContacts.put(contactNumber, contactName);
//                Log.i("Contact ", "Name " + contactName + "  Number " + contactNumber);
            }
        }
    }

    public void displayOnlyChatMembers(ArrayList<User> users) {
//        ArrayList<User> users = getUsers();
        SharedPreferences sp = getContext().getSharedPreferences("UserData", MODE_PRIVATE);
        String userMobileNumber = sp.getString("MobileNumber", "");
        Log.i("S1 S2 ", String.valueOf(users.size()));
        myRef = FirebaseDatabase.getInstance().getReference("messages");

        HashSet<String> chatKey = new HashSet<>();
//        chatKey.add("7042418424");
        ArrayList<User> chatUsers = new ArrayList<>();

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                chatUsers.clear();
                for (DataSnapshot data: snapshot.getChildren()) {
                    Log.i("Child ", data.getKey());
                    chatKey.add(data.getKey());
                }
                for (int i = 0; i < users.size(); i++) {
                    String senderMobileNumber = users.get(i).getNumber();
                    String num1 = userMobileNumber;
                    String num2 = senderMobileNumber;
                    if (num1.compareTo(num2) < 0) {
                        num1 = senderMobileNumber;
                        num2 = userMobileNumber;
                    }

                    if (chatKey.contains(num1 + "&" + num2)) {
                        if (userContacts.containsKey(users.get(i).getNumber()))
                            users.get(i).setName(userContacts.get(users.get(i).getNumber()));
                        else
                            users.get(i).setName(users.get(i).getNumber());
                        chatUsers.add(users.get(i));
                    }

                }
                usersRecyclerAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        usersRecyclerAdapter = new UsersRecyclerAdapter(getContext(), chatUsers);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(usersRecyclerAdapter);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CONTACTS_REQUEST_CODE) {
            Log.d("TAG", "onRequestPermissionsResult: ");

            // Checking whether user granted the permission or not.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                getContacts();
                // Showing the toast message
//                Toast.makeText(MainActivity.this, "Contacts Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else {
//                finish();
                Toast.makeText(getContext(), "Contacts Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }


    }
}*/
