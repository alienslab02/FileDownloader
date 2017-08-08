package com.bilal.filedownloader;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.bilal.filedownloader.models.DownloadableImage;
import com.bilal.filedownloader.utils.Downloader;
import com.bilal.filedownloader.utils.FileCache;
import com.bilal.filedownloader.utils.MemoryCache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by applepc on 07/08/2017.
 */

public class ImageDownloader extends FileDownloader {

    private MemoryCache memoryCache;
    private Map<ImageView, String> imageViews;
    private int placeHolderResId = -1;
    private int requiredSize = 70; // default

    public ImageDownloader(Context context) {
        super(context);
        memoryCache = new MemoryCache();
        imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    }

    public void loadImage(String url, ImageView imageView) {
        imageViews.put(imageView, url);
        Bitmap bitmap = memoryCache.get(url);
        if (bitmap != null)
            imageView.setImageBitmap(bitmap);
        else {
            queuePhoto(url, imageView);
            imageView.setImageResource(getPlaceHolderResId());
        }
    }

    private void queuePhoto(String url, ImageView imageView) {
        PhotosLoader p = new PhotosLoader(getFileCache(), new DownloadableImage(url, imageView));
        executeService(p);
    }

    // decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f) {
        try {
            // decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            // Find the correct scale value. It should be the power of 2.
            int tempWidth = o.outWidth, tempHeight = o.outHeight;
            int scale = 1;
            while (true) {
                if (tempWidth / 2 < getRequiredSize() || tempHeight / 2 < getRequiredSize())
                    break;
                tempWidth /= 2;
                tempHeight /= 2;
                scale *= 2;
            }

            // decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
        }
        return null;
    }

    boolean imageViewReused(DownloadableImage downloadableImage) {
        String tag = imageViews.get(downloadableImage.getImageView());
        if (tag == null || !tag.equals(downloadableImage.getUrl()))
            return true;
        return false;
    }


    @Override
    public void clearCache() {
        memoryCache.clear();
        super.clearCache();
    }

    /********************** private classes ************************************/

    // Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable {
        Bitmap bitmap;
        DownloadableImage downloadableImage;

        public BitmapDisplayer(Bitmap b, DownloadableImage p) {
            bitmap = b;
            downloadableImage = p;
        }

        public void run() {
            if (imageViewReused(downloadableImage))
                return;
            if (bitmap != null)
                downloadableImage.getImageView().setImageBitmap(bitmap);
            else
                downloadableImage.getImageView().setImageResource(getPlaceHolderResId());
        }
    }

    class PhotosLoader extends Downloader {
        DownloadableImage downloadableImage;

        PhotosLoader(FileCache fileCache, DownloadableImage downloadableImage) {
            super(downloadableImage.getUrl(), fileCache);
            this.downloadableImage = downloadableImage;
        }

        @Override
        public void run() {
            if (imageViewReused(downloadableImage))
                return;
            Bitmap bmp = null;
            if(!isCancelled()){
                bmp = getBitmap();
                if (bmp != null) memoryCache.put(downloadableImage.getUrl(), bmp);
            }
            BitmapDisplayer bd = new BitmapDisplayer(bmp, downloadableImage);;
            Activity a = (Activity) downloadableImage.getImageView().getContext();
            a.runOnUiThread(bd);
        }

        public Bitmap getBitmap() {
            try {
                File f = downloadFile();
                if(f.exists()){
                    Bitmap bm = decodeFile(f);
                    return bm;
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
                if (ex instanceof OutOfMemoryError)
                    memoryCache.clear();
            }
            return null;
        }
    }

    /********************** getters and setters ************************************/

    private int getRequiredSize() {
        return requiredSize;
    }

    public void setRequiredSize(int requiredSize) {
        this.requiredSize = requiredSize;
    }

    private int getPlaceHolderResId() {
        return placeHolderResId;
    }

    public void setPlaceHolderResId(int placeHolderResId) {
        this.placeHolderResId = placeHolderResId;
    }

    @Override
    public void setMemoryLimit(long limit) {
        if(memoryCache != null ) memoryCache.setLimit(limit);
    }
}
