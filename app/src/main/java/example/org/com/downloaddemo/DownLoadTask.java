package example.org.com.downloaddemo;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownLoadTask extends AsyncTask<String, Integer, Integer> {
    private static final int TYPE_SUCCESS = 0;
    private static final int TYPE_FAILED = 1;
    private static final int TYPE_PAUSED = 2;
    private static final int TYPE_CANCELEd = 3;

    private DownLoadListener mDownLoadListener;

    private boolean isCanceled = false;

    private boolean isPaused = false;

    private int lastProgress;

    public void pauseDownLoad() {
        isPaused = true;
    }

    public void cancelDownLoad() {
        isCanceled = true;
    }

    public DownLoadTask(DownLoadListener downLoadListener) {
        this.mDownLoadListener = downLoadListener;
    }

    @Override
    protected Integer doInBackground(String... strings) {
        InputStream is = null;
        RandomAccessFile saveFile = null;
        File file = null;
        try {
            long downloadedContentLength = 0;
            String downLoadUrl = strings[0];
            String fileName = downLoadUrl.substring(downLoadUrl.lastIndexOf("/"));
            String directory = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
            ).getPath();
            file = new File(directory + fileName);
            if (file.exists()) {
                downloadedContentLength = file.length();
            }
            long contentLength = getContentLength(downLoadUrl);
            if (contentLength == 0) {
                return TYPE_FAILED;
            } else if (contentLength == downloadedContentLength) {
                return TYPE_SUCCESS;
            }
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .addHeader("RANGE", "bytes=" + downloadedContentLength + "-")
                    .url(downLoadUrl)
                    .build();
            Response response = client.newCall(request).execute();
            if (response != null) {
                is = response.body().byteStream();
                saveFile = new RandomAccessFile(file, "rw");
                saveFile.seek(downloadedContentLength);//跳过已经下载的字节
                byte[] b = new byte[1024];
                int total = 0;
                int len;
                while ((len = is.read(b)) != -1) {
                    if (isCanceled) {
                        return TYPE_CANCELEd;
                    } else if (isPaused) {
                        return TYPE_PAUSED;
                    } else {
                        total += len;
                        saveFile.write(b, 0, len);
                        //计算已下载百分比
                        int progress = (int) ((total + downloadedContentLength) * 100 / contentLength);
                        publishProgress(progress);
                    }
                }
                response.body().close();
                return TYPE_SUCCESS;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (saveFile != null) {
                    saveFile.close();
                }
                if (isCanceled && file != null) {
                    file.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return TYPE_FAILED;
    }

    private long getContentLength(String downLoadUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(downLoadUrl)
                .build();
        Response response = client.newCall(request).execute();
        if (response != null && response.isSuccessful()) {
            long contentLength = response.body().contentLength();
            return contentLength;
        }
        return 0;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Integer status) {
        switch (status) {
            case TYPE_SUCCESS:
                mDownLoadListener.onSuccess();
                break;
            case TYPE_FAILED:
                mDownLoadListener.onFailed();
                break;
            case TYPE_CANCELEd:
                mDownLoadListener.onCanceled();
                break;
            case TYPE_PAUSED:
                mDownLoadListener.onPaused();
                break;
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if (progress > lastProgress) {
            mDownLoadListener.onProgress(progress);
            lastProgress = progress;
        }
    }
}
