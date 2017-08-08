package com.bilal.filedownloader.models;

/**
 * Created by applepc on 07/08/2017.
 */

public class DownloadableFile {
    String url;

    public DownloadableFile(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

}
