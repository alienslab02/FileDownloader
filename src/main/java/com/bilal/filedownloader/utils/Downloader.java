package com.bilal.filedownloader.utils;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CancellationException;

/**
 * Created by applepc on 08/08/2017.
 */

public abstract class Downloader implements Runnable {
    private FileCache fileCache;
    private boolean isCancelled = false;
    private String url;

    public Downloader(String url, FileCache fileCache) {
        this.url = url;
        this.fileCache = fileCache;
    }

    public File downloadFile(){
        File f = fileCache.getFile(url);
        if (f != null && f.exists()) {
            return f;
        }

        // download from web
        OutputStream os = null;
        try {
            f.createNewFile();
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            InputStream is = conn.getInputStream();
            os = new FileOutputStream(f);
            copyStream(is, os);
            os.close();
            return f;
        } catch (Throwable ex) {
            ex.printStackTrace();
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (ex instanceof OutOfMemoryError) {
                fileCache.clear();
            }

            if (ex instanceof CancellationException){
                f.delete();
            }
            return null;
        }
    }

    private void copyStream(InputStream is, OutputStream os) throws Exception{
        final int bufferSize = 1024;
        byte[] bytes = new byte[bufferSize];
        for (; ; ) {
            if(isCancelled()){
                throw new CancellationException();
            }
            int count = is.read(bytes, 0, bufferSize);
            if (count == -1)
                break;
            os.write(bytes, 0, count);
        }
    }

    public void cancel(){
        isCancelled = true;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public String getUrl() {
        return url;
    }
}
