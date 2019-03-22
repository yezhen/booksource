package example.org.com.downloaddemo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.File;

public class DownLoadService extends Service {
    private DownLoadTask mDownLoadTask;

    private String downLoadedUrl;

    private DownLoadBinder mBinder = new DownLoadBinder();

    private DownLoadListener mDownLoadListener = new DownLoadListener() {
        @Override
        public void onProgress(int progress) {
            getNotificationManager().notify(1, getNotification("downloading",
                    progress));
        }

        @Override
        public void onSuccess() {
            mDownLoadTask = null;
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("下载成功",
                    -1));
            Toast.makeText(DownLoadService.this, "download success",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed() {
            mDownLoadTask = null;
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("下载失败",
                    -1));
            Toast.makeText(DownLoadService.this, "下载失败",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPaused() {
            mDownLoadTask = null;
            Toast.makeText(DownLoadService.this, "暂停",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCanceled() {
            stopForeground(true);
            Toast.makeText(DownLoadService.this, "取消",
                    Toast.LENGTH_SHORT).show();
        }
    };

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification(String title, int progress) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentIntent(pi);
        builder.setContentTitle(title);
        if (progress > 0) {
            builder.setContentText("当前下载:" + progress + "%");
            builder.setProgress(100, progress, false);
        }
        return builder.build();
    }

    public DownLoadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;
    }

    class DownLoadBinder extends Binder {
        public void startDownload(String url) {
            if (mDownLoadTask == null) {
                downLoadedUrl = url;
                mDownLoadTask = new DownLoadTask(mDownLoadListener);
                mDownLoadTask.execute(downLoadedUrl);
                startForeground(1, getNotification("正在下载中", 0));
                Toast.makeText(DownLoadService.this, "正在下载中", Toast.LENGTH_SHORT)
                        .show();
            }
        }

        public void pauseDownload() {
            if (mDownLoadTask != null) {
                mDownLoadTask.pauseDownLoad();
            }
        }

        public void cancelDownload() {
            if (mDownLoadTask != null) {
                mDownLoadTask.cancelDownLoad();
            } else {
                if (downLoadedUrl != null) {
                    String fileName = downLoadedUrl.substring(downLoadedUrl.lastIndexOf("/"));
                    String directory = Environment
                            .getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DOWNLOADS).getPath();
                    File file = new File(directory, fileName);
                    if (file.exists()) {
                        file.delete();
                    }
                    getNotificationManager().cancel(1);
                    stopForeground(true);
                    Toast.makeText(DownLoadService.this, "取消下载",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }

    }
}
