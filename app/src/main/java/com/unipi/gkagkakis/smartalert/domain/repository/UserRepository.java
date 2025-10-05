package com.unipi.gkagkakis.smartalert.domain.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;

public interface UserRepository {
    Task<Void> saveUser(FirebaseUser user, String fullName, String phone);
    void getUserName(UserNameCallback callback);
    void logout();
    boolean isUserAuthenticated();
    void preloadUserData(UserDataCallback callback);

    interface UserNameCallback {
        void onUserNameLoaded(String name);
        void onUserNotAuthenticated();
    }

    interface UserDataCallback {
        void onUserDataLoaded();
        void onUserDataFailed();
    }
}