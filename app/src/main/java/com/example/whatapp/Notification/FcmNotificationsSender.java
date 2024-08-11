/*
 * Decompiled with CFR 0.0.
 * 
 * Could not load the following classes:
 *  android.app.Activity
 *  android.content.Context
 *  com.android.volley.AuthFailureError
 *  com.android.volley.Request
 *  com.android.volley.RequestQueue
 *  com.android.volley.Response
 *  com.android.volley.Response$ErrorListener
 *  com.android.volley.Response$Listener
 *  com.android.volley.VolleyError
 *  com.android.volley.toolbox.JsonObjectRequest
 *  com.android.volley.toolbox.Volley
 *  java.lang.Object
 *  java.lang.String
 *  java.util.HashMap
 *  java.util.Map
 *  org.json.JSONException
 *  org.json.JSONObject
 */
package com.example.whatapp.Notification;

import android.app.Activity;
import android.content.Context;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class FcmNotificationsSender {
    String body;
    private final String fcmServerKey = "AAAAUHkVkcY:APA91bEf8nB2EyV3ecX_kZnIY6OPJFA4IHNkJO_bBoc3-kpEVXjNy-A80aJ9bcdjTlHJGoyIHAEr4cWo0El4yDiHjF7Z73IROxOUlM0YFJJs4stB7puhMBwUkwADXxfe8-gCoP41Pch8";
    Activity mActivity;
    Context mContext;
    private final String postUrl = "https://fcm.googleapis.com/fcm/send";
    private RequestQueue requestQueue;
    String title;
    String userFcmToken;
    public FcmNotificationsSender(String userFcmToken, String title, String body, Context context, Activity activity) {
        this.userFcmToken = userFcmToken;
        this.title = title;
        this.body = body;
        this.mContext = context;
        this.mActivity = activity;
    }
    public void SendNotifications() {
        this.requestQueue = Volley.newRequestQueue(mContext);
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("to", (Object)this.userFcmToken);
            JSONObject jSONObject2 = new JSONObject();
            jSONObject2.put("title", (Object)this.title);
            jSONObject2.put("body", (Object)this.body);
            jSONObject2.put("icon", 2131165377);
            jSONObject.put("data", (Object)jSONObject2);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(1, "https://fcm.googleapis.com/fcm/send", jSONObject, (Response.Listener)new Response.Listener<JSONObject>(){

                public void onResponse(JSONObject jSONObject) {
                }
            }, new Response.ErrorListener(){

                public void onErrorResponse(VolleyError volleyError) {
                }
            }){

                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap hashMap = new HashMap();
                    hashMap.put((Object)"content-type", (Object)"application/json");
                    hashMap.put((Object)"authorization", (Object)"key=AAAAUHkVkcY:APA91bEf8nB2EyV3ecX_kZnIY6OPJFA4IHNkJO_bBoc3-kpEVXjNy-A80aJ9bcdjTlHJGoyIHAEr4cWo0El4yDiHjF7Z73IROxOUlM0YFJJs4stB7puhMBwUkwADXxfe8-gCoP41Pch8");
                    return hashMap;
                }
            };
            this.requestQueue.add((Request)jsonObjectRequest);
            return;
        }
        catch (JSONException jSONException) {
            jSONException.printStackTrace();
            return;
        }
    }

}

