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

    private TreeMap<String, Bitmap> m_imageCache;


    class ImageLoaderTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        String imageUrlString;

        public ImageLoaderTask(ImageView imageView) {
            this.bmImage = imageView;
        }

        protected Bitmap doInBackground(String... imageUrls) {
            imageUrlString = imageUrls[0];

            try {
                Bitmap loadedBitmap = convertToBitmap(loadImageData(imageUrlString));

                if (loadedBitmap == null && imageUrlString.startsWith("http:"))
                {
                    String imageUrlStringHttps = imageUrlString.replaceFirst("http", "https");
                    loadedBitmap = convertToBitmap(loadImageData(imageUrlStringHttps));
                }

                return loadedBitmap;
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }

            return null;
        }


        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                m_imageCache.put(imageUrlString, result);
                bmImage.setImageBitmap(result);
            }
        }
    }



    public ImageLoader()
    {
        // Create  map to hold loaded images
        m_imageCache = new TreeMap();
    }


    private static byte[] loadImageData(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        InputStream inputStream = null;
        try {
            try {
                // Read data from workstation
                inputStream = connection.getInputStream();
            } catch (IOException e) {
                // Read the error from the workstation
                inputStream = connection.getErrorStream();
            }

            // Can you think of a way to make the entire
            // HTTP more efficient using HTTP headers??

            return StreamUtils.readUnknownFully(inputStream);
        } finally {
            // Close the input stream if it exists.
            StreamUtils.close(inputStream);

            // Disconnect the connection
            connection.disconnect();
        }
    }

    private static Bitmap convertToBitmap(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
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
