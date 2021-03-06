package com.dtalk.dd.ui.widget;

import android.os.AsyncTask;

import com.dtalk.dd.utils.CommonUtil;
import com.dtalk.dd.utils.Logger;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.internal.DiskLruCache;
import com.squareup.okhttp.internal.Util;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Created by zhujian on 15/3/26.
 */
public class GifLoadTask extends AsyncTask<String, Void, byte[]> {

    public GifLoadTask() {
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected byte[] doInBackground(final String... params) {
        final String gifUrl = params[0];
        if (gifUrl == null)
            return null;
        byte[] gif = null;
        try {
            gif = byteArrayHttpClient(gifUrl);
        } catch (OutOfMemoryError e) {
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return gif;
    }

    private InputStream getFromCache(String url) throws Exception {
        String localFilePath;
        localFilePath = getLocalFilePath(url);
        if (new File(localFilePath).exists()) {
            InputStream targetStream = new FileInputStream(localFilePath);
            return targetStream;
        }

//        DiskLruCache cache = DiskLruCache.open(CommonUtil.getImageSavePath(), 1, 2, 2 * 1024 * 1024);
//        cache.flush();
//        String key = Util.hash(url);
//        final DiskLruCache.Snapshot snapshot;
//        try {
//            snapshot = cache.get(key);
//            if (snapshot == null) {
//                return null;
//            }
//        } catch (IOException e) {
//            return null;
//        }
//        Logger.d(key);
//        FilterInputStream bodyIn = new FilterInputStream(snapshot.getInputStream(1)) {
//            @Override
//            public void close() throws IOException {
//                snapshot.close();
//                super.close();
//            }
//        };
//        return bodyIn;
        return null;
    }

    public byte[] byteArrayHttpClient(final String urlString) throws Exception {
        OkHttpClient client = null;
        if (client == null) {
            client = new OkHttpClient();
//            Cache responseCache = new Cache(CommonUtil.getImageSavePath(), 2 * 1024 * 1024);
//            client.setCache(responseCache);
            client.setReadTimeout(30, java.util.concurrent.TimeUnit.SECONDS);
            client.setConnectTimeout(30, java.util.concurrent.TimeUnit.SECONDS);
        }
        InputStream inputStream = getFromCache(urlString);
        if (inputStream != null) {
            return IOUtils.toByteArray(inputStream);
        }
        InputStream in = null;
        try {
            final String decodedUrl = URLDecoder.decode(urlString, "UTF-8");
            final URL url = new URL(decodedUrl);
            final Request request = new Request.Builder().url(url).build();
            final Response response = client.newCall(request).execute();
            in = response.body().byteStream();
            saveFile(in, urlString);
            InputStream inputStream1 = getFromCache(urlString);
            if (inputStream1 != null) {
                return IOUtils.toByteArray(inputStream1);
            }
            return IOUtils.toByteArray(in);
        } catch (final MalformedURLException e) {
        } catch (final OutOfMemoryError e) {
        } catch (final UnsupportedEncodingException e) {
        } catch (final IOException e) {
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (final IOException ignored) {
                }
            }
        }
        return null;
    }

    private void saveFile(InputStream in, String decodedUrl) {
        String localFilePath;
        localFilePath = getLocalFilePath(decodedUrl);
//        if (new File(localFilePath).exists()) {
//            return;
//        }
        try {
            File tmpFile = new File(localFilePath);
            FileOutputStream fos = new FileOutputStream(tmpFile);
            byte buf[] = new byte[1024];
            int count = 0;
            do {
                int numread = in.read(buf);
                count += numread;
                if (numread <= 0) {
                    break;
                }
                fos.write(buf, 0, numread);
            } while (true);
            fos.close();
        } catch (Exception e) {
        }
    }

    public String getLocalFilePath(String remoteUrl) {
        File dir = CommonUtil.getImageSavePath();
        String savePath = dir.getAbsolutePath();
        String localPath;
        localPath = savePath + "/" + Util.hash(remoteUrl);
        return localPath;
    }
}

