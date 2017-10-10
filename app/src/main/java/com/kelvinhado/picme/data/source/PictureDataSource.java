package com.kelvinhado.picme.data.source;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * Created by kelvin on 10/10/2017.
 */

public interface PictureDataSource {

    /**
     * Gets the pictures from the data source
     * @param callback
     */
    void getPictures(@NonNull LoadPicturesCallback callback);

    /**
     * Deletes all the pictures from the data source
     */
    void deleteAllPictures();

    /**
     * Saves a picture into the data source
     * @param picture the picture thats need to be saved
     */
    void savePicture(@NonNull Picture picture);

    interface LoadPicturesCallback {

        void onPicturesLoaded(@NonNull List<Picture> pictures);

        void onDataNotAvailable();
    }
}
