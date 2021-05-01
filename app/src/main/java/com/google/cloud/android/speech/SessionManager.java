package com.google.cloud.android.speech;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

public class SessionManager {

    SharedPreferences userSession;
    SharedPreferences.Editor editor;
    Context context;

    public static final String IS_LOGGEDIN = "IsLoggedIn";
    public static final String KEY_USERNAME = "userName";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_TOKEN = "token";

    public SessionManager(Context _context){
        context = _context;
        userSession = context.getSharedPreferences("userLoginSession", Context.MODE_PRIVATE);
        editor = userSession.edit();

    }

    public void createLoginSession(String username, String password, String token)
    {

        editor.putString(IS_LOGGEDIN, "true");

        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_PASSWORD, password);
        editor.putString(KEY_TOKEN, token);

        editor.commit();
    }

    public HashMap<String, String> getUserDetailsFromSession()
    {
        HashMap<String, String> userData = new HashMap<String, String>();

        

        userData.put(KEY_USERNAME, userSession.getString(KEY_USERNAME, null));
        userData.put(KEY_PASSWORD, userSession.getString(KEY_PASSWORD, null));
        userData.put(KEY_TOKEN, userSession.getString(KEY_TOKEN, null));
        userData.put(IS_LOGGEDIN, userSession.getString(IS_LOGGEDIN, null));

        return userData;
    }

    public boolean checkLogin()
    {
        if (userSession.getString(IS_LOGGEDIN, "false").equals("true")){
            return true;
        }
        else
            return false;
    }

    public void logout(){
        editor.clear();
        editor.commit();
    }
}
