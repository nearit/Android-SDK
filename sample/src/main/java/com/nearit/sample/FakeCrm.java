package com.nearit.sample;

public class FakeCrm {

    public void logout() {
        // call to my crm server
    }

    public void login(String username, String password, LoginListener listener) {
        // call to my server and fetch UserData
        UserData userData = new UserData();
        listener.onLogin(userData);
    }

    public void saveProfileId(String nearProfileId) {
        // add the near profile to the user data and sync to my server
    }

    public interface LoginListener {
        void onLogin(UserData userData);
    }

    public class UserData {
        String username;
        String nearProfileId;
    }
}
