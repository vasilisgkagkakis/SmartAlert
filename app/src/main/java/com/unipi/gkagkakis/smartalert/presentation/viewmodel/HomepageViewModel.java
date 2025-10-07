package com.unipi.gkagkakis.smartalert.presentation.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.unipi.gkagkakis.smartalert.data.repository.UserRepositoryImpl;
import com.unipi.gkagkakis.smartalert.domain.repository.UserRepository;

public class HomepageViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    private final MutableLiveData<String> userName = new MutableLiveData<>("User");
    private final MutableLiveData<Boolean> shouldNavigateToLogin = new MutableLiveData<>(false);

    public HomepageViewModel(@NonNull Application application) {
        super(application);
        this.userRepository = new UserRepositoryImpl(application);
    }

    public LiveData<String> getUserName() {
        return userName;
    }

    public LiveData<Boolean> getShouldNavigateToLogin() {
        return shouldNavigateToLogin;
    }

    public void checkUserAndLoadName() {
        if (!userRepository.isUserAuthenticated()) {
            shouldNavigateToLogin.setValue(true);
            return;
        }

        userRepository.getUserName(new UserRepository.UserNameCallback() {
            @Override
            public void onUserNameLoaded(String name) {
                userName.postValue(name);
            }

            @Override
            public void onUserNotAuthenticated() {
                shouldNavigateToLogin.postValue(true);
            }
        });
    }

    public void logout() {
        userRepository.logout();
        shouldNavigateToLogin.setValue(true);
    }
}