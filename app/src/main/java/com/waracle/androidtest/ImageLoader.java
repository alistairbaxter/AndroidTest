package com.waracle.androidtest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.TreeMap;
import java.security.InvalidParameterException;

/**
 * Created by Riad on 20/05/2015.
 */

class ImageLoader
{
    private static final String TAG = ImageLoader.class.getSimpleName();

    // A Cache mapping urls to loaded bitmaps
    private TreeMap<String, Bitmap> m_imageCache;

    // Asynchronous task to load individual images
    class ImageLoaderTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        String imageUrlString;

        // This task provides an image for a particular view
        public ImageLoaderTask(ImageView imageView) {
            this.bmImage = imageView;
        }

        // Load a bitmap from our url in the Task
        protected Bitmap doInBackground(String... imageUrls) {
            imageUrlString = imageUrls[0];
            Bitmap loadedBitmap = loadBitmap(imageUrlString);
            if (loadedBitmap == null && imageUrlString.startsWith("http:")) {
                String imageUrlStringHttps = imageUrlString.replaceFirst("http", "https");
                loadedBitmap = loadBitmap(imageUrlStringHttps);
            }

            return loadedBitmap;
        }

        // Read from a url and build a bitmap
        Bitmap loadBitmap(String imageUrlString) {
            Bitmap loadedBitmap = null;
            try {
                URL imageUrl = new URL(imageUrlString);
                InputStream in = imageUrl.openStream();
                loadedBitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }

            return loadedBitmap;
        }

        // Store the bitmap in the image view that wanted it
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                m_imageCache.put(imageUrlString, result);
                bmImage.setImageBitmap(result);
            }
        }
    }



    public ImageLoader()
    {
        // Create  map to hold cached images
        m_imageCache = new TreeMap();
    }

    /**
     * Simple function for loading a bitmap image from the web
     * or from a cached copy, if we have one
     *
     * @param url       image url
     * @param imageView view to set image too.
     */
    public void load(String url, ImageView imageView) {
        if (TextUtils.isEmpty(url)) {
            throw new InvalidParameterException("URL is empty!");
        }

        // improve loading of bitmaps that have already been loaded previously
        Bitmap cachedBitmap = m_imageCache.get(url);
        if (cachedBitmap != null) {
            imageView.setImageBitmap(cachedBitmap);
        }
        else {
            ImageLoaderTask loadTask = new ImageLoaderTask(imageView);
            loadTask.execute(url);
        }
    }

}
