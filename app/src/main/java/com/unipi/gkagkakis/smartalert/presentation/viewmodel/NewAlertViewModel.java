package com.unipi.gkagkakis.smartalert.presentation.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.unipi.gkagkakis.smartalert.domain.usecase.LocationUseCase;
import com.unipi.gkagkakis.smartalert.Utils.BlurHelper;

/**
 * ViewModel for NewAlertFragment following MVVM pattern
 * Handles location operations and image preview logic
 */
public class NewAlertViewModel extends AndroidViewModel {

    // Private MutableLiveData for internal state management
    private final MutableLiveData<String> _currentLocation = new MutableLiveData<>();
    private final MutableLiveData<String> _locationError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _locationPermissionRequired = new MutableLiveData<>();

    // Public read-only LiveData for UI observation
    public final LiveData<String> currentLocation = _currentLocation;
    public final LiveData<String> locationError = _locationError;
    public final LiveData<Boolean> locationPermissionRequired = _locationPermissionRequired;

    private final LocationUseCase locationUseCase;

    public NewAlertViewModel(@NonNull Application application) {
        super(application);
        this.locationUseCase = new LocationUseCase(application);
    }

    /**
     * Requests current device location
     */
    public void requestCurrentLocation() {
        _locationError.setValue(null);
        _locationPermissionRequired.setValue(false);

        locationUseCase.getCurrentLocation(getApplication(), new LocationUseCase.LocationCallback() {
            @Override
            public void onLocationReceived(String formattedLocation) {
                _currentLocation.setValue(formattedLocation);
            }

            @Override
            public void onLocationError(String error) {
                _locationError.setValue(error);
            }

            @Override
            public void onPermissionRequired() {
                _locationPermissionRequired.setValue(true);
            }
        });
    }

    /**
     * Checks if location permissions are available
     */
    public boolean hasLocationPermissions() {
        return locationUseCase.hasLocationPermissions(getApplication());
    }

    /**
     * Creates blurred screenshot for image preview
     */
    @SuppressLint("ObsoleteSdkInt")
    public Bitmap createBlurredScreenshot(View rootView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Modern approach for API 26+
            Bitmap screenshot = Bitmap.createBitmap(
                rootView.getWidth(),
                rootView.getHeight(),
                Bitmap.Config.ARGB_8888
            );
            Canvas canvas = new Canvas(screenshot);
            rootView.draw(canvas);

            return BlurHelper.blur(getApplication(), screenshot, 15);
        } else {
            // Legacy approach for older versions
            return createLegacyBlurredScreenshot(rootView);
        }
    }

    @SuppressWarnings("deprecation")
    private Bitmap createLegacyBlurredScreenshot(View rootView) {
        rootView.setDrawingCacheEnabled(true);
        Bitmap screenshot = Bitmap.createBitmap(rootView.getDrawingCache());
        rootView.setDrawingCacheEnabled(false);

        return BlurHelper.blur(getApplication(), screenshot, 15);
    }

    /**
     * Clears location error state
     */
    public void clearLocationError() {
        _locationError.setValue(null);
    }

    /**
     * Clears current location state
     */
    public void clearCurrentLocation() {
        _currentLocation.setValue(null);
    }

    /**
     * Clears location permission required state
     */
    public void clearLocationPermissionRequired() {
        _locationPermissionRequired.setValue(false);
    }
}
