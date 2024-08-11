package com.example.whatapp.activity;

import android.app.Activity;
import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.example.whatapp.Common;
import com.example.whatapp.Fragment.BroadCastFragment;
import com.example.whatapp.Fragment.ChannelFragment;
import com.example.whatapp.Fragment.ChatsFragment;
import com.example.whatapp.Fragment.GroupFragment;
import com.example.whatapp.R;
import com.example.whatapp.adapter.VPAdapter;
import com.example.whatapp.adapter.UsersRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.*;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private int CONTACTS_REQUEST_CODE = 101;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private int activeFragmentId;
    BroadcastReceiver broadcastReceiver;
    public FloatingActionButton contacts_button;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();;
    private DrawerLayout drawerLayout;
    private TextView email;
    private ImageView logoutButton;
    private DatabaseReference myRef;
    private TextView name;
    private NavigationView navigationView;
    private RecyclerView recyclerView;
    private ImageView refressButton;
    private TextView setFragmentName;
    private TabLayout tabLayout;
    private HashMap<String, String> userContacts;
    private UsersRecyclerAdapter usersRecyclerAdapter;
    private ViewPager viewPager;

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
        initializeVariable();
        listeners();
        isNameSaved();
    }

    public void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener((OnCompleteListener) new OnCompleteListener<String>(){

            public void onComplete(Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.w("ContentValues", "Fetching FCM registration token failed", task.getException());
                    return;
                }
                String string2 = task.getResult();
                Log.d("token ", string2);
                MainActivity.this.myRef.child("notification").child(Common.getInstance().senderMobileNumber).child("token").setValue(string2);
            }
        });
    }

    public void inflateFragment() {
        this.tabLayout.setupWithViewPager(this.viewPager);
        VPAdapter vPAdapter = new VPAdapter(this.getSupportFragmentManager(), 1);
        vPAdapter.addFragment(new ChatsFragment(), "Chats");
        vPAdapter.addFragment(new BroadCastFragment(), "BroadCasts");
        vPAdapter.addFragment(new GroupFragment(), "Groups");
        this.viewPager.setAdapter((PagerAdapter) vPAdapter);
    }

    public void initializeVariable() {
        contacts_button = findViewById(R.id.contacts);
        setFragmentName = findViewById(R.id.set_fragment_name);
        navigationView = findViewById(R.id.navigation_view);
        Common.getInstance().senderMobileNumber = this.getSharedPreferences("ChatData", MODE_PRIVATE).getString("senderNumber", "");
        this.myRef = FirebaseDatabase.getInstance().getReference();

        SharedPreferences sharedPreferences = this.getSharedPreferences("UserData", MODE_PRIVATE);
        Common.getInstance().userName = sharedPreferences.getString("name", "");
        Common.getInstance().userAbout = sharedPreferences.getString("about", "");
        Common.getInstance().profilePic = sharedPreferences.getString("profilePic", "");

        drawerLayout = this.findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, this.drawerLayout, 2131755156, 2131755155);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String themeColor = getSharedPreferences("theme", MODE_PRIVATE).getString("color", "");
        if (!themeColor.equals(""))
            Common.getInstance().themeColor = Integer.parseInt(themeColor);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction("");
        broadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Log.i("Broadcast", "network change");
            }
        };

        registerReceiver(broadcastReceiver, intentFilter);
        getToken();
    }

    public void listeners() {
        contacts_button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ContactsActivity.class);
                intent.putExtra("toContacts", "true");
                MainActivity.this.startActivity(intent);
            }
        });

        this.setChatFragment();
        this.navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener(){

            public boolean onNavigationItemSelected(MenuItem menuItem) {
                MainActivity.this.getSupportFragmentManager().beginTransaction();
                switch (menuItem.getItemId()) {
                    case R.id.groups: {
                        setGroupFragment();
                        break;
                    }
                    case R.id.chats: {
                        setChatFragment();
                        break;
                    }
                    case R.id.broadcast: {
                        setBroadcastFragment();
                        break;
                    }
                    case R.id.channel: {
                        setChannelFragment();
                        break;
                    }
                    default: {
                        break;
                    }
                }
                return true;
            }
        });
    }

    public void onBackPressed() {
        if (this.activeFragmentId != R.id.chats) {
            this.setChatFragment();
            return;
        }
        super.onBackPressed();
    }


    public boolean onCreateOptionsMenu(Menu menu2) {
        getMenuInflater().inflate(R.menu.menu, menu2);
        return true;
    }

    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(this.broadcastReceiver);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (this.actionBarDrawerToggle.onOptionsItemSelected(menuItem)) {
            return true;
        }
        switch (menuItem.getItemId()) {
            default: {
                return super.onOptionsItemSelected(menuItem);
            }
//            com.example.whatapp.R.id.logout
            case R.id.setting: {
                startActivity(new Intent((Context)this, SettingActivity.class));
                return true;
            }
            case R.id.refresh:
        }
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment, new ChatsFragment());
        fragmentTransaction.commit();
        setFragmentName.setText("Chats");
        return true;
    }

    protected void onResume() {
        super.onResume();
        Common.getInstance().setTheme(this);
    }

    public void sendSms(String string2, String string3) {
        try {
            SmsManager.getDefault().sendTextMessage(string2, null, string3, null, null);
            return;
        }
        catch (Exception exception) {
            return;
        }
    }

    public void setBroadcastFragment() {
        activeFragmentId = R.id.broadcast;
        FragmentTransaction fragmentTransaction = this.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment, (Fragment)new BroadCastFragment());
        fragmentTransaction.commit();
        this.setFragmentName.setText((CharSequence)"Broadcast");
        this.drawerLayout.closeDrawers();
        this.setTitle((CharSequence)"Broadcast");
    }

    public void setChatFragment() {
        activeFragmentId = R.id.chats;
        FragmentTransaction fragmentTransaction = this.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment, (Fragment)new ChatsFragment());
        fragmentTransaction.commit();
        setFragmentName.setText("Chats");
        this.drawerLayout.closeDrawers();
        this.setTitle((CharSequence)"Chats");
    }

    public void setData() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        sharedPreferences.getString("name", "  ");
        sharedPreferences.getString("email", "  ");
        sharedPreferences.getString("about", "  ");
    }

    public void setGroupFragment() {
        activeFragmentId = R.id.groups;
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment, new GroupFragment());
        fragmentTransaction.commit();
        this.setFragmentName.setText("Groups");
        this.drawerLayout.closeDrawers();
        this.setTitle("Groups");
    }

    public void setChannelFragment() {
        activeFragmentId = R.id.channel;
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment, new ChannelFragment());
        fragmentTransaction.commit();
        setFragmentName.setText("Channel");
        drawerLayout.closeDrawers();
        setTitle("Channel");
    }

    public void isNameSaved() {
        SharedPreferences sp = getSharedPreferences("UserData", MODE_PRIVATE);
        String mobileNumber = sp.getString("MobileNumber", "");

        try {
            db.collection("users").document(mobileNumber).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                            if (!task.isSuccessful()) {
                                Log.e("Error", task.getException().toString());
                                return;
                            }

                            int nameSize = task.getResult().get("name").toString().length();

                            if (nameSize == 0) {
                                startActivity(new Intent(MainActivity.this, GetUserDataActivity.class));
                                finish();
                            }
                        }
                    });
        }
        catch (NullPointerException e) {
            // This exception occur then user data (name, email) is not saved on database

//            startActivity(new Intent(MainActivity.this, GetUserDataActivity.class));
//            finish();
            Log.e("exception", e.getMessage());
        }
    }
}

