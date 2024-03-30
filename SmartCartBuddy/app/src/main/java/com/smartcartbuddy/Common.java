package com.smartcartbuddy;

import android.content.Context;
import android.content.SharedPreferences;

import android.content.Context;
import android.content.SharedPreferences;

public class Common {
    private SharedPreferences sharedPreferences;
    private Context context;

    public Common(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("user_pref", Context.MODE_PRIVATE);
    }

    public String getUserId() {
        return sharedPreferences.getString("user_id", null);
    }
}

