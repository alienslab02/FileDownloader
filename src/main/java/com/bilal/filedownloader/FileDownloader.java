package com.bilal.filedownloader;

import android.content.Context;

import com.bilal.filedownloader.utils.Downloader;
import com.bilal.filedownloader.utils.FileCache;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by applepc on 05/08/2017.
 */

public abstract class FileDownloader {

    private final int MAX_THREADS = 5;
    private FileCache fileCache;
    private ExecutorService executorService;
    private HashMap<String, Downloader> tasks;

    public FileDownloader(Context context) {
        fileCache = new FileCache(context);
        tasks = new HashMap<>();
        executorService = Executors.newFixedThreadPool(MAX_THREADS);
    }

    protected void executeService(Downloader runnable) {
        tasks.put(runnable.getUrl(), runnable);
        executorService.submit(runnable);
    }

    public FileCache getFileCache() {
        return fileCache;
    }

    public void cancel(String url) {
        Downloader task = tasks.get(url);
        if(task != null) {
            task.cancel();
            tasks.remove(url);
        }
    }

    public void clearCache() {
        fileCache.clear();
    }

    // Set limit in bytes
    public abstract void setMemoryLimit(long limit);
}