/*

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
    private int CONTACTS_REQUEST_CODE = 101;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private TextView setFragmentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.whatapp.R.layout.activity_main);

        initializeVariable();
//        inflateFragment();
        setData();

        listeners();
    }


    public void initializeVariable() {
        ActionBarDrawerToggle actionBarDrawerToggle;
        String string2;
        BroadcastReceiver broadcastReceiver;
        contacts_button = findViewById(com.example.whatapp.R.id.contacts);
//        tabLayout = findViewById(R.id.tab_layout);
//        viewPager = findViewById(R.id.view_pager);
        setFragmentName = findViewById(R.id.set_fragment_name);

        navigationView = findViewById(R.id.navigation_view);
        SharedPreferences sp = getSharedPreferences("ChatData", MODE_PRIVATE);
        String senderNumber = sp.getString("senderNumber", "");
        Common.getInstance().senderMobileNumber = senderNumber;

        myRef = FirebaseDatabase.getInstance().getReference();
        SharedPreferences sharedPreferences = this.getSharedPreferences("UserData", 0);
        Common.getInstance().userName = sharedPreferences.getString("name", "");
        Common.getInstance().userAbout = sharedPreferences.getString("about", "");
        Common.getInstance().profilePic = sharedPreferences.getString("profilePic", "");

        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        // add navigation icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String string3 = this.getSharedPreferences("theme", 0).getString("color", "");
        if (!string3.equals((Object)"")) {
            Common.getInstance().themeColor = Integer.parseInt((String)string3);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction("");
        this.broadcastReceiver = broadcastReceiver = new BroadcastReceiver(){

            public void onReceive(Context context, Intent intent) {
                Log.i((String)"Broadcast", (String)"network change");
            }
        };
        this.registerReceiver(broadcastReceiver, intentFilter);
        this.getToken();

/*        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment, new ChatsFragment());
                ft.commit();
                setFragmentName.setText("Chats");

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {

                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                switch (item.getItemId()) {
                    case R.id.chats:
                        ft.replace(R.id.fragment, new ChatsFragment());
                        ft.commit();
                        setFragmentName.setText("Chats");
                        drawerLayout.closeDrawers();
//                        Toast.makeText(MainActivity.this, "Chats", Toast.LENGTH_SHO   RT).show();
                        break;

                    case R.id.groups:
                        ft.replace(R.id.fragment, new GroupFragment());
                        ft.commit();
                        setFragmentName.setText("Groups");
                        drawerLayout.closeDrawers();
//                        Toast.makeText(MainActivity.this, "Groups", Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.broadcast:
                        ft.replace(R.id.fragment, new BroadCastFragment());
                        ft.commit();
                        setFragmentName.setText("Broadcast");
                        drawerLayout.closeDrawers();
//                        Toast.makeText(MainActivity.this, "Broadcast", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });*/
    /*
    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#006054")));
        }

public void listeners() {
        contacts_button.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View view) {

        Intent intent = new Intent(MainActivity.this, ContactsActivity.class);
        intent.putExtra("toContacts", "true");
        startActivity(intent);
        }
        });

        this.setChatFragment();
        this.navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener(){

public boolean onNavigationItemSelected(MenuItem menuItem) {
        MainActivity.this.getSupportFragmentManager().beginTransaction();
        switch (menuItem.getItemId()) {
default: {
        break;
        }
        case 2131230970: {
        MainActivity.this.setGroupFragment();
        break;
        }
        case 2131230862: {
        MainActivity.this.setChatFragment();
        break;
        }
        case 2131230842: {
        MainActivity.this.setBroadcastFragment();
        }
        }
        return true;
        }
        });
        }

@Override
public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(com.example.whatapp.R.menu.menu, menu);
        return true;
        }

@Override
public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
        return true;
        }

        switch(item.getItemId()) {
        case com.example.whatapp.R.id.logout:
//                FirebaseAuth.getInstance().signOut();
//                startActivity(new Intent(MainActivity.this, LoginActivity.class));
//                finish();
        Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
        return true;

        case com.example.whatapp.R.id.refresh:

//                inflateFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment, new ChatsFragment());
        ft.commit();
        setFragmentName.setText("Chats");
        return true;

default:
        return super.onOptionsItemSelected(item);
        }
        }

public void inflateFragment() {
        tabLayout.setupWithViewPager(viewPager);
        VPAdapter vpAdapter = new VPAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        vpAdapter.addFragment(new ChatsFragment(), "Chats");
        vpAdapter.addFragment(new BroadCastFragment(), "BroadCasts");
        vpAdapter.addFragment(new GroupFragment(), "Groups");
        viewPager.setAdapter(vpAdapter);
        }

public void setData() {
        SharedPreferences sp = getSharedPreferences("UserData", MODE_PRIVATE);
        String getName = sp.getString("name", "  ");
        String getEmail = sp.getString("email", "  ");
        String getAbout = sp.getString("about", "  ");

//        name.setText(getName.substring(0, 1).toUpperCase() + getName.substring(1));
//        email.setText("Welcome " + getEmail.substring(0, 1).toUpperCase() + getEmail.substring(1));
//        profile.setText(getName.substring(0, 1).toUpperCase());
        }


        */
