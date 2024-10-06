package com.example.medialservice.ui;

import android.net.Uri;

import androidx.annotation.Nullable;

public class Audio {
    public String id;
    public String name;
    public String artist;

    public Uri uri;
    public boolean nowPlaying;
    public boolean isPlaying;

    public Audio(String id, String name, String artist) {
        this.id = id;
        this.name = name;
        this.artist = artist;
    }

    public Audio(String id, String name, String artist, Uri uri) {
        this.id = id;
        this.name = name;
        this.artist = artist;
        this.uri = uri;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj);
    }
}
