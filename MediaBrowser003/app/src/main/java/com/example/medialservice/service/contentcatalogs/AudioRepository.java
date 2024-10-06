package com.example.medialservice.service.contentcatalogs;

import android.app.Application;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.example.medialservice.ui.Audio;

import java.util.ArrayList;
import java.util.List;

public class AudioRepository {
    Application application;

    public AudioRepository(Application application) {
        this.application = application;
    }

    public List<Audio> listAudioFiles(){

        Uri collection = MediaStore.Audio.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL
        );

        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Artists.ARTIST
        };

        String selection = "${MediaStore.Audio.Media.MIME_TYPE} = ?";
        String[] selectionArgs = new String[]{"audio/mpeg"};
        String sortOrder = MediaStore.Audio.Media.DISPLAY_NAME;

        Cursor cursor =
                application.getContentResolver().query(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        null,
                        null,
                        null
                );

        List<Audio> audios = new ArrayList<>();

        if(cursor!=null) {
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int nameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
            int artisteColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                String artiste = cursor.getString(artisteColumn);
                Uri contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                );
                audios.add(new Audio(String.valueOf(id), name, artiste, contentUri));
            }
        }

        return audios;
    }
}
