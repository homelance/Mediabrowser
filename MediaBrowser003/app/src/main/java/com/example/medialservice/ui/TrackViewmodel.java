package com.example.medialservice.ui;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.support.v4.media.session.MediaControllerCompat;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.medialservice.service.contentcatalogs.AudioRepository;

import java.util.Arrays;
import java.util.List;

public class TrackViewmodel extends ViewModel {

    Application application;
    AudioRepository repo;
    ActivityResultLauncher<String> requestPermissionLauncher;

    MutableLiveData<MediaControllerCompat> mediaController = new MutableLiveData<MediaControllerCompat>();

    MutableLiveData<Boolean> storagePermissionGranted = new MutableLiveData<>();
    MutableLiveData<List<Audio>> audios =new  MutableLiveData<List<Audio>>();
    MutableLiveData<Audio> nowPlaying = new MutableLiveData<Audio>();

    public TrackViewmodel(Application application, ActivityResultLauncher<String> requestPermissionLauncher) {
        this.application = application;
        this.requestPermissionLauncher = requestPermissionLauncher;
        this.repo = new AudioRepository(application);
        storagePermissionGranted.setValue(false);
        checkPermission();
    }

    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(
                application,
                Manifest.permission.READ_MEDIA_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        ) {
            onPermissionGranted();
        } else {
            requestPermissionLauncher.launch(
                    Manifest.permission.READ_MEDIA_AUDIO
            );
        }
    }

    public void onPermissionGranted() {
        storagePermissionGranted.setValue(true);
//        List<Audio> items = repo.listAudioFiles();
//        audios.setValue(items);
        connectToMediaPlaybackService();
    }

    private void connectToMediaPlaybackService() {

    }

    List<Audio> items = Arrays.asList(
            new Audio("1", "music1", "spario"),
            new Audio("2", "music2", "spario"),
            new Audio("3", "music3", "spario"),
            new Audio("4", "music4", "spario")
    );
}
