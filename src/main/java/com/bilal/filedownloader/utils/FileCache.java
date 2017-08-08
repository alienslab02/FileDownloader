package com.bilal.filedownloader.utils;

import android.content.Context;

import java.io.File;

/**
 * Created by applepc on 07/08/2017.
 */

public class FileCache {

    private File cacheDir;

    public FileCache(Context context) {
        cacheDir = context.getCacheDir();
        if (!cacheDir.exists())
            cacheDir.mkdirs();
    }

    public File getFile(String url) {
        String filename = String.valueOf(url.hashCode());
        File f = new File(cacheDir, filename);
        return f;
    }

    public void clear() {
        File[] files = cacheDir.listFiles();
        if (files == null)
            return;
        for (File f : files)
            f.delete();
    }
}
