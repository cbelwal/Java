package com.dasmic.android.firebasetest.Activity;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dasmic.android.firebasetest.Enum.AppOptions;
import com.dasmic.android.firebasetest.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

public class ActivityMain extends ActivityBaseAd {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("CKIT", "ActivityMain is created");
        setContentView(R.layout.ui_main);

        setLocalVariablesAndEventHandlers();
        FirebaseMessaging.getInstance().subscribeToTopic("Updates.General");
    }

    private void setLocalVariablesAndEventHandlers(){

        TextView tvDeviceID = findViewById(R.id.textDeviceID);
        TextView tvDeviceIMEI = findViewById(R.id.textDeviceIMEI);

        AppOptions.MainActivity=this;

        try {
            if (AppOptions.AppToken == null) {
                Task<InstanceIdResult> instanceIdResult =
                        FirebaseInstanceId.getInstance().getInstanceId();
                instanceIdResult.addOnCompleteListener(
                    new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (task.isSuccessful()) {
                                InstanceIdResult iir = task.getResult();
                                //Thread.sleep(2000);
                                AppOptions.AppToken = iir.getToken();
                                TextView tvTokenID = findViewById(R.id.textTokenID);
                                tvTokenID.setText(AppOptions.AppToken);
                            }
                        }
                    });
            }
        }
        catch(Exception ex){

        }



        try {
            String android_id = Settings.Secure.getString(this.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            tvDeviceID.setText(android_id);
        }
        catch(Exception ex){

        }

        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);

        try {
            tvDeviceIMEI.setText(telephonyManager.getDeviceId());
        }
        catch(SecurityException ex){

        }

        Button btnStart = (Button) findViewById(R.id.btnSend);
        btnStart.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                sendHTTPRequestToServer();
            }
        });
    }


    /*private void sendMessageToFirebase(){
        int msgId = 12;//getFreeMsgId();
        try {
            FirebaseMessaging fm = FirebaseMessaging.getInstance();
            fm.send(new RemoteMessage.Builder("214375416438" + "@gcm.googleapis.com")
                    .setMessageId(Integer.toString(msgId))
                    .addData("to", "cW-xp5sK0ZM:APA91bFAEej8jzqSP1PLPh6plELDdYT27d3s-5AAIyAW_0XMji2tbatWvkkBu3YtbYT8-Oc45BusLxziKbWf5bu8Q20ukX_6pps9ic1ccRU3GUgQYKpVb5QX9JZcAGCYIm2OEOifCeMF")
                    //.addData("notification", "{\"title\":\"Updates.General\",\"body\":\"Yellow\"}")
                    .addData("password", "password")
                    .setTtl(1200)
                    .build());
        }
        catch(Exception ex){

        }
        //return msgId;
    }*/

    //Send REST queries
    /*private void sendHTTPRequestToServer(){
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, urlAdress, obj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("SUCCESS", response + "");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("ERRORS", error + "");
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "key=" + API_KEY);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        int socketTimeout = 1000 * 60;// 60 seconds
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsObjRequest.setRetryPolicy(policy);
        requestQueue.add(jsObjRequest);
    }
    }*/

}
