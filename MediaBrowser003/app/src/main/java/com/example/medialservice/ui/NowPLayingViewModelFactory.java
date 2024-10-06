package com.example.medialservice.ui;

import android.app.Application;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class NowPLayingViewModelFactory implements ViewModelProvider.Factory {
    Application application;
    ActivityResultLauncher<String> requestPermissionLauncher;

    public NowPLayingViewModelFactory(Application application, ActivityResultLauncher<String> requestPermissionLauncher) {
        this.application = application;
        this.requestPermissionLauncher = requestPermissionLauncher;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(TrackViewmodel.class)) {
            return (T) new TrackViewmodel(application, requestPermissionLauncher);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
