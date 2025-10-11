package com.unipi.gkagkakis.smartalert.domain.usecase;

import com.unipi.gkagkakis.smartalert.domain.repository.UserRepository;

/**
 * Use case for handling navigation operations following clean architecture
 * Manages admin status checking for navigation decisions
 */
public class NavigationUseCase {
    private final UserRepository userRepository;

    public NavigationUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Checks if current user is admin for navigation purposes
     */
    public void checkAdminStatusForNavigation(AdminNavigationCallback callback) {
        userRepository.checkIsAdmin(new UserRepository.IsAdminCallback() {
            @Override
            public void onIsAdminResult(boolean isAdmin) {
                if (isAdmin) {
                    callback.onNavigateToAdminHomepage();
                } else {
                    callback.onNavigateToUserHomepage();
                }
            }

            @Override
            public void onIsAdminFailed() {
                // Default to user homepage if admin check fails
                callback.onNavigateToUserHomepage();
            }
        });
    }

    /**
     * Callback interface for navigation results
     */
    public interface AdminNavigationCallback {
        void onNavigateToAdminHomepage();
        void onNavigateToUserHomepage();
    }
}
