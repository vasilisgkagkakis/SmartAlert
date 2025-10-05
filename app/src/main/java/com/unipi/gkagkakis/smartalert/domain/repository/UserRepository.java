package com.unipi.gkagkakis.smartalert.domain.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;

public interface UserRepository {
    Task<Void> saveUser(FirebaseUser user, String fullName, String phone);
    Task<Void> saveUser(FirebaseUser user, String fullName, String phone, boolean isAdmin);
    void getUserName(UserNameCallback callback);
    void logout();
    boolean isUserAuthenticated();
    void preloadUserData(UserDataCallback callback);
    void checkIsAdmin(IsAdminCallback callback);

    interface UserNameCallback {
        void onUserNameLoaded(String name);
        void onUserNotAuthenticated();
    }

    interface UserDataCallback {
        void onUserDataLoaded();
        void onUserDataFailed();
    }

    interface IsAdminCallback {
        void onIsAdminResult(boolean isAdmin);
        void onIsAdminFailed();
    }
}