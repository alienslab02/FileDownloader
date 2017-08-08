package com.bilal.filedownloader.models;

import android.widget.ImageView;

/**
 * Created by applepc on 07/08/2017.
 */

public class DownloadableImage extends DownloadableFile {

    ImageView imageView;

    public DownloadableImage(String url, ImageView imageView) {
        super(url);
        this.imageView = imageView;
    }

    public ImageView getImageView() {
        return imageView;
    }
}
