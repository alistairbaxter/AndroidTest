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
    private TreeMap<String, Bitmap> m_imageCache;


    class ImageLoaderTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        String imageUrlString;

        public ImageLoaderTask(ImageView imageView) {
            this.bmImage = imageView;
        }

        protected Bitmap doInBackground(String... imageUrls) {

            //Log.v("", "loading image " + imageUrls[0]);
            imageUrlString = imageUrls[0];

            Bitmap mIcon11 = null;
            try {
                URL imageUrl = new URL(imageUrlString);
                InputStream in = imageUrl.openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }


        protected void onPostExecute(Bitmap result) {

            m_imageCache.put(imageUrlString, result);
            bmImage.setImageBitmap(result);
        }
    }

    public ImageLoader()
    {
        m_imageCache = new TreeMap();
    }

    /**
     * Simple function for loading a bitmap image from the web
     *
     * @param url       image url
     * @param imageView view to set image too.
     */
    public void load(String url, ImageView imageView) {
        if (TextUtils.isEmpty(url)) {
            throw new InvalidParameterException("URL is empty!");
        }

        // Can you think of a way to improve loading of bitmaps
        // that have already been loaded previously??

//        try {
//            setImageView(imageView, convertToBitmap(loadImageData(url)));
//        } catch (IOException e) {
//            Log.e(TAG, e.getMessage());
//        }

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
