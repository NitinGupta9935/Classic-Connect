package com.example.whatapp;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.whatapp.model.BroadCast;
import com.example.whatapp.model.Channel;
import com.example.whatapp.model.Group;
import com.example.whatapp.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Common extends Activity {
    static Common common = null;
    public BroadCast broadCastModel;
    public Group groupModel;
    public Channel channelModel;
    public ArrayList<User> messageReceivers;
    public String profilePic;
    public String senderMobileNumber;
    public int themeColor = Color.parseColor("#006054");
    public String userAbout;
    public String userName;

    public static Common getInstance() {
        if (common == null) {
            common = new Common();
//            common.setThemeColor();
        }
        return common;
    }

    public String formatTime(String string, String string2) {

        StringBuilder stringBuilder;
        String string3;
        int n = Integer.parseInt((String)string.substring(0, 2));
        if (n < 12) {
            stringBuilder = new StringBuilder().append(string);
            string3 = " am";
        } else {
            stringBuilder = new StringBuilder().append(n - 12).append(string.substring(2));
            string3 = " pm";
        }
        String string4 = stringBuilder.append(string3).toString();
        if (Common.getInstance().getTodayDate().substring(2).equals((Object)string2.substring(2))) {
            int n2;
            int n3 = Integer.parseInt((String)Common.getInstance().getTodayDate().substring(0, 2));
            if (n3 - 1 == (n2 = Integer.parseInt((String)string2.substring(0, 2)))) {
                string4 = "yesterday";
            }
            return string4;
        }
        return string;
    }

    public String getCurrentTime() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()).split(" ")[1];
    }

    public String getProfilePic() {
        return this.profilePic;
    }

    public String getTodayDate() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()).split(" ")[0];
    }

    public String getUserAbout() {
        return this.userAbout;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setTheme(AppCompatActivity appCompatActivity) {
        appCompatActivity.getWindow().setStatusBarColor(themeColor);
        appCompatActivity.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(themeColor));
    }

//    public void setThemeColor() {
////        String color = getSharedPreferences("theme", MODE_PRIVATE).getString("color", "");
////        if (!color.equals(""))
////            themeColor = Integer.parseInt(color);
//    }

    public int getThemeColor() {
        return themeColor;
    }

    public void setUserAbout(String string) {
        this.userAbout = string;
    }

    public void setUserName(String string) {
        this.userName = string;
    }
}